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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>cancer_type_ids</td><td>List of cancer type identifiers (example: cll,brca,coad). Unrecognized ids are silently ignored. Empty string returns all.</td><td>false</td><td>multi string array</td></tr>
</table>

#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBCancerType">DBCancerType</a> array</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>cancer_type_ids</td><td>List of cancer type identifiers (example: cll,brca,coad). Unrecognized ids are silently ignored. Empty string returns all.</td><td>false</td><td>multi string array</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBCancerType">DBCancerType</a> array</td></tr>
<tr><td>201</td><td>Created</td><td>No Content</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>study_id</td><td>A single study id, such as those returned by /api/studies. (example: brca_tcga). Empty string returns clinical attributes across all studies.</td><td>false</td><td>string</td></tr>
<tr><td>QueryParameter</td><td>patient_ids</td><td>List of patient_ids. If provided, returned clinical attributes will be those which appear in any listed patient. Empty string returns clinical attributes across all patients.</td><td>false</td><td>multi string array</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBClinicalField">DBClinicalField</a> array</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>study_id</td><td>A single study id, such as those returned by /api/studies. (example: brca_tcga). Empty string returns clinical attributes across all studies.</td><td>false</td><td>string</td></tr>
<tr><td>QueryParameter</td><td>patient_ids</td><td>List of patient_ids. If provided, returned clinical attributes will be those which appear in any listed patient. Empty string returns clinical attributes across all patients.</td><td>false</td><td>multi string array</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBClinicalField">DBClinicalField</a> array</td></tr>
<tr><td>201</td><td>Created</td><td>No Content</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>study_id</td><td>A single study id, such as those returned by /api/studies. (example: brca_tcga). Empty string returns clinical attributes across all studies.</td><td>false</td><td>string</td></tr>
<tr><td>QueryParameter</td><td>sample_ids</td><td>List of sample_ids. If provided, returned clinical attributes will be those which appear in any listed sample. Empty string returns clinical attributes across all samples.</td><td>false</td><td>multi string array</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBClinicalField">DBClinicalField</a> array</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>study_id</td><td>A single study id, such as those returned by /api/studies. (example: brca_tcga). Empty string returns clinical attributes across all studies.</td><td>false</td><td>string</td></tr>
<tr><td>QueryParameter</td><td>sample_ids</td><td>List of sample_ids. If provided, returned clinical attributes will be those which appear in any listed sample. Empty string returns clinical attributes across all samples.</td><td>false</td><td>multi string array</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBClinicalField">DBClinicalField</a> array</td></tr>
<tr><td>201</td><td>Created</td><td>No Content</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>study_id</td><td>A single study id, such as those returned by /api/studies. (example: brca_tcga)</td><td>true</td><td>string</td></tr>
<tr><td>QueryParameter</td><td>attribute_ids</td><td>List of attribute ids, such as those returned by /api/clinicalattributes/patients. (example: PATIENT_ID,DFS_STATUS)</td><td>true</td><td>multi string array</td></tr>
<tr><td>QueryParameter</td><td>patient_ids</td><td>List of patient identifiers such as those returned by /api/patients. Unrecognized ids are silently ignored. Empty string returns all.</td><td>false</td><td>multi string array</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBClinicalPatientData">DBClinicalPatientData</a> array</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>study_id</td><td>A single study id, such as those returned by /api/studies. (example: brca_tcga)</td><td>true</td><td>string</td></tr>
<tr><td>QueryParameter</td><td>attribute_ids</td><td>List of attribute ids, such as those returned by /api/clinicalattributes/patients. (example: PATIENT_ID,DFS_STATUS)</td><td>true</td><td>multi string array</td></tr>
<tr><td>QueryParameter</td><td>patient_ids</td><td>List of patient identifiers such as those returned by /api/patients. Unrecognized ids are silently ignored. Empty string returns all.</td><td>false</td><td>multi string array</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBClinicalPatientData">DBClinicalPatientData</a> array</td></tr>
<tr><td>201</td><td>Created</td><td>No Content</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>study_id</td><td>A single study id, such as those returned by /api/studies. (example: brca_tcga)</td><td>true</td><td>string</td></tr>
<tr><td>QueryParameter</td><td>attribute_ids</td><td>List of attribute ids, such as those returned by /api/clinicalattributes/samples. (example: SAMPLE_TYPE,IS_FFPE)</td><td>true</td><td>multi string array</td></tr>
<tr><td>QueryParameter</td><td>sample_ids</td><td>List of sample identifiers. Unrecognized ids are silently ignored. Empty string returns all.</td><td>false</td><td>multi string array</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBClinicalSampleData">DBClinicalSampleData</a> array</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>study_id</td><td>A single study id, such as those returned by /api/studies. (example: brca_tcga)</td><td>true</td><td>string</td></tr>
<tr><td>QueryParameter</td><td>attribute_ids</td><td>List of attribute ids, such as those returned by /api/clinicalattributes/samples. (example: SAMPLE_TYPE,IS_FFPE)</td><td>true</td><td>multi string array</td></tr>
<tr><td>QueryParameter</td><td>sample_ids</td><td>List of sample identifiers. Unrecognized ids are silently ignored. Empty string returns all.</td><td>false</td><td>multi string array</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBClinicalSampleData">DBClinicalSampleData</a> array</td></tr>
<tr><td>201</td><td>Created</td><td>No Content</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>hugo_gene_symbols</td><td>List of hugo gene symbols. Unrecognized genes are silently ignored. Empty string returns all genes.</td><td>false</td><td>multi string array</td></tr>
</table>

