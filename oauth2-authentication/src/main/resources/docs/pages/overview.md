# Overview

The OAuth2 Authentication extension provides OAuth 2.0 authentication support for integration with other Krista extensions.

## Introduction

The OAuth 2.0 authorization framework enables a third-party application to obtain limited access to an HTTP service, either on behalf of a resource owner by orchestrating an approval interaction between the resource owner and the HTTP service, or by allowing the third-party application to obtain access on its own behalf.

## Supported Grant Types

| Grant Type | Description | Use Case |
|------------|-------------|----------|
| Authorization Code | User authorizes access through browser redirect | Web applications with server-side code |
| Client Credentials | Application authenticates using its own credentials | Server-to-server authentication |
| Implicit | Simplified flow for browser-based apps | Single-page applications (deprecated) |

## OAuth 2.0 Protocol Flow

The OAuth 2.0 protocol flow involves the following steps:

| Step | Action | Description |
|------|--------|-------------|
| A | Authorization Request | Client requests authorization from the resource owner |
| B | Authorization Grant | Client receives a credential representing the resource owner's authorization |
| C | Token Request | Client requests an access token by presenting the authorization grant |
| D | Access Token | Authorization server validates the grant and issues an access token |
| E | Resource Request | Client requests protected resource by presenting the access token |
| F | Protected Resource | Resource server validates the token and serves the request |

## Prerequisites

Before configuring this extension, ensure you have:

- Appropriate access and privileges to obtain the Client ID and Client Secret
- Authorization endpoint URL from your OAuth provider
- Token endpoint URL from your OAuth provider
- Required scopes for your application

## Related Topics

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Creating OAuth App](pages/CreatingOAuthApp.md)
- [Authentication](pages/Authentication.md)
- [API Requests](pages/APIRequests.md)