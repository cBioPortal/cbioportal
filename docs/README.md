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
* [Loading a Sample Study](Load-Sample-Cancer-Study.md)  
* [Deploying the Web Application](Deploying.md)

### 2.2 Authorization and Authentication
* [User Authorization](User-Authorization.md)
* [Authenticating Users via SAML](Authenticating-Users-via-SAML.md)

### 2.3 Customization 
* [Customizing your cBioPortal Instance via portal.properties](Customizing-your-instance-of-cBioPortal.md)
* [More portal.properties Settings](portal.properties-Reference.md)

### 2.4 Docker
* [Introduction to Docker](Docker-Introduction.md)
* [Deploying cBioPortal in Docker](Build-from-Docker.md)

## 3. cBioPortal Maintenance
* [Updating your cBioPortal Database Scheme](Updating-your-cBioPortal-installation.md)

## 4. Development      
* [cBioPortal Entity-relationship Diagram](cBioPortal-ER-Diagram.md)
* [Using the Data Manager to Get Data and Set Session Parameters](Data-Manager.md)
* [Accessing Services via the Web-API](cBioPortal-Web-API.md)
   * [API and API Client](The-API-and-API-Client-[Beta].md)
* [Providing cBioPortal Parameters](providing-cBioPortal-parameters.md)

## 5. Data Loading
### 5.1 Data Loading
* [Data Loading Overview](Data-Loading.md)
   * [Using the Dataset Validator](Using-the-dataset-validator.md)
   * [Using the metaImport Script](Using-the-metaImport-script.md)
   * [Development, Debugging and Maintenance Using cbioportalImporter](Development,-debugging-and-maintenance-mode-using-cbioportalImporter.md)
   * [File Formats](File-Formats.md)
       * [Z-Score Normalization](Z-Score-normalization-script.md)
   * [Data Loading: How the loader deals with Hugo Symbols](Data-Loading-How-the-loader-deals-with-Hugo-symbols.md) (TODO)    
   * [Data Loading: What You Need To Change](Data-Loading-What-You-Need-To-Change.md)
   * [Data Loading: Tips and Best Practices](Data-Loading-Tips-and-Best-Practices.md)
   * [Importer Tool](Importer-Tool.md) (deprecated)

### 5.2 Datasets
* [Downloads](Downloads.md)
* [Data Examples](Data-Examples.md)
* [Public Datasets](Public-datasets.md)

