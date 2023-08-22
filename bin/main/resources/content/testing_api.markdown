[TOC]

# Introduction

The Cancer Genomic Data Server (CGDS) web service interface provides direct programmatic access to all genomic data stored within the server.  This enables you to easily access data from your favorite programming language, such as Python, Java, Perl, R or MatLab.  The CGDS web service is REST-based, meaning that client applications create a query consisting of parameters appended to a URL, and receive back either either text or an XML response.  For CGDS, all responses are currently tab-delimited text.  Clients of the CGDS web service can issue the following types of queries:

* What cancer studies are stored on the server?
* What genetic profile types are available for cancer study X?  For example, does the server store mutation and copy number data for the TCGA Glioblastoma data?
* What case sets are available for cancer study X?  For example, what case sets are available for TCGA Glioblastoma?

Additionally, clients can easily retrieve "slices" of genomic data.  For example, a client can retrieve all mutation data from PTEN and EGFR in the TCGA Glioblastoma data.

The CGDS web service provides access to both public and private cancer studies.  Public studies are accessible to everyone.  Private studies are only accessible to users who have the right to access them. 

If a study is private, then it can be accessed by adding the fields email\_address and secret\_key to a data access command. All commands are data access commands _except_ getTypesOfCancer and getNetwork. See the section Accessing Private Studies below for details.

# The CGDS R Package

If you are interested in accessing CGDS via R, please check out our [CGDS-R library](cgds_r.jsp).

