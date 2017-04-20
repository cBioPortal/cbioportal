# Uninstall Docker cBioPortal

## 1. Remove cBioPortal docker container

In a docker terminal type the following command:

```bash
docker rm -fv ${CONTAINER_NAME}
```

Where:    
- **${CONTAINER_NAME}**: The name of your container instance, _i.e **cbioportal**_.

## 2. Remove MySQL docker container

In a docker terminal type the following command:

```bash
docker rm -fv ${CONTAINER_NAME}
```

Where:    
- **${CONTAINER_NAME}**: The name of your container instance, _i.e **cbio-DB**_.

## 3. Remove the docker network

In a docker terminal type the following command:

```bash
docker network rm ${DOCKER_NETWORK_NAME}
```

Where:
- **${DOCKER_NETWORK_NAME}** is the name of the network that cBioPortal and the cBioPortal DB are accessible. _i.e **cbio-net**_