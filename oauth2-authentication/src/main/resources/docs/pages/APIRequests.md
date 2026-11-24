# API Requests

This extension exposes JAX-RS HTTP endpoints for OAuth 2.0 authentication operations.

All endpoints are served under the JAX-RS binding id **`authn`**. The full URL is:

`<Extension Base URL>/rest/authn/<endpoint>`

## Available Endpoints

| Endpoint Path | Method | Description |
|---------------|--------|-------------|
| `/type` | GET | Returns the authentication type supported by this extension |
| `/getauthurl` | GET | Generates an OAuth2 authorization URL and returns an HTTP 302 redirect |
| `/callback` | GET | Handles the OAuth2 callback, authenticates the user, and creates a Krista session |
| `/refreshtoken` | GET | Refreshes an access token using a refresh token |
| `/revoketoken` | GET | Revokes an OAuth2 token and optionally clears the Krista session |

---

## Get Authentication Type

Returns the authentication extension type. You can use this endpoint as a lightweight health check.

**Endpoint:** `GET <Extension Base URL>/rest/authn/type`

**Request:**
- No input payload required

**Successful Response:**
```
HTTP 200 OK
"OAuth2"
```

---

## Get Authorization URL

Generates an OAuth2 authorization URL based on the current extension configuration and returns an HTTP 302 redirect to the identity provider.

**Endpoint:** `GET <Extension Base URL>/rest/authn/getauthurl`

**Query Parameters:**

| Name | Required | Description |
|------|----------|-------------|
| `roles` | No | Comma-separated list of required roles for the authenticated user |
| `X-Krista-Original-URI` | No | Original URI requested by the client; used as the redirect target after authentication |

**Successful Response:**
```
HTTP 302 Found
Location: <provider-authorization-url>
```

If configuration or domain validation fails, the endpoint returns an error with message `Failed to get authorization url.`.

---

## Callback

Handles the OAuth2 callback from the identity provider, authenticates the user, creates a Krista client session, and redirects the browser back to the original URI.

**Endpoint:** `GET <Extension Base URL>/rest/authn/callback`

**Query Parameters:**

| Name | Required | Description |
|------|----------|-------------|
| `state` | Yes | Opaque state returned by the identity provider |
| `code` | Yes | Authorization code returned by the identity provider |

**Successful Response:**
```
HTTP 302 Found
Set-Cookie: X-Krista-Session-Id=<clientSessionId>; HttpOnly; path=/; [SameSite=None; Secure for HTTPS]
Location: <original-uri-from-state>
```

If the callback cannot be parsed or authentication fails, the endpoint returns an error with message `Failed to parse callback response.`.

---

## Refresh Token

Obtains a new access token from the identity provider using a valid refresh token.

**Endpoint:** `GET <Extension Base URL>/rest/authn/refreshtoken`

**Query Parameters:**

| Name | Required | Description |
|------|----------|-------------|
| `refreshToken` | Yes | Refresh token issued by the identity provider |

**Successful Response:**
```
HTTP 200 OK
<new-access-token>
```

If the refresh token is missing or the provider response is invalid, the endpoint returns an error with message `Failed to get refreshToken.`.

---

## Revoke Token

Revokes an OAuth2 token with the identity provider and optionally clears the Krista client session.

**Endpoint:** `GET <Extension Base URL>/rest/authn/revoketoken`

**Query Parameters:**

| Name | Required | Description |
|------|----------|-------------|
| `token` | Yes | Access or refresh token to revoke |
| `workspaceId` | No | Workspace identifier associated with the client session |
| `clientSessionId` | No | Krista client session identifier to invalidate after revocation |

**Successful Response:**
```
HTTP 200 OK
"Successfully revoked token."
```

If the token or revoke endpoint configuration is invalid, the endpoint returns an error with message `Failed to revoke token.`.

---

## Related Topics

- [Extension Configuration](pages/ExtensionConfiguration.md) - Configure OAuth 2.0 settings
- [Authentication](pages/Authentication.md) - Authentication modes
- [Overview](pages/overview.md) - OAuth 2.0 protocol overview

