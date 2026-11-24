/*
 * Oauth2 Authentication Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.krista.authentication.oauth2_authentication.util;

import java.util.*;
import app.krista.extension.common.CommonUtils;
import app.krista.extensions.krista.authentication.oauth2_authentication.Configuration;
import app.krista.extensions.krista.authentication.oauth2_authentication.constants.Oauth2ExtensionConstants;
import app.krista.extensions.krista.authentication.oauth2_authentication.resource.ExtensionApiResource;
import com.github.scribejava.core.model.OAuth2AccessToken;

@SuppressWarnings("rawtypes")
public final class Validator {

    private Validator() {

    }

    /**
     * Validates that the authentication response from the OAuth2 provider is well-formed.
     *
     * @param authenticate authentication response received from the provider
     * @throws IllegalStateException if the token type or response payload is invalid
     */
    public static void validateAuthenticatorObject(OAuth2AccessToken authenticate) {
        if (authenticate == null) {
            throw new IllegalStateException("Null authentication response from oAuth service provider.");
        }
        if (!authenticate.getTokenType().equals("Bearer")) {
            throw new IllegalStateException("JWT token type is not Bearer.");
        }
        Map oAuthResponseMap =
                ExtensionApiResource.GSON_JSON_MAPPER.fromString(authenticate.getRawResponse(), Map.class);
        if (!oAuthResponseMap.containsKey("id_token")) {
            throw new IllegalStateException("JWT Token information not found in oAuth response.");
        }
    }

    /**
     * Validates that the account information map contains all required fields.
     *
     * @param accountMap map representing the account information
     * @throws IllegalArgumentException if the map is null, empty, or missing required keys
     */
    public static void validateAccount(Map accountMap) {
        if (accountMap == null || accountMap.isEmpty()) {
            throw new IllegalArgumentException("Account information is empty.");
        }
        List<String> missingKeys = new ArrayList<>();
        for (String requiredKey : Set.of("personId", "localId", "roles", "inboxId")) {
            if (!accountMap.containsKey(requiredKey)) {
                missingKeys.add(requiredKey);
            }
        }
        if (!missingKeys.isEmpty()) {
            throw new IllegalArgumentException("Account information missing data for few properties. " + missingKeys);
        }
    }

    /**
     * Validates the response from the client session creation API.
     *
     * @param createResponse response map returned by the create session API
     * @throws IllegalArgumentException if the response is null or missing required keys
     */
    public static void validateCreateResponse(Map<String, Object> createResponse) {
        if (createResponse == null) {
            throw new IllegalArgumentException("Create client session response is null.");
        }
        List<String> missingKeys = new ArrayList<>();
        for (String requiredKey : Set.of("clientSessionId", "person", "account")) {
            if (!createResponse.containsKey(requiredKey)) {
                missingKeys.add(requiredKey);
            }
        }
        if (!missingKeys.isEmpty()) {
            throw new IllegalArgumentException("Missing required keys from create session response. " + missingKeys);
        }
    }

    /**
     * Validates the attributes configured for the OAuth2 authentication extension.
     *
     * @param attributes configuration attributes supplied for the extension
     * @throws IllegalArgumentException if required attributes are missing or unsupported
     */
    public static void validateAttributes(Map<String, Object> attributes) {
        Set<String> requiredParams = Set.of(Oauth2ExtensionConstants.FLOWTYPE, Oauth2ExtensionConstants.CLIENT_ID,
                Oauth2ExtensionConstants.CLIENT_SECRET, Oauth2ExtensionConstants.SCOPE,
                Oauth2ExtensionConstants.CALLBACK_URL,
                Oauth2ExtensionConstants.AUTHORIZATION_BASE_URL,
                Oauth2ExtensionConstants.ACCESS_TOKEN_END_POINT);
        CommonUtils.validateInputParams(attributes, requiredParams,
                attributes.get(Oauth2ExtensionConstants.SUPPORTED_DOMAINS));

        String flowType = (String) attributes.get(Oauth2ExtensionConstants.FLOWTYPE);
        validateFlowType(flowType);
    }

    /**
     * Ensures that the supplied flow type value matches one of the supported {@link Configuration.FlowType}s.
     *
     * @param flowType flow type value to validate
     * @throws IllegalArgumentException if the supplied flow type is not supported
     */
    private static void validateFlowType(String flowType) {
        boolean requiredFlowTypeValueMatch = Arrays.stream(Configuration.FlowType.values())
                .anyMatch(flowTypeEntry -> flowTypeEntry.name().equals(flowType));
        if (!requiredFlowTypeValueMatch) {
            throw new IllegalArgumentException("oAuth2 FlowType " + flowType + " not supported.");
        }
    }

}
