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

* [Architecture overview](Architecture-Overview.md)
* [Hardware Requirements](Hardware-Requirements.md)

### 2.1.1 Deploy with Docker (Recommended)
* [Deploy with Docker](docker/README.md)
* [Import data with Docker](docker/import_data.md)
* [Example Commands](docker/example_commands.md)
* [Authenticating and Authorizing Users using Keycloak in Docker](docker/using-keycloak.md)

### 2.1.2 Deploy without Docker
* [Software Requirements](Software-Requirements.md)
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
* [Authenticating Users via Tokens](Authenticating-Users-via-Tokens.md)

### 2.3 Customization 
* [Customizing your cBioPortal Instance via portal.properties](Customizing-your-instance-of-cBioPortal.md)
* [More portal.properties Settings](portal.properties-Reference.md)
* [Configuring Caching Behavior](Caching.md)

### 2.4 Integration with Other Webservices
* [OncoKB Data Access](OncoKB-Data-Access.md)

## 3. cBioPortal Maintenance
* [Updating your cBioPortal Database Scheme](Updating-your-cBioPortal-installation.md)
* [Update genes and gene aliases](Updating-gene-and-gene_alias-tables.md)

## 4. Development      
* [cBioPortal Entity-relationship Diagram](cBioPortal-ER-Diagram.md)
* [Build cBioPortal with a different frontend version](Build-Different-Frontend.md)
* [Manual test cases](manual-test-cases.md)
* [Release Procedure](Release-Procedure.md)
* [Deployment Procedure](Deployment-Procedure.md)

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

## 6. Web API and Clients
 * [API and API Clients](API-and-API-Clients.md)
