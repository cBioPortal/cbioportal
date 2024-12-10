# Keycloak Management via API Access and User Creation

**⚠️ This documentation is for keycloak <v20, see related [ticket](https://github.com/cBioPortal/cbioportal/issues/10360) ⚠️**

## Introduction
You may wish to programmatically manage aspects of your Keycloak setup via the Keycloak API. 
This is particularly useful for tasks such as:
1. Bulk User Creation
2. Modifying group membership
3. Assigning roles to many users

The following instructions will show you how to configure a Keycloak Client Service Account and assign appropriate permissions required for the management task.

> [!NOTE]
> Important URLS
> https://\<KEYCLOAK_HOST\>/auth/admin/master/console/#/realms/\<REALM\>/clients
> https://\<KEYCLOAK_HOST\>/auth/realms/\<REALM\>/protocol/openid-connect/token

## Configure a Keycloak Client
Navigate to: <Realm> -> Clients -> Select Client: `realm-management` -> Settings tab

We’re using the `realm-management` client here but you can configure any other client. Make sure the following options are set.

![](/images/previews/keycloak-api-access-settings.png)

| parameter        | value  | comment  |
| ------------- |:-------------:| -----:|
| Enabled | true ||
| Client Protocol | openid-connect | (default value) |
| Access Type | confidential | This will allow us to make a call to the token service endpoint and follow an openid login flow. |
| Valid Redirect URIs | _url_ | _url_ refers to base url of keycloak instance. Access Type must be set to confidential for this option to show |
| Service Accounts Enabled | true | (default value). Access Type must be set to confidential for this option to show |

> [!NOTE]
> 1. The redirect url and service accounts enabled options will not appear on the UI until the Access Type -> confidential
> 2. The Service Account Roles TAB will not show until Service Accounts Enabled: True

> [!TIP]
> 1. Configure ONE client per application/script (set of scripts) that will make calls against the Keycloak API. That way you can manage/revoke permissions and also regenerate the client_secret if needed. 

## Obtain Client Credentials
Navigate to: <Realm> -> Clients -> Select Client: `realm-management` -> Credentials tab

1. Select `Client Id and Secret`
2. Click `Regenerate Secret` to generate a secret
3. Keep `ClientId` and `Secret` for obtaining an access token from keycloak

![](/images/previews/keycloak-api-access-credentials.png)

| parameter        | value  | comment  |
| ------------- |:-------------:| -----:|
| Client Authenticator | Client Id and Secret |  |

## Configure Service Account Roles
Navigate to: <Realm> -> Clients -> Select Client: `realm-management` -> `Service Account Roles` Tab

Under `Client Roles` -> Select the `realm-management` from the dropdown menu

From here scroll through the available roles for the `view-users` roles. Click `Add selected >>` 
Assign additional roles if needed.

![](/images/previews/keycloak-api-access-service-account.png)

> [!NOTE]
> 1. For managing users, we want to assign “manage-users” and “view-users” roles to “realm-management”
> 2. Only add the permissions you require for the tasks that will be performed.


## Make API calls to the Keycloak 12 REST API
See Keycloak REST-API [documentation](https://www.keycloak.org/docs-api/latest/rest-api/index.html)

Provide `client_id`, `client_secret`, `grant_type=”client_credentials”` as `x-www-form-urlencoded`

1. Make a call to the token service to obtain an access token
```bash
# Obtain an access token
curl -X POST https://<KEYCLOAK_HOST>/auth/realms/<REALM>/protocol/openid-connect/token \
    -H 'Content-type: application/x-www-form-urlencoded' \
    -d "client_id=$(KC_CLIENT_ID)" \
    -d "client_secret=$(KC_CLIENT_SECRET)" \
    -d "grant_type=$(KC_GRANT_TYPE)" | jq '.access_token'
```

2. Send the token which each request
```bash
# Get keycloak users
curl -X GET https://<KEYCLOAK_HOST>/auth/admin/realms/<REALM>/users \
    -H "Authorization: Bearer ${ACCESS_TOKEN}" \
    -H 'cache-control: no-cache'
```


> [!NOTE]
> 1. The access token by default only has a life of 300s (5min). This can be adjusted under the Settings Tab -> Advanced Settings Access Token Lifespan.
> 2. These calls were made against Keycloak version 12 so they must include <KEYCLOAK_HOST>`/auth/`