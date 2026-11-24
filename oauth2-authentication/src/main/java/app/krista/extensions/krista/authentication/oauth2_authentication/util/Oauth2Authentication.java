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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import app.krista.extension.common.AuthenticationResponse;
import app.krista.extension.common.TokenInfo;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.krista.authentication.oauth2_authentication.Configuration;
import app.krista.extensions.krista.authentication.oauth2_authentication.TokenManger;
import app.krista.extensions.krista.authentication.oauth2_authentication.constants.Oauth2ExtensionConstants;
import com.github.scribejava.core.model.OAuth2AccessToken;

public class Oauth2Authentication {

    private static Oauth2Authentication instance;
    private final Invoker invoker;

    private Oauth2Authentication(Invoker invoker) {
        this.invoker = invoker;
    }

    /**
     * Returns the singleton {@link Oauth2Authentication} instance for the given invoker.
     *
     * @param invoker current extension invoker
     * @return shared {@link Oauth2Authentication} instance
     */
    public static Oauth2Authentication getInstance(Invoker invoker) {
        return instance == null ? (instance = new Oauth2Authentication(invoker)) : instance;
    }

    public Configuration getConfiguration() {
        if (invoker == null) {
            throw new IllegalStateException("Invoker object is not initialized.");
        }
        Map<String, Object> attributes = invoker.getAttributes();
        return getConfiguration(attributes);
    }

    /**
     * Builds an immutable {@link Configuration} from the supplied attribute map.
     * <p>
     * This method is suitable both for persisted invoker attributes and for
     * transient connection attributes provided during first-time validation.
     *
     * @param attributes configuration attributes to use when building the configuration
     * @return resolved OAuth2 configuration
     * @throws IllegalArgumentException if required configuration attributes are missing or invalid
     */
    public Configuration getConfiguration(Map<String, Object> attributes) {
        Validator.validateAttributes(attributes);
        String flowType = (String) attributes.get(Oauth2ExtensionConstants.FLOWTYPE);
        return new Configuration(Configuration.FlowType.valueOf(flowType),
                (String) attributes.get(Oauth2ExtensionConstants.CLIENT_ID),
                (String) attributes.get(Oauth2ExtensionConstants.CLIENT_SECRET),
                List.of((String) attributes.get(Oauth2ExtensionConstants.SCOPE)),
                (String) attributes.get(Oauth2ExtensionConstants.CALLBACK_URL),
                (String) attributes.get(Oauth2ExtensionConstants.ACCESS_TOKEN_END_POINT),
                (String) attributes.get(Oauth2ExtensionConstants.AUTHORIZATION_BASE_URL),
                (String) attributes.get(Oauth2ExtensionConstants.REVOKE_TOKEN_URL));
    }

    /**
     * Persists token information for the authenticated account using the supplied token manager.
     *
     * @param tokenManager           manager used to store token details
     * @param authenticate           OAuth2 access token returned by the identity provider
     * @param authenticationResponse Krista authentication response for the account
     * @throws IOException if the token information cannot be stored
     */
    public void tokenInformation(TokenManger tokenManager, OAuth2AccessToken authenticate,
            AuthenticationResponse authenticationResponse)
            throws IOException {
        TokenInfo tokenInfo = new TokenInfo(authenticationResponse.getAccountId(),
                authenticate.getAccessToken(),
                authenticate.getRefreshToken(),
                OAuth2Util.getExpiryTime(59),
                OAuth2Util.getExpiryTime(24 * 60),
                invoker.getInvokerId());
        System.out.println("invokerID:" + invoker.getInvokerId());
        tokenManager.addTokenInfo(tokenInfo);
    }

}
