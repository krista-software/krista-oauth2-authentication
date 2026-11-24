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

package app.krista.extensions.krista.authentication.oauth2_authentication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import com.github.scribejava.core.builder.ScopeBuilder;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

public class KristaOAuth2Service {

    private final Configuration configuration;
    private final OAuth20Service oAuth20Service;

    /**
     * Factory method for creating a {@link KristaOAuth2Service} for the given configuration.
     *
     * @param configuration OAuth2 configuration for the current invoker
     * @return newly created {@link KristaOAuth2Service}
     */
    public static KristaOAuth2Service create(Configuration configuration) {
        OAuth20Service oAuth2Service = createOAuth2Service(configuration);
        return new KristaOAuth2Service(configuration, oAuth2Service);
    }

    private KristaOAuth2Service(Configuration configuration, OAuth20Service oAuth20Service) {
        this.configuration = configuration;
        this.oAuth20Service = oAuth20Service;
    }

    /**
     * Builds the authorization URL for the configured OAuth2 flow.
     *
     * @param states map of state parameters to include in the authorization request
     * @return authorization URL to which the user agent should be redirected
     * @throws IllegalStateException if the configured flow type is unsupported
     */
    public String getAuthorizationUrl(Map<String, String> states) {
        String authUrl;
        switch (configuration.getFlowType()) {
            case authorizationCode:
            case implicit:
                authUrl = getAuthUrl(states);
                break;
            case clientCredentials:
                authUrl = oAuth20Service.getAuthorizationUrl();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + configuration.getFlowType().name());
        }
        return authUrl;
    }

    /**
     * Exchanges the OAuth2 authorization callback for an access token.
     *
     * @param callbackStates state map created from the identity provider callback
     * @return {@link OAuth2AccessToken} obtained from the identity provider
     * @throws IOException          if the underlying HTTP call fails
     * @throws ExecutionException   if the asynchronous token acquisition fails
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public OAuth2AccessToken authenticate(Map<String, String> callbackStates)
            throws IOException, ExecutionException, InterruptedException {
        OAuth2AccessToken oAuth2AccessToken = null;
        switch (configuration.getFlowType()) {
            case clientCredentials:
                oAuth2AccessToken = oAuth20Service.getAccessTokenClientCredentialsGrant();
                break;
            case authorizationCode:
            case implicit:
                String code = callbackStates.get("code");
                oAuth2AccessToken = oAuth20Service.getAccessToken(code);
                break;
        }
        return oAuth2AccessToken;
    }

    /**
     * Revokes a token using the configured revoke token endpoint.
     *
     * @param tokenToRevoke access or refresh token to revoke
     * @throws IOException          if the HTTP revocation request fails
     * @throws ExecutionException   if the asynchronous revocation fails
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public void revokeToken(String tokenToRevoke) throws IOException, ExecutionException, InterruptedException {
        oAuth20Service.revokeToken(tokenToRevoke);
    }

    /**
     * Refreshes an access token using the supplied refresh token.
     *
     * @param refreshToken refresh token issued by the identity provider
     * @return new {@link OAuth2AccessToken} created from the refresh token
     * @throws IOException          if the HTTP refresh request fails
     * @throws ExecutionException   if the asynchronous refresh fails
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public OAuth2AccessToken refreshAccessToken(String refreshToken)
            throws IOException, ExecutionException, InterruptedException {
        return oAuth20Service.refreshAccessToken(refreshToken);
    }

    /**
     * Constructs an authorization URL for the authorization code or implicit flow.
     *
     * @param states state parameters to encode in the URL
     * @return fully built authorization URL
     */
    private String getAuthUrl(Map<String, String> states) {
        final Map<String, String> additionalParams = new HashMap<>();
        additionalParams.put("access_type", "offline");
        //force to regenerate refresh token (if user are asked not the first time)
        additionalParams.put("prompt", "consent");
        return oAuth20Service.createAuthorizationUrlBuilder()
                .state(getStateString(states))
                .additionalParams(additionalParams)
                .build();
    }

    /**
     * Serializes the given state parameters into a single string suitable for the OAuth2
     * {@code state} parameter.
     *
     * @param states state parameters to serialize
     * @return serialized state value
     */
    private String getStateString(Map<String, String> states) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : states.entrySet()) {
            stringBuilder.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append(",");
        }
        return stringBuilder.append("createDateTime").append("=").append(System.currentTimeMillis()).toString();
    }

    /**
     * Creates the underlying {@link OAuth20Service} instance from the supplied configuration.
     *
     * @param configuration OAuth2 configuration for the current invoker
     * @return configured {@link OAuth20Service}
     */
    private static OAuth20Service createOAuth2Service(Configuration configuration) {
        ScopeBuilder scopeBuilder = new ScopeBuilder();
        scopeBuilder.withScopes(configuration.getScopes());
        return new ServiceBuilder(configuration.getClientId())
                .apiSecret(configuration.getClientSecret())
                .defaultScope(scopeBuilder.build())
                .callback(configuration.getCallBackUrl())
                .build(KristaApi20.instance(configuration.getAccessTokenEndPoint(),
                        configuration.getAuthorizationBaseUrl(), configuration.getRevokeTokenEndPoint()));
    }

}
