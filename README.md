# cBioPortal

[![Endpoint Badge](https://img.shields.io/endpoint?url=https%3A%2F%2Fraw.githubusercontent.com%2FcBioPortal%2Fcbioportal-test%2Frefs%2Fheads%2Fmain%2Fsecurity-status.json)](https://docs.cbioportal.org/development/security/)

The cBioPortal for Cancer Genomics provides visualization, analysis, and download of large-scale cancer genomics data sets. For a short intro on cBioPortal, see [these introductory slides](https://docs.google.com/presentation/d/1hm0G77UklZnpQfFvywBfW2ZIsy8deKi5r1RfJarOPLg/edit?usp=sharing).

If you would like to know how to setup a private instance of the portal and/or get set up for developing, see the [documentation](https://docs.cbioportal.org). For details on contributing code changes via pull requests, see our [Contributing document](CONTRIBUTING.md).

If you are interested in coordinating the development of new features, please contact cbioportal@cbioportal.org or reach out on https://slack.cbioportal.org.

## 📘 Documentation
See [https://docs.cbioportal.org](https://docs.cbioportal.org)

## 🤝 License
See [LICENSE](./LICENSE)

## 💻 Run Backend
cBioPortal consists of several components, please read the [Architecture docs](https://docs.cbioportal.org/architecture-overview/) to figure out what repo would be relevant to edit. If you e.g. only want to make frontend changes, one can directly edit [the frontend repo](https://github.com/cbioportal/cbioportal-frontend) instead. Read the instructions in that repo for more info on how to do frontend development. This repo only contains the backend part. Before editing the backend, it's good to read the [backend code organization](docs/Backend-Code-Organization.md).

### Local Development

This section provides a summary. For Quick Start instructions, or for more additional information, please see [Deploy with Docker](https://docs.cbioportal.org/deployment/docker/)

#### What MySQL database to use
We recommend to set up a MySQL database automatically using [Docker Compose](https://github.com/cBioPortal/cbioportal-docker-compose). It's useful to know how to do this as it allows you to import any dataset of your choice. For debugging production issues, we also have a database available with all the data on https://cbioportal.org that one can connect to directly. Please reach out on slack to get the credentials.

#### Deploy your development image inside Docker Compose
The easiest option is to deploy your development image directly into the [docker-compose](https://github.com/cBioPortal/cbioportal-docker-compose/blob/5da068f0eb9b4f42db52ab5e91321b26a1826d7a/docker-compose.yml#L6) file. 

1. From the cbioportal repo, build the image:
```
docker build -t cbioportal/cbioportal:my-dev-cbioportal-image -f docker/web-and-data/Dockerfile .
```
2. From the cbioportal-docker-compose repo, change the [env file](https://github.com/cBioPortal/cbioportal-docker-compose/blob/master/.env) to use your image (e.g. **cbioportal/cbioportal:my-dev-cbioportal-image**).

3. Run the containers.
```
docker compose up
```

4. The app will be visible at http://localhost:8080.

For more information, please see [Deploy with Docker](https://docs.cbioportal.org/deployment/docker/#building-cbioportal).

#### Command Line

If you want to instead run the cBioPortal web app from the command line please follow these instructions. First, we want to make sure that all ports are open for the services set up through [docker compose](https://github.com/cBioPortal/cbioportal-docker-compose) (i.e. not just accessible to other containers within the same Docker Compose file). To do so, in the [docker compose repo](https://github.com/cBioPortal/cbioportal-docker-compose) run:

```
docker compose -f docker-compose.yml -f dev/open-ports.yml up
```
This should open the ports. Now we are ready to run the cBioPortal web app locally. You can compile the backend code with:

```

java -Xms2g -Xmx4g \
     -Dauthenticate=false \
     -Dsession.service.url=http://localhost:5000/api/sessions/my_portal/ \
     -Dsession.service.origin='*' \
     -Dspring.datasource.username=cbio_user \
     -Dspring.datasource.password=somepassword \
     -Dspring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver \
     -Dspring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect \
     -Dspring.datasource.url='jdbc:mysql://cbio_user:somepassword@localhost:3306/cbioportal?useSSL=false&allowPublicKeyRetrieval=true' \
     -Dshow.civic=true \
     -Dskin.footer='' \
     -Dapp.name='my-portal' \
     -Ddbconnector=dbcp \
     -cp "$PWD:$PWD/BOOT-INF/lib/*" \
     org.cbioportal.PortalApplication
```

The app should now show up at http://localhost:8080.

### Dev Database

Note: internally we have a dev database available with the public data set that one can connect to directly. Please reach out on slack to get the credentials. It is usually best to use a small test dataset, but if a copy of the production database is necessary for e.g. fixing a bug specific to production data that can be useful.

### 🕵️‍♀️ Debugging

If you want to attach a debugger you can change the `docker-compose.yml` file to include the parameters: `-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005` (make sure to expose the debug port by adding `5005:5005` in the ports section of the cbioportal container). If you are running the java app outside of docker you can add the same parameters to the java command line arguments instead.

You can then use a JAVA IDE to connect to that port. E.g. in [VSCode](https://code.visualstudio.com/), one would add the following configuration to `launch.json` to connect:

```
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Debug (Attach)",
            "request": "attach",
            "hostName": "localhost",
            "port": 5005,
            "projectName": "cbioportal"
        }
    ]
}
```

## 🌳 Branch Information

| | main branch | upcoming release branch | later release candidate branch |
| --- | --- | --- | --- |
| Branch name | [`master`](https://github.com/cBioPortal/cbioportal/tree/master) |  -- |  [`rc`](https://github.com/cBioPortal/cbioportal/tree/rc) |
| Description | All bug fixes and features not requiring database migrations go here. This code is either already in production or will be released this week | Next release that requires database migrations. Thorough manual product review often takes place for this branch before release | Later releases with features that require database migrations. This is useful to allow merging in new features without affecting the upcoming release. Could be seen as a development branch, but note that only high quality pull requests are merged. That is the feature should be pretty much ready for release after merge. |
| Live instance | https://www.cbioportal.org / https://master.cbioportal.org | -- | https://rc.cbioportal.org |
| Live instance version | https://www.cbioportal.org/api/info / https://master.cbioportal.org/api/info | -- | https://rc.cbioportal.org/api/info |
| Docker Image | cbioportal/cbioportal:master | --| cbioportal/cbioportal:rc |
| Kubernetes Config | [production](https://github.com/knowledgesystems/knowledgesystems-k8s-deployment/blob/master/cbioportal/cbioportal_spring_boot.yaml) / [master](https://github.com/knowledgesystems/knowledgesystems-k8s-deployment/blob/master/cbioportal/cbioportal_backend_master.yaml) | -- | [rc](https://github.com/knowledgesystems/knowledgesystems-k8s-deployment/blob/master/cbioportal/cbioportal_backend_rc.yaml) |
| Status | [![master build status](https://github.com/cbioportal/cbioportal/workflows/Core%20tests/badge.svg)](https://github.com/cBioPortal/cbioportal/actions/workflows/core-test.yml?query=branch%3Amaster) [![master build status](https://github.com/cbioportal/cbioportal/workflows/Integration%20tests/badge.svg)](https://github.com/cBioPortal/cbioportal/actions/workflows/integration-test.yml?query=branch%3Amaster) [![master build status](https://github.com/cbioportal/cbioportal/workflows/Docker%20Image%20CI/badge.svg)](https://github.com/cBioPortal/cbioportal/actions/workflows/dockerimage.yml?query=branch%3Amaster) [![master build status](https://github.com/cbioportal/cbioportal/workflows/Python%20validator/badge.svg)](https://github.com/cBioPortal/cbioportal/actions/workflows/validate-data.yml?query=branch%3Amaster) [![CircleCI](https://circleci.com/gh/cBioPortal/cbioportal/tree/master.svg?style=svg)](https://app.circleci.com/pipelines/github/cBioPortal/cbioportal?branch=master&filter=all) | -- | -- |

## 🚀 Releases
Release Notes on GitHub:

https://github.com/cBioPortal/cbioportal/releases

See also the cBioPortal News section for user focused release information:

https://www.cbioportal.org/news

Docker Images are available for each tag and branch:

https://hub.docker.com/repository/docker/cbioportal/cbioportal/tags

## 👉 Other Repos
Read the [Architecture docs](https://docs.cbioportal.org/2.1-deployment/architecture-overview) to see how these relate:

- https://github.com/cBioPortal/cbioportal-frontend
- https://github.com/cbioportal/session-service
- https://github.com/cBioPortal/datahub/
