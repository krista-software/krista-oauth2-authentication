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

package app.krista.extensions.krista.authentication.oauth2_authentication.resource;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import app.krista.extension.common.AuthenticationResponse;
import app.krista.extension.common.CommonUtils;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.krista.authentication.oauth2_authentication.Configuration;
import app.krista.extensions.krista.authentication.oauth2_authentication.KristaOAuth2Service;
import app.krista.extensions.krista.authentication.oauth2_authentication.OAuth2Service;
import app.krista.extensions.krista.authentication.oauth2_authentication.TokenManger;
import app.krista.extensions.krista.authentication.oauth2_authentication.constants.Oauth2ExtensionConstants;
import app.krista.extensions.krista.authentication.oauth2_authentication.manager.SessionManager;
import app.krista.extensions.krista.authentication.oauth2_authentication.util.Oauth2Authentication;
import app.krista.ksdk.accounts.AccountManager;
import app.krista.ksdk.authentication.AuthenticationSettings;
import app.krista.ksdk.authorization.RoleManager;
import app.krista.ksdk.context.RuntimeContext;
import app.krista.model.field.util.GsonJsonMapper;
import com.github.scribejava.core.model.OAuth2AccessToken;

@Path("/")
public class ExtensionApiResource {

    public static final GsonJsonMapper GSON_JSON_MAPPER = GsonJsonMapper.create();

    private final SessionManager sessionManager;
    private final AccountManager accountManager;
    private final AuthenticationSettings authenticationSettings;
    private final Invoker invoker;
    private final RuntimeContext runtimeContext;
    private final RoleManager roleManager;
    private final Oauth2Authentication oauth2Authentication;
    private final TokenManger tokenManager;
    private final OAuth2Service oAuth2Service = new OAuth2Service();

    private KristaOAuth2Service kristaOAuth2Service;

    @Inject
    public ExtensionApiResource(SessionManager sessionManager, AccountManager accountManager,
            AuthenticationSettings authenticationSettings, Invoker invoker,
            RuntimeContext runtimeContext, RoleManager roleManager,
            TokenManger tokenManger) {
        this.sessionManager = sessionManager;
        this.accountManager = accountManager;
        this.authenticationSettings = authenticationSettings;
        this.invoker = invoker;
        this.runtimeContext = runtimeContext;
        this.roleManager = roleManager;
        this.tokenManager = tokenManger;
        this.oauth2Authentication = Oauth2Authentication.getInstance(this.invoker);
    }

    @GET
    @Path("authenticator.js")
    /**
     * Serves the JavaScript authenticator resource used by the Krista web client.
     *
     * @return input stream for {@code authenticator.js}
     */
    public InputStream getAuthenticator() {
        return getClass().getClassLoader().getResourceAsStream("authenticator.js");
    }

    @GET
    @Path("/type")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * Returns the authentication type supported by this extension.
     *
     * @return the string {@code "OAuth2"}
     */
    public String getAuthType() {
        return "OAuth2";
    }

    @GET
    @Path("/getauthurl")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * Generates an OAuth2 authorization URL and returns an HTTP 302 redirect response.
     *
     * @param roles       optional comma-separated list of required roles
     * @param originalURI original URI requested by the client
     * @return redirect response with {@code Location} header set to the authorization URL
     * @throws IllegalArgumentException if configuration or domain validation fails
     */
    public Response getAuthorizationUrlAPI(@QueryParam("roles") String roles,
            @QueryParam(Oauth2ExtensionConstants.X_KRISTA_ORIG_URI) String originalURI) {
        try {
            CommonUtils.validateDomains(invoker.getAttributes().get(Oauth2ExtensionConstants.SUPPORTED_DOMAINS));
            Configuration configuration = oauth2Authentication.getConfiguration();
            String authorizationUrl =
                    oAuth2Service
                            .getAuthorizationUrl(configuration, roles, originalURI, kristaOAuth2Service, invoker);
            return Response.status(302).header(HttpHeaders.LOCATION, authorizationUrl).build();
        } catch (Exception cause) {
            throw new IllegalArgumentException("Failed to get authorization url.", cause);
        }
    }

