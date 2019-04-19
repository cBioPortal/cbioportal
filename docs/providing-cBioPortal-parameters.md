##Introduction
On this page we aim to describe the necessary parameters for cBioPortal to:

1. See the homepage with provided parameters filled in
2. Query a study using the provided parameters, resulting in the oncoprint page
3. Query a study using the provided parameters, resulting in the mutations page

###POST method
:warning: In order to support a large number of sample identifiers and genes, the `POST` method should be used. 

The parameters are described below. At the bottom of the page we provide some jsfiddle examples how this could be done. 

###URL
The URL to be used is `http://<your_server>/index.do`, e.g. `https://www.cbioportal.org/index.do`

To display specific tabs in the results page, you can add the tab id to the URL. Examples:
- `http:/<your_server>/index.do`: show the query results in the default oncoprint tab
- `http://<your_server>/index.do#mutation_details`: show the query results in the mutations tab 
- `http://<your_server>/index.do#enrichementTabDiv`: show the query results in the enrichment analysis view
- `http://<your_server>/index.do#plots`: plots view (e.g. mRNA x CNA plots)
- `http://<your_server>/index.do#coexp`: coexpression analysis view

###Parameters
The following parameters should be provided:

**cancer_study_id**
* Description: Used to identify the study in cBioPortal
* Values: \<study_id\>
* Example: `acc_tcga`

**genetic_profile_ids_PROFILE_MUTATION_EXTENDED**
* Description: Used to identify the mutations profile in cBioPortal
* Values: \<study_id\>`_mutations`
* Example: `acc_tcga_mutations`

**case_set_id**
* Description: Used to determine which case list to use or whether to use a custom set of case identifiers
* Values:   
   * When interested in all samples in the study, use \<study_id>`_all`
   * When interested in a custom set of samples or patients, set to `-1`
* Example: `acc_tcga_all`

**case_ids**
* Description: Used to describe the case identifiers
* Values: 
   * When interested in all samples, this parameter is not necessary, or the value should be left empty
   * When interested in a custom set of samples, separate them by a *white space*, e.g. `SAMPLE1 SAMPLE2`
   * When interested in a custom set of patients, separate them by a *white space*, e.g. `PATIENT1 PATIENT2`
      * This does not seem to work. Created an issue asking whether query by patient is possible  (https://github.com/cBioPortal/cbioportal/issues/1073)
* Example: `SAMPLE1 SAMPLE2`

**patient_case_select**
* Description: Describes whether the provided identifiers are sample or patient identifiers
* Values: 
   * When interested in all samples or in a set of samples, set to `sample`
   * When interested in a set of patients, set to `patient`
      * This does not seem to work. Created an issue asking whether query by patient is possible (https://github.com/cBioPortal/cbioportal/issues/1073)
* Example: `sample`

**gene_list**
* Description: List of genes to use in the query
* Values: List with genes separated by a *white space*
* Example: `ZFPM1 MUC5B`

**tab_index**
* Description: Set to `tab_visualize`, which is the main results tab with the oncoprint
* Values: `tab_visualize`
   * :warning: If not used, result page will just be the homepage.
* Example: `tab_visualize`

**Action**
* Description: Decides whether to submit the form, which will lead to the oncoprint or the mutation details view.
* Values:
   * Set to `Submit` to show oncoprint as result page
   * :warning: If not used, result page will just be the homepage.
* Example: `Submit`


###Examples:
Homepage with the acc_tcga study, the mutations profile, 2 genes and a custom case set with one sample:
http://jsfiddle.net/nfTWL/59/

Oncoprint for the acc_tcga study, the mutations profile, 2 genes and a custom case set with one sample.
http://jsfiddle.net/nfTWL/60/

Mutations view for the acc_tcga study, the mutations profile, 2 genes and a custom case set with one sample.
http://jsfiddle.net/nfTWL/64/

Oncoprint for the acc_tcga study, the mutations profile, 2 genes and all samples.
http://jsfiddle.net/nfTWL/62/