#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBGene">DBGene</a> array</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>hugo_gene_symbols</td><td>List of hugo gene symbols. Unrecognized genes are silently ignored. Empty string returns all genes.</td><td>false</td><td>multi string array</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBGene">DBGene</a> array</td></tr>
<tr><td>201</td><td>Created</td><td>No Content</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>genetic_profile_ids</td><td>List of genetic_profile_ids such as those returned by /api/geneticprofiles. (example: brca_tcga_pub_mutations). Unrecognized genetic profile ids are silently ignored. Profile data is only returned for matching ids.</td><td>true</td><td>multi string array</td></tr>
<tr><td>QueryParameter</td><td>genes</td><td>List of hugo gene symbols. (example: AKT1,CASP8,TGFBR1) Unrecognized gene ids are silently ignored. Profile data is only returned for matching genes.</td><td>true</td><td>multi string array</td></tr>
<tr><td>QueryParameter</td><td>sample_ids</td><td>List of sample identifiers such as those returned by /api/samples. Empty string returns all. Must be empty to query by sample list ids.</td><td>false</td><td>multi string array</td></tr>
<tr><td>QueryParameter</td><td>sample_list_id</td><td>A single sample list ids such as those returned by /api/samplelists. (example: brca_tcga_idc,brca_tcga_lobular). Empty string returns all. If sample_ids argument was provided, this argument will be ignored.</td><td>false</td><td>string</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBProfileData">DBProfileData</a> array</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>genetic_profile_ids</td><td>List of genetic_profile_ids such as those returned by /api/geneticprofiles. (example: brca_tcga_pub_mutations). Unrecognized genetic profile ids are silently ignored. Profile data is only returned for matching ids.</td><td>true</td><td>multi string array</td></tr>
<tr><td>QueryParameter</td><td>genes</td><td>List of hugo gene symbols. (example: AKT1,CASP8,TGFBR1) Unrecognized gene ids are silently ignored. Profile data is only returned for matching genes.</td><td>true</td><td>multi string array</td></tr>
<tr><td>QueryParameter</td><td>sample_ids</td><td>List of sample identifiers such as those returned by /api/samples. Empty string returns all. Must be empty to query by sample list ids.</td><td>false</td><td>multi string array</td></tr>
<tr><td>QueryParameter</td><td>sample_list_id</td><td>A single sample list ids such as those returned by /api/samplelists. (example: brca_tcga_idc,brca_tcga_lobular). Empty string returns all. If sample_ids argument was provided, this argument will be ignored.</td><td>false</td><td>string</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBProfileData">DBProfileData</a> array</td></tr>
<tr><td>201</td><td>Created</td><td>No Content</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>study_id</td><td>A single study id, such as those returned by /api/studies. (example: brca_tcga). Must be empty to query by genetic profile ids (across all studies).</td><td>false</td><td>string</td></tr>
<tr><td>QueryParameter</td><td>genetic_profile_ids</td><td>List of genetic_profile_ids. (example: brca_tcga_pub_mutations). Empty string returns all genetic profiles. If study_id argument was provided, this argument will be ignored.</td><td>false</td><td>multi string array</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBGeneticProfile">DBGeneticProfile</a> array</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>study_id</td><td>A single study id, such as those returned by /api/studies. (example: brca_tcga). Must be empty to query by genetic profile ids (across all studies).</td><td>false</td><td>string</td></tr>
<tr><td>QueryParameter</td><td>genetic_profile_ids</td><td>List of genetic_profile_ids. (example: brca_tcga_pub_mutations). Empty string returns all genetic profiles. If study_id argument was provided, this argument will be ignored.</td><td>false</td><td>multi string array</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBGeneticProfile">DBGeneticProfile</a> array</td></tr>
<tr><td>201</td><td>Created</td><td>No Content</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>study_id</td><td>A single study id, such as those returned by /api/studies. (example: brca_tcga)</td><td>true</td><td>string</td></tr>
<tr><td>QueryParameter</td><td>patient_ids</td><td>List of patient ids such as those returned by /api/patients. Empty string returns all. Must be empty to query by sample ids.</td><td>false</td><td>multi string array</td></tr>
<tr><td>QueryParameter</td><td>sample_ids</td><td>List of sample identifiers. Empty string returns all. If patient_ids argument was provided, this argument will be ignored.</td><td>false</td><td>multi string array</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBPatient">DBPatient</a> array</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>study_id</td><td>A single study id, such as those returned by /api/studies. (example: brca_tcga)</td><td>true</td><td>string</td></tr>
<tr><td>QueryParameter</td><td>patient_ids</td><td>List of patient ids such as those returned by /api/patients. Empty string returns all. Must be empty to query by sample ids.</td><td>false</td><td>multi string array</td></tr>
<tr><td>QueryParameter</td><td>sample_ids</td><td>List of sample identifiers. Empty string returns all. If patient_ids argument was provided, this argument will be ignored.</td><td>false</td><td>multi string array</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBPatient">DBPatient</a> array</td></tr>
<tr><td>201</td><td>Created</td><td>No Content</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>study_id</td><td>A single study id, such as those returned by /api/studies. (example: brca_tcga). Must be empty to query by sample list ids (across all studies).</td><td>false</td><td>string</td></tr>
<tr><td>QueryParameter</td><td>sample_list_ids</td><td>List of sample list ids. (example: brca_tcga_idc,brca_tcga_lobular). Empty string returns all genetic profiles. If study_id argument was provided, this argument will be ignored.</td><td>false</td><td>multi string array</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBSampleList">DBSampleList</a> array</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>study_id</td><td>A single study id, such as those returned by /api/studies. (example: brca_tcga). Must be empty to query by sample list ids (across all studies).</td><td>false</td><td>string</td></tr>
<tr><td>QueryParameter</td><td>sample_list_ids</td><td>List of sample list ids. (example: brca_tcga_idc,brca_tcga_lobular). Empty string returns all genetic profiles. If study_id argument was provided, this argument will be ignored.</td><td>false</td><td>multi string array</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBSampleList">DBSampleList</a> array</td></tr>
<tr><td>201</td><td>Created</td><td>No Content</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>study_id</td><td>A single study id, such as those returned by /api/studies. (example: brca_tcga)</td><td>true</td><td>string</td></tr>
<tr><td>QueryParameter</td><td>sample_ids</td><td>List of sample identifiers. Unrecognized ids are silently ignored. Empty string returns all. Must be empty to query by patient_ids.</td><td>false</td><td>multi string array</td></tr>
<tr><td>QueryParameter</td><td>patient_ids</td><td>List of patient identifiers such as those returned by /api/patients. Unrecognized ids are silently ignored. Empty string returns all. If sample_ids argument was provided, this arument will be ignored.</td><td>false</td><td>multi string array</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBSample">DBSample</a> array</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>study_id</td><td>A single study id, such as those returned by /api/studies. (example: brca_tcga)</td><td>true</td><td>string</td></tr>
<tr><td>QueryParameter</td><td>sample_ids</td><td>List of sample identifiers. Unrecognized ids are silently ignored. Empty string returns all. Must be empty to query by patient_ids.</td><td>false</td><td>multi string array</td></tr>
<tr><td>QueryParameter</td><td>patient_ids</td><td>List of patient identifiers such as those returned by /api/patients. Unrecognized ids are silently ignored. Empty string returns all. If sample_ids argument was provided, this arument will be ignored.</td><td>false</td><td>multi string array</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBSample">DBSample</a> array</td></tr>
<tr><td>201</td><td>Created</td><td>No Content</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>study_ids</td><td>List of study_ids. Unrecognized ids are silently ignored. Empty string returns all.</td><td>false</td><td>multi string array</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBStudy">DBStudy</a> array</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


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
<table>
<tr><td>Type</td><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>QueryParameter</td><td>study_ids</td><td>List of study_ids. Unrecognized ids are silently ignored. Empty string returns all.</td><td>false</td><td>multi string array</td></tr>
</table>


