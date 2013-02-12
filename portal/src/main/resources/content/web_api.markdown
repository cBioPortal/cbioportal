# Introduction

The Cancer Genomic Data Server (CGDS) web service interface provides direct programmatic access to all genomic data stored within the server.  This enables you to easily access data from your favorite programming language, such as Python, Java, Perl, R or MatLab.  The CGDS web service is REST-based, meaning that client applications create a query consisting of parameters appended to a URL, and receive back either either text or an XML response.  For CGDS, all responses are currently tab-delimited text.  Clients of the CGDS web service can issue the following types of queries:

* What cancer studies are stored on the server?
* What genetic profile types are available for cancer study X?  For example, does the server store mutation and copy number data for the TCGA Glioblastoma data?
* What case sets are available for cancer study X?  For example, what case sets are available for TCGA Glioblastoma?

Additionally, clients can easily retrieve "slices" of genomic data.  For example, a client can retrieve all mutation data from PTEN and EGFR in the TCGA Glioblastoma data.

Please note that the example queries below are accurate, but they are not guaranteed to return data, as our database is constantly being updated.

# The CGDS R Package

If you are interested in accessing CGDS via R, please check out our [CGDS-R library](cgds_r.jsp).

# Basic Query Syntax
All web queries are available at: [webservice.do](webservice.do). All calls to the Web interface are constructed by appending URL parameters.   Within each call, you must specify:

* **cmd** = the command that you wish to execute.  The command must be equal to one of the following: getTypesOfCancer, getNetwork, getCancerStudies, getGeneticProfiles, getProfileData, getCaseLists, getClinicalData, getMutationData or getMutationFrequency.
* optional additional parameters, depending of the command (see below).

For example, the following query will request all case lists for the TCGA GBM data:

[webservice.do?cmd=getCaseLists&cancer_study_id=gbm_tcga](webservice.do?cmd=getCaseLists&cancer_study_id=gbm_tcga)

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

# Deprecated API

As of August, 2011:

* In previous versions of the API, the **getCancerStudies** command was referred to as **getCancerTypes**.  For backward compatibility, **getCancerTypes** still works, but is now considered deprecated.

* In previous versions of the API, the **cancer_study_id** parameter was referred to as **cancer_type_id**.  For backward compatibility,, **cancer_type_id** still works, but is now considered deprecated.

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

