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
import app.krista.extensions.krista.authentication.oauth2_authentication.manager.SessionManager;
import app.krista.extensions.krista.authentication.oauth2_authentication.resource.ExtensionApiResource;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class ExtensionApiResourceAuthorizationURLTest {

    @Test
    public void testGetAuthType() {
        //given
        Invoker invoker = Mockito.mock(Invoker.class);
        ExtensionApiResource extensionApis = new ExtensionApiResource(null, null,
                null, invoker, null,
                null, null);

        //When
        String s = extensionApis.getAuthType();

        //Then
        Assert.assertEquals("OAuth2", s);
    }

    @Test
    public void testGetAuthorizationUrlAPI_EmptyInvokerAttributes() throws URISyntaxException {
        //Given
        Invoker invoker = Mockito.mock(Invoker.class);
        OAuth2Service oAuth2Service = new OAuth2Service();
        ExtensionApiResource extensionApis = new ExtensionApiResource(null, null, null,
                invoker, null, null, null);
        Mockito.when(invoker.getAttributes()).thenReturn(Map.of());

        //When
        try {
            extensionApis.getAuthorizationUrlAPI(null, null);
        }
        //Then
        catch (Exception cause) {
            Assert.assertEquals("Failed to get authorization url.", cause.getMessage());

        }
    }

    @Test
    public void testGetAuthorizationUrlAPI_MissingRequiredInvokerAttributes() throws URISyntaxException {
        //Given
        Invoker invoker = Mockito.mock(Invoker.class);
        SessionManager sessionManager = Mockito.mock(SessionManager.class);
        OAuth2Service oAuth2Service = new OAuth2Service();

        ExtensionApiResource extensionApiResource = new ExtensionApiResource(sessionManager, null,
                null, invoker, null,
                null, null);

        Mockito.when(invoker.getAttributes())
                .thenReturn(Map.of(Oauth2ExtensionConstants.FLOWTYPE, "authorizationCode"));
        //When
        try {
            extensionApiResource.getAuthorizationUrlAPI(null, null);
        }
        //Then
        catch (Exception cause) {
            Assert.assertEquals("Failed to get authorization url.", cause.getMessage());
        }
    }

}

