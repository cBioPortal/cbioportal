# Import Study Using Docker

:warning: Every time you add/remove/overwrite a study please restart tomcat (or the Docker container), or 
call the `/api/cache` endpoint with a `DELETE` http-request (see [here](/deployment/customization/application.properties-Reference.md#evict-caches-with-the-apicache-endpoint) for more information).

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