#### Responses
<table>
<tr><td>HTTP Code</td><td>Description</td><td>Schema</td></tr>
<tr><td>200</td><td>OK</td><a href="#DBStudy">DBStudy</a> array</td></tr>
<tr><td>201</td><td>Created</td><td>No Content</td></tr>
<tr><td>401</td><td>Unauthorized</td><td>No Content</td></tr>
<tr><td>403</td><td>Forbidden</td><td>No Content</td></tr>
<tr><td>404</td><td>Not Found</td><td>No Content</td></tr>
</table>


#### Consumes

* application/json

#### Produces

* */*

#### Tags

* api-controller

## <a name="Definitions">Definitions</a>
### <a name="DBCancerType">DBCancerType</a>
<table>
<tr><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>color</td><td>false</td><td>string</td></tr>
<tr><td>id</td><td>false</td><td>string</td></tr>
<tr><td>name</td><td>false</td><td>string</td></tr>
</table>


### <a name="DBClinicalField">DBClinicalField</a>
<table>
<tr><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>attr_id</td><td>false</td><td>string</td></tr>
<tr><td>datatype</td><td>false</td><td>string</td></tr>
<tr><td>description</td><td>false</td><td>string</td></tr>
<tr><td>display_name</td><td>false</td><td>string</td></tr>
<tr><td>is_patient_attribute</td><td>false</td><td>string</td></tr>
<tr><td>priority</td><td>false</td><td>string</td></tr>
</table>


### <a name="DBClinicalPatientData">DBClinicalPatientData</a>
<table>
<tr><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>attr_id</td><td>false</td><td>string</td></tr>
<tr><td>attr_val</td><td>false</td><td>string</td></tr>
<tr><td>patient_id</td><td>false</td><td>string</td></tr>
<tr><td>study_id</td><td>false</td><td>string</td></tr>
</table>


### <a name="DBClinicalSampleData">DBClinicalSampleData</a>
<table>
<tr><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>attr_id</td><td>false</td><td>string</td></tr>
<tr><td>attr_val</td><td>false</td><td>string</td></tr>
<tr><td>sample_id</td><td>false</td><td>string</td></tr>
<tr><td>study_id</td><td>false</td><td>string</td></tr>
</table>


### <a name="DBGene">DBGene</a>
<table>
<tr><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>entrez_gene_id</td><td>false</td><td>string</td></tr>
<tr><td>hugo_gene_symbol</td><td>false</td><td>string</td></tr>
</table>


### <a name="DBGeneticProfile">DBGeneticProfile</a>
<table>
<tr><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>datatype</td><td>false</td><td>string</td></tr>
<tr><td>description</td><td>false</td><td>string</td></tr>
<tr><td>genetic_alteration_type</td><td>false</td><td>string</td></tr>
<tr><td>id</td><td>false</td><td>string</td></tr>
<tr><td>name</td><td>false</td><td>string</td></tr>
<tr><td>show_profile_in_analysis_tab</td><td>false</td><td>string</td></tr>
<tr><td>study_id</td><td>false</td><td>string</td></tr>
</table>


### <a name="DBPatient">DBPatient</a>
<table>
<tr><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>id</td><td>false</td><td>string</td></tr>
<tr><td>study_id</td><td>false</td><td>string</td></tr>
</table>


### <a name="DBProfileData">DBProfileData</a>
<table>
<tr><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>entrez_gene_id</td><td>false</td><td>string</td></tr>
<tr><td>genetic_profile_id</td><td>false</td><td>string</td></tr>
<tr><td>hugo_gene_symbol</td><td>false</td><td>string</td></tr>
<tr><td>sample_id</td><td>false</td><td>string</td></tr>
<tr><td>sample_list_id</td><td>false</td><td>string</td></tr>
<tr><td>study_id</td><td>false</td><td>string</td></tr>
</table>


### <a name="DBSample">DBSample</a>
<table>
<tr><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>id</td><td>false</td><td>string</td></tr>
<tr><td>internal_id</td><td>false</td><td>string</td></tr>
<tr><td>patient_id</td><td>false</td><td>string</td></tr>
<tr><td>sample_type</td><td>false</td><td>string</td></tr>
<tr><td>study_id</td><td>false</td><td>string</td></tr>
</table>


### <a name="DBSampleList">DBSampleList</a>
<table>
<tr><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>description</td><td>false</td><td>string</td></tr>
<tr><td>id</td><td>false</td><td>string</td></tr>
<tr><td>name</td><td>false</td><td>string</td></tr>
<tr><td>sample_ids</td><td>false</td><td>string array</td></tr>
<tr><td>study_id</td><td>false</td><td>string</td></tr>
</table>


### <a name="DBStudy">DBStudy</a>
<table>
<tr><td>Name</td><td>Description</td><td>Required</td><td>Schema</td><td>Default</td></tr>
<tr><td>citation</td><td>false</td><td>string</td></tr>
<tr><td>description</td><td>false</td><td>string</td></tr>
<tr><td>groups</td><td>false</td><td>string</td></tr>
<tr><td>id</td><td>false</td><td>string</td></tr>
<tr><td>name</td><td>false</td><td>string</td></tr>
<tr><td>pmid</td><td>false</td><td>string</td></tr>
<tr><td>short_name</td><td>false</td><td>string</td></tr>
<tr><td>type_of_cancer</td><td>false</td><td>string</td></tr>
</table>

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
