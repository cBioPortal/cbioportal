To learn more on Docker, kindly refer here: [What is Docker?](https://www.docker.com/what-docker).

### 1 | Install Docker

First, make sure that you have the latest version of Docker installed on your machine.
[Get latest Docker version](https://www.docker.com/products/docker)

### 2 | Database Setup

#### 2.1 | Setup a Docker MySQL instance

##### 2.1.1 | Setup a Docker Network

Because MySQL and cBioPortal are running on separate containers, Docker needs to know how to link them. Using Docker's legacy --link flag tends to be fragile since it will break if the MySQL container is restarted. We can get around this by using the newer ‘Docker networks’ feature.

```bash
docker network create cbio-net
```

##### 2.1.2 | Start a MySQL docker container:

In the following command, replace `{/PATH/TO/cbioportal-seed.sql.gz}` by the local filename of the seed database file `cbioportal-seed.sql.gz` on the host machine (you can [download the seed DB here](Downloads.md#seed-database)). This will automatically import it before starting the MySQL server if the database does not yet exist, which may take a while.

Kindly also replace any content between brackets {} with your own preferences.

```bash
docker run -d --name "{container_name}" \
	--restart=always \
	--net=cbio-net \
	-p 8306:3306 \
	-e MYSQL_ROOT_PASSWORD={root_password} \
	-e MYSQL_USER=cbio \
	-e MYSQL_PASSWORD={mysql_password} \
	-e MYSQL_DATABASE=cbioportal \
	-v {/PATH/TO/cbioportal-seed.sql.gz}:/docker-entrypoint-initdb.d/cbioportal-seed.sql.gz:ro \
	mysql
```

You can check the status of MySQL using the _Kitematic_ tool that comes with the Docker Toolbox. Or run
```bash
docker ps
```
to see if the container is running and
```bash
docker logs cbioDB
```
to see the MySQL status logs.

[MySQL Docker Hub](https://hub.docker.com/_/mysql/)
[MySQL Docker Github](https://github.com/docker-library/docs/tree/master/mysql)

#### 2.2 | Setup a MySQL instance running on a host

##### 2.2.1 | Install MySQL

To install MySQL kindly follow the installation instructions below:
[Installing and Upgrading MySQL](http://dev.mysql.com/doc/refman/5.7/en/installing.html)

##### 2.2.2 | Create cBioPortal MySQL Databases and User

Once MySQL is installed on host please follow the configuration instructions below:
[Create the cBioPortal MYSQL Databases and User](https://github.com/cBioPortal/cbioportal/blob/master/docs/Pre-Build-Steps.md#create-the-cbioportal-mysql-databases-and-user)

### 3 | Prepare Configuration files

Coming soon...

### Step 4 | Run docker container

```bash
docker run -d --name "cbioportal" \
    --restart=always \
    --net=cbio-net \
    -p 8080:8080 \
    -v {/custom_config/folder_path/}:/custom_config/ \
    -v {/customization/folder_path/}:/custom_files/ \
    -v {/logs/folder_path/}:/cbio_logs/ \
    -v {/studies/path/}:/cbio_studies/ \
    cbioportal/cbioportal
```

## Docker Container Maintenance

### Restart Docker Container

```bash
docker restart cbioportal
```
### Stop Docker Container

```bash
docker stop cbioportal
```

### Remove Docker Container (Make sure container is stopped first)

```bash
docker rm cbioportal
```