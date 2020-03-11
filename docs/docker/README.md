# Deploy with Docker

## Prerequisites

Docker provides a way to run applications securely isolated in a container, packaged with all its dependencies and libraries.
To learn more on Docker, kindly refer here: [Docker overview](https://docs.docker.com/engine/docker-overview/).

Make sure that you have the latest version of Docker installed on your machine. [Get latest version](https://www.docker.com/products/overview#/install_the_platform)

[Notes for non-Linux systems](notes-for-non-linux.md)

## Usage instructions ##

### Step 1 - Setup network ###
Create a network in order for the cBioPortal container and mysql database to communicate.
```
docker network create cbio-net
```

### Step 2 - Run mysql with seed database ###
Start a MySQL server. The command below stores the database in a folder named
`/<path_to_save_mysql_db>/db_files/`. This should be an absolute path.

Download the SQL schema (cgds.sql) and the seed database (seed-cbioportal_<genome_build>_<seed_version>.sql.gz) from the
[cBioPortal Datahub](https://github.com/cBioPortal/datahub/blob/master/seedDB/README.md),
and use the command below to upload both to the server started above.

Make sure to replace
`/<path_to_seed_database>/seed-cbioportal_<genome_build>_<seed_version>.sql.gz`
with the path and name of the downloaded seed database. Again, this should be
an absolute path.

```
docker run -d --restart=always \
  --name=cbioDB \
  --net=cbio-net \
  -e MYSQL_ROOT_PASSWORD='P@ssword1' \
  -e MYSQL_USER=cbio \
  -e MYSQL_PASSWORD='P@ssword1' \
  -e MYSQL_DATABASE=cbioportal \
  -v /<path_to_save_mysql_db>/db_files/:/var/lib/mysql/ \
  -v /<path_to_seed_database>/cgds.sql:/docker-entrypoint-initdb.d/cgds.sql:ro \
  -v /<path_to_seed_database>/seed-cbioportal_<genome_build>_<seed_version>.sql.gz:/docker-entrypoint-initdb.d/seed_part1.sql.gz:ro \
  mysql:5.7
```

Follow the logs of this step to ensure that no errors occur. If any error
occurs, make sure to check it. A common cause is pointing the `-v` parameters
above to folders or files that do not exist.

### Step 3 - Set up a portal.properties file ###

Copy the
[`portal.properties.EXAMPLE`](../../src/main/resources/portal.properties.EXAMPLE)
and change it according to your wishes. See the [full
reference](../portal.properties-Reference.md) and the [skin
properties](../Customizing-your-instance-of-cBioPortal.md) for more information on
the relevant properties.

Make sure to at least provide the database parameters from step 1, which are
required for the next step:

```
db.user=cbio
db.password=P@ssword1
db.host=cbioDB
db.portal_db_name=cbioportal
db.connection_string=jdbc:mysql://cbioDB/
```

If you are using an external database change the `cbioDB` hostname to the
hostname of the MySQL database. If it requires an SSL connection use:

```
db.use_ssl=true
```

If you would like to enable OncoKB see [OncoKB data access](../OncoKB-Data-Access.md) for 
how to obtain a data access token. After obtaining a valid token use:

```
show.oncokb=true
oncokb.token=<private_oncokb_access_token>
```

### Step 4 - Migrate database to latest version ###

Update the seeded database schema to match the cBioPortal version in the image,
by running the following command. Note that this will most likely make your
database irreversibly incompatible with older versions of the portal code.

```
docker run --rm -it --net cbio-net \
    -v /<path_to_config_file>/portal.properties:/cbioportal/portal.properties:ro \
    cbioportal/cbioportal:latest \
    migrate_db.py -p /cbioportal/portal.properties -s /cbioportal/db-scripts/src/main/resources/migration.sql
```

### Step 5 - Run Session Service containers
First, create the mongoDB database:

```
docker run -d --name=mongoDB --net=cbio-net \
    -e MONGO_INITDB_DATABASE=session_service \
    mongo:3.6.6
```

Finally, create a container for the Session Service, adding the link to the mongoDB database using `-Dspring.data.mongodb.uri`:

```
docker run -d --name=cbio-session-service --net=cbio-net \
    -e SERVER_PORT=5000 \
    -e JAVA_OPTS="-Dspring.data.mongodb.uri=mongodb://mongoDB:27017/session-service" \
    cbioportal/session-service:latest
```

### Step 6 - Run the cBioPortal web server ###

Add any cBioPortal configuration in `portal.properties` as appropriate—see
the documentation on the
[main properties](https://github.com/cBioPortal/cbioportal/blob/master/docs/portal.properties-Reference.md)
and the
[skin properties](https://github.com/cBioPortal/cbioportal/blob/master/docs/Customizing-your-instance-of-cBioPortal.md).
Then start the web server as follows.

```
docker run -d --restart=always \
    --name=cbioportal-container \
    --net=cbio-net \
    -v /<path_to_config_file>/portal.properties:/cbioportal/portal.properties:ro \
    -e JAVA_OPTS='
        -Xms2g
        -Xmx4g
        -Dauthenticate=noauthsessionservice
        -Dsession.service.url=http://cbio-session-service:5000/api/sessions/my_portal/
    ' \
    -p 8081:8080 \
    cbioportal/cbioportal:latest \
    /bin/sh -c 'java ${JAVA_OPTS} -jar webapp-runner.jar /cbioportal-webapp'
```

To read more about the various ways to use authentication and `webapp-runner`
see the relevant [backend deployment
documentation](../Deploying.md#run-the-cbioportal-backend).

On server systems that can easily spare 4 GiB or more of memory, set the `-Xms`
and `-Xmx` options to the same number. This should increase performance of
certain memory-intensive web services such as computing the data for the
co-expression tab. If you are using MacOS or Windows, make sure to take a look
at [these notes](notes-for-non-linux.md) to allocate more memory for the
virtual machine in which all Docker processes are running.

cBioPortal can now be reached at <http://localhost:8081/>

Activity of Docker containers can be seen with:

```
docker ps -a
```

## A note on versioning ##

For production you might want to deploy a specific docker image tag, instead of
the `latest` image. The version can be seen in the footer of the home page. The
various image versions can be found here:
https://hub.docker.com/r/cbioportal/cbioportal/tags. You can also get the
latest version programmatically like this:

```
LATEST_VERSION=$(curl --silent "https://api.github.com/repos/cBioPortal/cbioportal/releases/latest" \
    | grep "tag_name" \
    | cut -d'"' -f4 \
    | cut -dv -f2)
echo $LATEST_VERSION
```

## Data loading & more commands ##
For more uses of the cBioPortal image, see [this file](example_commands.md)

To Dockerize a Keycloak authentication service alongside cBioPortal,
see [this file](using-keycloak.md).

## Uninstalling cBioPortal ##
First we stop the Docker containers.
```
docker stop cbioDB
docker stop cbioportal-container
docker stop mongoDB
docker stop cbio-session-service
```

Then we remove the Docker containers.
```
docker rm cbioDB
docker rm cbioportal-container
docker rm mongoDB
docker rm cbio-session-service
```

Cached Docker images can be seen with:
```
docker images
```

Finally we remove the cached Docker images.
```
docker rmi mysql:5.7
docker rmi mongo:3.6.6
docker rmi cbioportal/cbioportal:latest
```
