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

import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import app.krista.extension.common.AuthenticationResponse;
import app.krista.model.field.util.GsonJsonMapper;
import com.kristasoft.common.jaxrs.impl.GsonMessageBodyHandler;

public class TestApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(TestResource.class);
    }

    @Override
    public Set<Object> getSingletons() {
        return Set.of(GsonMessageBodyHandler.create());
    }

    @Path("/")
    public static class TestResource {

        final static GsonJsonMapper gsonJsonMapper = GsonJsonMapper.create();

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public AuthenticationResponse get() {
            String emailData = "{\n" +
                    "  \"clientSessionId\": \"device_b195adb3-52d8-475d-9c4f-0677e57abd9c\",\n" +
                    "  \"name\": \"theja\",\n" +
                    "  \"avatarUrl\": \"\",\n" +
                    "  \"accountId\": \"wsContact_e0376556-c1d6-4262-a2eb-071d5eedf547\",\n" +
                    "  \"personId\": \"single_56af6382-c755-4631-9ed2-ebc52e303d7b\",\n" +
                    "  \"roles\": [\n" +
                    "    \"defnRole_4f262fa7-f4cc-4f09-a58e-0ecda477e07a\",\n" +
                    "    \"defnRole_e589209f-929b-4b97-a172-3086e67bd35f\"\n" +
                    "  ],\n" +
                    "  \"inboxId\": \"inbox_wsContact_e0376556-c1d6-4262-a2eb-071d5eedf547\",\n" +
                    "  \"isWorkspaceAdmin\": false,\n" +
                    "  \"isApplianceManager\": false,\n" +
                    "  \"identificationToken\": {\n" +
                    "    \"email\": \"theja619@gmail.com\"\n" +
                    "  },\n" +
                    "  \"extras\": {\n" +
                    "    \"oAuth2\": \"{\\n  \\\"access_token\\\": \\\"REDACTED_SECRET\\\",\\n  \\\"expires_in\\\": 3598,\\n  \\\"refresh_token\\\": \\\"REDACTED_SECRET\\\",\\n  \\\"scope\\\": \\\"https://www.googleapis.com/auth/userinfo.email openid\\\",\\n  \\\"token_type\\\": \\\"Bearer\\\",\\n  \\\"id_token\\\": \\\"REDACTED_SECRET\\\"\\n}\"\n" +
                    "  }\n" +
                    "}";
            return gsonJsonMapper.fromString(emailData, AuthenticationResponse.class);
        }

    }

}
