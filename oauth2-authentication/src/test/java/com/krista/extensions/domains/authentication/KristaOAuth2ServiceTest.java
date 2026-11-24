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

package com.krista.extensions.domains.authentication;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import app.krista.extensions.krista.authentication.oauth2_authentication.Configuration;
import app.krista.extensions.krista.authentication.oauth2_authentication.KristaOAuth2Service;
import org.junit.Assert;
import org.junit.Test;

public class KristaOAuth2ServiceTest {

    @Test
    public void getAuthorizationUrl_Google_GetAuthorizationUrl()
            throws IOException, ExecutionException, InterruptedException {
        Configuration configuration = getCustom_GoogleConfiguration();
        System.out.println(configuration);
        String authorizationUrl = getAuthorizationUrl(configuration);
        Assert.assertNotNull(authorizationUrl);
    }

    @Test
    public void getAuthorizationUrl_Microsoft_GetAuthorizationUrl()
            throws IOException, ExecutionException, InterruptedException {
        Configuration configuration = getMicrosoftConfiguration();
        String authorizationUrl = getAuthorizationUrl(configuration);
        Assert.assertNotNull(authorizationUrl);
    }

    @Test
    public void getAuthorizationUrl_LinkedIn_GetAuthorizationUrl()
            throws IOException, ExecutionException, InterruptedException {
        Configuration configuration = getCustomConfiguration_LinkedIn();
        String authorizationUrl = getAuthorizationUrl(configuration);
        Assert.assertNotNull(authorizationUrl);
    }

    @Test
    public void getAuthorizationUrl_Custom_GetAuthorizationUrl()
            throws IOException, ExecutionException, InterruptedException {
        Configuration configuration = getCustomConfiguration_LinkedIn();
        String authorizationUrl = getAuthorizationUrl(configuration);
        Assert.assertNotNull(authorizationUrl);
    }

    @Test
    public void getAuthorizationUrl_Custom_GOOGLE_GetAuthorizationUrl()
            throws IOException, ExecutionException, InterruptedException {
        Configuration configuration = getCustom_GoogleConfiguration();
        String authorizationUrl = getAuthorizationUrl(configuration);
        Assert.assertNotNull(authorizationUrl);
    }

    private String getAuthorizationUrl(Configuration configuration) {
        KristaOAuth2Service kristaOAuth2Service = KristaOAuth2Service.create(configuration);
        String authorizationUrl = kristaOAuth2Service.getAuthorizationUrl(Map.of("name", "deepak"));
        System.out.println(authorizationUrl);
        return authorizationUrl;
    }

    private Configuration getMicrosoftConfiguration() {
        return new Configuration(Configuration.FlowType.authorizationCode,
                "363e19cc-2df3-4f3d-9213-ea01d6f629a8",
                "ai-H8lYV8ThS.8_IF~Lb4Ms9XA-1RTyMtX",
                List.of("https://graph.microsoft.com/.default"),
                "http://localhost:8080/callback",
                "https://login.microsoftonline.com/common/oauth2/token",
                "https://login.microsoftonline.com/common/oauth2/authorize",
                null);
    }

    private Configuration getCustomConfiguration_LinkedIn() {
        return new Configuration(Configuration.FlowType.authorizationCode,
                "861wdl3dqjs2ng",
                "mpEjxF018DeHXsfl",
                List.of("r_liteprofile", "r_emailaddress"),
                "http://localhost:8080/callback",
                "https://www.linkedin.com/oauth/v2/accessToken",
                "https://www.linkedin.com/oauth/v2/authorization",
                null);
    }

    private Configuration getCustom_GoogleConfiguration() {
        return new Configuration(Configuration.FlowType.authorizationCode,
                "1045400217786-2g6mnvt4paus24enga9ndr4e9slcpel7.apps.googleusercontent.com",
                "vqmOfj33X96n-K52mHrpWTqq",
                List.of("profile"),
                "http://localhost:8080/callback",
                "https://oauth2.googleapis.com/token",
                "https://accounts.google.com/o/oauth2/v2/auth",
                "https://oauth2.googleapis.com/revoke");
    }

}