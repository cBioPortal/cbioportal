# Introduction

The cBioPortal includes support for SAML (Security Assertion Markup Language).  This document explains why you might find SAML useful, and how to configure SAML within your own instance of cBioPortal.

Please note that configuring your local instance to support SAML requires many steps.  This includes configuration changes, a small amount of debugging and modest code changes.  If you follow the steps below, you should be up and running relatively quickly, but be forewarned that you may have do a few trial runs to get everything working.

In the documentation below, we also provide details on how to preform SAML authentication via a commercial company:  [OneLogin](https://www.onelogin.com/).  OneLogin provides a free tier for testing out SAML authentication, and is one of the easier options to get a complete SAML workflow set-up.  Once you have OneLogin working, you should then have enough information to transition to your final authentication service.

## What is SAML?

SAML is an open standard that enables one to more easily add an authentication service on top of any existing web application.  For the full definition, see the [SAML Wikipedia entry](http://en.wikipedia.org/wiki/Security_Assertion_Markup_Language).

In its simplest terms, SAML boils down to four terms:

* **identity provider**:  this is a web-based service that stores user names and passwords, and provides a login form for users to authenticate.  Ideally, it also provides easy methods to add / edit / delete users, and also provides methods for users to reset their password.  In the documentation below, OneLogin.com serves as the identity provider.

* **service provider**:  any web site or web application that provides a service, but should only be available to authenticated and authorized users.  In the documentation below, the cBioPortal is the service provider.

* **authentication**:  a means of verifying that a user is who they purport to be.  Authentication is performed by the identify provider, by extracting the user name and password provided in a login form, and matching this with information stored in a database.

* **authorization**:  defines resources a user can access.  When authorization is turned on with cBioPortal, users can only access cancer studies they are specifically authorized to view.  This enables one to store multiple cancer studies within a single instance of cBioPortal, but provide fine-grained control over which users can access which studies.  Authorization is implemented within the core cBioPortal code, and not the identify provider.

## Why is SAML Relevant to cBioPortal?

The cBioPortal code has no means of storing user name and passwords and no means of directly authenticating users.  If you want to restrict access to your instance of cBioPortal, you therefore have to consider an external authentication service.  SAML is one means of doing so, and your larger institution may already provide SAML support.  For example, at Sloan Kettering and Dana-Farber, users of the internal cBioPortal instances login with their regular credentials via SAML.  This greatly simplifies user management.

# Setting up an Identity Provider

As noted above, we provide details on how to preform SAML authentication via a commercial company:  [OneLogin](https://www.onelogin.com/).  OneLogin provides a free tier for testing out SAML authentication, and is one of the easier options to get a complete SAML workflow set-up.  Once you have OneLogin working, you should then have enough information to transition to your final authentication service.  As you follow the steps below, the following link may be helpful: [How to Use the OneLogin SAML Test Connector](https://support.onelogin.com/hc/en-us/articles/202673944-How-to-Use-the-OneLogin-SAML-Test-Connector).

To get started:

* [Register a new OneLogin.com Account](https://www.onelogin.com/signup?ref=floating-sidebar)

## Setting up a SAML Test Connector

* [Login to OneLogin.com](https://app.onelogin.com/login).
* Under Apps, Select Add Apps.
* Search for SAML.
* Select the option labeled: OneLogin SAML Test (IdP).

![](http://f.cl.ly/items/2u2I1D3J021U2u081K0X/Image%202015-05-24%20at%2010.00.11%20PM.png)

* Under the Configuration Tab for OneLogin SAML Test (IdP), paste the following fields (this is assuming you are testing everything via localhost).

    * ACS (Consumer) URL Validator*:  ^http:\/\/localhost\:8080\/cbioportal\/$
    * ACS (Consumer) URL*:  http://localhost:8080/cbioportal/

![](https://cloud.githubusercontent.com/assets/366003/17789818/ce5eb1c2-6561-11e6-9887-c373023e1acd.png)

## Downloading the SAML Test Connector Meta Data

* Go to the SSO Tab within OneLogin SAML Test (IdP), find the field labeled:  Issuer URL.  Copy this URL and download it's contents.  This is an XML file that describes the identity provider.

![](http://f.cl.ly/items/1R0v0L3P1U2E23202V1d/Image%202015-05-24%20at%2010.07.56%20PM.png)

then, move this XML file to:

    portal/src/main/resources/

You should now be all set with OneLogin.com.  Next, you need to configure your instance of cBioPortal.

# Configuring SAML within cBioPortal

## Creating a KeyStore

In order to use SAML, you must create a [Java Keystore](http://docs.oracle.com/javase/7/docs/api/java/security/KeyStore.html).  

This can be done via the Java `keytool` command, which is bundled with Java.

Type the following:

    keytool -genkey -alias secure-key -keyalg RSA -keystore samlKeystore.jks

This will create a Java keystore for a key called:  `secure-key` and place the keystore in a file named `samlKeystore.jks`.  You will be prompted for:

* keystore password (required, for example:  apollo1)
* your name, organization and location (optional)
* key password for `secure-key` (required, for example apollo2)

When you are done, copy `samlKeystore.jsk` to the correct location:
    
    mv samlKeystore.jks portal/src/main/resources/

If you need to export the public certificate associated within your keystore, run:

    keytool -export -keystore samlKeystore.jks -alias secure-key -file cBioPortal.cer

## Modifying portal.properties

Within portal.properties, make sure that:

    app.name=cbioportal

Then, modify the section labeled `authentication`.  For example:

    # authentication
    authenticate=saml
    authorization=true
    googleplus.consumer.key=
    googleplus.consumer.secret=
    saml.sp.metadata.entityid=cbioportal
    saml.idp.metadata.location=classpath:/onelogin_metadata_448340.xml
    saml.idp.metadata.entityid=https://app.onelogin.com/saml/metadata/448340
    saml.keystore.location=classpath:/samlKeystore.jks
    saml.keystore.password=apollo1
    saml.keystore.private-key.key=secure-key
    saml.keystore.private-key.password=apollo2
    saml.keystore.default-key=secure-key

Please note that you will have to modify all the above to match your own settings.

## Authorizing Users

Next, please read the Wiki page on [User Authorization](User-Authorization.md), and add user rights for a single user.

## Modifying MSKCCPortalUserDetailsService.java

This step requires that you make a modest amount of code changes.  You can either choose to modify:  [MSKCCPortalUserDetailsService.java](https://github.com/cBioPortal/cbioportal/blob/master/core/src/main/java/org/mskcc/cbio/portal/authentication/saml/MSKCCPortalUserDetailsService.java), or copy it and use as a reference.

To get MSKCCPortalUserDetailsService.java working with OneLogin, you only need to make two changes:

1.  Within ```initializeDefaultEmailSuffixes()```, add your email suffixes.  For example:

```
toReturn.add("harvard.edu");
```

2.  Modify user id and name.

For example:

```
-        String userid = credential.getAttributeAsString("/UserAttribute[@ldap:targetAttribute=\"mail\"]");
-        String name = credential.getAttributeAsString("/UserAttribute[@ldap:targetAttribute=\"displayName\"]");
+        String userid = credential.getNameID().getValue();
+        String name = credential.getNameID().getValue();
```

## Modify the Login.jsp Page

The current [Login.jsp](https://github.com/cBioPortal/cbioportal/blob/master/portal/src/main/webapp/login.jsp) page is hard-coded for Sloan Kettering Cancer Center.  We hope to fix this soon, and make the page configurable via `portal.properties`.  Until then, you will need to modify `Login.jsp` to match your specific set-up.  For example:

```
-            If you think you have received this message in error, please contact us at <a style="color:#FF0000" href="mailto:cbioportal-access@cbio.mskcc.
+           Please request access XXXXXXX.
```

and

```
-                            Sign in with MSK</button>
+                            Sign in via XXXX</button>
```

## Doing a Test Run

You are now ready to go.

Rebuild the WAR file and re-deploy:

```
mvn -DskipTests clean install
cp portal/target/cbioportal.war $CATALINA_HOME/webapps/
```

Then, go to:  [http://localhost:8080/cbioportal/](http://localhost:8080/cbioportal/).

If all goes well, the following should happen:

* You will be redirected to the OneLogin Login Page.
* After authenticating, you will be redirected back to your local instance of cBioPortal.

If this does not happen, see the Debugging Tips below.

## Tips

### Logging

Getting this to work requires many steps, and can be a bit tricky.  If you get stuck or get an obscure error message, your best bet is to turn on all DEBUG logging.  This can be done via `src/main/resources/log4j.properties`.  For example:

```
# Change INFO to DEBUG, if you want to see debugging info on underlying libraries we use.
log4j.rootLogger=DEBUG, a

# Change INFO to DEBUG, if you want see debugging info on our packages only.
log4j.category.org.mskcc=DEBUG
```

Then, rebuild the WAR, redeploy, and try to authenticate again.  Your log file will then include hundreds of SAML-specific messages, even the full XML of each SAML message, and this should help you debug the error.

### Obtaining the Service Provider Meta Data File

By default, the portal will automatically generate a Service Provider (SP) Meta Data File.  You may need to provide this file to your Identity Provider (IP).

You can access the Service Provider Meta Data File via a URL such as:

[http://localhost:8080/cbioportal/saml/metadata](http://localhost:8080/cbioportal/saml/metadata)

