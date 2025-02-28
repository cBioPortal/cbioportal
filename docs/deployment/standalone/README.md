# Deploy Standalone Isolated Version

## Overview
In some cases a cBioPortal instance needs to be completely isolated from connecting to outside resources. While cBioPortal will run in an isolated environment some features will not be available. In this documentation we cover how to setup and deploy a docker version of cBioPortal that is isolated from all outside services.


## Setup Instructions

### Docker Configuration and Setup
Our docker configuration is based off the default cBioPortal docker configuration however we have made some changes to support our requirements. We create two networks in this deployment. The first being a bridge network that allows the services to connect to the outside world. The second is an internal network that only allows for services to talk to other services in that network. Most of the services are configured to use the internal network, with the exception being the NGINX and cBioPortal services. The NGINX needs to be able to communicate with the outside world in order to make the cBioPortal instance available to the outside world. The cBioPortal instance needs to be able to communicate with the outside world in order to allow for authentication with an outside service. However, if the cBioPortal instance does not authenticate then the service can be deployed only on the internal network.


```
services:
  nginx-wrapper:
    restart: unless-stopped
    image: nginx:1.27.1
    container_name: cbioportal-nginx-wrapper-container
    ports: #NGINX_WRAPPER
    - 80:80
    - 443:443
    expose:
      - "8777"
    volumes:
    - ./config/nginx.conf:/etc/nginx/conf.d/default.conf
    - ./cert/cert.cer:/etc/nginx/cert.cer
    - ./cert/cert.key:/etc/nginx/cert.key
    - ./gn-static:/gn-static
    depends_on:
    - cbioportal
    networks:
    - cbio-bridge
    - cbio-internal
  cbioportal:
    restart: unless-stopped
    image: cbioportal/cbioportal:6.0.12
    container_name: cbioportal-container
    cap_add:
      - NET_ADMIN
      - NET_RAW
    environment:
      SHOW_DEBUG_INFO: "true"
      PORTAL_HOME: "/cbioportal-webapp"
    extra_hosts:
      - "civicdb.org:127.0.0.1"
      - "mutationassessor.org:127.0.0.1"
      - "cancerhotspots.org:127.0.0.1"
      - "genomenexus.org:127.0.0.1"
    volumes:
     - ./config/cbioportal_ipblock.sh:/cbioportal_ipblock.sh
     - ./config/application.properties:/cbioportal-webapp/application.properties:ro
    depends_on:
     - cbioportal-database
     - cbioportal-session
    networks:
     - cbio-internal
     - cbio-bridge
    command: /bin/sh -c "/cbioportal_ipblock.sh && rm -rf /cbioportal-webapp/lib/servlet-api-2.5.jar && java -Xms2g -Xmx4g -cp '/cbioportal-webapp:/cbioportal-webapp/lib/*' org.cbioportal.PortalApplication --spring.config.location=/cbioportal-webapp/application.properties --session.service.url=http://cbioportal-session:5000/api/sessions/portal/"
  cbioportal-database:
    restart: unless-stopped
    image: mysql:5.7
    container_name: cbioportal-database-container
    environment:
      MYSQL_DATABASE: cbioportal
      MYSQL_USER: cbio_user
      MYSQL_PASSWORD: somepassword
      MYSQL_ROOT_PASSWORD: somepassword
    volumes:
     - ./data/cgds.sql:/docker-entrypoint-initdb.d/cgds.sql:ro
     - ./data/seed.sql.gz:/docker-entrypoint-initdb.d/seed.sql.gz:ro
     - cbioportal_mysql_data:/var/lib/mysql
    networks:
     - cbio-internal
  cbioportal-session:
    restart: unless-stopped
    image: cbioportal/session-service:0.6.1
    container_name: cbioportal-session-container
    environment:
      SERVER_PORT: 5000
      JAVA_OPTS: -Dspring.data.mongodb.uri=mongodb://cbioportal-session-database:27017/session-service
    depends_on:
      - cbioportal-session-database
    networks:
      - cbio-internal
  cbioportal-session-database:
    restart: unless-stopped
    image: mongo:4.2
    container_name: cbioportal-session-database-container
    environment:
      MONGO_INITDB_DATABASE: session_service
    volumes:
      - ../cbioportal_mongo_data:/data/db
    networks:
      - cbio-internal
  oncokb:
    image: oncokb/oncokb:3.20.3
    ports: #TODO: CHANGE TO EXPOSE
      - "8080:8080"
    environment:
      JAVA_OPTS: >
        -Djdbc.driverClassName=com.mysql.jdbc.Driver
        -Djdbc.url=jdbc:mysql://oncokb-mysql:3306/oncokb?useUnicode=yes&characterEncoding=UTF-8&useSSL=false
        -Djdbc.username=oncokb_user
        -Djdbc.password=somepassword
        -Doncokb_transcript.url=http://oncokb-transcript:9090-Doncokb_transcript.token=<TOKEN>
        -Dgenome_nexus.grch37.url=http://gn-spring-boot:8888
        -Dgenome_nexus.grch38.url=http://gn-spring-boot-grch38:8888
        -Dis_public_instance=false
        -Dcancerhotspots.website.link=http://cancerhotspots:8888
    depends_on:
      - "oncokb-transcript"
    networks:
      - cbio-internal
    volumes:
     - ./config/oncokb.config:/src/main/resources/properties/config.properties:ro
  oncokb-mysql:
    restart: unless-stopped
    image: mysql:5.7.28
    environment:
      MYSQL_DATABASE: oncokb
      MYSQL_USER: oncokb_user
      MYSQL_PASSWORD: somepassword
      MYSQL_ROOT_PASSWORD: somepassword
    volumes:
         - ../oncokb:/var/lib/mysql
    networks:
      - cbio-internal
  oncokb-transcript:
    image: oncokb/oncokb-transcript:0.9.4
    ports: #TODO: CHANGE TO EXPOSE
      - "9090:9090"
    environment:
      - SPRING_PROFILES_ACTIVE=prod,api-docs,no-liquibase
      - APPLICATION_REDIS_ENABLED=false
      - SPRING_DATASOURCE_URL=jdbc:mysql://oncokb-transcript-mysql:3306/oncokb_transcript?useUnicode=yes&characterEncoding=UTF-8&useSSL=false
      - SPRING_DATASOURCE_USERNAME=oncokb_user
      - SPRING_DATASOURCE_PASSWORD=somepassword
    depends_on:
      - "oncokb-transcript-mysql"
    networks:
      - cbio-internal
  oncokb-transcript-mysql:
    restart: unless-stopped
    image: mysql:5.7.28
    environment:
      MYSQL_DATABASE: oncokb_transcript
      MYSQL_USER: oncokb_user
      MYSQL_PASSWORD: somepassword
      MYSQL_ROOT_PASSWORD: somepassword
    volumes:
         - ../oncokb_transcript:/var/lib/mysql
    networks:
      - cbio-internal
  cancerhotspots:
    image: ksg/cancerhotspots
    ports: #TODO: CHANGE TO EXPOSE
      - "8888:28080"
    networks:
      - cbio-internal
  gn-spring-boot:
    image: genomenexus/gn-spring-boot:1.4.1
    ports: #TODO: CHANGE TO EXPOSE
      - "8888:8888"
    cap_add:
      - NET_ADMIN
      - NET_RAW
    environment:
      - SERVER_PORT=8888
    command: > 
      java 
      -Dspring.data.mongodb.uri=mongodb://gn-mongo:27017/annotator 
      -Dgn_vep.region.url=http://gn-vep:8080/vep/human/region/VARIANT 
      -Drevue.url=http://nginx-wrapper:8777/VUEs.json
      -Dgenexrefs.url=http://127.0.0.1/genexrefs
      -Dmyvariantinfo.url=http://127.0.0.1/myvariant
      -Dpdb.header_service_url=http://127.0.0.1/pdb
      -Doncokb.url=http://oncokb/api/v1/annotate/mutations/byProteinChange?PROTEINCHANGE
      -jar /app.war
    links:
      - gn-mongo
    depends_on:
      - gn-mongo
      - gn-vep
    networks:
      - cbio-internal
  gn-mongo:
    image: genomenexus/gn-mongo:0.31
    restart: always
    environment:
      - REF_ENSEMBL_VERSION=grch37_ensembl98
      - SPECIES=homo_sapiens
    networks:
      - cbio-internal
  gn-vep:
    image: genomenexus/genome-nexus-vep:v0.0.1
    environment:
      - VEP_ASSEMBLY=GRCh37
      - VEP_FASTAFILERELATIVEPATH=homo_sapiens/98_GRCh37/Homo_sapiens.GRCh37.75.dna.primary_assembly.fa.gz
    user: root
    restart: always
    ports: #TODO: CHANGE TO EXPOSE
      - "6060:8080"
    volumes:
      - ../gn-vep-data/98_GRCh37:/opt/vep/.vep
      - ../gn-vep-data/98_GRCh37:/root/.vep/
    networks:
      - cbio-internal
  gn-spring-boot-grch38:
    image: genomenexus/gn-spring-boot:v1.2.2
    ports: #TODO: CHANGE TO EXPOSE
      - "8889:8888"
    environment:
      - SERVER_PORT=8888
    command: >
      java 
      -Dspring.data.mongodb.uri=mongodb://gn-mongo-grch38:27017/annotator
      -Dgn_vep.region.url=http://gn-vep-grch38:8080/vep/human/region/VARIANT
      -Drevue.url=http://nginx-wrapper:8777/VUEs.json
      -Dgenexrefs.url=http://127.0.0.1/genexrefs
      -Dmyvariantinfo.url=http://127.0.0.1/myvariant
      -Dpdb.header_service_url=http://127.0.0.1/pdb
      -jar 
      /app.war
    links:
      - gn-mongo-grch38
    depends_on:
      - gn-mongo-grch38
      - gn-vep-grch38
    networks:
      - cbio-internal
  gn-mongo-grch38:
    image: genomenexus/gn-mongo:v0.24_grch38_ensembl95
    restart: always
    networks:
       - cbio-internal
  gn-vep-grch38:
    image: genomenexus/genome-nexus-vep:v0.0.1
    environment:
      - VEP_ASSEMBLY=GRCh38
      - VEP_FASTAFILERELATIVEPATH=homo_sapiens/98_GRCh38/Homo_sapiens.GRCh38.dna.toplevel.fa.gz
    user: root
    restart: always
    ports: #TODO: CHANGE TO EXPOSE
      - "6061:8080"
    volumes:
      - ../gn-vep-data/98_GRCh38/:/opt/vep/.vep/
      - ../gn-vep-data/98_GRCh38:/root/.vep/
    networks:
      - cbio-internal

networks:
  cbio-internal:
    internal: true
    ipam:
      config:
      - subnet: 192.187.1.0/24
  cbio-bridge:
    driver: bridge
    ipam:
      driver: default
      config:
      - subnet: 192.187.0.0/24
```

