# Tools for development

In this folder is some additional configuration that can be useful for local development. None of this should be deployed directly to production

# Set up keycloak for cBioPortal >v6

Requirements:
- System runs docker (including docker compose)

  1. Run from the root of the repository:

```
cd dev
docker compose up -d 
```

2. (Option 1) Apply SAML2 configuration to _security.properties_ in cBioPortal:

```properties
authenticate=saml
spring.security.saml2.relyingparty.registration.keycloak.assertingparty.metadata-uri=http://localhost:8084/realms/cbio/protocol/saml/descriptor
spring.security.saml2.relyingparty.registration.keycloak.assertingparty.entity-id=http://localhost:8084/realms/cbio
spring.security.saml2.relyingparty.registration.keycloak.entity-id=cbioportal
spring.security.saml2.relyingparty.registration.keycloak.signing.credentials[0].certificate-location=classpath:/dev/security/signing-cert.pem
spring.security.saml2.relyingparty.registration.keycloak.signing.credentials[0].private-key-location=classpath:/dev/security/signing-key.pem
```

3. (Option 2) Apply OIDC configuration to _security.properties_ in cBioPortal:

```properties
authenticate=oauth2
spring.security.oauth2.client.registration.keycloak.redirect-uri=http://localhost:8080/login/oauth2/code/keycloak
spring.security.oauth2.client.registration.keycloak.client-name=cbioportal_oauth2
spring.security.oauth2.client.registration.keycloak.client-id=cbioportal_oauth2
spring.security.oauth2.client.registration.keycloak.client-secret=client_secret
spring.security.oauth2.client.registration.keycloak.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.keycloak.client-authentication-method=client_secret_post
spring.security.oauth2.client.registration.keycloak.scope=openid,email,roles
spring.security.oauth2.client.provider.keycloak.issuer-uri=http://localhost:8084/realms/cbio
spring.security.oauth2.client.provider.keycloak.user-name-attribute=email
```

4. Set the following in _application.properties_:

```properties
persistence.cache_type=no-cache
session.service.url=http://localhost:5000/api/sessions/my_portal/

spring.datasource.url=jdbc:mysql://localhost:3306/cbioportal?useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=cbio_user
spring.datasource.password=somepassword
spring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
```

4. Start cBioPortal application on port 8080. The login credentials are `testuser:P@assword1`.

⚠️ Warning: Do not use this directly for production use as it takes several shortcuts to get a quick keycloak instance up.