    @GET
    @Path("/callback")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * Handles the OAuth2 callback, authenticates the user and creates a Krista session.
     *
     * @param headers HTTP headers received with the callback request
     * @param state   opaque state parameter returned by the identity provider
     * @param code    authorization code returned by the identity provider
     * @return redirect response back to the original URI with session cookie set
     * @throws IllegalArgumentException if the callback cannot be parsed or authentication fails
     */
    public Response getCallBack(@Context HttpHeaders headers, @QueryParam("state") String state,
            @QueryParam("code") String code) {
        try {
            if (state.isEmpty() || code.isEmpty()) {
                throw new IllegalArgumentException("Query parameter refreshToken not found.");
            }
            Map<String, String> states = oAuth2Service.getCallbackStates(state, code);
            if (kristaOAuth2Service == null) {
                kristaOAuth2Service = KristaOAuth2Service.create(oauth2Authentication.getConfiguration());
            }
            String userAgent = "NOT FOUND";
            if (headers != null && headers.getRequestHeader("User-Agent") != null) {
                userAgent = headers.getRequestHeader("User-Agent").get(0);
            }
            String workspaceId = invoker.getWorkspaceId();
            OAuth2AccessToken authenticate = kristaOAuth2Service.authenticate(states);
            String email = oAuth2Service.getEmailFromJwtToken(authenticate);
            AuthenticationResponse authenticationResponse =
                    oAuth2Service.getAuthenticationResponse(states, userAgent,
                            workspaceId, email, invoker, authenticationSettings, accountManager,
                            sessionManager, roleManager, runtimeContext);
            oauth2Authentication.tokenInformation(tokenManager, authenticate, authenticationResponse);
            String originalURL = states.get(Oauth2ExtensionConstants.X_KRISTA_ORIG_URI);
            URI originalUri = URI.create(originalURL);
            return Response.status(302)
                    .header("Set-Cookie",
                            createSessionIdCookie(authenticationResponse.getClientSessionId(), originalUri))
                    .header(HttpHeaders.LOCATION, originalUri).build();
        } catch (Exception cause) {
            throw new IllegalArgumentException("Failed to parse callback response.", cause);
        }
    }

    /**
     * Builds the {@code Set-Cookie} header value for the Krista session id.
     *
     * @param sessionInfo client session identifier to encode in the cookie
     * @param originalUri original URI requested by the client
     * @return cookie string including security attributes
     * @throws MalformedURLException if the original URI cannot be converted to a URL
     */
    private String createSessionIdCookie(String sessionInfo, URI originalUri)
            throws MalformedURLException {
        String cookieOptions = ";HttpOnly;path=/";
        if (originalUri.toURL().getProtocol().equals("https")) {
            cookieOptions += ";SameSite=None;Secure";
        }
        return Oauth2ExtensionConstants.X_KRISTA_SESSION_ID + "=" + sessionInfo + cookieOptions;
    }

    @GET
    @Path("/refreshtoken")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * Refreshes an access token using the provided refresh token.
     *
     * @param refreshToken refresh token issued by the identity provider
     * @return HTTP 200 response containing the new access token
     * @throws IllegalArgumentException if the refresh token is missing or the provider response is invalid
     */
    public Response getRefreshToken(@QueryParam("refreshToken") String refreshToken) {
        try {
            if (refreshToken.isEmpty()) {
                throw new IllegalArgumentException("Query parameter refreshToken not found.");
            }
            if (kristaOAuth2Service == null) {
                kristaOAuth2Service = KristaOAuth2Service.create(oauth2Authentication.getConfiguration());
            }
            OAuth2AccessToken oAuth2AccessToken = kristaOAuth2Service.refreshAccessToken(refreshToken);
            if (oAuth2AccessToken == null) {
                throw new IllegalArgumentException("Refresh token null response from identity provider.");
            }
            return Response.ok().entity(oAuth2AccessToken.getAccessToken()).build();
        } catch (Exception cause) {
            throw new IllegalArgumentException("Failed to get refreshToken.");
        }
    }

    @GET
    @Path("/revoketoken")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * Revokes an OAuth2 token with the identity provider and optionally clears the Krista session.
     *
     * @param token          access or refresh token to revoke
     * @param workspaceId    workspace identifier associated with the client session
     * @param clientSessionId Krista client session identifier to invalidate
     * @return HTTP 200 response confirming token revocation
     * @throws IllegalArgumentException if the token or revoke endpoint configuration is invalid
     */
    public Response getRevokeToken(@QueryParam("token") String token,
            @QueryParam("workspaceId") String workspaceId,
            @QueryParam("clientSessionId") String clientSessionId) {

        try {
            if (token.isEmpty()) {
                throw new IllegalArgumentException("Query parameter token not found.");
            }
            String revokeTokenEndPoint = oauth2Authentication.getConfiguration().getRevokeTokenEndPoint();
            if (revokeTokenEndPoint == null || revokeTokenEndPoint.isEmpty()) {
                throw new IllegalArgumentException(
                        "Revoke token end point is not configured as part of extension configuration.");
            }
            if (kristaOAuth2Service == null) {
                kristaOAuth2Service = KristaOAuth2Service.create(oauth2Authentication.getConfiguration());
            }

            //log out from oAuth2 service provider
            kristaOAuth2Service.revokeToken(token);

            //logout from krista
            if (workspaceId != null && clientSessionId != null) {
                sessionManager.deleteSession(clientSessionId);
            }
            return Response.ok().entity("Successfully revoked token.").build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to revoke token.");
        }
    }

}
