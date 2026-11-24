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

package com.krista.extensions.domains.authentication.oauth2authenticator;

import java.net.URISyntaxException;
import java.util.Map;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.krista.authentication.oauth2_authentication.OAuth2Service;
import app.krista.extensions.krista.authentication.oauth2_authentication.constants.Oauth2ExtensionConstants;
import app.krista.extensions.krista.authentication.oauth2_authentication.resource.ExtensionApiResource;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class ExtensionApiResourceRefreshTokenTest {

    @Test
    public void testRefreshToken_EmptyQueryParams() throws URISyntaxException {
        //Given
        Invoker invoker = Mockito.mock(Invoker.class);
        OAuth2Service oAuth2Service = new OAuth2Service();
        ExtensionApiResource extensionApis = new ExtensionApiResource(null, null,
                null, invoker, null,
                null, null);
        Mockito.when(invoker.getAttributes())
                .thenReturn(Map.of(Oauth2ExtensionConstants.FLOWTYPE, "authorizationCode",
                        Oauth2ExtensionConstants.CLIENT_ID, "dummyClientCode",
                        Oauth2ExtensionConstants.CLIENT_SECRET, "dummySecret",
                        Oauth2ExtensionConstants.SCOPE, "dummyScope",
                        Oauth2ExtensionConstants.CALLBACK_URL, "http://dummy.callback.example.com",
                        Oauth2ExtensionConstants.AUTHORIZATION_BASE_URL,
                        "http://dummy.authorization.base.url.example.com",
                        Oauth2ExtensionConstants.ACCESS_TOKEN_END_POINT,
                        "http://dummy.access.token.end.point.example.com"));
        Mockito.when(invoker.getWorkspaceId()).thenReturn("ws_dummy_workspaceId");
        Mockito.when(invoker.getInvokerId()).thenReturn("INV_dummy_invokerId");

        //When
        try {
            extensionApis.getRefreshToken("");
        }

        //Then
        catch (Exception cause) {
            Assert.assertEquals("Failed to get refreshToken.", cause.getMessage());
        }
    }

}
