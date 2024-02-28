# Deploying the Web Application

## Before running cbioportal backend

You will need to update the src/main/resources/applications.properties to include your DB connection information.

The configuration defined in `application.properties` can also be passed as command line arguments. The priority of property loading is as follows:

1. `-D` command line parameters overrides all
2. `src/main/resources/application.properties`
3. `application.properties` supplied at compile time
4. Defaults defined in code

Note that the `authenticate` property is currently required to be set as a command line argument, it won't work when set in `application.properties` (See issue [#6109](https://github.com/cBioPortal/cbioportal/issues/6109)).

Some scripts require a `${PORTAL_HOME}/application.properties` file, so it is best to define the properties there.

For more information about the `application.properties` file, see the [reference](/deployment/customization/Customizing-your-instance-of-cBioPortal.md) page.


## Run the cbioportal backend

To run the backend execute the following commabd

```
java -jar target/cbioportal-exec.jar
```

This runs the app in the foreground. If a port is already in use it will raise an error mentioning that. To change the port use the `--server.port` flag.



There are three main ways to run the portal: without authentication, with optional login, and with required login. All of them require the cBioPortal session service to be running.

### Without authentication

In this mode users are able to use the portal, but they won't be able to save their own virtual studies and groups.

```
java -jar target/cbioportal-exec.jar -Dauthenticate=false
```

### With authentication

To configure the authentication and authorization please consult the [Authorization](./../authorization-and-authentication/User-Authorization.md) and [Authentication](./../authorization-and-authentication/Authenticating-and-Authorizing-Users-via-keycloak.md) Sections.
