# Build with different frontend versions

Maven will build cBioPortal with a cBioPortal-frontend version and git repository url as determined by respectively the _frontend.version_ and _frontend.groupId_ parameters in the root POM.xml.

```
  <properties>
    <frontend.version>v2.1.0_/frontend.version>
    <frontend.groupId>com.github.cbioportal</frontend.groupId>
    ...
```
To build cBioPortal with a different frontend version different values for  _frontend.version_ and _frontend.groupId_ parameters can be specified as part of the maven install command. For example:

```
mvn clean -DskipTests install -Dfrontend.version=93d9cbcbf007ff620ab51ef5af5927a0eb1ebed4 -Dfrontend.groupId=com.github.thehyve
```

Remarks:
- The _frontend.version_ parameter allows release tags (e.g. 'v2.1.0') and commit sha-hashes (e.g., '93d9cbcb').
- The _frontend.groupId_ is a reversed, dot-separated derivative of the git url. Git repository location _github.com/cbioportal_ is represented by the _com.github.cbioportal_ groupId.