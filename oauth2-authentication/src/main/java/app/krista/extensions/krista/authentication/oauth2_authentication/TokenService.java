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

import java.util.Map;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import app.krista.extension.common.TokenInfo;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.krista.authentication.oauth2_authentication.constants.Oauth2ExtensionConstants;
import app.krista.extensions.krista.authentication.oauth2_authentication.util.OAuth2Util;
import com.kristasoft.common.jaxrs.impl.GsonMessageBodyHandler;

public class TokenService {

    /**
     * Calls the configured access token URL to generate a new access token using the
     * provided refresh token.
     *
     * @param accountId   identifier of the account for which the token is being refreshed
     * @param refreshToken refresh token issued by the identity provider
     * @param invoker     current extension invoker providing configuration attributes
     * @return newly created {@link TokenInfo} containing the refreshed access token
     */
    private TokenInfo generateNewAccessToken(String accountId, String refreshToken, Invoker invoker) {
        Response postResponse = ClientBuilder.newClient().register(GsonMessageBodyHandler.create())
                .target(invoker.getAttributes()
                        .get(Oauth2ExtensionConstants.ACCESS_TOKEN_URL).toString())
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Map.of("client_id", Oauth2ExtensionConstants.CLIENT_ID,
                        "client_secret", Oauth2ExtensionConstants.CLIENT_SECRET,
                        "refreshToken", refreshToken,
                        "grant_type", "refresh_token")));
        AccessTokenResponseDTO accessTokenResponseDTO = postResponse.readEntity(AccessTokenResponseDTO.class);
        return new TokenInfo(accountId,
                accessTokenResponseDTO.getAccessToken(),
                accessTokenResponseDTO.getRefreshToken(),
                OAuth2Util.getExpiryTime(59), OAuth2Util.getExpiryTime(24 * 60),
                invoker.getInvokerId());
    }

}
