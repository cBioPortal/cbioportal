# Import Study Using Docker

:warning: Please make sure to restart tomcat every time you add/remove/overwrite a study

## Adding a Study

In a docker terminal type the following command:

```bash
docker exec -it ${CBIOPORTAL_CONTAINER_NAME} \
metaImport.py -u http://${CBIOPORTAL_CONTAINER_NAME}:8080/cbioportal -s ${PATH_TO_STUDIES}
```

Where:    
- **${CBIOPORTAL_CONTAINER_NAME}**: The name of your cbioportal container instance, _i.e **cbioportal**_.
- **${PATH_TO_STUDIES}**: The external path where cBioPortal studies are stored.

## Restarting Tomcat

In a docker terminal type the following command:

```bash
docker exec -it ${CBIOPORTAL_CONTAINER_NAME} \
sh '${CATALINA_HOME}'/bin/shutdown.sh \
'&&' sh '${CATALINA_HOME}'/bin/startup.sh \
```

Where:    
- **${CBIOPORTAL_CONTAINER_NAME}**: The name of your cbioportal container instance, _i.e **cbioportal**_.
