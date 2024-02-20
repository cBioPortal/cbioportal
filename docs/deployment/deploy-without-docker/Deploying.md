# Deploying the Web Application

## Before running cbioportal backend

You will need to update the src/main/resources/applications.properties to include your DB connection information.

For more information about the `application.properties` file, see the [reference](/deployment/customization/Customizing-your-instance-of-cBioPortal.md) page.

## Run the cbioportal backend

To run the backend execute the following commabd

```
java -jar target/cbioportal-exec.jar
```

There are three main ways to run the portal: without authentication, with optional login and with required login. All of them require the cBioPortal session service to be running.

### Without authentication

In this mode users are able to use the portal, but they won't be able to save their own virtual studies and groups.

```
java -jar target/cbioportal-exec.jar -Dauthenticate=false
```

### With authentication

To configure the authentication and authorization please consult the Authorization and Authentication Sections.
