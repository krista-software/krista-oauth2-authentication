# OAuth2 Authentication Extension

## Overview

The OAuth2 Authentication Extension enables secure authentication for Krista applications using the OAuth 2.0 protocol. This extension supports multiple OAuth 2.0 flows including Authorization Code, Client Credentials, and Implicit grant types, allowing seamless integration with various identity providers and third-party services.

## Key Features

| Feature | Description |
|---------|-------------|
| **Multiple OAuth Flows** | Supports Authorization Code, Client Credentials, and Implicit grant types |
| **Secure Token Management** | Handles access token acquisition, refresh, and revocation |
| **Auto User Creation** | Optionally creates user accounts automatically upon first authentication |
| **Domain Restrictions** | Restrict authentication to specific email domains |
| **Flexible Configuration** | Customizable endpoints for authorization, token, and revocation URLs |

## Quick Start Guide

1. **Create OAuth Application** - Register your application with your OAuth provider to obtain Client ID and Client Secret. See [Creating OAuth App](pages/CreatingOAuthApp.md).

2. **Configure Extension** - Set up the extension with your OAuth credentials and endpoints. See [Extension Configuration](pages/ExtensionConfiguration.md).

3. **Enable Authentication** - Configure authentication settings for your Krista application. See [Authentication](pages/Authentication.md).

## Documentation Structure

| Document | Description |
|----------|-------------|
| [Overview](pages/overview.md) | Detailed overview of the OAuth2 Authentication Extension |
| [Creating OAuth App](pages/CreatingOAuthApp.md) | Guide to obtaining Client ID and Client Secret |
| [Extension Configuration](pages/ExtensionConfiguration.md) | Configuration settings and parameters |
| [Authentication](pages/Authentication.md) | Authentication flow and user management |
| [API Requests](pages/APIRequests.md) | Available API endpoints and requests |
| [Release Notes](pages/ReleaseNotes.md) | Version history and updates |

## Support & Resources

For additional assistance:
- Extension version: **3.5.7**
- Contact Krista Support for technical issues
- Check [Release Notes](pages/ReleaseNotes.md) for the latest updates and fixes