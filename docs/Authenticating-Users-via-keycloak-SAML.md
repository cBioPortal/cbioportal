# Introduction

The cBioPortal includes support for Keycloak SAML (Security Assertion Markup Language) authentication. This document explains why you might find keycloak SAML authentication useful for storing your user login information outside 
the cbioportal database. It also shows you how to configure Keycloak to communicate with your instance of cBioPortal.

Please note that configuring your local instance to use keycloak SAML authentication requires a local/remote keycloak Identity Provider. For details on how to set up a keycloak server, please read online document at (https://keycloak.gitbooks.io/documentation/server_installation/index.html) .  
This document focuses mainly on the steps to configure the keyclock for authenticating and authorizing cbioportal users.  

## Why keycloak?

keycloak is an open source identity and access management solution. It has a build-in RDBM system for storing login information. It can help build a security layer on top of cBioPortal web application.

Keycloak boils down to three simple terms:
* **realm**: A realm secures and manages security metadata for a set of users, application, and registered auth clients.
* **client**: Clients are entities that can request authentication of a user within a realm.
* **role**: Roles identify a type or category of user. Keycloak often assigns access and permissions to specific roles rather than individual users for a fine-grained access control. 

KeyCloak offers three types of roles: 
* Realm-level roles are in global namespace shared by all clients. 
* Client roles have basically a namespace dedicated to a client. 
* A composite role is a role that has one or more additional roles associated with it. 


## How does Keycloak SAML Authentication work?

KeyCloak supports both OpenID-Connect and SAML authentication. When you use SAML authentication, the KeyCloak server exchanges XML documents with a web application. XML signatures and encryption are then used to verify requests from the application.

# Configure keycloak to authenticate your cbioportal instance
**you will need to login into your keycloak server as an admin user to complete the following tasks.**
1. login to your keycloak Identity Provider e.g http://localhost:8080/auth as an admin user
2. Hover over the top left corner drop down menu that is titled with Master to create a new realm. 
![](images/previews/add-realm.png)
Please Note if you are logged in the master realm, this drop down menu lists all the realms created. The last entry of this drop down menu is always Add Realm. Click this to add a realm. Then type 'demo' in the name field and click on create button.
3. To create a SAML client, go to the Clients left menu item. On this page you’ll see a Create button on the right. This will bring you to the Add Client page.
Enter in the Client ID of the client e.g 'cbioportal'. 
![](images/previews/add-client.png)
Enter 'http://localhost:8080/auth/realms/demo/protocol/saml' in the **Root URL** textbox (Please note this is often a URL and will be the expected issuer value in SAML requests sent by the application), then click on Save button. This will take you to the client page.


4. Select **saml** in the Client Protocol drop down box and use **email** for your Name ID. 
5. Next, enter a redirect URL for keycloak to use upon a successful authentication e.g http://localhost:8081/cbioportal/*, and leave everything else as it is. 
6. Finally enter in the Client SAML Endpoint URL. Enter the URL you want the Keycloak server to send requests and responses to e.g http://localhost:8080/auth/realms/demo/protocol/saml. Click Save. 
![](images/previews/edit-client.png)

## Map SAML Assertion Attributes
To specify attributes included in the SAML assertion, simply click on the **Mapper** tab, then click on either **Add Buildin** or **Create** button. 
Make sure you add at least **X500 email** User Property attribute and **roles** Role Mapper attribute.
![](images/previews/add-mappers.png)
![](images/previews/edit-email-attribute.png)
![](images/previews/edit-role-attribute.png)

## Export SAML Keys
Click on **SML Keys** tab next to **Setting**, and click on **export** button, this will bring your "Export SAML Key Cbioportal" page.
 Accept everything and type a password e.g. apollo1 in the Store Password textbox and click on the **Download** button. 
When you are done, move the jks file to portal/src/main/resources/.
![](images/previews/export-SAML-keys.png)
Please note you can also use the java 'keytool' to generate keystore and then import to the keycloak to customize your keystore attributes.
```
keytool -genkey -alias secure-key -keyalg RSA -keystore samlKeystore.jks
```
## Download SAML Metadata IDPSSODescriptor file
1. Click on **Installation** tab.
2. From dropdown list of Format Option, select **SAML Metadata IDPSSODescriptor**
3. Click on **Download** button
4. Move SAML Metadata IDPSSODescriptor XML file to portal/src/main/resources/
![](images/previews/download-IDPSSODescriptor-file.png)

# Create Roles for authorizing cBioPortal users
* To create a role, just click on the Add Role button on this page. Enter in the name e.g **brca_tcga_pub** and description of the role and hit the Save button.
![](images/previews/add-role.png)
**To turn a regular role into a composite role, go to the role detail page and flip the Composite Role switch on.**


## Create Users and Assign Roles to a User
1. To create a user, click on Users in the left menu bar. This menu option brings you to the user list page. On the right side of the empty user list,
 you should see an Add User button. Click that to start creating your new user.
![](images/previews/add-user.png)
2. Next, assign roles to each user through the Role Mappings tab for that single user.
**A composite role is a role that has one or more additional roles associated with it**. When a composite role is mapped to the user,
 the user also gains the roles associated with that composite. This inheritance is recursive so any composite of composites also gets inherited.
![](images/previews/assign-role-to-user.png)

## Modifying portal.properties

1. Within the portal.properties file , make sure that this line is present:
```
    app.name=cbioportal
```
2. Then, modify the section labeled `authentication`. See SAML parameters shown in example below:
```
    # authentication
    authenticate=saml
    authorization=true
    saml.sp.metadata.entityid=cbioportal
    saml.idp.metadata.location=classpath:/saml-idp-metadata.xml
    saml.idp.metadata.entityid=http://localhost:8080/auth/realms/yourdomain
    saml.keystore.location=classpath:/keystore.jks
    saml.keystore.password=apollo1
    saml.keystore.private-key.key=secure-key
    saml.keystore.private-key.password=apollo2
    saml.keystore.default-key=secure-key
    saml.idp.comm.binding.settings=defaultBinding
    saml.idp.comm.binding.type=
    saml.idp.metadata.attribute.email=email
    saml.idp.metadata.attribute.role=roles
    saml.custom.userservice.class=org.cbioportal.security.spring.authentication.keycloak.SAMLUserDetailsServiceImpl
    # global logout (as opposed to local logout):
    saml.logout.url=/login.jsp?logout_success=true
```

## Doing a Test Run

You are now ready to go. Rebuild the WAR file and re-deploy:

```
mvn -DskipTests clean install
cp portal/target/cbioportal.war $CATALINA_HOME/webapps/
```

Then, go to:  [http://localhost:8081/cbioportal/](http://localhost:8081/cbioportal/).

If all goes well, the following should happen:

* You will be redirected to the Keycloak Login Page.
* After authenticating, you will be redirected back to your local instance of cBioPortal.

If this does not happen, see the Troubleshooting Tips  below.

## Troubleshooting Tips 

### Logging

Getting this to work requires many steps, and can be a bit tricky.  If you get stuck or get an obscure error message, your best bet is to turn on all DEBUG logging. 
 This can be done via `src/main/resources/log4j.properties`.  For example:

```
# Change INFO to DEBUG, if you want to see debugging info on underlying libraries we use.
log4j.rootLogger=DEBUG, a

# Change INFO to DEBUG, if you want see debugging info on our packages and spring security packages.
log4j.category.org.mskcc=DEBUG
log4j.logger.org.springframework.security=DEBUG
```
Then, rebuild the WAR, redeploy, and try to authenticate again.  Your log file will then include hundreds of SAML-specific messages, even the full XML of each SAML message, and this should help you debug the error.
