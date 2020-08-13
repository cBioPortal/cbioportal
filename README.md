# cBioPortal

The cBioPortal for Cancer Genomics provides visualization, analysis, and download of large-scale cancer genomics data sets. For a short intro on cBioPortal, see [these introductory slides](https://docs.google.com/presentation/d/1hm0G77UklZnpQfFvywBfW2ZIsy8deKi5r1RfJarOPLg/edit?usp=sharing).

If you would like to know how to setup a private instance of the portal and/or get set up for developing, see the [documentation](https://docs.cbioportal.org). For details on contributing code changes via pull requests, see our [Contributing document](CONTRIBUTING.md).

If you are interested in coordinating the development of new features, please contact cbioportal@cbio.mskcc.org or reach out on https://slack.cbioportal.org.

## 📘 Documentation
See [https://docs.cbioportal.org](https://docs.cbioportal.org)

## 🤝 License
See [LICENSE](./LICENSE)

## 💻 Run Backend
cBioPortal consists of several components, please read the [Architecture docs](https://docs.cbioportal.org/2.1-deployment/architecture-overview) to figure out what repo would be relevant to edit. If you e.g. only want to make frontend changes, one can directly edit [the frontend repo](https://github.com/cbioportal/cbioportal-frontend) instead. Read the instructions in that repo for more info on how to do frontend development. This repo only contains the backend part. Before editing the backend, it's good to read the [backend code organization](docs/Backend-Code-Organization.md). For development of the backend repo one should first set up a database. Please follow the [Docker deployment documentation](https://docs.cbioportal.org/2.1.1-deploy-with-docker-recommended/docker) to do so. Change the [docker-compose](https://github.com/cBioPortal/cbioportal-docker-compose/blob/5da068f0eb9b4f42db52ab5e91321b26a1826d7a/docker-compose.yml#L6) file to use your image instead:

```
docker build -t cbioportal/cbioportal:my-dev-cbioportal-image -f docker/web-and-data/Dockerfile .
```

Note: internally we have a dev database available with the public data set that one can connect to directly. Please reach out on slack to get the credentials. It is usually best to use a small test dataset, but if a copy of the production database is necessary for e.g. fixing a bug specific to production data that can be useful.

### 🕵️‍♀️ Debugging

If you want to attach a debugger you can change the `docker-compose.yml` file to include the paramaters: `-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005`. <ake sure to expose that port by adding `5005:5005` in the ports section of the cbioportal container.

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
| Status | [![master build status](https://travis-ci.org/cBioPortal/cbioportal.svg?branch=master)](https://travis-ci.org/cBioPortal/cbioportal/branches) | -- | [![Build Status](https://travis-ci.org/cBioPortal/cbioportal.svg?branch=rc)](https://travis-ci.org/cBioPortal/cbioportal/branches) |


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
