# Authenticating and Authorizing Users using Keycloak in Docker

**⚠️ This documentation for keycloak might be outdated, see related [ticket](https://github.com/cBioPortal/cbioportal/issues/10360) ⚠️**

This guide describes a way to Dockerise Keycloak along with cBioPortal, for authentication.

First, create an isolated network in which the Keycloak and MySQL servers can talk to one another.

```shell
docker network create kcnet
```

Run a MySQL database in which Keycloak can store its data. This database server will not be addressable from outside the Docker network. Replace `<path_to_database>` with the absolute path where the folder `kcdb-files` will be placed. This folder is used by the database to store its files.

```shell
docker run -d --restart=always \
    --name=kcdb \
    --net=kcnet \
    -v "<path_to_database>/kcdb-files:/var/lib/mysql" \
    -e MYSQL_DATABASE=keycloak \
    -e MYSQL_USER=keycloak \
    -e MYSQL_PASSWORD=password \
    -e MYSQL_ROOT_PASSWORD=root_password \
    mysql:5.7
```

Then run the actual Keycloak server, using [this image](https://quay.io/repository/keycloak/keycloak) available from Red Hat Image Registry Quay.io. This will by default connect to the database using the (non-root) credentials in the example above. The server will be accessible to the outside world on port 8180, so make sure to choose a strong administrator password.

The command below uses the default values for `MYSQL_DATABASE`, `MYSQL_USER` and `MYSQL_PASSWORD` (listed in the command above). If you wish to change these credentials, specify them in the command below. For instance, if `MYSQL_USER` in the database container is `user`, you need to add `-e MYSQL_USER=user`.

```
docker run -d --restart=always \
    --name=cbiokc \
    --net=kcnet \
    -p 8180:8080 \
    -e DB_VENDOR=mysql \
    -e DB_ADDR=kcdb \
    -e KEYCLOAK_USER=admin \
    -e "KEYCLOAK_PASSWORD=<admin_password_here>" \
    quay.io/keycloak/keycloak:16.1.1
```

Finally, configure Keycloak and cBioPortal as explained in the [Keycloak documentation](./../authorization-and-authentication/Authenticating-and-Authorizing-Users-via-keycloak.md). Remember to specify port 8180 for the Keycloak server, wherever the guide says 8080.

After configuring Keycloak, set up cBioPortal containers [as specified in the documentation](./). Make sure to update the `-Dauthenticate` in the [docker-compose file](https://github.com/cBioPortal/cbioportal-docker-compose/blob/5da068f0eb9b4f42db52ab5e91321b26a1826d7a/docker-compose.yml#L20) to `-Dauthenticate=saml`.
