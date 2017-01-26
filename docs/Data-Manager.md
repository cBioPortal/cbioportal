The Data Manager is designed to handle all the data needs of a query session, as well as the OncoQueryLanguage (OQL) functionality and properties. As of now it is only a place to get data, but soon it will also be a place to set session parameters. It is accessible via the global property window.PortalDataManager.

## Methods ##
### getOQLQuery():String ###
Returns the active OQL query.

### getQueryGenes():Array[String] ###
Returns the genes in the active OQL query.

### getGeneticProfileIds():Array[String]###
Returns the genetic profile ids currently queried.

### getSampleIds():Array[String] ###
Returns the sample ids currently queried.

### getCancerStudyIds():Array[String] ###
Returns the cancer studies currently queried.

### getGenomicEventData():Promise[Array[Object]] ###
Returns oncoprint-formatted data (always having the properties 'sample' and 'gene', sometimes having the properties 'mutation', 'mut_type', 'cna', 'prot', and 'na', among potentially others (this list may not be up to date)).

### getAlteredSamples():Promise[Array[String]] ###
Returns the ids of altered samples among those currently queried.

### getUnalteredSamples():Promise[Array[String]] ###
Returns the ids of unaltered samples among those currently queried.

### getSampleClinicalAttributes():Promise[Array[Object]] ###
Returns a list of the clinical attributes for which there is data for the queried samples (see [The API and API Client](The-API-and-API-Client-%5BBeta%5D.md#sample-clinical-attributes) for the form of the returned objects).

### getSampleClinicalData(Array[String] attribute_ids):Promise[Array[Object]] ###
Returns sample clinical data for the given attribute ids for the currently queried samples (see [The API and API Client](The-API-and-API-Client-%5BBeta%5D.md#sample-clinical-data) for the form of the returned objects).