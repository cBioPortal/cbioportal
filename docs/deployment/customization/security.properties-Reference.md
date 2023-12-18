# Application Properties

The following are the properties for configuring authentication and authorization in the application.

## Authentication Configuration

### General Authentication Settings

```properties
# authentication (available options: [false, oauth2, optional_oauth2, saml])
authenticate=false
```
### OAUTH2
#### Google OAuth2 Client/Login Configuration

#### Example of utilizing google client for oAuth2 (Authentication)
```properties
spring.security.oauth2.client.registration.google.clientId=
spring.security.oauth2.client.registration.google.clientSecret=
```
#### Custom OAUTH2 Client Configuration

```properties
# For OIDC clients the issuer-uri is sufficient to autoconfigure the provider (via .well-known endpoint)
spring.security.oauth2.client.provider.cbio-idp.issuer-uri=http://localhost:8080/realms/cbioportal
spring.security.oauth2.client.provider.cbio-idp.user-name-attribute=email
# Required Scopes [openid, email, roles] 
spring.security.oauth2.client.registration.cbio-idp.scope=openid,email,roles
spring.security.oauth2.client.registration.cbio-idp.client-id=
spring.security.oauth2.client.registration.cbio-idp.client-secret=
```

##### Configuring the individual settings below is not recommended.
```properties
# spring.security.oauth2.client.provider.cbio-idp.authorization-uri=
# spring.security.oauth2.client.provider.cbio-idp.token-uri=
# TODO update docs, the user info endpoint must expose the roles !!
# spring.security.oauth2.client.provider.cbio-idp.user-info-uri=
# spring.security.oauth2.client.provider.cbio-idp.jwk-set-uri=
# spring.security.oauth2.client.provider.cbio-idp.logout-uri= # NOTE: this is not an official property.
# TODO Can be authorization_code, ...
#spring.security.oauth2.client.registration.cbio-idp.authorization-grant-type=
# TODO Can be client_secret_post, ...
#spring.security.oauth2.client.registration.cbio-idp.client-authentication-method=
#spring.security.oauth2.client.registration.cbio-idp.redirect-uri=<server-url>/login/oauth2/authorization/<client-registration-id (cbio-idp in this case)>
```

### SAML Configuration

```properties

# For SAML 2.0
## SAML settings
# TODO add options for auto- and manual config to docs
# TODO add to docs: metadata-uri can be both URL or metadata xml file
spring.security.saml2.relyingparty.registration.cbio-saml-idp.assertingparty.metadata-uri=classpath:/client-tailored-saml-idp-metadata.xml
#spring.security.saml2.relyingparty.registration.cbio-saml-idp.assertingparty.metadata-uri=http://localhost:8080/realms/cbioportal/protocol/saml/descriptor
spring.security.saml2.relyingparty.registration.cbio-saml-idp.entity-id=cbioportal-saml
spring.security.saml2.relyingparty.registration.cbio-saml-idp.signing.credentials[0].certificate-location=classpath:/local.crt
spring.security.saml2.relyingparty.registration.cbio-saml-idp.signing.credentials[0].private-key-location=classpath:/local.key
spring.security.saml2.relyingparty.registration.cbio-saml-idp.singlelogout.binding=POST
# TODO add to docs (in minutes; default 1)
spring.security.oauth2.allowed-clock-skew=
```
### Data Access Token Settings

```properties

## data access token settings
## Resource Server issuer-uri for Data Access Token
#spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/cbioportal
# TODO: Currently Dat.method has only been tested with oauth2
dat.unauth_users=
dat.method=oauth2
dat.ttl_seconds=2592000
dat.uuid.max_number_per_user=1
dat.jwt.secret_key=
dat.filter_user_role=

# OAuth2 token data access settings (If using OAuth2 for Login can copy setting here)
## TODO: Reuse OAUTH2 Spring settings defined above
dat.oauth2.clientId=<client-id>
dat.oauth2.clientSecret=<client-secret>
dat.oauth2.issuer=<token-issuer>
dat.oauth2.accessTokenUri=<authorization-server-url>/.../token
dat.oauth2.userAuthorizationUri=<authorization-server-url>/.../auth
dat.oauth2.jwkUrl=<authorization-server-url>/.../certs
dat.oauth2.redirectUri=<cbioportal-url>/.../api/data-access-token/oauth2
```
### Authorization Configuration/Study View Settings

```properties

## Authorization
## study view settings
## always show studies with this group
always_show_study_group=PUBLIC
## Should the permissions for groups and users be filtered by this instance's app.name?
## (true means the system only handles "CBIOPORTAL:someGroupPermission" groups, false means "someGroupPermission" works)
filter_groups_by_appname=false
# Can disable authorization
security.method_authorization_enabled=true
```