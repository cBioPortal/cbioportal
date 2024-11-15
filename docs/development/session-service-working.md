# Session Service

## 1. Steps to add new Session type through session-service

1. Define new type at [SessionType.java](https://github.com/cBioPortal/session-service/blob/master/src/main/java/org/cbioportal/session\_service/domain/SessionType.java).
2. Add necessary tests if required.
3. Update [session-service](https://github.com/cBioPortal/session-service#valid-type) documentation.

## 2. cBioPortal Backend

### 2.1 configuring session-service

Here are the properties that needs to be set

```
session.service.url=
#if basic authentication is enabled on session service one should set:
session.service.user=
session.service.password=
```

#### session.service.url format

```
session.service.url: http://[host]:[port]/[session_service_app]/api/sessions/[portal_instance]/
```

#### example

```
session.service.url: http://localhost:8080/session_service/api/sessions/public_portal/
```

### 2.2 Updates to code

1. Update session-service dependency version in [pom.xml](https://github.com/cBioPortal/cbioportal/blob/master/pom.xml).
2. Add/Update api's in [SessionServiceController.java](https://github.com/cBioPortal/cbioportal/blob/master/web/src/main/java/org/cbioportal/web/SessionServiceController.java) to support new session type.
3. Sometimes we might need to defined model for session. Check how session class is extended in [VirtualStudy](https://github.com/cBioPortal/cbioportal/blob/master/web/src/main/java/org/cbioportal/web/parameter/VirtualStudy.java) and how it is consumed in [SessionServiceController.java](https://github.com/cBioPortal/cbioportal/blob/master/web/src/main/java/org/cbioportal/web/SessionServiceController.java).

## 3. cBioPortal Frontend

1. Client code for cbio session service is in https://github.com/cBioPortal/cbioportal-frontend/blob/master/src/shared/api/sessionServiceAPI.ts. Update it accordingly when the new api added in cBioPortal backend SessionServiceController.java

## 4. Local development

### 4.1 Expose database port

If [docker-compose.yml](https://github.com/cBioPortal/session-service/blob/master/docker-compose.yml) is used for running session service and if you like to access mongodb directly then uncomment this block of [code](https://github.com/cBioPortal/session-service/blob/master/docker-compose.yml#L21)

### 4.2 Test results with cURL in terminal

After you have local cBioPortal instance running, you can use cURL to test session-service endpoints.

**Example for GET methods:**

```
curl 'http://localhost:8080/api/session/custom_gene_list' \
  -H 'Cookie: COOCKIE_COPIED_FROM_BROWSER_REQUEST'
```

```
curl 'http://localhost:8080/api/session/custom_gene_list' \
  -H 'Cookie: _ga=GA1.1.984976088.1616537420; _ga_F1EX2C1M11=GS1.1.1616537420.1.1.1616538749.0; JSESSIONID=E7498ECA52F30588CAA1B34F420122DA'
```

**Example for POST methods:**

```
curl 'http://localhost:8080/api/session/custom_gene_list/save' \
  -H 'Content-Type: application/json' \
  -H 'Accept: */*' \
  -H 'Cookie: COOCKIE_COPIED_FROM_BROWSER_REQUEST' \
  --data-raw '{"name":"test","description":"test description","geneList":["GENE1","GENE2"]}'
```

```
curl 'http://localhost:8080/api/session/custom_gene_list/save' \
  -H 'Content-Type: application/json' \
  -H 'Accept: */*' \
  -H 'Cookie: _ga=GA1.1.984976088.1616537420; _ga_F1EX2C1M11=GS1.1.1616537420.1.1.1616538749.0; JSESSIONID=04457E173F769F7BCF9072CD5480308F' \
  --data-raw '{"name":"test","description":"test description","geneList":["GENE1","GENE2"]}'
```

Note on **Cookie**: You can log in to your account in your local cBioPortal instance, after login, copy cookies from requests in network tab. See [login configuration](/deployment/deploy-without-docker/Deploying.md#optional-login) for more information.
