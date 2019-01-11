# cBioPortal Documentation

Welcome to the documentation for cBioPortal! Below you can find an index of all available documentation to help you to deploy and maintain a local instance of cBioPortal, as well as information on how to upload data. Specific pages are also available for developers. For details on contributing code changes and pull-requests to cBioPortal, please see our [CONTRIBUTING Doc](https://github.com/cBioPortal/cbioportal/blob/master/CONTRIBUTING.md).

We also maintain an active [list of RFCs (Requests for Comments)](RFC-List.md) where we describe new features and solicit community feedback.


## 1. General
* [News](News.md)
* [Frequently Asked Questions](FAQ.md)
* [About Us](About-Us.md)
* [List of RFCs](RFC-List.md)

## 2. cBioPortal Deployment
### 2.1 Deployment
* [Hardware and Software Requirements](System-Requirements.md)
* [Pre-Build Steps](Pre-Build-Steps.md)
* [Building from Source](Build-from-Source.md)
* [Importing the Seed Database](Import-the-Seed-Database.md)
* [Deploying the Web Application](Deploying.md)
* [Loading a Sample Study](Load-Sample-Cancer-Study.md)

### 2.2 Authorization and Authentication
* [User Authorization](User-Authorization.md)
* [Authenticating Users via SAML](Authenticating-Users-via-SAML.md)
* [Authenticating Users via LDAP](Authenticating-Users-via-LDAP.md)
* [Authenticating and Authorizing Users via Keycloak](Authenticating-and-Authorizing-Users-via-keycloak.md)

### 2.3 Customization 
* [Customizing your cBioPortal Instance via portal.properties](Customizing-your-instance-of-cBioPortal.md)
* [More portal.properties Settings](portal.properties-Reference.md)

### 2.4 Docker
* [Docker Prerequisites](Docker-Prerequisites.md)
* [Deploy using Docker](Deploy-Using-Docker.md)
* [Uninstall Docker cBioPortal](Uninstall-Docker-cBioPortal.md)
* [Import a Study Using Docker](Import-Study-Using-Docker.md)

## 3. cBioPortal Maintenance
* [Updating your cBioPortal Database Scheme](Updating-your-cBioPortal-installation.md)
* [Update genes and gene aliases](Updating-gene-and-gene_alias-tables.md)

## 4. Development      
* [cBioPortal Entity-relationship Diagram](cBioPortal-ER-Diagram.md)
* [Using the Data Manager to Get Data and Set Session Parameters](Data-Manager.md)
* [Accessing Services via the Web-API](cBioPortal-Web-API.md)
   * [API and API Client](The-API-and-API-Client-[Beta].md)
* [Providing cBioPortal Parameters](providing-cBioPortal-parameters.md)
* [Manual test cases](manual-test-cases.md)

## 5. Data Loading
### 5.1 Data Loading
* [Data Loading Overview](Data-Loading.md)
   * [Using the Dataset Validator](Using-the-dataset-validator.md)
   * [Using the metaImport Script](Using-the-metaImport-script.md)
   * [File Formats](File-Formats.md)
       * [Z-Score Normalization](Z-Score-normalization-script.md)
   * [Maintaining Studies](Data-Loading-Maintaining-Studies.md)
   * [For Developers](Data-Loading-For-Developers.md)
   * [Tips and Best Practices](Data-Loading-Tips-and-Best-Practices.md)
   * [Importing Gene Sets for Gene Set Scoring Data](Import-Gene-Sets.md)
   * [Importing Gene Panels](Import-Gene-Panels.md)
   * [Study View Customization with Priority Data](Study-View.md)

### 5.2 Datasets
* [Downloads](Downloads.md)

