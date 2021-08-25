## Working on Session-service
###  1. Steps to add new Session type through session-service

1.  Define new type at [SessionType.java](https://github.com/cBioPortal/session-service/blob/master/src/main/java/org/cbioportal/session_service/domain/SessionType.java).
2. Add necessary tests if required.
3. Update [session-service](https://github.com/cBioPortal/session-service#valid-type) documentation.

###  2. cBioPortal Backend
#### 2.1 configuring session-service
Here are the properties that needs to be set
```
WARNING: do not use session service with -Dauthenticate=false
 either use authentication or change to -Dauthenticate=noauthsessionservice
session.service.url=
#if basic authentication is enabled on session service one should set:
session.service.user=
session.service.password=

```
##### session.service.url format
```
session.service.url: http://[host]:[port]/[session_service_app]/api/sessions/[portal_instance]/
```
##### example
```
session.service.url: http://localhost:8080/session_service/api/sessions/public_portal/
```

#### 2.2 Updates to code
1. Update session-service dependency version in [pom.xml](https://github.com/cBioPortal/cbioportal/blob/master/pom.xml).
2. Add/Update api's in [SessionServiceController.java](https://github.com/cBioPortal/cbioportal/blob/master/web/src/main/java/org/cbioportal/web/SessionServiceController.java)  to support new session type.
3. Sometime we might need to defined model for session. Check how session class is extended in [VirtualStudy](https://github.com/cBioPortal/cbioportal/blob/master/web/src/main/java/org/cbioportal/web/parameter/VirtualStudy.java) and how it is consumed in [SessionServiceController.java](https://github.com/cBioPortal/cbioportal/blob/master/web/src/main/java/org/cbioportal/web/SessionServiceController.java).

###  3. cBioPortal Frontend

1. Client code for cbio session service is in https://github.com/cBioPortal/cbioportal-frontend/blob/master/src/shared/api/sessionServiceAPI.ts. Update it accordingly when the new api added in cBioPortal backend SessionServiceController.java

### 4. Local development

- If [docker-compse.yml](https://github.com/cBioPortal/session-service/blob/master/docker-compose.yml) is used for running session service and if. you you. like to access mongodb directly then uncomment this block of [code](https://github.com/cBioPortal/session-service/blob/master/docker-compose.yml#L21)