### NGINX Configuration and Setup
The NGINX configuration below supports HTTPS, a static landing page, cBioPortal being hosted as a subdirectory. You will need to provide your own certificates. You will note in the configuration that we host the static site in the `/gn-static` directory. This directory will also need to contain an index file as well as any supporting resources. The `/gn-static` is also used by a site the connects to a server running on port 8777. This hosts the `VUEs.json` file used by Genome Nexus and the port is only available to services inside the internal network. We also have a location lookup for `/reactapp` that handles cBioPortal requests that do not support running cBioPortal in a subdirectory.


```
server {
  listen 80;
  
  return 301 https://$host$request_uri;
}

server {

  listen                    443 ssl;

  ssl_certificate           /etc/nginx/cert.cer;
  ssl_certificate_key       /etc/nginx/cert.key;

  ssl_session_cache         builtin:1000  shared:SSL:10m;
  ssl_protocols             TLSv1 TLSv1.1 TLSv1.2;
  ssl_ciphers               HIGH:!aNULL:!eNULL:!EXPORT:!CAMELLIA:!DES:!MD5:!PSK:!RC4;
  ssl_prefer_server_ciphers on;

  access_log                /var/log/nginx/cbioportal_login.access.log;

  client_max_body_size 1024g;
  client_body_buffer_size 1024m;

  proxy_http_version 1.1;
  chunked_transfer_encoding off;

  location / {
      root /gn-static;
  }
  
  location /cbioportal {
    proxy_pass          http://cbioportal:8443;
    proxy_set_header    Host $host;
    proxy_set_header    X-Real-IP $remote_addr;
    proxy_set_header    X-Forwarded-For $remote_addr;
    proxy_set_header    X-Forwarded-Proto https;
    proxy_set_header    X-Forwarded-Scheme https;
    proxy_set_header    Connection keep-alive;
    proxy_set_header    Upgrade $http_upgrade;
    proxy_set_header    Transfer-Encoding "";
    proxy_send_timeout          "20m";
    proxy_read_timeout          "20m";
  }

  location /reactapp {
    return 301 /cbioportal$request_uri;
  }

  expires -1;
}

server {
   listen       8777;
   location / {
      root /gn-static;
   }

   expires -1;
}
```

