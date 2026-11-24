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

import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.inject.Inject;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.common.CSVUtils;
import app.krista.extension.common.TokenInfo;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.*;
import app.krista.extensions.krista.authentication.oauth2_authentication.constants.Oauth2ExtensionConstants;
import app.krista.extensions.krista.authentication.oauth2_authentication.manager.SessionManager;
import app.krista.extensions.krista.authentication.oauth2_authentication.util.Oauth2Authentication;
import app.krista.extensions.util.KeyValueStore;
import app.krista.ksdk.context.AuthorizationContext;

@Field(name = Oauth2ExtensionConstants.FLOWTYPE, type = "Text")
@Field(name = Oauth2ExtensionConstants.CLIENT_ID, type = "Text")
@Field(name = Oauth2ExtensionConstants.CLIENT_SECRET, type = "Text", attributes = @Attribute(name = "isSecured", value = "true"))
@Field(name = Oauth2ExtensionConstants.SCOPE, type = "Text")
@Field(name = Oauth2ExtensionConstants.CALLBACK_URL, type = "Text")
@Field(name = Oauth2ExtensionConstants.ACCESS_TOKEN_END_POINT, type = "Text")
@Field(name = Oauth2ExtensionConstants.AUTHORIZATION_BASE_URL, type = "Text")
@Field(name = Oauth2ExtensionConstants.REVOKE_TOKEN_URL, type = "Text")
@Field(name = Oauth2ExtensionConstants.ALLOW_AUTO_PERSON_CREATION, type = "Switch")
@Field(name = Oauth2ExtensionConstants.SUPPORTED_DOMAINS, type = "Text")
@Field(name = Oauth2ExtensionConstants.ACCESS_TOKEN_URL, type = "Text")
@Java(version = Java.Version.JAVA_21)
@Extension(jaxrsId = "authn", implementingDomainIds = "catEntryDomain_db053e8f-a194-4dde-aa6f-701ef7a6b3a7",
        requireWorkspaceAdminRights = true, name="OAuth2 Authentication", version = "3.5.7")
@Domain(id = "catEntryDomain_db053e8f-a194-4dde-aa6f-701ef7a6b3a7",
        name = "Authentication",
        ecosystemId = "catEntryEcosystem_d3b05047-07b0-4b06-95a3-9fb8f7f608d9",
        ecosystemName = "Krista",
        ecosystemVersion = "3e7e09ed-688f-41fa-ab7c-ff879e750011")
@StaticResource(path = "docs", file = "docs")
public class Oauth2AuthenticationExtension {

    public final Invoker invoker;
    public final SessionManager sessionManager;
    private final AuthorizationContext authorizationContext;
    private final KeyValueStore keyValueStore;
    private final Oauth2Authentication oauth2Authentication;
    private RequestAuthenticator requestAuthenticator;

    private final CSVUtils csvUtils = new CSVUtils();

    @Inject
    public Oauth2AuthenticationExtension(Invoker invoker, SessionManager sessionManager,
            AuthorizationContext authorizationContext, KeyValueStore keyValueStore) {
        this.invoker = invoker;
        this.sessionManager = sessionManager;
        this.authorizationContext = authorizationContext;
        this.keyValueStore = keyValueStore;
        this.oauth2Authentication = Oauth2Authentication.getInstance(this.invoker);
        this.requestAuthenticator = new ExtensionRequestAuthenticator(invoker, authorizationContext, sessionManager);
    }

    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    /**
     * Provides a {@link RequestAuthenticator} capable of authenticating incoming requests
     * using the OAuth2 flow configured for this extension.
     *
     * @return configured {@link RequestAuthenticator} instance
     */
    public RequestAuthenticator getAuthenticatedAccountId() {
        this.requestAuthenticator = new ExtensionRequestAuthenticator(invoker, authorizationContext, sessionManager);
        return this.requestAuthenticator;
    }

    @InvokerRequest(InvokerRequest.Type.INVOKER_UPDATED)
    /**
     * Migrates legacy token information stored as CSV files into the configured key-value store.
     *
     * @param old  previous invoker attributes
     * @param newA updated invoker attributes
     * @throws IOException if reading from or writing to the token store fails
     */
    public void invokerUpgrade(Map<String, Object> old, Map<String, Object> newA) throws IOException {
        File directory = new File("/opt/krista/tokens/");
        if (directory.list() == null) {
            return;
        }
        for (String files : directory.list()) {
            if (files.endsWith(".csv")) {
                TokenInfo tokenInfo = csvUtils.readFromCSV(files);
                if (tokenInfo == null) {
                    continue;
                }
                keyValueStore.put(files.substring(0, files.indexOf(".")), tokenInfo.toString());
                csvUtils.deleteFromCSV(files);
            }
        }
    }

    @InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
    /**
     * Validates the extension configuration by generating an authorization URL.
     *
     * @param connectionAttributes attributes supplied for the connection test
     */
    public void testConnection(Map<String, Object> connectionAttributes) {
        Configuration configuration = oauth2Authentication.getConfiguration(connectionAttributes);
        KristaOAuth2Service kristaOAuth2Service = KristaOAuth2Service.create(configuration);
        OAuth2Service authService = new OAuth2Service();
        String authorizationURL = authService.getAuthorizationUrl(configuration, "", "", kristaOAuth2Service, invoker);
        System.out.println(
                "\n\n********* Authorization URL generated successfully and is :" + authorizationURL +
                        " *********\n\n");
    }

    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    /**
     * Exposes custom configuration tabs for the extension UI.
     *
     * @return map linking tab labels to static resource paths
     */
    public Map<String, String> customTab() {
        return Map.of("Documentation", "static/docs");
    }

}
