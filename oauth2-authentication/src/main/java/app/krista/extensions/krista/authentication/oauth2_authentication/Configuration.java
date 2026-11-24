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

import java.util.List;

public class Configuration {

    public enum FlowType {
        authorizationCode,
        implicit,
        clientCredentials
    }

    private FlowType flowType;
    private String clientId;
    private String clientSecret;
    private List<String> scopes;
    private String callBackUrl;
    private String accessTokenEndPoint;
    private String authorizationBaseUrl;
    private String revokeTokenEndPoint;

    public Configuration(FlowType flowType, String clientId, String clientSecret, List<String> scopes,
            String callBackUrl, String accessTokenEndPoint, String authorizationBaseUrl, String revokeTokenEndPoint) {
        this.flowType = flowType;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scopes = scopes;
        this.callBackUrl = callBackUrl;
        this.accessTokenEndPoint = accessTokenEndPoint;
        this.authorizationBaseUrl = authorizationBaseUrl;
        this.revokeTokenEndPoint = revokeTokenEndPoint;
    }

    public FlowType getFlowType() {
        return flowType;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public String getCallBackUrl() {
        return callBackUrl;
    }

    public String getAccessTokenEndPoint() {
        return accessTokenEndPoint;
    }

    public String getAuthorizationBaseUrl() {
        return authorizationBaseUrl;
    }

    public String getRevokeTokenEndPoint() {
        return revokeTokenEndPoint;
    }

}