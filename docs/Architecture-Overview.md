# Architecture Overview
cBioPortal consists of the following components:

- [backend](https://github.com/cBioPortal/cbioportal)
    - MySQL database
    - REST API written in Java Spring
    - Redis cache for storing frequently used queries (optional)
- [validator](https://github.com/cBioPortal/cbioportal/tree/master/core/src/main/scripts/importer)
  checks file formats before importing data into the database
- [frontend](https://github.com/cBioPortal/cbioportal-frontend)
  built with React, Mobx and Bootstrap
- [session service](https://github.com/cBioPortal/session-service) for storing
  user saved data such as virtual studies and groups
     - REST API written in Java Spring enabling retrieval and writing to the database
     - MongoDB database
- cBioPortal also uses the APIs from various [external services](#external-services) to provide more information about a variant

## Backend

The [backend](https://github.com/cBioPortal/cbioportal) is written in Java and
connects to a MySQL database to serve a REST API following the OpenAPI
specification (https://www.cbioportal.org/api/). Note that the repo where this
lives in (https://github.com/cBioPortal/cbioportal) also contains Java classes
to import data as well as the validator. The backend can be configured to
connect to a Redis cache to store database query results for improved performance.

The backend is organized as a multi-module Maven project.
See [cBioPortal backend code organization](Backend-Code-Organization.md).

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

## External Services
cBioPortal uses the APIs from several external services to provide more
information about a variant:

- [OncoKB](#oncokb)
- [CIVIC](#civic)
- [Genome Nexus](#genome-nexus)
- [G2S](#g2s)

For privacy concerns see the section: [A note on privacy](#a-note-on-privacy).

### OncoKB
[OncoKB](https://www.oncokb.org) is a precision oncology knowledge base that
contains information about the effects and treatment implications of specific
cancer gene alterations. See the section [OncoKB Data Access](/deployment/integration-with-other-webservices/OncoKB-Data-Access.md)
for how to configure external OncoKB service.

### CIVIC
[CIVIC](https://civicdb.org) is a community-edited forum for discussion and
interpretation of peer-reviewed publications pertaining to the clinical
relevance of variants (or biomarker alterations) in cancer. For information on
how to deploy this service yourself see:
https://github.com/griffithlab/civic-server. It is also possible to disable
showing CIVIC in cBioPortal by setting `show.civic=false` in the
`application.properties` (See [application.properties reference](/deployment/customization/application.properties-Reference.md#civic-integration)).

### Genome Nexus
[Genome Nexus](https://www.genomenexus.org) is a comprehensive one-stop
resource for fast, automated and high-throughput annotation and interpretation
of genetic variants in cancer. For information on how to deploy this service
yourself see: https://github.com/genome-nexus/genome-nexus. For more
information on the various annotation sources and versions provided by Genome
Nexus see: https://docs.genomenexus.org/annotation-sources.

### G2S
[G2S (Genome to Structure)](https://g2s.genomenexus.org) maps genomic variants
to 3D structures. cBioPortal uses it on the mutations tab to show the variants
on a 3D structure. For information on how to deploy this service yourself see:
https://github.com/genome-nexus/g2s.

### A note on privacy

cBioPortal calls these services with variant information from the cBioPortal
database. It however does not send over information that links a variant to a
particular sample or patient. If this is a concern for your use case we recommmend
to deploy your own versions of these services. See the sections above to
linkouts for instructions on how to do this.
