To learn more on Docker, kindly refer to the [Docker Introduction](Docker-Introduction.md) wiki page.

### Step 1 | Install Docker

First, make sure that you have the latest version of Docker installed on your machine.    
[Get latest Docker version](https://www.docker.com)

#### Notes for non-Linux systems ####

Because the Docker Engine daemon currently uses Linux-specific kernel features, you can’t run Docker Engine natively in Windows or OS X. Instead, you must use the Docker Machine command, `docker-machine`, to create and attach to a small Linux VM on your machine. This VM hosts Docker Engine for you on your system.

The Docker Quickstart Terminal in the Docker Toolbox will automatically create a default VM for you (`docker-machine create`), boot it up (`docker-machine start`) and set environment variables in the running shell to transparently forward the docker commands to the VM (`eval $(docker-machine env)`). Do note however, that forwarded ports in the docker commands will pertain to the VM and not your Windows/OS X system. The local cBioPortal and MySQL servers will not be available on `localhost` or `127.0.0.1`, but on the address printed by the command `docker-machine ip`, unless you configure VirtualBox to further forward the port to the host system.

### Step 2 | Database Setup

#### Option 1 | Setup a Docker MySQL instance

##### A. Setup a Docker Network

Because MySQL and cBioPortal are running on separate containers, Docker needs to know how to link them. Using Docker's legacy --link flag tends to be fragile since it will break if the MySQL container is restarted. We can get around this by using the newer ‘Docker networks’ feature.

```bash
docker network create cbio-net
```

##### B. Start a MySQL docker container:

```bash
docker run -d --name "cbioDB" \
	--restart=always \
	--net=cbio-net \
	-p 8306:3306 \
	-e MYSQL_ROOT_PASSWORD=P@ssword1 \
	-e MYSQL_USER=cbio \
	-e MYSQL_PASSWORD=P@ssword1 \
	-e MYSQL_DATABASE=cbioportal \
	-v /seed/DB/path:/cbioDB \
	mysql
```
:warning: change the /seed/DB/path to a local folder on the host machine which contains the seed database `cbioportal-seed.sql.gz` (you can [download the seed DB here](Downloads.md#seed-database)). 

:information_source: you can check the status of mysql using *Kitematic* tool that comes with Docker. Or run
```bash
docker ps
```
to see if the container is running and
```bash
docker logs cbioDB
```
to see the MySQL server logs.

##### C. Import Seed DB

Connect to the running docker container

```bash
docker exec -it cbioDB /bin/bash
```

Once inside the docker container run

```bash
gunzip /cbioDB/cbioportal-seed.sql.gz
mysql --user=cbio --password=P@ssword1 cbioportal  < /cbioDB/cbioportal-seed.sql
```

[MySQL Docker Hub] (https://hub.docker.com/_/mysql/)    
[MySQL Docker Github] (https://github.com/docker-library/docs/tree/master/mysql)

#### Option 2 | Setup a MySQL instance running on a host

Coming soon...

### Step 3 | Prepare Configuration files

Coming soon...

### Step 4 | Run docker container

Coming soon...

## Docker Container Maintenance

Coming soon...