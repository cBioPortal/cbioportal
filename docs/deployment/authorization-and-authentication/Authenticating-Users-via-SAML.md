# Introduction

The cBioPortal includes support for SAML (Security Assertion Markup Language).  This document explains why you might find SAML useful, and how to configure SAML within your own instance of cBioPortal.

Please note that configuring your local instance to support SAML requires many steps.  This includes configuration changes and a small amount of debugging.  If you follow the steps below, you should be up and running relatively quickly, but be forewarned that you may have do a few trial runs to get everything working.

In the documentation below, we also provide details on how to perform SAML authentication via a commercial company:  [OneLogin](https://www.onelogin.com/).  OneLogin provides a free tier for testing out SAML authentication, and is one of the easier options to get a complete SAML workflow set-up.  Once you have OneLogin working, you should then have enough information to transition to your final authentication service.

## What is SAML?

SAML is an open standard that enables one to more easily add an authentication service on top of any existing web application.  For the full definition, see the [SAML Wikipedia entry](https://en.wikipedia.org/wiki/Security_Assertion_Markup_Language).

In its simplest terms, SAML boils down to four terms:

* **identity provider**:  this is a web-based service that stores user names and passwords, and provides a login form for users to authenticate.  Ideally, it also provides easy methods to add / edit / delete users, and also provides methods for users to reset their password.  In the documentation below, OneLogin.com serves as the identity provider.

* **service provider**:  any web site or web application that provides a service, but should only be available to authenticated and authorized users.  In the documentation below, the cBioPortal is the service provider.

* **authentication**:  a means of verifying that a user is who they purport to be.  Authentication is performed by the identify provider, by extracting the user name and password provided in a login form, and matching this with information stored in a database. When authentication is enabled, multiple cancer studies can be stored within a single instance of cBioPortal while providing fine-grained control over which users can access which studies.  Authorization is implemented within the core cBioPortal code, and *not* the identify provider.

## Why is SAML Relevant to cBioPortal?

cBioPortal has no means of directly authenticating users. If you want to restrict access to your instance of cBioPortal, you therefore have to consider an external authentication service.  SAML is one means of doing so, and your larger institution may already provide SAML support.  For example, at Memorial Sloan Kettering and Dana-Farber, users of the internal cBioPortal instances login with their regular credentials via SAML.  This greatly simplifies user management.

# Setting up an Identity Provider

As noted above, we provide details on how to perform SAML authentication via a commercial company:  [OneLogin](https://www.onelogin.com/).  
If you already have an IDP set up, you can skip this part and go to [Configuring SAML within cBioPortal](#configuring-saml-within-cbioportal).

OneLogin provides a free tier for testing out SAML authentication, and is one of the easier options to get a complete SAML workflow set-up.  Once you have OneLogin working, you should then have enough information to transition to your final authentication service.  As you follow the steps below, the following link may be helpful: [How to Use the OneLogin SAML Test Connector](https://support.onelogin.com/hc/en-us/articles/202673944-How-to-Use-the-OneLogin-SAML-Test-Connector).

To get started:

* [Register a new OneLogin.com Account](https://www.onelogin.com/signup?ref=floating-sidebar)

## Setting up a SAML Test Connector

* [Login to OneLogin.com](https://app.onelogin.com/login).
* Under Apps, Select Add Apps.
* Search for SAML.
* Select the option labeled: OneLogin SAML Test (IdP w/attr).
* "SAVE" the app, then select the Configuration Tab.

* Under the Configuration Tab for OneLogin SAML Test (IdP w/attr), paste the following fields (this is assuming you are testing everything via localhost).

    * Audience: cbioportal
    * Recipient: http://localhost:8080/saml/SSO
    * ACS (Consumer) URL Validator*:  ^http:\/\/localhost\:8080\/cbioportal\/saml\/SSO$
    * ACS (Consumer) URL*:  http://localhost:8080/saml/SSO

![](/images/previews/onelogin-config.png)
Note: These URLs presume that cBioPortal is operating at the root level and no context window is set.

* Add at least the parameters: 
    * Email (Attribute)
    * Email (SAML NameID)

![](/images/previews/onelogin-config-parameters.png)

* Find your user in the "Users" menu

![](/images/previews/onelogin-users-search.png)

* Link the SAML app to your user (click "New app" on the **+** icon found on the top right of the "Applications" table to do this - see screenshot below): 

![](/images/previews/onelogin-add-app.png)

* Configure these **email** parameters in the Users menu:

![](/images/previews/onelogin-users.png)
 


## Downloading the SAML Test Connector Meta Data

* Go to the SSO Tab within OneLogin SAML Test (IdP), find the field labeled:  Issuer URL.  Copy this URL and download it's contents.  This is an XML file that describes the identity provider.

![](https://s3.amazonaws.com/f.cl.ly/items/1R0v0L3P1U2E23202V1d/Image%202015-05-24%20at%2010.07.56%20PM.png)

then, move this XML file to:

    src/main/resources/

You should now be all set with OneLogin.com.  Next, you need to configure your instance of cBioPortal.

# Configuring SAML within cBioPortal

## Creating a KeyStore

In order to use SAML, you must create a [Java Keystore](https://docs.oracle.com/javase/7/docs/api/java/security/KeyStore.html).  

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

##### HTTPS and Tomcat

:warning: If you already have an official (non-self-signed) SSL certificate, and need to get your site 
running on HTTPS directly from Tomcat, then you need to import your certificate into the keystore instead. 
See [this Tomcat documentation page](https://tomcat.apache.org/tomcat-8.0-doc/ssl-howto.html) for more details.

:warning: An extra warning for when configuring HTTPS for Tomcat: use the same password for 
both keystore and secure-key. This seems to be an extra restriction by Tomcat.


## Modifying configuration

Within application.properties, make sure that:

    app.name=cbioportal

Then, modify the section labeled `authentication`. See SAML parameters shown in example below:

 ```
spring.security.saml2.relyingparty.registration.cbio-idp.assertingparty.metadata-uri=https://ksg.dfci.harvard.edu/keycloak/realms/partners/protocol/saml/descriptor
spring.security.saml2.relyingparty.registration.cbio-idp.entity-id=cbioportal
spring.security.saml2.relyingparty.registration.cbio-idp.signing.credentials[0].certificate-location=classpath:/samlCertificate.crt
spring.security.saml2.relyingparty.registration.cbio-idp.signing.credentials[0].private-key-location=classpath:/samlPrivateKey-pkcs8.pem
saml.logout.local=false
saml.logout.url=/
```

Please note that you will have to modify all the above to match your own settings. `saml.idp.comm.binding.type` can be left empty if `saml.idp.comm.binding.settings=defaultBinding`. The `saml.logout.*` settings above reflect the settings of an IDP that supports Single Logout (hopefully the default in most cases - more details in section below).

In the case that you are running cBioPortal behind a reverse proxy that handles the SSL certificates (such as nginx or traefik), you will have to also specify `server.forward-headers-strategy=FRAMEWORK` and `server.use-forward-headers=true`

### Custom scenarios

:warning: Some IDPs like to provide their own logout page (e.g. when they don't support the custom SAML Single Logout protocol). For this you can adjust the  
`saml.logout.url` property to a custom URL provided by the IDP. Also set the `saml.logout.local=true` property in this case to indicate that global logout (or Single Logout) is not supported by IDP:

    # local logout followed by a redirect to a global logout page:
    saml.logout.local=true
    saml.logout.url=<idp specific logout URL, e.g. https://idp.logoutpage.com >


## Authorizing Users

Next, please read the Wiki page on [User Authorization](User-Authorization.md), and add user rights for a single user.


## Configuring the Login.html Page (not applicable to most external IDPs)

The login page is configurable via the `application.properties` properties `skin.authorization_message` and `skin.login.saml.registration_htm`. 
For example in `skin.authorization_message` you can be set to something like this:

```
skin.authorization_message= Welcome to this portal. Access to this portal is available to authorized test users at YOUR ORG. [<a href="https://youorg.com/">Request Access</a>].
```

You can also set a standard text in `skin.login.contact_html` that will appear in case of problems: 

```
skin.login.contact_html=If you think you have received this message in error, please contact us at <a style="color:#FF0000" href="mailto:cbioportal-access@your.org">cbioportal-access@your.org</a>
```


## Doing a Test Run

You are now ready to go.

Rebuild the WAR file and follow the [Deployment with authentication
steps](/deployment/deploy-without-docker/Deploying.md#required-login) using `authenticate=saml`.

Then, go to:  [http://localhost:8080/](http://localhost:8080/).

If all goes well, the following should happen:

* You will be redirected to the OneLogin Login Page.
* After authenticating, you will be redirected back to your local instance of cBioPortal.

If this does not happen, see the Troubleshooting Tips  below.

## Troubleshooting Tips 

### Logging

Getting this to work requires many steps, and can be a bit tricky.  If you get stuck or get an obscure error message,
your best bet is to turn on all DEBUG logging.  This can be done via `src/main/resources/logback.xml`.  For example:

```
<root level="debug">
    <appender-ref ref="STDOUT" />
    <appender-ref ref="FILE" />
</root>

<logger name="org.mskcc" level="debug">
    <appender-ref ref="STDOUT" />
    <appender-ref ref="FILE" />
</logger>

<logger name="org.cbioportal.security" level="debug">
    <appender-ref ref="STDOUT" />
    <appender-ref ref="FILE" />
</logger>
```

Your log file will then include hundreds of SAML-specific messages, even the full XML of each SAML message, and this should help you debug the error.

### Seeing the SAML messages

Another tool we can use to troubleshoot is SAML tracer (https://addons.mozilla.org/en-US/firefox/addon/saml-tracer/ ). You can add this to Firefox and it will give you an extra menu item in "Tools". Go through the loging steps and you will see the SAML messages that are sent by the IDP. 

### Obtaining the Service Provider Meta Data File

By default, the portal will automatically generate a Service Provider (SP) Meta Data File.  You may need to provide this file to your Identity Provider (IP).

You can access the Service Provider Meta Data File via a URL such as:

[http://localhost:8080/saml/metadata](http://localhost:8080/saml/metadata)