# Basic Query Syntax
All web queries are available at: [http://172.21.218.60:8080//cgds-public/webservice.do](http://172.21.218.60:8080//cgds-public/webservice.do). All calls to the Web interface are constructed by appending URL parameters.   Within each call, you must specify:

* **cmd** = the command that you wish to execute.  The command must be equal to one of the following: getTypesOfCancer, getNetwork, getCancerStudies, getGeneticProfiles, getProfileData, getCaseLists, getClinicalData, or getMutationData.
* optional additional parameters, depending of the command (see below).

For example, the following query will request all case lists for the TCGA GBM data:

[webservice.do?cmd=getCaseLists&cancer_study_id=2](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getCaseLists&cancer_study_id=2)

# Response Header and Error Messages

The first line of each response begins with a hash mark (#), and will contain data regarding the server status.  For example:

     # CGDS Kernel:  Data served up fresh at:  Wed Oct 27 13:02:30 EDT 2010

If any errors have occurred in processing your query, this will appear directly after the status message.  Error messages begin with the "Error:" tag.  Warning messages begin with the "# Warning:" tag.  Unrecoverable errors are reported as errors.  For example:

     # CGDS Kernel:  Data served up fresh at:  Wed Oct 27 13:02:30 EDT 2010
     Error:  No case lists available for cancer_study_id:  gbs.

Recoverable errors, such as invalid gene symbols are reported as warnings.  Multiple warnings may also be returned.  For example:

     # CGDS Kernel:  Data served up fresh at:  Wed Oct 27 13:06:34 EDT 2010
     # Warning:  Unknown gene:  EGFR11
     # Warning:  Unknown gene:  EGFR12

# Commands

## Get All Types of Cancer

### Description

Retrieves a list of all the clinical types of cancer stored on the server.

### Query Format

* **cmd=getTypesOfCancer** (required)

### Response Format

A tab-delimited file with two columns:

* **type\_of\_cancer\_id:**  a unique text identifier used to identify the type of cancer.  For example, "gbm" identifies Glioblastoma multiforme.
* **name:**  short name of the type of cancer.

### Example

Get all Types of Cancer: [webservice.do?cmd=getTypesOfCancer](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getTypesOfCancer)

## Get All Cancer Studies

### Description

Retrieves meta-data regarding cancer studies stored on the server. Returns information about cancer studies accessible to the currently logged-in user: all public studies, and private studies which the user has the right to access.

### Query Format

* **cmd=getCancerStudies** (required)

### Response Format

A tab-delimited file with three columns:

* **cancer\_study\_id:**  a unique integer ID that should be used to identify the cancer study in subsequent interface calls.  
* **name:**  short name of the cancer study.
* **description:**  short description of the cancer study.

### Example

Get all Cancer Studies: [webservice.do?cmd=getCancerStudies](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getCancerStudies)

## Get All Genetic Profiles for a Specific Cancer Study

### Description

Retrieves meta-data regarding all genetic profiles, e.g. mutation or copy number profiles, stored about a specific cancer study.

### Query Format

* **cmd**=getGeneticProfiles (required)
* **cancer\_study\_id**=[cancer study ID] (required)

### Response Format

A tab-delimited file with six columns:

* **genetic\_profile\_id**:  a unique ID used to identify the genetic profile ID in subsequent interface calls.  This is a human readable ID.  For example, "gbm_mutations" identifies the TCGA GBM mutation genetic profile.
* **genetic\_profile\_name**:  short profile name.
* **genetic\_profile\_description**:  short profile description.
* **cancer\_study\_id**:  cancer study ID tied to this genetic profile.  Will match the input cancer\_study\_id.  
* **genetic\_alteration\_type**:  indicates the profile type.  Will be one of:
    * MUTATION
    * MUTATION\_EXTENDED
    * COPY\_NUMBER\_ALTERATION
    * MRNA\_EXPRESSION
    * METHYLATION
* **show\_profile\_in\_analysis\_tab**:  a boolean flag used for internal purposes (you can safely ignore it).

### Example

Get all Genetic Profiles for Glioblastoma (TCGA): [webservice.do?cmd=getGeneticProfiles&cancer_study_id=2](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getGeneticProfiles&cancer_study_id=2)

## Get All Case Lists for a Specific Cancer Study

### Description

Retrieves meta-data regarding all case lists stored about a specific cancer study.  For example, a within a particular study, only some cases may have sequence data, and another subset of cases may have been sequenced and treated with a specific therapeutic protocol.  Multiple case lists may be associated with each cancer study, and this method enables you to retrieve meta-data regarding all of these case lists.

### Query Format

* **cmd**=getCaseLists (required)
* **cancer\_study\_id**=[cancer study ID] (required)

### Response Format

A tab-delimited file with five columns:

* **case\_list\_id**:  a unique ID used to identify the case list ID in subsequent interface calls.  This is a human readable ID.  For example, "gbm_all" identifies all cases profiles in the TCGA GBM study.
* **case\_list\_name**:  short name for the case list.
* **case\_list\_description**:  short description of the case list.
* **cancer\_study\_id**:  cancer study ID tied to this genetic profile.  Will match the input cancer\_study\_id.  
* **case\_ids**:  space delimited list of all case IDs that make up this case list.

### Example

Get all Case Lists for Glioblastoma (TCGA): [webservice.do?cmd=getCaseLists&cancer_study_id=2](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getCaseLists&cancer_study_id=2)

## Get Profile Data

### Description

Retrieves genomic profile data for one or more genes.

### Query Format

* **cmd**=getProfileData (required)
* **case\_set_id**= [case set ID] (required)
* **genetic\_profile\_id**= [one or more genetic profile IDs] (required). Multiple genetic profile IDs must be separated by comma (,) characters, or URL encoded spaces, e.g. +
* **gene\_list**= [one or more genes, specified as HUGO Gene Symbols or Entrez Gene IDs] (required). Multiple genes must be separated by comma (,) characters, or  URL encoded spaces, e.g. +

You can either:

* Specify multiple genes and a single genetic profile ID. Example: [webservice.do?cmd=getProfileData&case_set_id=gbm_all&genetic_profile_id=gbm_mutations&gene_list=BRCA1+BRCA2+TP53](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getProfileData&case_set_id=gbm_all&genetic_profile_id=gbm_mutations&gene_list=BRCA1+BRCA2+TP53)
* Specify a single gene and multiple genetic profile IDs. Example: [webservice.do?cmd=getProfileData&case_set_id=gbm_all&genetic_profile_id=gbm_cna_consensus, gbm_cna_rae&gene_list=EGFR](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getProfileData&case_set_id=gbm_all&genetic_profile_id=gbm_cna_consensus,%20gbm_cna_rae&gene_list=EGFR)

#### Response Format 1

When requesting one or multiple genes and a single genetic profile ID (see above), you will receive a 2x2 matrix with the following columns:

1. **GENE\_ID**:  Entrez Gene ID   
2. **COMMON**:  HUGO Gene Symbol
3. **Columns 3 - N**:  Data for each case

#### Response Format 2
When requesting a single gene and multiple genetic profile IDs (see above), you will receive a 2x2 matrix with the following columns:

1. **GENETIC\_PROFILE_ID**:  The Genetic Profile ID.   
2. **ALTERATION\_TYPE**:  The Genetic Alteration Type, e.g. MUTATION, MUTATION_EXTENDED, COPY_NUMBER_ALTERATION, or MRNA_EXPRESSION.
3. **GENE\_ID**:  Entrez Gene ID.   
4. **COMMON**:  HUGO Gene Symbol.
5. **Columns 5 - N**:  Data for each case.

### Examples

See Query Format above.

## Get Extended Mutation Data

### Description

For data of type EXTENDED_MUTATION, you can request the full set of annotated extended mutation data.  This enables you to, for example, determine which sequencing center sequenced the mutation, the amino acid change that results from the mutation, or gather links to predicted functional consequences of the mutation.

### Query Format

* **cmd**=getMutationData (required)
* **case\_set\_id**= [case set ID] (required)
* **genetic\_profile\_id**= [a single genetic profile IDs] (required).
* **gene\_list**= [one or more genes, specified as HUGO Gene Symbols or Entrez Gene IDs] (required). Multiple genes must be separated by comma (,) characters, or  URL encoded spaces, e.g. +

### Response Format

A tab-delimited file with the following columns:

* **entrez\_gene\_id**:  Entrez Gene ID.
* **gene\_symbol**:  HUGO Gene Symbol.
* **case\_id**:  Case ID.
* **sequencing\_center**:  Sequencer Center responsible for identifying this mutation.  For example:  broad.mit.edu.
* **mutation\_status**:  somatic or germline mutation status.  all mutations returned will be of type somatic.
* **mutation\_type**:  mutation type, such as nonsense, missense, or frameshift_ins.
* **validation\_status**:  validation status.  Usually valid, invalid, or unknown.
* **amino\_acid\_change**:  amino acid change resulting from the mutation.
* **functional\_impact_score**:  predicted functional impact score, as predicted by:  [Mutation Assessor](http://mutationassessor.org/).
* **xvar\_link**:  Link to the Mutation Assessor web site.
* **xvar\_link\_pdb**:  Link to the Protein Data Bank (PDB) View within Mutation Assessor web site.
* **xvar\_link\_msa**:  Link the Multiple Sequence Alignment (MSA) view within the  Mutation Assessor web site.
* **chr**:  chromosome where mutation occurs.
* **start\_position**:  start position of mutation.
* **end\_position**:  end position of mutation.

### Example

Get Extended Mutation Data for EGFR and PTEN in TCGA GBM:

[webservice.do?cmd=getMutationData&case_set_id=gbm_all&genetic_profile_id=gbm_mutations&gene_list=EGFR+PTEN](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getMutationData&case_set_id=gbm_all&genetic_profile_id=gbm_mutations&gene_list=EGFR+PTEN)

## Get Clinical Data

### Description

Retrieves overall survival, disease free survival and age at diagnosis for specified cases.  Due to patient privacy restrictions, no other clinical data is available.

### Query Format

* **cmd**=getClinicalData (required)
* **case\_set\_id**= [case set ID] (required)

### Response Format

A tab-delimited file with the following columns:

* **case\_id**:  Unique Case Identifier.
* **overall\_survival\_months**:  Overall survival, in months.
* **overall\_survival\_status**:  Overall survival status, usually indicated as "0" -> "LIVING" or "1" -> "DECEASED".
* **disease\_free\_survival\_months**:  Disease free survival, in months.
* **disease\_free\_survival\_status**:  Disease free survival status, usually indicated as "0" -> "DiseaseFree" or "1" -> "Recurred/Progressed".
* **age_at_diagnosis**:  Age at diagnosis.

### Example

Get Clinical Data for All TCGA Ovarian Cases:

[webservice.do?cmd=getClinicalData&case_set_id=ova_all](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getClinicalData&case_set_id=ova_all)

# Accessing Private Studies

## Details

Authorized users can access private cancer studies on the CGDS web service. Currently (July 2011) only system administrators can create user accounts and load private studies into the CGDS web service.  

If a study is private, then it can be accessed by adding the fields email\_address and secret\_key to a data access command. All commands above are data access commands _except_ getTypesOfCancer and getNetwork. The secret key must be obtained from the system administrator.

## Example

### Query Format

These fields must be added to a request's query string to access a private study:

* **email\_address**= [email address] (required to access a private study)
* **secret\_key**= [secret key] (required to access a private study)

### Response Format

Either "No cancer studies accessible..." or the data normally returned by the command.

### Example

Get all Cancer Studies accessible to arthur@gmail.com: [webservice.do?cmd=getCancerStudies&email_address=arthur@gmail.com&secret_key=Secret](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getCancerStudies&email_address=arthur@gmail.com&secret_key=Secret)


### More examples
Get all Cancer Studies accessible to schultz@cbio.mskcc.org: [webservice.do?cmd=getCancerStudies&email_address=schultz@cbio.mskcc.org&secret_key=ExampleSecretKey675524](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getCancerStudies&email_address=schultz@cbio.mskcc.org&secret_key=ExampleSecretKey675524)

Get all Cancer Studies accessible to schultz@cbio.mskcc.org, but with wrong key: [webservice.do?cmd=getCancerStudies&email_address=schultz@cbio.mskcc.org&secret_key=NotKey](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getCancerStudies&email_address=schultz@cbio.mskcc.org&secret_key=NotKey)

Get all Cancer Studies accessible to jerk@badsite.com: [webservice.do?cmd=getCancerStudies&email_address=jerk@badsite.com&secret_key=ExampleSecretKey675524](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getCancerStudies&email_address=jerk@badsite.com&secret_key=ExampleSecretKey675524)

Get all Genetic Profiles for Glioblastoma (TCGA), no credentials: [webservice.do?cmd=getGeneticProfiles&cancer_study_id=2](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getGeneticProfiles&cancer_study_id=2)

Get all Genetic Profiles for Breast Cancer, no credentials: [webservice.do?cmd=getGeneticProfiles&cancer_study_id=1](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getGeneticProfiles&cancer_study_id=1)

Get all Genetic Profiles for Breast Cancer accessible to schultz@cbio.mskcc.org: [webservice.do?cmd=getGeneticProfiles&cancer_study_id=1&email_address=schultz@cbio.mskcc.org&secret_key=ExampleSecretKey675524](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getGeneticProfiles&cancer_study_id=1&email_address=schultz@cbio.mskcc.org&secret_key=ExampleSecretKey675524)

Get all Genetic Profiles for Breast Cancer accessible to schultz@cbio.mskcc.org, but with wrong key: [webservice.do?cmd=getGeneticProfiles&cancer_study_id=1&email_address=schultz@cbio.mskcc.org&secret_key=woops](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getGeneticProfiles&cancer_study_id=1&email_address=schultz@cbio.mskcc.org&secret_key=woops)

Get all Case Lists for Glioblastoma (TCGA): [webservice.do?cmd=getCaseLists&cancer_study_id=2](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getCaseLists&cancer_study_id=2)

* Specify multiple genes and a single genetic profile ID. Example: [webservice.do?cmd=getProfileData&case_set_id=gbm_all&genetic_profile_id=gbm_mutations&gene_list=BRCA1+BRCA2+TP53](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getProfileData&case_set_id=gbm_all&genetic_profile_id=gbm_mutations&gene_list=BRCA1+BRCA2+TP53)
* Specify a single gene and multiple genetic profile IDs. Example: [webservice.do?cmd=getProfileData&case_set_id=gbm_all&genetic_profile_id=gbm_cna_consensus, gbm_cna_rae&gene_list=EGFR](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getProfileData&case_set_id=gbm_all&genetic_profile_id=gbm_cna_consensus,%20gbm_cna_rae&gene_list=EGFR)

Get Extended Mutation Data for EGFR and PTEN in TCGA GBM:

[webservice.do?cmd=getMutationData&case_set_id=gbm_all&genetic_profile_id=gbm_mutations&gene_list=EGFR+PTEN](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getMutationData&case_set_id=gbm_all&genetic_profile_id=gbm_mutations&gene_list=EGFR+PTEN)

Get all Case Lists for Breast, no cred: [webservice.do?cmd=getCaseLists&cancer_study_id=1](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getCaseLists&cancer_study_id=1)

getProfileData for Breast, no cred: 

* Specify multiple genes and a single genetic profile ID. Example: [webservice.do?cmd=getProfileData&case_set_id=BC_all&genetic_profile_id=BC_mRNA_DBCG&gene_list=BRCA1+BRCA2+TP53](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getProfileData&case_set_id=BC_all&genetic_profile_id=BC_mRNA_DBCG&gene_list=BRCA1+BRCA2+TP53)
* Specify a single gene and multiple genetic profile IDs. Example: [webservice.do?cmd=getProfileData&case_set_id=BC_all&genetic_profile_id=BC_mRNA_DBCG,BC_mRNA_FW_MDG&gene_list=EGFR](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getProfileData&case_set_id=BC_all&genetic_profile_id=BC_mRNA_DBCG,BC_mRNA_FW_MDG&gene_list=EGFR)

Get Extended Mutation Data for EGFR and PTEN for Breast, no cred: 

[webservice.do?cmd=getMutationData&case_set_id=BC_all&genetic_profile_id=BC_mutation&gene_list=EGFR+PTEN](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getMutationData&case_set_id=BC_all&genetic_profile_id=BC_mutation&gene_list=EGFR+PTEN)


Get all Case Lists for Breast, accessible to schultz@cbio.mskcc.org: [webservice.do?cmd=getCaseLists&cancer_study_id=1&email_address=schultz@cbio.mskcc.org&secret_key=ExampleSecretKey675524](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getCaseLists&cancer_study_id=1&email_address=schultz@cbio.mskcc.org&secret_key=ExampleSecretKey675524)

getProfileData for Breast, accessible to schultz@cbio.mskcc.org: 

* Specify multiple genes and a single genetic profile ID. Example: [webservice.do?cmd=getProfileData&case_set_id=BC_all&genetic_profile_id=BC_mRNA_DBCG&gene_list=BRCA1+BRCA2+TP53&email_address=schultz@cbio.mskcc.org&secret_key=ExampleSecretKey675524](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getProfileData&case_set_id=BC_all&genetic_profile_id=BC_mRNA_DBCG&gene_list=BRCA1+BRCA2+TP53&email_address=schultz@cbio.mskcc.org&secret_key=ExampleSecretKey675524)
* Specify a single gene and multiple genetic profile IDs. Example: [webservice.do?cmd=getProfileData&case_set_id=BC_all&genetic_profile_id=BC_mRNA_DBCG,BC_mRNA_FW_MDG&gene_list=EGFR&email_address=schultz@cbio.mskcc.org&secret_key=ExampleSecretKey675524](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getProfileData&case_set_id=BC_all&genetic_profile_id=BC_mRNA_DBCG,BC_mRNA_FW_MDG&gene_list=EGFR&email_address=schultz@cbio.mskcc.org&secret_key=ExampleSecretKey675524)

Get Extended Mutation Data for EGFR and PTEN for Breast, accessible to schultz@cbio.mskcc.org: 

[webservice.do?cmd=getMutationData&case_set_id=BC_all&genetic_profile_id=BC_mutation&gene_list=EGFR+PTEN&email_address=schultz@cbio.mskcc.org&secret_key=ExampleSecretKey675524](http://172.21.218.60:8080//cgds-public/webservice.do?cmd=getMutationData&case_set_id=BC_all&genetic_profile_id=BC_mutation&gene_list=EGFR+PTEN&email_address=schultz@cbio.mskcc.org&secret_key=ExampleSecretKey675524)

