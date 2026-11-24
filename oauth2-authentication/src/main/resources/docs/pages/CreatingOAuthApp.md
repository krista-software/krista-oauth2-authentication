# Creating an OAuth App

## Overview

This page provides instructions for obtaining OAuth 2.0 credentials (Client ID and Client Secret) from various providers.

## Prerequisites

Before configuring the OAuth2 Authentication extension, you must:

1. Have administrator access to the OAuth provider's developer console
2. Create an OAuth application/project in the provider's console
3. Obtain the **Client ID** and **Client Secret**
4. Configure the appropriate **redirect/callback URL**

---

## Step-by-Step Instructions

1. Review the **Prerequisites** and ensure you have administrator access to the provider console.
2. Follow the steps in the provider-specific sections below to create an OAuth application.
3. Configure the redirect/Callback URL to match the value shown in the extension configuration screen.
4. Copy the generated **Client ID** and **Client Secret**.
5. Return to the Krista extension configuration and paste these values into the corresponding fields.

## Provider-Specific Setup Guides

### Google OAuth 2.0

Set up OAuth 2.0 credentials for Google APIs:

[Google OAuth 2.0 Setup Guide](https://support.google.com/googleapi/answer/6158849?hl=en#zippy=%2Cuser-consent)

**Key Steps:**
1. Go to Google Cloud Console
2. Create or select a project
3. Navigate to APIs & Services > Credentials
4. Create OAuth 2.0 Client ID
5. Configure consent screen and scopes

---

### Microsoft Graph API OAuth 2.0

Set up OAuth 2.0 credentials for Microsoft Graph API:

[Microsoft Graph API OAuth 2.0 Setup Guide](https://www.manageengine.com/ad-recovery-manager/help/account-configuration/finding-client-id-and-secret.html)

**Key Steps:**
1. Go to Azure Portal
2. Navigate to App registrations
3. Register a new application
4. Configure API permissions
5. Create client secret

---

### Jira OAuth 2.0

Set up OAuth 2.0 credentials for Atlassian Jira:

[Jira OAuth 2.0 Setup Guide](https://developer.atlassian.com/cloud/jira/platform/oauth-2-3lo-apps/)

**Key Steps:**
1. Go to Atlassian Developer Console
2. Create a new OAuth 2.0 app
3. Configure scopes and callback URL
4. Obtain Client ID and Client Secret

---

### LinkedIn OAuth 2.0

Set up OAuth 2.0 credentials for LinkedIn:

[LinkedIn OAuth 2.0 Setup Guide](https://www.linkedin.com/pulse/how-get-signin-linkedin-work-taric-andrade/)

**Key Steps:**
1. Go to LinkedIn Developer Portal
2. Create a new application
3. Configure OAuth 2.0 settings
4. Obtain Client ID and Client Secret

---

## Screenshots

Screenshots are not included in this version of the documentation. Use the official provider guides linked above for the most up-to-date UI screenshots.

## Verification

After obtaining your OAuth credentials:

1. Enter the **Client ID** and **Client Secret** in the extension configuration.
2. Configure the appropriate **Authorization Base URL**, **Access Token End Point**, and (optionally) **Revoke Token Url**.
3. Verify that the **Callback Url** in the extension matches the redirect URI configured in the provider console.
4. Click the **Test Connection** button.

The **Test Connection** button validates all configuration parameters and generates an authorization URL. You can find the generated URL in the **Diagnostics** tab under the **Extension Logs** section.

## Next Steps

After your app is created and validated:

1. Complete the remaining fields in [Extension Configuration](pages/ExtensionConfiguration.md).
2. Decide whether to use service account or user account authentication as described in [Authentication](pages/Authentication.md).
3. Integrate the authentication flow into your solution using the endpoints documented in [API Requests](pages/APIRequests.md).

## Related Topics

- [Extension Configuration](pages/ExtensionConfiguration.md) - Configure OAuth 2.0 settings
- [Overview](pages/overview.md) - OAuth 2.0 protocol overview


