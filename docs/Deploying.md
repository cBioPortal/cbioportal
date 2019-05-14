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

```
java \
    -jar \
    portal/target/dependency/webapp-runner.jar \
    portal/target/cbioportal.war
```

As you can see the configuration defined in `portal.properties` can also be
passed as command line arguments. The priority of property loading is as
follows:

1. `-D` command line parameters overrides all
2. `${PORTAL_HOME}/portal.properties`
3. `portal.properties` supplied at compile time
4. Defaults defined in code

Note that some scripts require a `${PORTAL_HOME}/portal.properties` file, so
usually it is best to define the properties there.


## Verify the Web Application

Lastly, open a browser and go to:  
<http://localhost:8080>

## Important

- Each time you modify any java code, you must recompile and redeploy the app.
- Each time you modify any properties (see customization options), you must restart the app
- Each time you add new data, you must restart the app


[Next Step: Loading a Sample Study](Load-Sample-Cancer-Study.md)
