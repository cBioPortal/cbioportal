_**Note: this API is still under development and subject to change.**_

You can use our REST-ful API to access data using either GET or POST requests, or by using the API client, which is exposed as the global variable ```window.cbioportal_client```. The client wraps API requests (POST'ed) and automatically caches results. Client method calls return a jQuery promise which resolves with the requested data. 
## Example Code ##

The following code block iterates over all cancer studies and prints out the id and name of each.

```
    window.cbioportal_client.getStudies().then(function(studies) {
        for (var i=0; i<studies.length; i++) {
            console.log("Id: " + studies[i].id
                    + "; Name: " + studies[i].name);
        }
    });
``` 

The available calls follow.
### Format of this page ###
Endpoint

Corresponding client method name

JSON return format

Arguments (which can be passed in the argument string when using the API directly, or put as members of an object passed in as the argument to a client method)

## Cancer Types ##
/api/cancertypes

getCancerTypes

```
{  
	"id": "brca",  
	"name":"Invasive Breast Carcinoma",  
	"color":"HotPink"  
}
```

Optional: "cancer_type_ids": List of cancer type ids

## Sample Clinical Data ##
/api/clinicaldata/samples

getSampleClinicalData

```
{
	"attr_id":"TUMOR",
	"attr_val":"T3",
	"study_id":"brca_tcga_pub",
	"sample_id":"TCGA-A2-A0T2-01"
}
```

Required:  
-"study_id": Cancer study id  
-"attribute_ids": List of attribute ids  

Optional: "sample_ids":  List of sample ids

## Patient Clinical Data ##
/api/clinicaldata/patients
getPatientClinicalData
```
{
	"attr_id":"GENDER",
	"attr_val":"FEMALE",
	"study_id":"brca_tcga_pub",
	"patient_id":"TCGA-A2-A0T2"
}
```
Required:  
-"study_id": Cancer study id  
-"attribute_ids": List of attribute ids

Optional: "patient_ids":  List of patient ids
          
## Sample Clinical Attributes ##
/api/clinicalattributes/samples

getSampleClinicalAttributes

```
{
	"attr_id":"TUMOR",
	"display_name":"Tumor",
	"description":"Tumor",
	"datatype":"STRING",
	"is_patient_attribute":"0",
	"priority":"1"
}
```

Optional:  
-"study_id": Cancer study id  
-"sample_ids": List of sample ids (you must also pass in "study_id")

## Patient Clinical Attributes ##
/api/clinicalattributes/patients

getPatientClinicalAttributes

```
{
	"attr_id":"GENDER",
	"display_name":"Person Gender",
	"description":"Patient gender.",
	"datatype":"STRING",
	"is_patient_attribute":"1",
	"priority":"1"
}
```

Optional:  
-"study_id": Cancer study id  
-"patient_ids":  List of patient ids (you must also pass in "study_id")

## Genes ##
/api/genes

getGenes

```
{
	"hugo_gene_symbol":"BRCA1",
	"entrez_gene_id":672
}
```
Optional: "hugo_gene_symbols": List of genes

## Genetic Profiles ##
/api/geneticprofiles

getGeneticProfiles

```
{
	"id":"brca_tcga_pub_gistic",
	"name":"Putative copy-number alterations from GISTIC",
	"description":"Putative copy-number from GISTIC 2.0. Values: -2 = homozygous deletion; -1 = hemizygous deletion; 0 = neutral / no change; 1 = gain; 2 = high level amplification.",
	"datatype":"DISCRETE",
	"study_id":"brca_tcga_pub",
	"genetic_alteration_type":"COPY_NUMBER_ALTERATION",
	"show_profile_in_analysis_tab":false
}
```

Optional:  
-"study_id": Cancer study id  
-"genetic_profile_ids": List of genetic profile ids

## Sample Lists ##
/api/samplelists

getSampleLists

```
{
	"id":"brca_tcga_pub_sequenced",
	"name":"Sequenced Tumors",
	"description":"All sequenced samples (507 samples)",
	"study_id":"brca_tcga_pub",
	"sample_ids":["TCGA-A8-A06N-01","TCGA-A8-A06O-01","TCGA-A8-A06Q-01",...]
}
```

Optional:  
-"study_id": Cancer study id  
-"sample_list_ids": List of patient list ids

## Patients ##
/api/patients

getPatients

```
{
	"id":"TCGA-A2-A0T2",
	"study_id":"brca_tcga_pub"
}
```

Required: "study_id": Cancer study id

Optional: "patient_ids": List of patient ids

## Genetic Profile Data ##
/api/geneticprofiledata

getGeneticProfileData

###Non-mutation profiles:###
```
{
	"genetic_profile_id":"brca_tcga_pub_gistic",
	"entrez_gene_id":672,
	"hugo_gene_symbol":"BRCA1",
	"sample_id":"TCGA-A1-A0SB-01",
	"study_id":"brca_tcga_pub",
	"profile_data":"0"
}
```

###Mutation profiles:###
```
{
	"genetic_profile_id":"brca_tcga_pub_mutations",
	"entrez_gene_id":672,
	"hugo_gene_symbol":"BRCA1",
	"sample_id":"TCGA-BH-A0WA-01",
	"study_id":"brca_tcga_pub",
	"sequencing_center":"genome.wustl.edu",
	"mutation_status":"Somatic",
	"mutation_type":"Splice_Site",
	"validation_status":"Unknown",
	"amino_acid_change":"X1580_splice",
	"functional_impact_score":"NA",
	"xvar_link":"NA",
	"xvar_link_pdb":"NA",
	"xvar_link_msa":"NA",
	"chr":17,
	"start_position":41223256,
	"end_position":41223256,
	"reference_allele":"C",
	"variant_allele":"T",
	"reference_read_count_tumor":"-1",
	"variant_read_count_tumor":"-1",
	"reference_read_count_normal":"-1",
	"variant_read_count_normal":"-1"
}
```

Required:  
-"genetic_profile_ids": List of genetic profile ids  
-"genes": List of hugo gene symbols

Optional: "sample_ids": List of sample ids

## Samples ##
/api/samples

getSamples

```
{
	"id":"TCGA-A2-A0T2-01",
	"sample_type":"Primary Solid Tumor",
	"patient_id":"TCGA-A2-A0T2",
	"study_id":"brca_tcga_pub"
}
```

Required: "study_id": Cancer study id

Optional: "sample_ids": List of sample ids

## Studies ##
/api/studies

getStudies

```
{
	"id":"brca_tcga_pub",
	"type_of_cancer":"brca",
	"name":"Breast Invasive Carcinoma (TCGA, Nature 2012)",
	"short_name":"Breast (TCGA pub)",
	"description":"<a href=\"http://cancergenome.nih.gov/\">The Cancer Genome Atlas (TCGA)</a> Breast Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\"https://tcga-data.nci.nih.gov/docs/publications/brca_2012/\">Raw data via the TCGA Data Portal</a>.",
	"pmid":23000897,
	"citation":"TCGA, Nature 2012",
	"groups":"PUBLIC"
}
```

Optional: "study_ids": List of study ids