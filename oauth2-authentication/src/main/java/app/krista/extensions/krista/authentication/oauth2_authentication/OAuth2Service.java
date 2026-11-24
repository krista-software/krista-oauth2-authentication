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

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import app.krista.extension.authorization.AccessDeniedException;
import app.krista.extension.authorization.AuthorizationException;
import app.krista.extension.common.AuthenticationResponse;
import app.krista.extension.common.CommonUtils;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.krista.authentication.oauth2_authentication.constants.Oauth2ExtensionConstants;
import app.krista.extensions.krista.authentication.oauth2_authentication.manager.SessionManager;
import app.krista.extensions.krista.authentication.oauth2_authentication.resource.ExtensionApiResource;
import app.krista.extensions.krista.authentication.oauth2_authentication.util.Validator;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.accounts.AccountManager;
import app.krista.ksdk.accounts.ModifiableAccount;
import app.krista.ksdk.authentication.AuthenticationSettings;
import app.krista.ksdk.authorization.ModifiableRole;
import app.krista.ksdk.authorization.Role;
import app.krista.ksdk.authorization.RoleManager;
import app.krista.ksdk.context.RuntimeContext;
import com.github.scribejava.core.model.OAuth2AccessToken;

public class OAuth2Service {

    /**
     * Parses the {@code state} and {@code code} parameters returned by the identity provider
     * into a map of callback state values.
     *
     * @param state raw state parameter returned by the identity provider
     * @param code  authorization code returned by the identity provider
     * @return map containing the authorization code and parsed state values
     */
    public Map<String, String> getCallbackStates(String state, String code) {
        Map<String, String> states = new HashMap<>();
        //add code
        states.put("code", code);
        //add state values
        parseAndAddStateValues(states, state);
        return states;
    }

    /**
     * Builds the authorization URL for the given configuration and optional role requirements.
     *
     * @param configuration      OAuth2 configuration for the invoker
     * @param roles              optional comma-separated list of required roles
     * @param originalURI        original URI requested by the client
     * @param kristaOAuth2Service optional preconfigured OAuth2 service; may be {@code null}
     * @param invoker            current extension invoker
     * @return authorization URL to which the user agent should be redirected
     * @throws IllegalArgumentException if the invoker workspace id is missing
     */
    public String getAuthorizationUrl(Configuration configuration, String roles, String originalURI,
            KristaOAuth2Service kristaOAuth2Service, Invoker invoker) {
        if (kristaOAuth2Service == null) {
            kristaOAuth2Service = KristaOAuth2Service.create(configuration);
        }
        if (invoker.getWorkspaceId() == null) {
            throw new IllegalArgumentException("Workspace Id missing for invoker " + invoker.getInvokerId());
        }
        Map<String, String> states = new HashMap<>();
        states.put("workspaceId", invoker.getWorkspaceId());
        if (roles != null && !roles.isEmpty()) {
            states.put("roles", roles);
        }
        if (originalURI != null && !originalURI.isBlank() && !originalURI.isEmpty()) {
            states.put(Oauth2ExtensionConstants.X_KRISTA_ORIG_URI, originalURI);
        }
        return kristaOAuth2Service.getAuthorizationUrl(states);
    }

    /**
     * Creates a JSON fragment containing the current time to be stored as creation metadata.
     *
     * @return JSON string with a {@code creationTime} field set to the current time in millis
     */
    public String getCreationTimeString() {
        String extras = "{\"creationTime\":\"$currentTime\"}";
        extras = extras.replace("$currentTime", "" + System.currentTimeMillis());
        return extras;
    }