### cBioPortal Configuration and Setup
Our cBioPortal configuration of changes that need to be reflected in the configuration file. The first is that the public instance of Genome Nexus can no longer be accessed. To accomplish this we update the url for Genome Nexus to be a local path and create proxy services for the internal Genome Nexus. Additionally, we configure the server to support being behind a reverse proxy (NGINX) and set the context path to `/cbioportal`. We set our database settings to communicate with our MySQL instance. Finally, we configure security for OAuth2 authentication and authorization.


```
# Genome Nexus Configuration

genomenexus.url=/cbioportal/proxy/genomenexus
genomenexus.url.grch38=/cbioportal/proxy/genomenexus38

proxy.routes.genomenexus=http://gn-spring-boot:8888
proxy.routes.genomenexus38=http://gn-spring-boot-grch38:8888

# Server Configuration
server.forward-headers-strategy=FRAMEWORK
server.use-forward-headers=true
server.port=8443
server.servlet.context-path=/cbioportal
server.tomcat.redirect-context-root=false

security.cors.allowed-origins=*

# database
spring.datasource.url=jdbc:mysql://cbioportal-database:3306/cbioportal?useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=cbio_user
spring.datasource.password=somepassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect


# security
authenticate=oauth2
authorization=true
oauth2.logout.url=/../

spring.security.oauth2.client.registration.google.client-id=<CLIENT_ID>
spring.security.oauth2.client.registration.google.client-secret=<CLIENT_SECRET>
```

