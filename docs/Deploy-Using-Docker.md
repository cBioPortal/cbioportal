# Deploy Using Docker

:warning: Please make sure to review [Docker prerequisites](Docker-Prerequisites.md) before continuing with deployment.

## 1. Create a docker network

Because MySQL and cBioPortal are running on separate containers, docker needs to know how to link them. Using Docker's legacy `--link` flag tends to be fragile since it will break if the MySQL container is restarted. 

To get around this, we can use the newer `Docker networks` feature by typing the following command in a docker terminal:

#### Template

```bash
docker network create ${DOCKER_NETWORK_NAME}
```

Where:
- **${DOCKER_NETWORK_NAME}** is the name of the network that cBioPortal and the cBioPortal DB are going to be accessible. _i.e If the network is called **"cbio-net"** the command should be:_

#### Example

```bash
docker network create "cbio-net"
```

#### Useful Resources
- [Docker container networking](https://docs.docker.com/engine/userguide/networking/).
- [Docker network create](https://docs.docker.com/engine/reference/commandline/network_create/).

## 2. Database Setup

The cBioPortal software should run properly on MySQL version 5.7.x. Versions higher than 5.7.x can cause an issue
while loading the database schema. Minor versions lower than 5.7.x will cause issues with persistent cache invalidation.
The software can be found and downloaded from the [MySQL website](http://www.mysql.com/).

There are two options to set up the cBioPortal Database:    
2.1 Run MySQL on the host.    
2.2 Run MySQL as a Docker container.    

### 2.1 Run MySQL on the host

To install MySQL 5.7, kindly follow the vendor’s official detailed installation guide, available [here](http://dev.mysql.com/doc/refman/5.7/en/installing.html).

### 2.2 Run MySQL as a docker container

#### 2.2.1 Launch MySQL docker container

In a docker terminal type the following command:

##### Template

```bash
docker run -d --name ${CONTAINER_NAME} \
--restart=always \
--net=${DOCKER_NETWORK_NAME} \
-p ${PREFERRED_EXTERNAL_PORT}:3306 \
-e MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD} \
-e MYSQL_USER=${MYSQL_USER} \
-e MYSQL_PASSWORD=${MYSQL_PASSWORD} \
-e MYSQL_DATABASE=${MYSQL_DATABASE} \
-v ${PATH_TO_MYSQL_DATA}:/var/lib/mysql \
-v ${PATH_TO_SEED_DATABASE}/cgds.sql:/docker-entrypoint-initdb.d/cgds.sql:ro \
-v ${PATH_TO_SEED_DATABASE}/seed-cbioportal_no-pdb_hg19.sql.gz:/docker-entrypoint-initdb.d/seed_part1.sql.gz:ro \
-v ${PATH_TO_SEED_DATABASE}/seed-cbioportal_only-pdb.sql.gz:/docker-entrypoint-initdb.d/seed_part2.sql.gz:ro \
mysql
```

Where:    
- **${CONTAINER_NAME}**: The name of your container instance _i.e **cbio-DB**_.
- **${DOCKER_NETWORK-NAME}**: The name of your network _i.e **cbio-net**_.
- **${PREFERRED_EXTERNAL_PORT}**: The port that the container internal port will be mapped to _i.e **8306**_.
- **${MYSQL_ROOT_PASSWORD}**: The root password for the MySQL installation. For password restrictions please read carefully this [link](http://dev.mysql.com/doc/refman/5.7/en/user-names.html).
- **${MYSQL_USER}: The username for the cbioportal MySQL user _i.e **cbio**_.
- **${MYSQL_PASSWORD}**: The MySQL user password for the MySQL installation. For password restrictions please read carefully this [link](http://dev.mysql.com/doc/refman/5.7/en/user-names.html).
- **${MYSQL_DATABASE}**: The Database to create _i.e **cbioportal**_.
- **${PATH_TO_MYSQL_DATA}**: The MySQL path were all MySQL Data are stored.
- **${PATH_TO_SEED_DATABASE}**: Path to the seed databases.

Running the above command will create a MySQL docker container and will automatically import all Seed Databases.

:warning: This process can take about 45 minutes. For much faster
loading, we can choose to not load the PDB data, by removing the
line that loads `cbioportal-seed_only-pdb.sql.gz`. Please note that
your instance will be missing the 3D structure view feature (in the
mutations view) if you chose to leave this out.

#### 2.2.2 MySQL Logs monitoring in Docker

MySQL logs can easily be monitored by executing the following command on a terminal with docker.

##### Template

```bash
docker logs "{CONTAINER_NAME}"
```

Where:    
- **{CONTAINER_NAME}**: The name of your container instance _i.e **cbio-DB**_.

Learn more on [docker logs](https://docs.docker.com/engine/reference/commandline/logs/).

#### 2.2.3 Access mysql shell on docker container

To access the `mysql` shell on a docker container simply execute the following command:

##### Template

```bash
docker exec -it "{CONTAINER_NAME}" mysql -p"{MYSQL_ROOT_PASSWORD}"
```

Where:    
- **{CONTAINER_NAME}**: The name of your container instance _i.e **cbio-DB**_.
- **{MYSQL_ROOT_PASSWORD}**: The root password for the MySQL installation. For password restrictions please read carefully this [link](http://dev.mysql.com/doc/refman/5.7/en/user-names.html).

#### 2.2.4 Useful Resources
[MySQL Docker Hub](https://hub.docker.com/_/mysql/)    
[MySQL Docker Github](https://github.com/docker-library/docs/tree/master/mysql)

## 3. cBioPortal Setup

### 3.1 Run the cBioPortal docker container 

In a docker terminal type the following command:

##### Template

```bash
docker run -d --name ${CONTAINER_NAME} \
    --restart=always \
    --net=${DOCKER_NETWORK_NAME} \
    -p ${PREFERRED_EXTERNAL_PORT}:8080 \
    -v ${PATH_TO_portal.properties}:/cbioportal/src/main/resources/portal.properties:ro \
    -v ${PATH_TO_log4j.properties}:/cbioportal/src/main/resources/log4j.properties:ro \
    -v ${PATH_TO_settings.xml}:/root/.m2/settings.xml:ro \
    -v ${PATH_TO_context.xml}:/usr/local/tomcat/conf/context.xml:ro \
    -v ${PATH_TO_CBIOPORTAL_LOGS}:/cbioportal_logs/ \
    -v ${PATH_TO_TOMCAT_LOGS}:/usr/local/tomcat/logs/ \
    -v ${PATH_TO_STUDIES}:/cbioportal_studies/:ro \
    cbioportal/cbioportal:${TAG}
```

Where:    
- **${CONTAINER_NAME}**: The name of your container instance, _i.e **cbioportal**_.
- **${DOCKER_NETWORK_NAME}**: The name of your network, _i.e **cbio-net**_.
- **${PREFERRED_EXTERNAL_PORT}**: The port that the container internal port will be mapped to, _i.e **8306**_.
- **${PATH_TO_portal.properties}**: The external path were portal.properties are stored.
- **${PATH_TO_log4j.properties}**: The external path were log4j.properties are stored.
- **${PATH_TO_settings.xml}**: The external path were settings.xml is stored.
- **${PATH_TO_context.xml}**: The external path were context.xml is stored.
- **${PATH_TO_CUSTOMIZATION}**: The external path were customization files are stored.
- **${PATH_TO_CBIOPORTAL_LOGS}**: The external path where you want cBioPortal Logs to be stored.
- **${PATH_TO_TOMCAT_LOGS}**: The external path where you want Tomcat Logs to be stored.
- **${PATH_TO_STUDIES}**: The external path where cBioPortal studies are stored.
- **${TAG}**: The cBioPortal Version that you would like to run, _i.e **latest**_.

### 3.2 Run DB Migrations

Update the seeded database schema to match the cBioPortal version in the image.

:warning: Running the following docker command will most likely make your database irreversibly incompatible with older versions of the portal code.

##### Template

```bash
docker exec -it ${CONTAINER_NAME} \
migrate_db.py -p /cbioportal/src/main/resources/portal.properties -s /cbioportal/db-scripts/src/main/resources/migration.sql
```

Where:    
- **${CONTAINER_NAME}**: The name of your network, _i.e **cbioportal**_.