[Get all Types of Cancer.](http://cbio.mskcc.org/cgx/webservice.do?cmd=getTypesOfCancer)

## Get All Cancer Studies

### Description

Retrieves meta-data regarding cancer studies stored on the server.

### Query Format

* **cmd=getCancerStudies** (required)

### Response Format

A tab-delimited file with three columns:

* **cancer\_study\_id:**  a unique integer ID that should be used to identify the cancer study in subsequent interface calls.  
* **name:**  short name of the cancer study.
* **description:**  short description of the cancer study.

### Example

[Get all Cancer Studies.](webservice.do?cmd=getCancerStudies)

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

[Get all Genetic Profiles for Glioblastoma (TCGA).](webservice.do?cmd=getGeneticProfiles&cancer_study_id=gbm_tcga)

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

[Get all Case Lists for Glioblastoma (TCGA).](webservice.do?cmd=getCaseLists&cancer_study_id=gbm_tcga)

## Get Profile Data

### Description

Retrieves genomic profile data for one or more genes.

### Query Format

* **cmd**=getProfileData (required)
* **case\_set_id**= [case set ID] (required)
* **genetic\_profile\_id**= [one or more genetic profile IDs] (required). Multiple genetic profile IDs must be separated by comma (,) characters, or URL encoded spaces, e.g. +
* **gene\_list**= [one or more genes, specified as HUGO Gene Symbols or Entrez Gene IDs] (required). Multiple genes must be separated by comma (,) characters, or  URL encoded spaces, e.g. +

You can either:

* [Specify multiple genes and a single genetic profile ID.](webservice.do?cmd=getProfileData&case_set_id=gbm_tcga_all&genetic_profile_id=gbm_tcga_mutations&gene_list=BRCA1+BRCA2+TP53)
* [Specify a single gene and multiple genetic profile IDs.](webservice.do?cmd=getProfileData&case_set_id=gbm_tcga_all&genetic_profile_id=gbm_tcga_log2CNA,gbm_tcga_gistic&gene_list=EGFR)

#### Response Format 1

When requesting one or multiple genes and a single genetic profile ID (see above), you will receive a tab-delimited matrix with the following columns:

1. **GENE\_ID**:  Entrez Gene ID   
2. **COMMON**:  HUGO Gene Symbol
3. **Columns 3 - N**:  Data for each case

#### Response Format 2
When requesting a single gene and multiple genetic profile IDs (see above), you will receive a tab-delimited matrix with the following columns:

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

[Get Extended Mutation Data for EGFR and PTEN in TCGA GBM.](webservice.do?cmd=getMutationData&case_set_id=gbm_tcga_all&genetic_profile_id=gbm_tcga_mutations&gene_list=EGFR+PTEN)

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
* **overall\_survival\_status**:  Overall survival status, usually indicated as "LIVING" or "DECEASED".
* **disease\_free\_survival\_months**:  Disease free survival, in months.
* **disease\_free\_survival\_status**:  Disease free survival status, usually indicated as "DiseaseFree" or "Recurred/Progressed".
* **age_at_diagnosis**:  Age at diagnosis.

### Example

[Get Clinical Data for All TCGA Ovarian Cases.](webservice.do?cmd=getClinicalData&case_set_id=ov_tcga_pub_all)

## Get Protein/Phosphoprotein Antibody Information

### Description

Retrieves information on antibodies used by reverse-phase protein arrays (RPPA) to measure protein/phosphoprotein levels.

### Query Format
* **cmd**=getProteinArrayInfo (required)
* **cancer_study_id**= [cancer study ID] (required)
* **protein_array_type**= [protein_level or phosphorylation]
* **gene_list**= [one or more genes, specified as HUGO Gene Symbols or Entrez Gene IDs]. Multiple genes must be separated by comma (,) characters, or URL encoded spaces, e.g. +

### Response Format

You will receive a tab-delimited matrix with the following 4 columns:

* **ARRAY_ID**: The protein array ID.
* **ARRAY_TYPE**: The protein array antibody type, i.e. protein_level or phosphorylation.
* **GENE**: The targeted gene name (HUGO gene symbol).
* **RESIDUE**: The targeted resdue(s).

### Example

* [Get Information on RPPA Antibodies Measuring TCGA Colorectal Cases.](webservice.do?cmd=getProteinArrayInfo&cancer_study_id=coadread_tcga)
* [Get Information on RPPA Phosphoprotein Antibodies Measuring TCGA Colorectal Cases.](webservice.do?cmd=getProteinArrayInfo&cancer_study_id=coadread_tcga&protein_array_type=phosphorylation)
* [Get Information on ERBB2 and TP53 RPPA Protein Antibodies Measuring TCGA Colorectal Cases.](webservice.do?cmd=getProteinArrayInfo&cancer_study_id=coadread_tcga&protein_array_type=protein_level&gene_list=ERBB2+TP53)

## Get RPPA-based Proteomics Data

### Description

Retrieves protein and/or phosphoprotein levels measured by reverse-phase protein arrays (RPPA).

### Query Format
* **cmd**=getProteinArrayData (required)
* **case_set_id**= [case set ID] (required)
* **array_info**= [1 or 0]. If 1, antibody information will also be exported. 

#### Response Format 1

If the parameter of array_info is not specified or it is not 1, you will receive a tab-delimited matrix with the following columns:

* **ARRAY_ID**: The protein array ID.
* **Columns 2 - N**: Data for each case.

#### Response Format 2

If the parameter of array_info is 1, you will receive a tab-delimited matrix with the following columns:

* **ARRAY_ID**: The protein array ID.
* **ARRAY_TYPE**: The protein array antibody type, i.e. protein_level or phosphorylation.
* **GENE**: The targeted gene name (HUGO gene symbol).
* **RESIDUE**: The targeted resdue(s).
* **Columns 5 - N**: Data for each case.

### Example

* [Get All RPPA Data in TCGA Colorectal Cases.](webservice.do?cmd=getProteinArrayData&case_set_id=coadread_tcga_RPPA)
* [Get All RPPA Data with antibody information in TCGA Colorectal Cases.](webservice.do?cmd=getProteinArrayData&case_set_id=coadread_tcga_RPPA&array_info=1)

# Linking to Us

Once you have a cancer\_study\_id, it is very easy to create stable links from your web site to the cBio Portal.  Stable links must point to link.do, and can include the following parameters:

* **cancer\_study\_id**=[cancer study ID] (required)
* **gene\_list**=[a comma separated list of HUGO gene symbols] (required)
* **report**=[report to display;  can be one of:  full (default), oncoprint_html]

For example, the following links to the TCGA GBM data for EGFR and NF1:

[link.do?cancer_study_id=gbm_tcga&gene_list=EGFR,NF1](link.do?cancer_study_id=gbm_tcga&gene_list=EGFR,NF1)

This link displays the same data as an Oncoprint only:

[link.do?cancer_study_id=gbm_tcga&gene_list=EGFR,NF1&report=oncoprint_html](link.do?cancer_study_id=gbm_tcga&gene_list=EGFR,NF1&report=oncoprint_html)
