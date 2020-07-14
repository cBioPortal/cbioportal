# Authenticate using Keycloak #

This guide describes a way to Dockerise Keycloak along with
cBioPortal, for authentication as described in
the
[cBioPortal documentation](https://docs.cbioportal.org/2.2-authorization-and-authentication/authenticating-and-authorizing-users-via-keycloak#introduction).

First, create an isolated network in which the Keycloak and MySQL
servers can talk to one another.

```shell
docker network create kcnet
```

Run a MySQL database in which Keycloak can store its data. This
database server will not be addressable from outside the Docker
network. Replace `<path_to_database>` with the absolute path where
the folder `kcdb-files` will be placed. This folder is used by the
database to store its files.

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

Then run the actual Keycloak server, using
[this image](https://hub.docker.com/r/jboss/keycloak/)
available from Docker Hub. This will by default connect to the
database using the (non-root) credentials in the example above. The
server will be accessible to the outside world on port 8180, so make
sure to choose a strong administrator password.

The command below uses the default values for `MYSQL_DATABASE`, `MYSQL_USER` and `MYSQL_PASSWORD` (listed in the command above). If you wish to change these credentials, specify them in the command below. For instance, if `MYSQL_USER` in the database container is `user`, you need to add `-e MYSQL_USER=user`.

```sh
docker run -d --restart=always \
    --name=cbiokc \
    --net=kcnet \
    -p 8180:8080 \
    -e DB_VENDOR=mysql \
    -e DB_ADDR=kcdb \
    -e KEYCLOAK_USER=admin \
    -e "KEYCLOAK_PASSWORD=<admin_password_here>" \
    jboss/keycloak:4.8.3.Final
```

Finally, configure Keycloak and cBioPortal as explained in the [Keycloak
documentation](../Authenticating-and-Authorizing-Users-via-keycloak.md#configure-keycloak-to-authenticate-your-cbioportal-instance).
Remember to specify port 8180 for the Keycloak server, wherever the guide says
8080.

After configuring Keycloak, set up cBioPortal containers [as specified in the documentation](README.md). Make sure to replace
the `JAVA_OPTS` argument in Step 6 to include the `-Dauthenticate=saml` flag:

<pre>
docker run -d --restart=always \
    --name=cbioportal-container \
    --net=cbio-net \
    -v /&lt;path_to_config_file&gt;/portal.properties:/cbioportal/portal.properties:ro \
    -e JAVA_OPTS='
        -Xms2g
        -Xmx4g
        <b>-Dauthenticate=saml</b>
        -Dsession.service.url=http://cbio-session-service:5000/api/sessions/my_portal/
    ' \
    -p 8081:8080 \
    cbioportal/cbioportal:3.4.2 \
    /bin/sh -c 'java ${JAVA_OPTS} -jar webapp-runner.jar /cbioportal-webapp'
</pre>