    /**
     * Creates an {@link AuthenticationResponse} for the authenticated user, handling account lookup
     * or creation and session persistence.
     *
     * @param states                 state parameters from the OAuth2 callback
     * @param userAgent              user agent string of the client
     * @param workspaceId            identifier of the current workspace
     * @param email                  email address resolved from the identity provider
     * @param invoker                current extension invoker
     * @param authenticationSettings workspace authentication settings
     * @param accountManager         account manager used for lookups and creation
     * @param sessionManager         session manager used to create client sessions
     * @param roleManager            role manager used to resolve and create roles
     * @param runtimeContext         runtime context of the current request
     * @return populated {@link AuthenticationResponse}
     * @throws AuthorizationException if authorization checks fail
     * @throws IOException            if session persistence fails
     */
    public AuthenticationResponse getAuthenticationResponse(Map<String, String> states, String userAgent,
            String workspaceId, String email, Invoker invoker,
            AuthenticationSettings authenticationSettings,
            AccountManager accountManager, SessionManager sessionManager,
            RoleManager roleManager, RuntimeContext runtimeContext) throws AuthorizationException, IOException {
        boolean allowAutoUserCreationBool = isAutoUserCreationAllowed(invoker);
        System.out.println("AllowAutoUserCreation:" + allowAutoUserCreationBool);
        List<String> supportedDomainsForWorkspace = authenticationSettings.getSupportedDomains();
        System.out.println("supported Domains:" + supportedDomainsForWorkspace);
        String supportedDomain = String.join(",", supportedDomainsForWorkspace);
        CommonUtils.validateAutoUserCreation(workspaceId, email, allowAutoUserCreationBool, supportedDomain,
                accountManager);
        Object invokerDomains = invoker.getAttributes().get(Oauth2ExtensionConstants.SUPPORTED_DOMAINS);
        if (invokerDomains != null) {
            CommonUtils.validateIfSupportedDomain(email, supportedDomain, invokerDomains);
        }
        CommonUtils.addSupportedDomainsToWorkspace(email, supportedDomainsForWorkspace,
                authenticationSettings);

        List<String> rolesList = List.of();
        if (!Objects.isNull(states.get("roles"))) {
            String roles = states.get("roles");
            String[] split = roles.split(",");
            rolesList = Arrays.asList(split);
        }
        Account account = getOrCreateAccount(email, allowAutoUserCreationBool,
                userAgent, rolesList, accountManager, roleManager);
        String sessionId = sessionManager.createSession(account.getAccountId());
        System.out.println("Session ID:" + sessionId);
        return createAuthenticationResponse(account, sessionId, runtimeContext, roleManager);
    }

    /**
     * Extracts the email address from the {@code id_token} contained in the OAuth2 response.
     *
     * @param authenticate OAuth2 access token containing an {@code id_token} claim
     * @return email address extracted from the JWT claims
     * @throws IllegalStateException if the JWT is invalid or does not contain an email claim
     */
    public String getEmailFromJwtToken(OAuth2AccessToken authenticate) {
        Validator.validateAuthenticatorObject(authenticate);
        String jwtToken =
                (String) ExtensionApiResource.GSON_JSON_MAPPER.fromString(authenticate.getRawResponse(), Map.class)
                        .get("id_token");
        String payloadJson = getJWTPayload(jwtToken);
        Map<?, ?> map = ExtensionApiResource.GSON_JSON_MAPPER.fromString(payloadJson, Map.class);
        //Google jwt case
        if (map.containsKey("email")) {
            return (String) map.get("email");
        }
        //Microsoft jwt case
        if (map.containsKey("unique_name")) {
            return (String) map.get("unique_name");
        }
        System.err.println("Email address not found from JWT claims. Payload :" + payloadJson);
        throw new IllegalStateException("Email address not found from JWT claims.");
    }

    /**
     * Determines whether automatic user creation is enabled for the given invoker.
     *
     * @param invoker current extension invoker
     * @return {@code true} if auto user creation is allowed; {@code false} otherwise
     */
    private boolean isAutoUserCreationAllowed(Invoker invoker) {
        Object allowAutoUserCreation =
                invoker.getAttributes().get(Oauth2ExtensionConstants.ALLOW_AUTO_PERSON_CREATION);
        return allowAutoUserCreation != null &&
                (Boolean.parseBoolean(allowAutoUserCreation.toString()));
    }

    /**
     * Looks up an existing account by email or creates a new one if allowed.
     *
     * @param email                    email address of the user
     * @param allowAutoUserCreationBool whether auto user creation is enabled
     * @param userAgent                user agent string of the client
     * @param rolesList                list of roles required for the user
     * @param accountManager           account manager used for lookups and creation
     * @param roleManager              role manager used to resolve and create roles
     * @return existing or newly created {@link Account}
     * @throws AuthorizationException if user creation is not allowed when required
     */
    private Account getOrCreateAccount(String email, boolean allowAutoUserCreationBool, String userAgent,
            List<String> rolesList, AccountManager accountManager, RoleManager roleManager)
            throws AuthorizationException {
        ModifiableAccount modifiableAccount = accountManager.lookupAccount(email);
        List<String> roleNames = ensureHasAllRoles(modifiableAccount, rolesList, roleManager);
        if (modifiableAccount == null) {
            if (!allowAutoUserCreationBool) {
                throw new AccessDeniedException();
            }
            modifiableAccount = accountManager.createAccount(email.substring(0, email.indexOf("@")), email,
                    new LinkedHashSet<>(roleNames),
                    Map.of("ORG", email.substring(email.indexOf("@") + 1), "KRISTA_SOURCE", "EXTENSION_OAUTH2",
                            "KRISTA_LAST_LOGIN",
                            CommonUtils.getDate(), "KRISTA_USER_AGENT_INFO",
                            userAgent == null ? "" : userAgent));
        }
        return modifiableAccount;
    }

