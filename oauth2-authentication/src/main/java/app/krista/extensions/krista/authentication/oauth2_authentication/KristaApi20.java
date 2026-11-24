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

import com.github.scribejava.core.builder.api.DefaultApi20;

public class KristaApi20 extends DefaultApi20 {

    private final String accessTokenEndPoint;
    private final String authorizationBaseUrl;
    private final String revokeTokenEndpoint;

    public KristaApi20(String accessTokenEndPoint, String authorizationBaseUrl, String revokeTokenEndpoint) {
        this.accessTokenEndPoint = accessTokenEndPoint;
        this.authorizationBaseUrl = authorizationBaseUrl;
        this.revokeTokenEndpoint = revokeTokenEndpoint;
    }

    /**
     * Creates a {@link DefaultApi20} implementation using the supplied endpoint URLs.
     *
     * @param accessTokenEndPoint    OAuth2 access token endpoint URL
     * @param authorizationBaseUrl   OAuth2 authorization endpoint URL
     * @param revokeTokenEndpoint    token revocation endpoint URL
     * @return configured {@link DefaultApi20} instance
     */
    public static DefaultApi20 instance(String accessTokenEndPoint, String authorizationBaseUrl,
            String revokeTokenEndpoint) {
        return new KristaApi20(accessTokenEndPoint, authorizationBaseUrl, revokeTokenEndpoint);
    }

    @Override
    /**
     * {@inheritDoc}
     *
     * @return configured access token endpoint URL
     */
    public String getAccessTokenEndpoint() {
        return accessTokenEndPoint;
    }

    @Override
    /**
     * {@inheritDoc}
     *
     * @return configured authorization base URL
     */
    protected String getAuthorizationBaseUrl() {
        return authorizationBaseUrl;
    }

    @Override
    /**
     * {@inheritDoc}
     *
     * @return configured revoke token endpoint URL
     */
    public String getRevokeTokenEndpoint() {
        return revokeTokenEndpoint;
    }

}
