# Architecture Overview
cBioPortal consists of the following components:

- [backend](https://github.com/cBioPortal/cbioportal) written in Java.
- MySQL database that the backend uses
- [validator](https://github.com/cBioPortal/cbioportal/tree/master/core/src/main/scripts/importer)
  which checks file formats before importing data into the database.
- [frontend](https://github.com/cBioPortal/cbioportal-frontend)
  built with React, Mobx and Bootstrap.
- [session service](https://github.com/cBioPortal/session-service) for storing
  user saved data such as virtual studies and groups
- Mongo database which session service uses

cBioPortal also uses the APIs from various [external services](External-Services.md) to provide more information about a variant.

## Backend

The [backend](https://github.com/cBioPortal/cbioportal) is written in Java and
connects to a MySQL database to serve a REST API following the OpenAPI
specification (https://www.cbioportal.org/api/). Note that the repo where this
lives in (https://github.com/cBioPortal/cbioportal) also contains Java classes
to import data as well as the validator.

## Validator
The
[validator](https://github.com/cBioPortal/cbioportal/tree/master/core/src/main/scripts/importer)
checks file formats before importing data into the database. There is a wrapper
script `metaImport.py` that validates the data and subsequently calls the
relevant Java classes to import the data.

## Session Service

The [session service](https://github.com/cBioPortal/session-service) is used
for storing user saved data such as virtual studies and groups. See the
[tutorials](https://www.cbioportal.org/tutorials) section to read more about
these features. Session service is a Java app that serves a REST API backed by
a Mongo database. The session service is served as a proxy through the
cBioPortal backend REST API. The backend is therefore the only component that
needs to be able to connect to it. The frontend does not connect to it
directly. 

## Frontend
The frontend is a single page app built with React, Mobx and Bootstrap. The
data gets pulled from the backend REST API. The frontend is by default included
with the backend so no extra setup is required. 
