This page describes the main properties within portal.properties.

- [Database Settings](#database-settings)
- [Segment File URL](#segment-file-url)
- [Bitly API Username and Key](#bitly-api-username-and-key)
- [Google Analytics](#google-analytics)
- [Password Authentication](#password-authentication)
- [CIViC integration](#civic-integration)
- [OncoPrint](#oncoprint)
- [Custom annotation of driver and passenger mutations](#custom-annotation-of-driver-and-passenger-mutations)
	- [Enabling custom annotations in the OncoPrint](#enabling-custom-annotations-in-the-oncoprint)
	- [Automatic selection of custom annotations](#automatic-selection-of-custom-annotations)
	- [Automatic selection of OncoKB annotations](#automatic-selection-of-oncokb-annotations)
	- [Automatic hiding of putative passenger mutations](#automatic-hiding-of-putative-passenger-mutations)
- [Gene sets used for gene querying](#gene-sets-used-for-gene-querying)

# Database Settings

```
db.user=
db.password=
db.host=[e.g. localhost to connect via socket, or e.g. 127.0.0.1:3307 to connect to a different port like 3307. Used by Java data import layer]
db.portal_db_name=[the database name in myslq, e.g. cbiodb]
db.driver=[this is the name of your JDBC driver, e.g., com.mysql.jdbc.Driver]
db.connection_string=jdbc:mysql://localhost/
db.tomcat_resource_name=jdbc/cbioportal
```
db.tomcat_resource_name is required in order to work with the tomcat database connection pool and should have the default value jdbc/cbioportal in order to work correctly with the your WAR file.

# Segment File URL

This is a root URL to where segment files can be found.  This is used when you want to provide segment file viewing via external tools such as [IGV](http://www.broadinstitute.org/igv/).

```
segfile.url=
```

# Bitly API Username and Key

The following properties are used to provide shortened bookmarks to the cBioPortal:

```
bitly.user=
bitly.api_key=
```

To obtain a bitly username and key, first register at:  https://bitly.com/

Then, go to:  https://bitly.com/a/your_api_key

**Note:**  If you are developing on a local machine, and using localhost, the bitly URL shortening service will not work.  This is because bitly will not shorten URLs for localhost.  Once you deploy to your final server, the issue should go away.

# Google Analytics

If you so desire, the following property is used to track your site's usage via google analytics.
```
google_analytics_profile_id
```

# Password Authentication

The portal supports password authentication via Google+. Before you start you need to setup a google account that will own the authentication API. Follow https://developers.google.com/identity/sign-in/web/devconsole-project to get clientID and secret. Fill it in portal.properties:
```
googleplus.consumer.key=195047654890-499gl89hj65j8d2eorqe0jvjnfaxcln0.apps.googleusercontent.com 
googleplus.consumer.secret=2jCfg4SPWdGfXF44WC588dK
```
(note: these are just examples, you need to get your own) You will also need to go to "Google+ API" and click Enable button. In case of problems make sure to enable DEBUG logging for org.springframework.social and org.springframework.security.web.authentication.


To active password authentication, then the following properties are required:
```
app.name=
authenticate=googleplus
authorization=true
```
app.name should be set to the name of the portal instance referenced in the "AUTHORITY" column of the "AUTHORITIES" table.  See the [User Authorization](User-Authorization.md) for more information.

# CIViC integration

CIViC integration can be turned on or off with the following property (default: true):
```
show.civic=true|false
```
The CIViC API url is set to https://civic.genome.wustl.edu/api/ by default. It can be overridden using the following property:
```
civic.url=
```

# OncoPrint

The default view in OncoPrint ("patient" or "sample") can be set with the following option. The default is "patient".
```
oncoprint.defaultview=sample
```

# Custom annotation of driver and passenger mutations
cBioPortal supports 2 formats to add custom annotations for driver and passenger mutations. 
1. **cbp_driver**: This will define whether a mutation is a driver or not.
2. **cbp_driver_tiers**: This can be used to define multiple classes of driver mutations. 

These data formats are described in the [cBioPortal MAF specifications](File-Formats.md#extending-the-maf-format). 

#### Enabling custom annotations in the OncoPrint
To enable functionality for one or both types of custom annotations, enter values for the following properties. These labels will appear in the OncoPrint's "Mutation color" menu.
```
oncoprint.custom_driver_annotation.binary.menu_label=Custom driver annotation
oncoprint.custom_driver_annotation.tiers.menu_label=Custom driver tiers
```

#### Automatic selection of custom annotations
OncoKB and Hotspots are by default automatically selected as annotation source. To add automatic selection of custom driver or custom driver tiers annotations, set the respective property to `true`. Default is `false`.
```
oncoprint.custom_driver_annotation.default=true|false
oncoprint.custom_driver_tiers_annotation.default=true|false
```

#### Automatic selection of OncoKB annotations
OncoKB and Hotspots are by default automatically selected as annotation source. To disable this, set the following property to `false`. To only select OncoKB and Hotspots when there are no custom driver mutation annotations, set this property to `custom`. Default is `true`.
```
oncoprint.oncokb_hotspots.default=true|false|custom
``` 

#### Automatic hiding of variants of unknown significance (VUS)
By default, the selection box to hide VUS mutations is unchecked. If you want to automatically hide VUS, set this property to `true`. Default is `false`.
```
oncoprint.hide_vus.default=true|false
```

# Gene sets used for gene querying
To change the gene sets used for gene querying, create a JSON file and add gene sets in line with the format of [gene_lists.ts](https://github.com/cBioPortal/cbioportal-frontend/blob/master/src/shared/components/query/gene_lists.ts). The JSON file should contain a structure equal to the part after `const gene_lists = `. Set the path to this file (e.g. `file:/cbioportal/custom_gene_sets.json`) in the following property and restart Tomcat to apply the update.
```
querypage.setsofgenes.location=file:/<path>
```
# Reference Genome information for validating the genomic data
The reference genome information will be used by the importer validation script to ensure the version of Genome Reference Consortium Build is the same version that was used to analyse the genomic data. 
Currently, the portal only supports multiple reference genome builds for single species.

1. Use **species** to indicate the organism of the reference genome. For example, **human** is for Genome Reference Consortium human build 37.
2. Use **genome.name** to indicate the name of reference genome as used by the UCSC browser. For example, **hg19** is the name for Genome Reference Consortium human build 37. 
3. Use **genome.build** to specify the version of Genome Reference Consortium Build as published by the NCBI. For example, **GRCh37** is Genome Reference Consortium Human Build 37.
4. Use a comma to separate multiple genome names and builds. 
5. Each genome name should have a matched genome build.

**species, genome.name, genome.build** properties need to be added or updated in each of the following cases:
1. After reference genomes are imported to the database (details on how to import reference genomes can be found in [this document](import-reference-genome.md))
2. After existing database is migrated to a new schema version using migration.sql (details on how to update your existing cBioportal can be found in [this document](Updating-your-cBioPortal-installation.md)) 
3. After new database is created from a seed database that contains these reference genomes (details on how to load seed databases can be found in [this document](Import-the-Seed-Database.md)), 

Below is the sample reference genome information for a human
```
# species and genomic information
species=human
genome.name=hg19,hg19,hg38
genome.build=37,GRCh37,GRCh38
```