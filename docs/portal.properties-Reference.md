This page describes the main properties within portal.properties.

- [Database Settings](#database-settings)
- [cBioPortal Customization](#cbioportal-customization)
- [Ensembl transcript lookup URL](#ensembl-transcript-lookup)
- [Segment File URL](#segment-file-url)
- [Bitly API Username and Key](#bitly-api-username-and-key)
- [Google Analytics](#google-analytics)
- [Password Authentication](#password-authentication)
- [OncoKB integration](#oncokb-integration)
- [CIViC integration](#civic-integration)
- [OncoPrint](#oncoprint)
- [Custom annotation of driver and passenger mutations](#custom-annotation-of-driver-and-passenger-mutations)
	- [Enabling custom annotations in the OncoPrint](#enabling-custom-annotations-in-the-oncoprint)
	- [Automatic selection of custom annotations](#automatic-selection-of-custom-annotations)
	- [Automatic selection of OncoKB annotations](#automatic-selection-of-oncokb-annotations)
	- [Automatic hiding of putative passenger mutations](#automatic-hiding-of-putative-passenger-mutations)
- [Gene sets used for gene querying](#gene-sets-used-for-gene-querying)
- [Ehcache Settings](#ehcache-settings)
- [Enable GSVA functionality](#enable-gsva-functionality)

# Database Settings

```
db.user=
db.password=
db.host=[e.g. localhost to connect via socket, or e.g. 127.0.0.1:3307 to connect to a different port like 3307. Used by Java data import layer]
db.portal_db_name=[the database name in mysql, e.g. cbiodb]
db.driver=[this is the name of your JDBC driver, e.g., com.mysql.jdbc.Driver]
```

Include `db_connection_string` with the format specified below, and replace `localhost` by the value of `db.host`:
```
db.connection_string=jdbc:mysql://localhost/
```

db.tomcat_resource_name is required in order to work with the tomcat database connection pool and should have the default value jdbc/cbioportal in order to work correctly with the your WAR file.
```
db.tomcat_resource_name=jdbc/cbioportal
```

# cBioPortal Customization

## Hide tabs (pages)
Settings controlling which tabs (pages) to hide. Set them to `false` if you want to hide those tabs, otherwise set the properties to `true`.

```
skin.show_data_tab=
skin.show_web_api_tab=
skin.show_r_matlab_tab=
skin.show_tutorials_tab=
skin.show_faqs_tab=
skin.show_news_tab=
skin.show_tools_tab=
skin.show_about_tab=
```

**Note:** `skin.show_tools_tab` refers to the `Visualize Your Data` tab, while `skin.show_data_tab` refers to the `Data Sets` tab.

## Cross Cancer Study Query Default
The cross cancer study query default is a list of studies used when querying
one or more genes and not specifying a specific study or list of studies. There
are two ways in which the default cross cancer study list is used:

1. When using the linkout links without a study e.g. `/ln?q=TP53:MUT`. Those
   links are used mostly used to allow for easy linking to particular queries.
   One can't get those links using the cBioPortal user interface itself, they
   are only mentioned in the documentation of the Web API
   (https://www.cbioportal.org/webAPI).
2. In the quick search when querying for a gene. Quick search is
   disabled by default. It is a beta feature. See the
   [quick search documentation](#quick-search-beta).

The configuration is set with the following if you have session service
enabled:

```
default_cross_cancer_study_session_id=
```

The title will be pulled from the virtual study. Make sure to create a
`virtual_study` with studies that everybody has access to and don't use a
`main_session` id.

If session service is disabled one can use the following instead:

```
# query this comma separated list of studies
default_cross_cancer_study_list=
default_cross_cancer_study_list_name=
```

## Quick Search (BETA)
![Quick search example](images/previews/quick_search_example.png)

Enable or disable the quick search with the following:

```
# Enable/Disable quick search (default is false)
quick_search.enabled=true
```

The default studies queried when searching for a single gene is defined with
the `default_cross_cancer_study_session_id` or
`default_cross_cancer_study_list` properties as described in the
[cross cancer study query default section](#cross-cancer-study-query-default).


## Hide sections in the right navigation bar
Settings controlling what to show in the right navigation bar. Set them to `false` if you want to hide those sections, otherwise set the properties to `true`.

```
#Cancer Studies section:
skin.right_nav.show_data_sets=

#Example Queries section:
skin.right_nav.show_examples=

#Testimonials section:
skin.right_nav.show_testimonials=

#What's New section
skin.right_nav.show_whats_new=
```

## Control the content of specific sections
Setting controlling the blurb: you can add any HTML code here that you want to visualize. This will be shown between the cBioPortal menu and the Query selector in the main page.
```
skin.blurb=
```

Setting controlling the citation below the blurb: you can add any HTML code here that you want to visualize. If the field is left empty, this HTML code will be shown: `Please cite: <a href="https://cancerdiscovery.aacrjournals.org/content/2/5/401.abstract" target="_blank">Cerami et al., 2012</a> &amp; <a href="https://www.ncbi.nlm.nih.gov/pubmed/23550210" target="_blank">Gao et al., 2013</a>`).
```
skin.citation_rule_text=
```

Setting controlling the footer: you can add any HTML code here that you want to visualize. If the field is left empty, the default footer (from www.cbioportal.org) will be shown.
```
skin.footer=
```

Settings controlling the "What's New" blurb in the right navigation bar: you can add any HTML code here that you want to visualize. If the field is left empty, the Twitter timeline will be shown (as long as `skin.right_nav.show_whats_new` is `true`, otherwise this section will not be displayed).

```
skin.right_nav.whats_new_blurb=
```

Add a custom logo in the right side of the menu. Place here the full name of the logo file (e.g. `logo.png`). This file should be saved in `$PORTAL_HOME/portal/images/`.
```
skin.right_logo=
```

## Control default setting for filtering of genes in mutation and CNA tables of patient view
Different samples of a patient may have been analyzed with different gene panels. In patient view mutations and discrete CNA's can be filtered based on whether the gene of respective mutations/CNA's was profiled in all samples of the patient (mutations profiled in `all samples`), or not (mutations profiled in `any sample`). Setting this field to `true` will make patient view select the `all samples` filter at startup. When set to false or left blank the patient view will default to the `any samples` filter setting.
```
skin.patientview.filter_genes_profiled_all_samples=
```
# Ensembl transcript lookup URL
The Mutations tab contains various links, redirecting the user to external information resources regarding the displayed transcript. The Ensembl template URL can be customized by modifying the property:
```
ensembl.transcript_url=
```
The default setting is `https://ensembl.org/homo_sapiens/Transcript/Summary?t=<%= transcriptId %>`. The `<%= transcriptId %>` is substituted by the frontend code into respective transcript ID.


# Segment File URL

This is a root URL to where segment files can be found.  This is used when you want to provide segment file viewing via external tools such as [IGV](https://www.broadinstitute.org/igv/).

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

To activate password authentication follow the [Deployment with authentication
steps](Deploying.md#required-login) and set `authenticate=googleplus`.

In addition, set this property in `portal.properties`:
```
app.name=cbioportal
```
app.name should be set to the name of the portal instance referenced in the "AUTHORITY" column of the "AUTHORITIES" table.  See the [User Authorization](User-Authorization.md) for more information.

# OncoKB integration

OncoKB integration can be turned on or off with the following property (default: true):
```
show.oncokb=true|false
```
A private token is required to access the OncoKB Data (for details see the section [OncoKB Data Access](OncoKB-Data-Access.md)):
```
oncokb.token=
```

# CIViC integration

CIViC integration can be turned on or off with the following property (default: true):
```
show.civic=true|false
```
The CIViC API url is set to https://civic.genome.wustl.edu/api/ by default. It can be overridden using the following property:
```
civic.url=
```

# Genome Nexus Integration
Genome Nexus provides annotations of mutations in cBioPortal. The mutations tab relies heavily on the Genome Nexus API, therefore that tab won't work well without it. By default cBioPortal will use the public Genome Nexus API, such that no extra configuration is necessary.

## Genome Build
Genome Nexus supports both GRCh37 and GRCh38, but support for the latter is limited. Several annotation sources served by Genome Nexus might not have official GRCh38 support yet i.e. [OncoKB](https://www.oncokb.org/), [CIViC](https://civicdb.org/), [Cancer Hotspots](https://www.cancerhotspots.org/), [My Cancer Genome](https://www.mycancergenome.org/) and [3D structures](https://g2s.genomenexus.org/). Although most of the time the canonical transcript for a gene will be the same between GRCh37 and GRCh38 there might be some that cause issues. In addition the complete integration of cBioPortal with Genome Nexus' GRCh38 is not complete yet. That is cBioPortal currently only connects to one Genome Nexus API by default (the GRCh37 one), so it's not possible to have multiple genome builds in one instance of cBioPortal and get the correct annotations from Genome Nexus for both. Currently only the [mutation mapper tool page](https://www.cbioportal.org/mutation_mapper) is able to handle both.

## Properties
By default the Genome Nexus API url is set to https://v1.genomenexus.org/, which uses GRCh37. It can be overridden using the following property:

```
genomenexus.url=
```

The mutation mapper tool page can annotate GRCh38 coordinates. By default it uses https://grch38.genomenexus.org. It can be overridden by setting:

```
genomenexus.url.grch38=
```

The GRCh38 annotation in mutation mapper can be hidden by setting `show.mutation_mappert_tool.grch38=false`, by default it's set to `true`;

# MDACC Heatmap Integration

MDACC Heatmap integration (button in OncoPrint heatmap dropdown and tab on Study page can be turned on or off by setting the following property:
```
show.mdacc.heatmap=true
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

#### Automatic selection of OncoKB, hotspots and custom annotations
OncoKB and Hotspots are by default automatically selected as annotation source, if `show.oncokb` and `show.hotspots` are set to `true`. To add automatic selection of custom driver or custom driver tiers annotations, set the respective property to `true`. Default is `false`.
```
oncoprint.custom_driver_annotation.binary.default=true|false
oncoprint.custom_driver_annotation.tiers.default=true|false
```

If you want to disable the automatic selection of OncoKB and hotspots as annotation source, set these properties to `false`:
```
oncoprint.oncokb.default=true|false
oncoprint.hotspots.default=true|false
``` 

#### Automatic hiding of variants of unknown significance (VUS)
By default, the selection box to hide VUS mutations is unchecked. If you want to automatically hide VUS, set this property to `true`. Default is `false`.
```
oncoprint.hide_vus.default=true|false
```

# Gene sets used for gene querying
To change the gene sets used for gene querying, create a JSON file and add gene sets, following the format specified in the examples below. Set the path to this file (e.g. `file:/cbioportal/custom_gene_sets.json`) in the following property and restart Tomcat to apply the update. The default gene sets will be replaced by the ones in `custom_gene_sets.json`.
```
querypage.setsofgenes.location=file:/<path>
```

## Example with gene names
In this example, two gene sets will appear in the query page, under the names "Prostate Cancer: AR Signaling" and "Prostate Cancer: AR and steroid synthesis enzymes".
```
[{
	"id": "Prostate Cancer: AR Signaling",
	"genes": ["SOX9", "RAN", "TNK2", "EP300", "PXN", "NCOA2", "AR", "NRIP1", "NCOR1", "NCOR2"]
}, {
	"id": "Prostate Cancer: AR and steroid synthesis enzymes",
	"genes": ["AKR1C3", "AR", "CYB5A", "CYP11A1", "CYP11B1", "CYP11B2", "CYP17A1", "CYP19A1", "CYP21A2", "HSD17B1", "HSD17B10", "HSD17B11", "HSD17B12", "HSD17B13", "HSD17B14", "HSD17B2", "HSD17B3", "HSD17B4", "HSD17B6", "HSD17B7", "HSD17B8", "HSD3B1", "HSD3B2", "HSD3B7", "RDH5", "SHBG", "SRD5A1", "SRD5A2", "SRD5A3", "STAR"]
}]
```

## Example with specific alterations
In this example, only one gene set will appear in the query page, under the name "Genes with alterations", which will add the different genetic alterations stated below in the query box.
```
[{
	"id": "Genes with alterations",
	"genes": ["TP53: MUT=R273C;", "KRAS: HOMDEL MUT=NONSENSE MUT=NONSTART MUT=NONSTOP MUT=FRAMESHIFT MUT=SPLICE MUT=TRUNC;"]
}]
```

## Example with merged gene tracks
In this example, only one gene set will appear in the query page, under the name "BRCA genes test", containing the merged gene track called "BRCA genes".
```
[{
	"id": "BRCA genes test",
	"genes": ["[\\\"BRCA genes\\\" BRCA1: MUT=E1258D;", "BRCA2: HOMDEL MUT=NONSENSE MUT=NONSTART MUT=NONSTOP MUT=FRAMESHIFT MUT=SPLICE MUT=TRUNC;]"]
}]
```

This gene set will add the following in the query box:
```
"BRCA genes" BRCA1: MUT=E1258D; BRCA2: HOMDEL MUT=NONSENSE MUT=NONSTART MUT=NONSTOP MUT=FRAMESHIFT MUT=SPLICE MUT=TRUNC;
```
# Ehcache Settings
cBioPortal is supported on the backend with Ehcache. The configuration, size, and location of these caches are configurable from within portal.properties through the following properties.

The cache type is set using `ehcache.cache_type`. Valid values are `none`, `heap` (heap-only), `disk` (disk-only), and `hybrid` (disk + heap). By default, `ehcache.cache_type` is set to `none` which disables the cache. When the cache is disabled, no responses will be stored in the cache. 
```
ehcache.cache_type=[none or heap or disk or hybrid]
```

Ehcache initializes caches using a template found in an Ehcache xml configuration file. When caching is enabled, set `ehcache.xml_configuration` to the name of the Ehcache xml configuration file. The default provided is `ehcache.xml`; to change the cache template, directly edit this file. Alternatively, you can create your own Ehcache xml configuration file, place it under `/persistence/persistence-api/src/main/resources/` and set `ehcache.xml_configuration` to `/[Ehcache xml configuration filename]`.  
```
ehcache.xml_configuration=
```

If the cache is configured to use disk resources, users must make a directory available and set it with the `ehcache.persistence_path` property. Ehcache will create separate directories under the provided path for each cache defined in the ehcache.xml_configuration file. 
```
ehcache.persistence_path=[location on the disk filesystem where Ehcache can write the cache to /tmp/]
```

Cache size must be set for heap and/or disk depending on which are in use; Ehcache requires disk size to be greater than heap size in a hybrid configuration. Zero is not a supported size and will cause an exception. Units are in megabytes. Default values are provided. The general repository cache is specified to use 1024MB of heap and 4096MB of disk. The static repository cache is specified to use 30MB of heap and 32MB of disk. For installations with increased traffic or data, cache sizes can be increased to further improve performance. 
```
ehcache.general_repository_cache.max_mega_bytes_heap=
ehcache.general_repository_cache.max_mega_bytes_local_disk=

ehcache.static_repository_cache_one.max_mega_bytes_heap=
ehcache.static_repository_cache_one.max_mega_bytes_local_disk=
```

Logged metrics and additional information such as cache size and cached keys are available through an optional endpoint. The optional endpoint is turned off by default but can be turned on by setting `cache.statistics_endpoint_enabled` to true.
```
cache.statistics_endpoint_enabled=false[true or false]
```
The cache statistics endpoint is hidden on the api page; users must directly access the URL to view the response. The cache statistics endpoint can be accessed in the following ways.

For general statistics about the cache such as memory usage:
```
/api/cacheStatistics
```

For a list of all keys in the cache:
```
/api/[name of cache]/keysInCache
```

For a list of counts of keys in cache per repository class:
```
/api/[name of cache]/keyCountsPerClass
```

**WARNING**: It must be noted that since cache statistics endpoint returns data on cache keys, the endpoint may expose otherwise hidden database query parameters such as sample identifiers, study names, etc. Generally, it is recommended that the endpoint only be turned on during cache-related development for testing. Deployers of a protected portal where users only have authorities to a subset of studies should carefully consider whether or not to turn on the cache statistics endpoint, as it does not filter the results. 

For more information on Ehcache, refer to the official documentation [here](https://www.ehcache.org/documentation/3.7/index.html)

For more information on how Ehcache is implemented in cBioPortal refer to the [Caching](Caching.md) documentation.

# Enable GSVA functionality

[GSVA functionality](https://github.com/cBioPortal/cbioportal/blob/master/docs/File-Formats.md#gene-set-data) can be enabled by uncommenting this line (and making sure it is set to `true`): 
```
skin.show_gsva=true
```