To limit the ability for our server to communicate with outside services we leverage iptables that allow only communication to DNS entries and ports we authorize. All other communications are blocked. In the example below we block all communication except to specific Google services for which we use for authorization.

```
apt-get update
apt-get install -y --no-install-recommends iptables

echo "Iptables chain block installation - Starting"
# Create some new chains for these rules
iptables -N BLOCK_OUTBOUND_HTTP
iptables -N BLOCK_OUTBOUND_HTTPS
iptables -N OUTBOUND_HTTP_WHITELIST
iptables -N OUTBOUND_HTTPS_WHITELIST
iptables -N RESTRICT_OUTBOUND_WEB

# Block all outgoing HTTP
iptables -A BLOCK_OUTBOUND_HTTP
iptables -A BLOCK_OUTBOUND_HTTP -p tcp --dport 80 -j LOG --log-prefix 'cbioportal iptable block: '
iptables -A BLOCK_OUTBOUND_HTTP -p tcp --dport 80 -j REJECT
iptables -A BLOCK_OUTBOUND_HTTP -j RETURN

# Block all outgoing HTTPS
iptables -A BLOCK_OUTBOUND_HTTPS
iptables -A BLOCK_OUTBOUND_HTTPS -p tcp --dport 443 -j LOG --log-prefix 'cbioportal iptable block: '
iptables -A BLOCK_OUTBOUND_HTTPS -p tcp --dport 443 -j REJECT
iptables -A BLOCK_OUTBOUND_HTTPS -j RETURN

# Allow HTTP to specific destination hosts (replace <host> with a hostname, IP, network, etc.)
iptables -A OUTBOUND_HTTP_WHITELIST -p tcp ! --dport 80 -j RETURN
iptables -A OUTBOUND_HTTP_WHITELIST -j RETURN

# Allow HTTPS to specific destination hosts (replace <host> with a hostname, IP, network, etc.)
iptables -A OUTBOUND_HTTPS_WHITELIST -p tcp ! --dport 443 -j RETURN
iptables -A OUTBOUND_HTTPS_WHITELIST --destination gmail.googleapis.com -j ACCEPT
iptables -A OUTBOUND_HTTPS_WHITELIST --destination accounts.google.com -j ACCEPT
iptables -A OUTBOUND_HTTPS_WHITELIST --destination googleusercontent.com -j ACCEPT
iptables -A OUTBOUND_HTTPS_WHITELIST --destination google.com -j ACCEPT
iptables -A OUTBOUND_HTTPS_WHITELIST -j RETURN

# Group the above into an easier to include chain
iptables -A RESTRICT_OUTBOUND_WEB -j OUTBOUND_HTTP_WHITELIST
iptables -A RESTRICT_OUTBOUND_WEB -j OUTBOUND_HTTPS_WHITELIST
iptables -A RESTRICT_OUTBOUND_WEB -j BLOCK_OUTBOUND_HTTP
iptables -A RESTRICT_OUTBOUND_WEB -j BLOCK_OUTBOUND_HTTPS
iptables -A RESTRICT_OUTBOUND_WEB -j RETURN

# Link the new chains into our OUTPUT chain
iptables -A OUTPUT -j RESTRICT_OUTBOUND_WEB
iptables -A OUTPUT -j ACCEPT
echo "Iptables chain block installation - Complete"
```

