# Deploy with Docker

## Prerequisites

Docker provides a way to run applications securely isolated in a container, packaged with all its dependencies and libraries.
To learn more on Docker, kindly refer here: [What is Docker?](https://www.docker.com/what-docker).

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

```
docker run -d --restart=always \
  --name=cbioDB \
  --net=cbio-net \
  -e MYSQL_ROOT_PASSWORD='P@ssword1' \
  -e MYSQL_USER=cbio \
  -e MYSQL_PASSWORD='P@ssword1' \
  -e MYSQL_DATABASE=cbioportal \
  -v /<path_to_save_mysql_db>/db_files/:/var/lib/mysql/ \
  mysql:5.7
```

Download the seed database from the
[cBioPortal Datahub](https://github.com/cBioPortal/datahub/blob/master/seedDB/README.md),
and use the command below to upload the seed data to the server started above.

Make sure to replace
`/<path_to_seed_database>/seed-cbioportal_<genome_build>_<seed_version>.sql.gz`
with the path and name of the downloaded seed database. Again, this should be
an absolute path.

```
docker run \
  --name=load-seeddb \
  --net=cbio-net \
  -e MYSQL_USER=cbio \
  -e MYSQL_PASSWORD='P@ssword1' \
  -v /<path_to_seed_database>/cgds.sql:/mnt/cgds.sql:ro \
  -v /<path_to_seed_database>/seed-cbioportal_<genome_build>_<seed_version>.sql.gz:/mnt/seed.sql.gz:ro \
  mysql:5.7 \
  sh -c 'cat /mnt/cgds.sql | mysql -hcbioDB -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" cbioportal \
      && zcat /mnt/seed.sql.gz |  mysql -hcbioDB -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" cbioportal'
```

Follow the logs of this step to ensure that no errors occur. If any error
occurs, make sure to check it. A common cause is pointing the `-v` parameters
above to folders or files that do not exist.

Note that another option would be to use an external database. In that case one
does not need to run the `cbioDB` container. In the command for the
`load-seeddb` change the cbioDB host to the host of the external MySQL
database.

### Step 3 - Set up a portal.properties file ###

Copy the
[`portal.properties.EXAMPLE`](../../src/main/resources/portal.properties.EXAMPLE)
and change it according to your wishes. See the [full
reference](portal.properties-Reference.md) and the [skin
properties](Customizing-your-instance-of-cBioPortal.md) for more information on
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

### Step 4 - Migrate database to latest version ###

Update the seeded database schema to match the cBioPortal version
in the image, by running the following command. Note that this will
most likely make your database irreversibly incompatible with older
versions of the portal code.

```
docker run --rm -it --net cbio-net \
    -v /<path_to_config_file>/portal.properties:/cbioportal/portal.properties:ro \
    cbioportal/cbioportal:3.0.1 \
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
    cbioportal/cbioportal:3.0.1 \
    /bin/sh -c 'java ${JAVA_OPTS} -jar webapp-runner.jar /app.war'
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

## Data loading & more commands ##
For more uses of the cBioPortal image, see [this file](example_commands.md)

To Dockerize a Keycloak authentication service alongside cBioPortal,
see [this file](using-keycloak.md).

## Uninstalling cBioPortal ##
First we stop the Docker containers.
```
docker stop cbioDB
docker stop cbioportal-container
```

Then we remove the Docker containers.
```
docker rm cbioDB
docker rm cbioportal-container
```

Cached Docker images can be seen with:
```
docker images
```

Finally we remove the cached Docker images.
```
docker rmi mysql:5.7
docker rmi mongo:3.6.6
```
