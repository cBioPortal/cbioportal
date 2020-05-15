# saml-oauth2-setup

Tests oauth2 security workflow of the API with SAML authentication method for the UI.

- `OfflineTokenDownloadIntegrationTests` tests downloading an offline token to gain access to cbioportal API.
- `Oauth2ResourceServerIntegrationTests` tests API access by different type of users (anonymous, non authorized and authorized).

## Start mysql server for integration tests

database name: cgds_test
username: cbio_user
password: somepassword
port: 3306  

## Start web container with cbioportal and saml IDP

You can start tomcat to play with the setup with the following command (replace with your values):

```$bash
cd portal/src/integration-tests/saml-oauth2-setup
CBIO_TEST_DB_USR=cbio_user \
    CBIO_TEST_DB_PSW=somepassword \
    CBIO_TEST_DB_HOST=127.0.0.1:3306 \
    CBIO_TEST_DB_NAME=cgds_test \
    CBIO_TEST_DB_CONNECTION_STRING=jdbc:mysql://127.0.0.1:3306/cgds_test?sessionVariables=default_storage_engine=InnoDB \
    CBIO_WAR_LOCATION=../../../target/cbioportal.war \
    mvn org.codehaus.cargo:cargo-maven2-plugin:run --non-recursive
```

## Run integration tests only

After starting the tomcat server above, you can run all tests with:

```$bash
cd portal/src/integration-tests/saml-oauth2-setup
mvn test failsafe:integration-test
```
