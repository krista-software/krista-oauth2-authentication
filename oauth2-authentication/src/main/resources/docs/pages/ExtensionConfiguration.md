# Extension Configuration

## Overview

This page describes all configuration parameters required to set up the OAuth2 Authentication extension and how they are used during the OAuth 2.0 flow.

## Configuration Parameters

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| **Flow Type** | Text | Yes | The OAuth 2.0 flow type to use. Valid values: `authorizationCode`, `implicit`, `clientCredentials`. This must match one of the supported flow types in the extension. | `authorizationCode` |
| **Client Id** | Text | Yes | OAuth 2.0 client identifier obtained from the identity provider (for example, Google, Microsoft, Jira, LinkedIn). | `8b2e81cd-1e19-4ff6-8e44-2343ee4f5691` |
| **Client Secret** | Text (secured) | Yes | OAuth 2.0 client secret obtained from the identity provider. This value is stored securely and never logged. | `9964d2b0-fd84-4bcc-a8b3-1ada4487f3f5` |
| **Scope** | Text | Yes | Space-separated list of OAuth 2.0 scopes that define the level of access requested. The exact values depend on the provider documentation. | `openid profile email offline_access User.Read` |
| **Callback Url** | Text | Yes | Redirect URL where the identity provider sends the user after consent. You must configure the same URL in the provider console. | `https://<krista-extension-host>/extension/api/<extension-id>/authn/callback` |
| **Access Token End Point** | Text | Yes | URL used to exchange the authorization code for an access token. | `https://login.microsoftonline.com/common/oauth2/v2.0/token` |
| **Authorization Base URL** | Text | Yes | URL used to redirect users for consent/authorization. | `https://login.microsoftonline.com/common/oauth2/v2.0/authorize` |
| **Revoke Token Url** | Text | No | URL used to revoke access or refresh tokens when logging out. | `https://login.microsoftonline.com/common/oauth2/v2.0/logout` |
| **Allow Auto Person Creation** | Switch | No | When enabled, the extension automatically creates a Krista person account for new users during authentication if one does not already exist. | `true` or `false` |
| **Supported Domains** | Text | No | Comma-separated list of email domains allowed for authentication. If set, sign-in attempts from other domains are rejected. | `krista.ai, example.com` |
| **Access Token Url** | Text | No | Optional audience/resource URL used by some providers when requesting access tokens. | `https://graph.microsoft.com/.default` |

## Step-by-Step Setup

1. Create an OAuth application with your identity provider and obtain the **Client Id**, **Client Secret**, and endpoint URLs. See [Creating an OAuth App](pages/CreatingOAuthApp.md).
2. In Krista, install and open the **OAuth2 Authentication** extension configuration.
3. Enter all required fields from the **Configuration Parameters** table.
4. (Optional) Configure **Supported Domains** and **Allow Auto Person Creation** to control who can sign in and whether new accounts are created automatically.
5. Click **Test Connection** to validate the configuration before saving.

## Authentication Type Selection

The **OAuth2 Authentication** extension supports two authentication modes that are controlled from the conversation authentication settings:

- **Service Account Authentication** – Executes conversations using the service account configured during extension setup. Suitable for background or system-to-system operations.
- **User Account Authentication** – Requires each end user to sign in with their own account. Suitable when you need to audit or personalize actions per user.

For detailed behavior and UI screenshots, see [Authentication](pages/Authentication.md).

## Redirect URI Configuration

The **Callback Url** must exactly match the redirect URI configured in your identity provider:

- Use the value shown on the extension configuration screen for the current environment.
- Copy the entire URL (including `/extension/api/<extension-id>/authn/callback`) into the provider's redirect/redirect URI settings.
- If you have multiple environments (QA, staging, production), register the corresponding redirect URI for each environment as required by your provider.

If the redirect URI does not match, the provider will reject the sign-in attempt.

## Allow Retry Flag

This extension does not expose a separate **Allow Retry** flag in its configuration. Retry behavior is typically controlled by the Krista conversation that calls the extension and by the client application. When a request fails, you can:

- Re-run the conversation step.
- Re-initiate the OAuth flow using the same endpoints described in [API Requests](pages/APIRequests.md).

## Security Considerations

- Store the **Client Secret** only in the extension configuration UI; do not share it outside trusted administrators.
- Use the minimum required scopes for your use case (principle of least privilege).
- Restrict access using **Supported Domains** so that only users from allowed email domains can authenticate.
- Always use HTTPS endpoints for all OAuth URLs.
- Periodically rotate client secrets and review who has access to the provider console.

## Test Connection

Clicking the **Test Connection** button performs the following actions:

1. Validates all configuration parameters defined on the setup page using the current values (even on first-time configuration).
2. Generates the authorization URL for the configured OAuth 2.0 flow.
3. Logs the generated authorization URL in the **Diagnostics** tab under the **Extension Logs** section.

The configuration values are only persisted after you save the extension settings.

## Troubleshooting

- **"Invoker attributes are not found."** – This error applied to older extension versions when validating a configuration that had never been saved. From version **3.5.7** onward, validation uses the values on the screen, so upgrade to 3.5.7 or later.
- **"oAuth2 FlowType &lt;value&gt; not supported."** – The **Flow Type** value does not match one of the supported values: `authorizationCode`, `implicit`, or `clientCredentials`.
- **Domain validation failures** – Ensure **Supported Domains** and workspace authentication settings allow the user's email domain.

## OAuth 2.0 Provider Setup

For detailed instructions on obtaining OAuth 2.0 credentials from various providers, see [Creating an OAuth App](pages/CreatingOAuthApp.md).