    /**
     * Parses the raw state string and populates the state map with extracted values.
     *
     * @param states state map to populate
     * @param state  raw state string received from the identity provider
     * @throws IllegalArgumentException if required state values are missing
     */
    private void parseAndAddStateValues(Map<String, String> states, String state) {
        if (state != null) {
            if (state.contains("roles=") && state.contains("workspaceId")) {
                String roles = state.substring(state.indexOf("roles="), state.indexOf(",workspaceId"));
                parseAndAddRoles(states, roles);
            }
            if (state.contains("workspaceId")) {
                String stateValues = state.substring(state.indexOf("workspaceId"));
                parseAndAddStates(states, stateValues);
            }
            if (state.contains(Oauth2ExtensionConstants.X_KRISTA_ORIG_URI)) {
                String stateValues = state.substring(state.indexOf(Oauth2ExtensionConstants.X_KRISTA_ORIG_URI));
                parseAndAddStates(states, stateValues);
            }
        }
        List<String> missingRequiredStates = new ArrayList<>();
        for (String requiredStates : Set.of("workspaceId")) {
            if (!states.containsKey(requiredStates)) {
                missingRequiredStates.add(requiredStates);
            }
        }
        if (!missingRequiredStates.isEmpty()) {
            throw new IllegalArgumentException("Missing required states:" + missingRequiredStates);
        }
    }

    /**
     * Extracts role information from the raw state token and adds it to the state map.
     *
     * @param states state map to populate
     * @param roles  raw roles token from the state string
     */
    private void parseAndAddRoles(Map<String, String> states, String roles) {
        String[] pair = roles.split("=");
        if (pair.length == 2) {
            states.put(pair[0], pair[1]);
        }
    }

    /**
     * Parses comma-separated key/value pairs and adds them to the state map.
     *
     * @param states state map to populate
     * @param values comma-separated list of {@code key=value} tokens
     */
    private void parseAndAddStates(Map<String, String> states, String values) {
        String[] tokens = values.split(",");
        for (String token : tokens) {
            String[] pair = token.split("=");
            if (pair.length == 2) {
                states.put(pair[0], pair[1]);
            }
        }
    }

    /**
     * Creates an {@link AuthenticationResponse} object for the given account and session.
     *
     * @param account        authenticated account
     * @param sessionId      session identifier to include in the response
     * @param runtimeContext runtime context of the current request
     * @param roleManager    role manager used to determine workspace admin status
     * @return populated {@link AuthenticationResponse}
     * @throws AuthorizationException if role checks fail
     */
    private AuthenticationResponse createAuthenticationResponse(Account account, String sessionId,
            RuntimeContext runtimeContext, RoleManager roleManager)
            throws AuthorizationException {
        return new AuthenticationResponse(sessionId, account.getPerson().getPersonName(),
                account.getPerson().getAvatarUrl(), account.getAccountId(),
                runtimeContext.getKristaAccount().getAccountId(), account.getPerson().getPersonId(),
                account.getRoles().stream().map(Role::getName).collect(Collectors.toList()),
                account.getInboxId(), roleManager.isWorkspaceAdmin(account), false,
                Map.of("email", account.getPrimaryEmailAddress()),
                Map.of("creationTime", getCreationTimeString()));
    }

    /**
     * Ensures that all required roles exist in the workspace and are assigned to the account.
     *
     * @param modifiableAccount existing account, or {@code null} if the account does not yet exist
     * @param roles             list of required role names
     * @param roleManager       role manager used to resolve and create roles
     * @return list of role identifiers that should be associated with the account
     * @throws AuthorizationException if role creation or lookup fails
     */
    private List<String> ensureHasAllRoles(ModifiableAccount modifiableAccount, List<String> roles,
            RoleManager roleManager)
            throws AuthorizationException {
        List<String> allRoles = new ArrayList<>();
        Iterable<ModifiableRole> workspaceRoles = roleManager.getRoles();
        for (String accountRole : roles) {
            boolean roleExit = false;
            for (ModifiableRole workspaceRole : workspaceRoles) {
                if (Objects.equals(accountRole, workspaceRole.getName())) {
                    allRoles.add(workspaceRole.getRoleId());
                    roleExit = true;
                    break;
                }
            }
            if (!roleExit) {
                ModifiableRole role = roleManager.createRole(accountRole);
                allRoles.add(role.getRoleId());
            }
        }
        if (modifiableAccount != null) {
            for (Role role : modifiableAccount.getRoles()) {
                allRoles.add(role.getRoleId());
            }
            modifiableAccount.addRole(allRoles.toArray(String[]::new));
        }
        return allRoles;
    }

    /**
     * Extracts and decodes the payload portion of the supplied JWT.
     *
     * @param jwtToken raw JWT token string
     * @return decoded JWT payload as a JSON string
     * @throws IllegalArgumentException if the token format is invalid
     */
    private String getJWTPayload(String jwtToken) {
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String[] parts = jwtToken.split("\\.");
        if (parts.length < 2) {
            System.err.println("Invalid jwt token :" + jwtToken);
            throw new IllegalArgumentException("Invalid jwt token.");
        }
        return new String(decoder.decode(parts[1]));
    }

}
