## Current Web API

The current Web API offers services through the servlet `webservice.do`. It is described on [the `web_api.jsp` webpage](http://www.cbioportal.org/web_api.jsp), and responds to these commands: getTypesOfCancer getCancerStudies getGeneticProfiles getCaseLists getCaseLists getMutationData getClinicalData getProteinArrayInfo getProteinArrayData.

## Revised Web API (**In Development**)
There is a new Web API in development. A few of these functions are currently in use in cBioPortal, but the API is currently being expanded and revised. Below is a summary of the API captured with OpenAPI/swagger annotation (generated using tools from [the SpringFox project](http://springfox.github.io/springfox/) and [the Swagger2Markup project](https://github.com/Swagger2Markup)). There is also a springfox swagger-ui [webpage](http://www.cbioportal.org/beta/swagger-ui.html).

### Sections
* [Overview](#Overview)
* [Paths](#Paths)
* [Definitions](#Definitions)
* [Upcoming](#Upcoming)

## <a name="Overview">Overview</a>
A web service for supplying JSON formatted data to cBioPortal clients.

### Version information
Version: 1.0 (beta)

### Contact information
Contact: cbioportal@googlegroups.com

### License information
License: License
License URL: https://github.com/cBioPortal/cbioportal/blob/master/LICENSE

Terms of service: www.cbioportal.org

### URI scheme
Host: cbioportal.org
BasePath: /

### Tags

* api-controller: Api Controller


## <a name="Paths">Paths</a>
### Get cancer types with meta data
```
GET /api/cancertypes
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|cancer_type_ids|List of cancer type identifiers (example: cll,brca,coad). Unrecognized ids are silently ignored. Empty string returns all.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBCancerType](#DBCancerType) array|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get cancer types with meta data
```
POST /api/cancertypes
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|cancer_type_ids|List of cancer type identifiers (example: cll,brca,coad). Unrecognized ids are silently ignored. Empty string returns all.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBCancerType](#DBCancerType) array|
|201|Created|No Content|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get clinical attribute identifiers, filtered by patient
```
GET /api/clinicalattributes/patients
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|study_id|A single study id, such as those returned by /api/studies. (example: brca_tcga). Empty string returns clinical attributes across all studies.|false|string||
|QueryParameter|patient_ids|List of patient_ids. If provided, returned clinical attributes will be those which appear in any listed patient. Empty string returns clinical attributes across all patients.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBClinicalField](#DBClinicalField) array|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get clinical attribute identifiers, filtered by patient
```
POST /api/clinicalattributes/patients
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|study_id|A single study id, such as those returned by /api/studies. (example: brca_tcga). Empty string returns clinical attributes across all studies.|false|string||
|QueryParameter|patient_ids|List of patient_ids. If provided, returned clinical attributes will be those which appear in any listed patient. Empty string returns clinical attributes across all patients.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBClinicalField](#DBClinicalField) array|
|201|Created|No Content|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get clinical attribute identifiers, filtered by sample
```
GET /api/clinicalattributes/samples
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|study_id|A single study id, such as those returned by /api/studies. (example: brca_tcga). Empty string returns clinical attributes across all studies.|false|string||
|QueryParameter|sample_ids|List of sample_ids. If provided, returned clinical attributes will be those which appear in any listed sample. Empty string returns clinical attributes across all samples.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBClinicalField](#DBClinicalField) array|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get clinical attribute identifiers, filtered by sample
```
POST /api/clinicalattributes/samples
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|study_id|A single study id, such as those returned by /api/studies. (example: brca_tcga). Empty string returns clinical attributes across all studies.|false|string||
|QueryParameter|sample_ids|List of sample_ids. If provided, returned clinical attributes will be those which appear in any listed sample. Empty string returns clinical attributes across all samples.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBClinicalField](#DBClinicalField) array|
|201|Created|No Content|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get clinical data records filtered by patient ids
```
GET /api/clinicaldata/patients
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|study_id|A single study id, such as those returned by /api/studies. (example: brca_tcga)|true|string||
|QueryParameter|attribute_ids|List of attribute ids, such as those returned by /api/clinicalattributes/patients. (example: PATIENT_ID,DFS_STATUS)|true|multi string array||
|QueryParameter|patient_ids|List of patient identifiers such as those returned by /api/patients. Unrecognized ids are silently ignored. Empty string returns all.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBClinicalPatientData](#DBClinicalPatientData) array|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get clinical data records filtered by patient ids
```
POST /api/clinicaldata/patients
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|study_id|A single study id, such as those returned by /api/studies. (example: brca_tcga)|true|string||
|QueryParameter|attribute_ids|List of attribute ids, such as those returned by /api/clinicalattributes/patients. (example: PATIENT_ID,DFS_STATUS)|true|multi string array||
|QueryParameter|patient_ids|List of patient identifiers such as those returned by /api/patients. Unrecognized ids are silently ignored. Empty string returns all.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBClinicalPatientData](#DBClinicalPatientData) array|
|201|Created|No Content|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get clinical data records, filtered by sample ids
```
GET /api/clinicaldata/samples
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|study_id|A single study id, such as those returned by /api/studies. (example: brca_tcga)|true|string||
|QueryParameter|attribute_ids|List of attribute ids, such as those returned by /api/clinicalattributes/samples. (example: SAMPLE_TYPE,IS_FFPE)|true|multi string array||
|QueryParameter|sample_ids|List of sample identifiers. Unrecognized ids are silently ignored. Empty string returns all.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBClinicalSampleData](#DBClinicalSampleData) array|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get clinical data records, filtered by sample ids
```
POST /api/clinicaldata/samples
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|study_id|A single study id, such as those returned by /api/studies. (example: brca_tcga)|true|string||
|QueryParameter|attribute_ids|List of attribute ids, such as those returned by /api/clinicalattributes/samples. (example: SAMPLE_TYPE,IS_FFPE)|true|multi string array||
|QueryParameter|sample_ids|List of sample identifiers. Unrecognized ids are silently ignored. Empty string returns all.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBClinicalSampleData](#DBClinicalSampleData) array|
|201|Created|No Content|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get gene meta data by hugo gene symbol lookup
```
GET /api/genes
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|hugo_gene_symbols|List of hugo gene symbols. Unrecognized genes are silently ignored. Empty string returns all genes.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBGene](#DBGene) array|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get gene meta data by hugo gene symbol lookup
```
POST /api/genes
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|hugo_gene_symbols|List of hugo gene symbols. Unrecognized genes are silently ignored. Empty string returns all genes.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBGene](#DBGene) array|
|201|Created|No Content|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get genetic profile data across samples for given genes, and filtered by sample id or sample list id
```
GET /api/geneticprofiledata
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|genetic_profile_ids|List of genetic_profile_ids such as those returned by /api/geneticprofiles. (example: brca_tcga_pub_mutations). Unrecognized genetic profile ids are silently ignored. Profile data is only returned for matching ids.|true|multi string array||
|QueryParameter|genes|List of hugo gene symbols. (example: AKT1,CASP8,TGFBR1) Unrecognized gene ids are silently ignored. Profile data is only returned for matching genes.|true|multi string array||
|QueryParameter|sample_ids|List of sample identifiers such as those returned by /api/samples. Empty string returns all. Must be empty to query by sample list ids.|false|multi string array||
|QueryParameter|sample_list_id|A single sample list ids such as those returned by /api/samplelists. (example: brca_tcga_idc,brca_tcga_lobular). Empty string returns all. If sample_ids argument was provided, this argument will be ignored.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBProfileData](#DBProfileData) array|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get genetic profile data across samples for given genes, and filtered by sample id or sample list id
```
POST /api/geneticprofiledata
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|genetic_profile_ids|List of genetic_profile_ids such as those returned by /api/geneticprofiles. (example: brca_tcga_pub_mutations). Unrecognized genetic profile ids are silently ignored. Profile data is only returned for matching ids.|true|multi string array||
|QueryParameter|genes|List of hugo gene symbols. (example: AKT1,CASP8,TGFBR1) Unrecognized gene ids are silently ignored. Profile data is only returned for matching genes.|true|multi string array||
|QueryParameter|sample_ids|List of sample identifiers such as those returned by /api/samples. Empty string returns all. Must be empty to query by sample list ids.|false|multi string array||
|QueryParameter|sample_list_id|A single sample list ids such as those returned by /api/samplelists. (example: brca_tcga_idc,brca_tcga_lobular). Empty string returns all. If sample_ids argument was provided, this argument will be ignored.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBProfileData](#DBProfileData) array|
|201|Created|No Content|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get list of genetic profile identifiers by study
```
GET /api/geneticprofiles
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|study_id|A single study id, such as those returned by /api/studies. (example: brca_tcga). Must be empty to query by genetic profile ids (across all studies).|false|string||
|QueryParameter|genetic_profile_ids|List of genetic_profile_ids. (example: brca_tcga_pub_mutations). Empty string returns all genetic profiles. If study_id argument was provided, this argument will be ignored.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBGeneticProfile](#DBGeneticProfile) array|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get list of genetic profile identifiers by study
```
POST /api/geneticprofiles
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|study_id|A single study id, such as those returned by /api/studies. (example: brca_tcga). Must be empty to query by genetic profile ids (across all studies).|false|string||
|QueryParameter|genetic_profile_ids|List of genetic_profile_ids. (example: brca_tcga_pub_mutations). Empty string returns all genetic profiles. If study_id argument was provided, this argument will be ignored.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBGeneticProfile](#DBGeneticProfile) array|
|201|Created|No Content|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get patient id list by study or by sample id
```
GET /api/patients
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|study_id|A single study id, such as those returned by /api/studies. (example: brca_tcga)|true|string||
|QueryParameter|patient_ids|List of patient ids such as those returned by /api/patients. Empty string returns all. Must be empty to query by sample ids.|false|multi string array||
|QueryParameter|sample_ids|List of sample identifiers. Empty string returns all. If patient_ids argument was provided, this argument will be ignored.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBPatient](#DBPatient) array|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get patient id list by study or by sample id
```
POST /api/patients
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|study_id|A single study id, such as those returned by /api/studies. (example: brca_tcga)|true|string||
|QueryParameter|patient_ids|List of patient ids such as those returned by /api/patients. Empty string returns all. Must be empty to query by sample ids.|false|multi string array||
|QueryParameter|sample_ids|List of sample identifiers. Empty string returns all. If patient_ids argument was provided, this argument will be ignored.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBPatient](#DBPatient) array|
|201|Created|No Content|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get list of sample lists (list name and sample id list) by study
```
GET /api/samplelists
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|study_id|A single study id, such as those returned by /api/studies. (example: brca_tcga). Must be empty to query by sample list ids (across all studies).|false|string||
|QueryParameter|sample_list_ids|List of sample list ids. (example: brca_tcga_idc,brca_tcga_lobular). Empty string returns all genetic profiles. If study_id argument was provided, this argument will be ignored.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBSampleList](#DBSampleList) array|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get list of sample lists (list name and sample id list) by study
```
POST /api/samplelists
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|study_id|A single study id, such as those returned by /api/studies. (example: brca_tcga). Must be empty to query by sample list ids (across all studies).|false|string||
|QueryParameter|sample_list_ids|List of sample list ids. (example: brca_tcga_idc,brca_tcga_lobular). Empty string returns all genetic profiles. If study_id argument was provided, this argument will be ignored.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBSampleList](#DBSampleList) array|
|201|Created|No Content|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get list of samples ids with meta data by study, filtered by sample ids or patient ids
```
GET /api/samples
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|study_id|A single study id, such as those returned by /api/studies. (example: brca_tcga)|true|string||
|QueryParameter|sample_ids|List of sample identifiers. Unrecognized ids are silently ignored. Empty string returns all. Must be empty to query by patient_ids.|false|multi string array||
|QueryParameter|patient_ids|List of patient identifiers such as those returned by /api/patients. Unrecognized ids are silently ignored. Empty string returns all. If sample_ids argument was provided, this arument will be ignored.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBSample](#DBSample) array|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get list of samples ids with meta data by study, filtered by sample ids or patient ids
```
POST /api/samples
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|study_id|A single study id, such as those returned by /api/studies. (example: brca_tcga)|true|string||
|QueryParameter|sample_ids|List of sample identifiers. Unrecognized ids are silently ignored. Empty string returns all. Must be empty to query by patient_ids.|false|multi string array||
|QueryParameter|patient_ids|List of patient identifiers such as those returned by /api/patients. Unrecognized ids are silently ignored. Empty string returns all. If sample_ids argument was provided, this arument will be ignored.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBSample](#DBSample) array|
|201|Created|No Content|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get studies
```
GET /api/studies
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|study_ids|List of study_ids. Unrecognized ids are silently ignored. Empty string returns all.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBStudy](#DBStudy) array|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

### Get studies
```
POST /api/studies
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|study_ids|List of study_ids. Unrecognized ids are silently ignored. Empty string returns all.|false|multi string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|OK|[DBStudy](#DBStudy) array|
|201|Created|No Content|
|401|Unauthorized|No Content|
|403|Forbidden|No Content|
|404|Not Found|No Content|


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

## <a name="Definitions">Definitions</a>
### <a name="DBCancerType">DBCancerType</a>
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|color||false|string||
|id||false|string||
|name||false|string||


### <a name="DBClinicalField">DBClinicalField</a>
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|attr_id||false|string||
|datatype||false|string||
|description||false|string||
|display_name||false|string||
|is_patient_attribute||false|string||
|priority||false|string||


### <a name="DBClinicalPatientData">DBClinicalPatientData</a>
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|attr_id||false|string||
|attr_val||false|string||
|patient_id||false|string||
|study_id||false|string||


### <a name="DBClinicalSampleData">DBClinicalSampleData</a>
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|attr_id||false|string||
|attr_val||false|string||
|sample_id||false|string||
|study_id||false|string||


### <a name="DBGene">DBGene</a>
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|entrez_gene_id||false|string||
|hugo_gene_symbol||false|string||


### <a name="DBGeneticProfile">DBGeneticProfile</a>
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|datatype||false|string||
|description||false|string||
|genetic_alteration_type||false|string||
|id||false|string||
|name||false|string||
|show_profile_in_analysis_tab||false|string||
|study_id||false|string||


### <a name="DBPatient">DBPatient</a>
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id||false|string||
|study_id||false|string||


### <a name="DBProfileData">DBProfileData</a>
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|entrez_gene_id||false|string||
|genetic_profile_id||false|string||
|hugo_gene_symbol||false|string||
|sample_id||false|string||
|sample_list_id||false|string||
|study_id||false|string||


### <a name="DBSample">DBSample</a>
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id||false|string||
|internal_id||false|string||
|patient_id||false|string||
|sample_type||false|string||
|study_id||false|string||


### <a name="DBSampleList">DBSampleList</a>
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|description||false|string||
|id||false|string||
|name||false|string||
|sample_ids||false|string array||
|study_id||false|string||


### <a name="DBStudy">DBStudy</a>
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|citation||false|string||
|description||false|string||
|groups||false|string||
|id||false|string||
|name||false|string||
|pmid||false|integer (int64)||
|short_name||false|string||
|type_of_cancer||false|string||

## <a name="Upcoming">Upcoming</a>
####Services to be rewritten to use the new Web API
* **getProfileData.json** : get genetic alterations for profile, genes
* **getGeneticProfile.json** : get genetic alterations for study, genes
* **GeneData.json** : get data from genetic profiles
* **CheckGeneSymbol.json** : simple gene lookup
* **clinicalAttributes.jso**n : fetch clinical attibute list
* **getAlterationData.json** : lookup set of alterations by study, profile, genes
* **portal_meta_data.json** : Fetch meta data for study
* **clinical_timeline_data.json** : Fetch clinical data for a patient ID
* **ClinicalFreeForm.json** : fetch clinical data based on availability
* **getMutationData.json** : Get mutations across genetic profile (with gene filter)
* **portalMetadata.json** : Get sample profile metadata

####Services to be migrated into additional Web API functionality
* **mutations.json** : Bundle of functions providing study/gene/annotation merges for mutation tab
* **Gistic.json** : lookup gistic entries for a study
* **cna.json** : Bundle of functions providing copy number changes and segment metadata
* **MutSig.json** : Fetch mutsig data for a study
* **getPfamSequence.json** : Fetch pfam alignment strings
* **drugs.json** : Fetch drug info and interactions
* **clinicaltrials.json** : Search for matching clinical trials
* **getSurvivalData.json** : Select survival data for sample set of a study
* **similar_patients.json** : Find.Merge 2 sample sets: similar by mutation, similar by cna
* **pancancerMutations.json** : Bundle of functions to find mutations in several ways
* **get3dPdb.json** : Bundle of functions to get pdb data and related sequence data
* **crosscancerquery.json** : Query by Studies and Genes, return profiles and patients
* **crosscancermutation.json** : Query by Studies and Genes, return patients and mutations
* **igvlinking.json** : Make igv links for study/case/locus
* **getMutationAligner.json** : Create URL links to mutation aligner
* **bioGeneQuery.do** : Create URL links to bioGene
* **oranalysis.do** : Compute p-value for alteration subset in profile, gene
* **getCoExp.do** : Compute co-expression for genes across profiles
* **network.do** : Bundle of functions to return genes and interactions in a study,profile with meta data

####Services to be Deprecated and Dropped eventually
* **webservice.do** : old WEB API requests