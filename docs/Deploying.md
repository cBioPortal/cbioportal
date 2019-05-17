# Deploying the Web Application

## Prepare the global configuration file

The portal is configured using a global configuration file, `portal.properties`.
An example file is available in the `src/main/resources` folder.
Use it as a template to create your own:

    cd src/main/resources
    cp portal.properties.EXAMPLE $HOME/cbioportal/portal.properties

For more information about the `portal.properties` file, see the
[reference](portal.properties-Reference.md) page.

Several scripts of cBioPortal use this `portal.properties` file to get info
like db connection parameters. You can indicate the folder where this file is
with an environment variable:

```
export PORTAL_HOME=$HOME/cbioportal
```

if your properties file is at `PORTAL_HOME/portal.properties`

## Run the app
To run the app we use `webapp-runner`. It's a command line version of Tomcat
provided by [Heroku](https://github.com/jsimone/webapp-runner). All parameters
can be seen with:

```
java -jar portal/target/dependency/webapp-runner.jar --help
```

This runs the app in the foreground. If a port is already in use it will raise
an error mentioning that. To change the port use the `--port` flag.

There are three main ways to run the portal: without authentication, with
optional login and with required login.

### Without authentication
In this mode users are able to use the portal, but they won't be able to save
their own virtual studies and groups. See the [optional login
section](#optional-login) to enable this.
 
```bash
java \
    -jar \
    -Dauthenticate=noauthsessionservice \
    portal/target/dependency/webapp-runner.jar \
    portal/target/cbioportal.war
```

### Optional login

In this mode users can see all the data in the portal, but to save their own
groups and virtual studies they are required to log in. This will allow them to
store user data in the session service. See the
[tutorials](https://www.cbioportal.org/tutorials) section to read more about
these features.

```bash
java \
    -jar \
    -Dauthenticate=social_auth
    portal/target/dependency/webapp-runner.jar \
    portal/target/cbioportal.war
```

Only google is supported as optional login currently. One needs to set the
Google related configuration in the `portal.properties` file:

```
googleplus.consumer.key=
googleplus.consumer.secret=
```

See [Google's Sign in
Documentation](https://developers.google.com/identity/sign-in/web/sign-in#before_you_begin)
to obtain these values.

### Required login

```bash
java \
    -Dauthenticate=CHOOSE_DESIRED_AUTHENTICATION_METHOD \
    -jar \
    portal/target/dependency/webapp-runner.jar \
    portal/target/cbioportal.war
```

Change `CHOOSE_DESIRED_AUTHENTICATION_METHOD` to one of `googleplus`,
`social_auth`, `saml`, `openid`, `ad`, `ldap`. The various methods of
authentication are described in the [Authorization and
Authentication](https://docs.cbioportal.org/#2-2-authorization-and-authentication)
section.

### Property configuration
The configuration defined in `portal.properties` can also be
passed as command line arguments. The priority of property loading is as
follows:

1. `-D` command line parameters overrides all
2. `${PORTAL_HOME}/portal.properties`
3. `portal.properties` supplied at compile time
4. Defaults defined in code

Note that the `authenticate` property is currently required to be set as a
command line argument, it won't work when set in `portal.properties` (See issue
[#6109](https://github.com/cBioPortal/cbioportal/issues/6109)).

Some scripts require a `${PORTAL_HOME}/portal.properties` file, so it is best
to define the properties there.

## Verify the Web Application

Lastly, open a browser and go to:  
<http://localhost:8080>

## Important

- Each time you modify any java code, you must recompile and redeploy the app.
- Each time you modify any properties (see customization options), you must restart the app
- Each time you add new data, you must restart the app


[Next Step: Loading a Sample Study](Load-Sample-Cancer-Study.md)