### OncoKB Setup
You will need to obtain OncoKB transcript information and an OncoKB license in order to use it. Contact the OncoKB team to learn more.


### GenomeNexus Setup

Populate Genome Nexus Data

```
cd ../
sudo mkdir gn-vep-data && cd "$_"

sudo mkdir 98_GRCh37 && cd "$_"
sudo curl -o 98_GRCh37.tar https://oncokb.s3.amazonaws.com/gn-vep-data/98_GRCh37/98_GRCh37.tar
sudo tar xvf 98_GRCh37.tar

cd ..
sudo mkdir 98_GRCh38 && cd "$_"
sudo curl -o 98_GRCh38.tar https://oncokb.s3.amazonaws.com/gn-vep-data/98_GRCh38/98_GRCh38.tar
sudo tar xvf 98_GRCh38.tar
```


### Cancer Hotspots Setup
The default Cancer Hotspots does not support docker by default. You can either add your own files as listed below our use the DFCI forked version: https://github.com/dfci/cancerhotspots.

docker/Dockerfile

```
FROM maven:3.9.9-eclipse-temurin-8
COPY $PWD .
RUN mvn clean install -DskipTests
ENTRYPOINT java -jar webapp/target/cancerhotspots.jar
```

Build

```
docker build -t ksg/cancerhotspots -f docker/Dockerfile .
```

