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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.ws.rs.core.HttpHeaders;
import app.krista.extension.authorization.MustAuthenticateException;
import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.extension.request.ProtoRequest;
import app.krista.extension.request.ProtoResponse;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extension.request.protos.http.HttpRequest;
import app.krista.extension.request.protos.http.HttpResponse;
import app.krista.extensions.krista.authentication.oauth2_authentication.constants.Oauth2ExtensionConstants;
import app.krista.extensions.krista.authentication.oauth2_authentication.manager.SessionManager;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.model.field.NamedField;

import static app.krista.extension.request.RoutingInfo.Type.*;

public class ExtensionRequestAuthenticator implements RequestAuthenticator {

    private final Invoker invoker;
    private final AuthorizationContext authorizationContext;
    private final SessionManager sessionManager;

    public ExtensionRequestAuthenticator(Invoker invoker, AuthorizationContext authorizationContext,
            SessionManager sessionManager) {
        this.invoker = invoker;
        this.authorizationContext = authorizationContext;
        this.sessionManager = sessionManager;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public Set<String> getSupportedProtocols() {
        return null;
    }

    @Override
    /**
     * Resolves the authenticated account identifier for the given request.
     *
     * <p>If a Krista session cookie is present, the account id is obtained from the
     * {@link SessionManager}. Otherwise this delegates to {@link #handleAuthRequest(ProtoRequest)}
     * to determine the authenticated account.</p>
     *
     * @param protoRequest incoming request to authenticate; expected to be an {@link HttpRequest}
     * @return authenticated account id, or {@code null} if authentication cannot be resolved
     */
    public String getAuthenticatedAccountId(ProtoRequest protoRequest) {
        HttpRequest httpRequest = (HttpRequest) protoRequest;
        try {
            String accountId;
            String sessionId = getSessionIdFromCookie(httpRequest);
            if (sessionId != null) {
                accountId = sessionManager.getAccountId(sessionId);
            } else {
                accountId = handleAuthRequest(httpRequest);
            }
            return accountId;
        } catch (IOException cause) {
            System.err.println(cause.getMessage());
            return null;
        }
    }

    /**
     * Extracts the Krista session identifier from the HTTP Cookie header.
     *
     * @param protoRequest HTTP request carrying the session cookie
     * @return session id value if present; otherwise {@code null}
     */
    private String getSessionIdFromCookie(HttpRequest protoRequest) {
        String sessionId = null;
        String cookie = protoRequest.getHeader("Cookie");
        if (cookie != null && cookie.contains(Oauth2ExtensionConstants.X_KRISTA_SESSION_ID)) {
            String[] commaSeparatedValues = cookie.split(";");
            for (String cookieKeyValue : commaSeparatedValues) {
                if (cookieKeyValue.contains(Oauth2ExtensionConstants.X_KRISTA_SESSION_ID)) {
                    String[] parts = cookieKeyValue.trim().split("=", 2);
                    if (parts.length > 1) {
                        sessionId = parts[1];
                    }
                }
            }
        }
        return sessionId;
    }

    @Override
    public boolean setServiceAuthorization(String s) {
        return false;
    }

    @Override
    public Map<String, NamedField> getAttributeFields() {
        return null;
    }

    @Override
    /**
     * Creates a redirect response that initiates the OAuth2 authentication flow.
     *
     * @param cause   the reason authentication is required
     * @param request original unauthenticated request
     * @return HTTP 302 response with {@code Location} header set to the authorization URL
     */
    public ProtoResponse getMustAuthenticateResponse(MustAuthenticateException cause, ProtoRequest request) {
        String uri = ((HttpRequest) request).getHeader(Oauth2ExtensionConstants.X_KRISTA_ORIG_URI);
        String encodedUri = URLEncoder.encode(uri, StandardCharsets.UTF_8);
        return new HttpResponse(302,
                Map.of(HttpHeaders.LOCATION, getAuthUrl(encodedUri)),
                new ByteArrayInputStream(new byte[0]));
    }

    /**
     * Builds the authorization URL that the client should be redirected to.
     *
     * @param encodedUri percent-encoded original URI of the client request
     * @return absolute authorization URL including the original URI as a query parameter
     */
    public String getAuthUrl(String encodedUri) {
        return invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, APPLIANCE)
                + "/authn/getauthurl?" + Oauth2ExtensionConstants.X_KRISTA_ORIG_URI + "=" + encodedUri;

        /** for local testing use below return statement.....
         return "http://localhost:8765/extension/api/" + invoker.getRoutingInfo().getRoutingId()
         + "/authn/getauthurl?" + Oauth2ExtensionConstants.X_KRISTA_ORIG_URI + "=" + encodedUri; **/
    }

    @Override
    public AuthorizationResponse getMustAuthenticateResponse(MustAuthenticateException cause) {
        return null;
    }

    @Override
    public ProtoResponse getMustAuthorizeResponse(MustAuthorizeException cause, ProtoRequest request) {
        return null;
    }

    @Override
    public AuthorizationResponse getMustAuthorizeResponse(MustAuthorizeException cause) {
        return null;
    }

    /**
     * Handles authentication-related HTTP requests and resolves the account id on callbacks.
     *
     * @param protoRequest incoming request, expected to be an {@link HttpRequest}
     * @return authorized account id when handling a callback, or {@code null} otherwise
     */
    private String handleAuthRequest(ProtoRequest protoRequest) {
        if (!(protoRequest instanceof HttpRequest)) {
            return null;
        }
        System.out.println(((HttpRequest) protoRequest).getUri().getPath());
        if (Objects.equals("/callback", ((HttpRequest) protoRequest).getUri().getPath())) {
            System.out.println("call back account id: " + authorizationContext.getAuthorizedAccount().getAccountId());
            return authorizationContext.getAuthorizedAccount().getAccountId();
        }
        return null;
    }

}
