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

The cBioPortal code has no means of storing user name and passwords and no means of directly authenticating users.  If you want to restrict access to your instance of cBioPortal, you therefore have to consider an external authentication service.  SAML is one means of doing so, and your larger institution may already provide SAML support.  For example, at Sloan Kettering and Dana-Farber, users of the internal cBioPortal instances login with their regular credentials via SAML.  This greatly simplifies user management.

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

    portal/src/main/resources/

You should now be all set with OneLogin.com.  Next, you need to configure your instance of cBioPortal.

# Configuring SAML within cBioPortal

## Setup Downloading Certificate Files
Referencing the above or another site you will need to create an asserting party. In Keycloak this is called a client. You will need to download the credentials certificate and private key and place them in a location that is accessible from cBioPortal.


## Modifying configuration

```
spring.security.saml2.relyingparty.registration.cbio-idp.assertingparty.metadata-uri=https://authorizing-site.com/keycloak/realms/cbioportal/protocol/saml/descriptor
spring.security.saml2.relyingparty.registration.cbio-idp.entity-id=cbioportal
spring.security.saml2.relyingparty.registration.cbio-idp.signing.credentials[0].certificate-location=classpath:/samlCertificate.crt
spring.security.saml2.relyingparty.registration.cbio-idp.signing.credentials[0].private-key-location=classpath:/samlPrivateKey-pkcs8.pem
saml.logout.local=false
saml.logout.url=/../
```
The above configuration presumes that the downloaded certificate files are placed in the /cbioportal-webapp directory in the docker image. You will need to update your configuration as needed.

## Authorizing Users

Next, please read the Wiki page on [User Authorization](User-Authorization.md), and add user rights for a single user.

