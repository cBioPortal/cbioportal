## July 21, 2020
*   **New Feature**: The *Mutations* tab now has the option to show mutation effects for different transcripts / isoforms. Note that some annotation features are only available for the canonical isoform. [example](https://www.cbioportal.org/results/mutations?Action=Submit&RPPA_SCORE_THRESHOLD=2.0&Z_SCORE_THRESHOLD=2.0&cancer_study_list=msk_impact_2017&case_set_id=msk_impact_2017_cnaseq&data_priority=0&gene_list=FGFR2&geneset_list=%20&genetic_profile_ids_PROFILE_COPY_NUMBER_ALTERATION=msk_impact_2017_cna&genetic_profile_ids_PROFILE_MUTATION_EXTENDED=msk_impact_2017_mutations&mutations_transcript_id=ENST00000358487&profileFilter=0&tab_index=tab_visualize)
![image](https://user-images.githubusercontent.com/840895/88306535-d95d6400-ccd8-11ea-9e64-0c6600f65e50.png)

*   **Enhancement**: The *Plots* tab is now supported in multi-study queries. [example](https://www.cbioportal.org/results/plots?Action=Submit&RPPA_SCORE_THRESHOLD=2.0&Z_SCORE_THRESHOLD=2.0&cancer_study_list=laml_tcga_pan_can_atlas_2018%2Cacc_tcga_pan_can_atlas_2018%2Cblca_tcga_pan_can_atlas_2018%2Clgg_tcga_pan_can_atlas_2018%2Cbrca_tcga_pan_can_atlas_2018%2Ccesc_tcga_pan_can_atlas_2018%2Cchol_tcga_pan_can_atlas_2018%2Ccoadread_tcga_pan_can_atlas_2018%2Cdlbc_tcga_pan_can_atlas_2018%2Cesca_tcga_pan_can_atlas_2018%2Cgbm_tcga_pan_can_atlas_2018%2Chnsc_tcga_pan_can_atlas_2018%2Ckich_tcga_pan_can_atlas_2018%2Ckirc_tcga_pan_can_atlas_2018%2Ckirp_tcga_pan_can_atlas_2018%2Clihc_tcga_pan_can_atlas_2018%2Cluad_tcga_pan_can_atlas_2018%2Clusc_tcga_pan_can_atlas_2018%2Cmeso_tcga_pan_can_atlas_2018%2Cov_tcga_pan_can_atlas_2018%2Cpaad_tcga_pan_can_atlas_2018%2Cpcpg_tcga_pan_can_atlas_2018%2Cprad_tcga_pan_can_atlas_2018%2Csarc_tcga_pan_can_atlas_2018%2Cskcm_tcga_pan_can_atlas_2018%2Cstad_tcga_pan_can_atlas_2018%2Ctgct_tcga_pan_can_atlas_2018%2Cthym_tcga_pan_can_atlas_2018%2Cthca_tcga_pan_can_atlas_2018%2Cucs_tcga_pan_can_atlas_2018%2Cucec_tcga_pan_can_atlas_2018%2Cuvm_tcga_pan_can_atlas_2018&case_set_id=all&data_priority=0&gene_list=TP53%2520PTEN%2520BRCA1%2520IGF2%2520EGFR&geneset_list=%20&plots_coloring_selection=%7B%22colorByCopyNumber%22%3A%22false%22%7D&plots_horz_selection=%7B%22selectedGeneOption%22%3A1956%7D&plots_vert_selection=%7B%22logScale%22%3A%22true%22%7D&profileFilter=0&tab_index=tab_visualize)
![image](https://user-images.githubusercontent.com/840895/88309239-07907300-ccdc-11ea-995b-7ddd658ec46e.png)

*   **New Feature**: You can now share custom groups in the *Study View* [example](https://www.cbioportal.org/study/summary?id=msk_impact_2017#sharedGroups=5d092670e4b0ab4137876374,5c99454ee4b0ab4137873dc5)

    <img width="333" alt="" src="https://user-images.githubusercontent.com/840895/88309724-b7fe7700-ccdc-11ea-969a-28ed8551ffd7.png">


## Jun 11, 2020
*   **Added data** consisting of 267 samples from 2 studies:
     * [Gastric Cancer (OncoSG, 2018)](https://www.cbioportal.org/study/summary?id=stad_oncosg_2018) *147 samples* 
     * 120 ctDNA samples added to [Non-Small Cell Lung Cancer (TRACERx, NEJM & Nature 2017)](https://www.cbioportal.org/study/summary?id=nsclc_tracerx_2017) *447 samples* 

## Jun 9, 2020

*   **Enhancement**: using [OQL](https://www.cbioportal.org/oql#oql-modifiers) to query for mutations based on a protein position range. [example](https://www.cbioportal.org/results/mutations?Action=Submit&RPPA_SCORE_THRESHOLD=2.0&Z_SCORE_THRESHOLD=2.0&cancer_study_list=msk_impact_2017&case_set_id=msk_impact_2017_cnaseq&data_priority=0&gene_list=TP53%253AMUT_(95-288*)&geneset_list=%20&genetic_profile_ids_PROFILE_COPY_NUMBER_ALTERATION=msk_impact_2017_cna&genetic_profile_ids_PROFILE_MUTATION_EXTENDED=msk_impact_2017_mutations&profileFilter=0&tab_index=tab_visualize)
![image](https://user-images.githubusercontent.com/840895/84427197-83ce6b80-abf2-11ea-9d18-3a4f4524e545.png)

*   **New Feature**: you can now send the OncoPrint data to the [OncoPrinter tool](https://www.cbioportal.org/oncoprinter) for customization.
![image](https://user-images.githubusercontent.com/840895/84318326-3c85a380-ab3c-11ea-97c1-34343cb3e996.png)

*   **Enhancement**: Mutational spectrum data can be downloaded from OncoPrint
![image](https://user-images.githubusercontent.com/840895/84322695-68585780-ab43-11ea-9224-a965331e35fc.png)


## Jun 2, 2020

*   **Enhancement**: Pediatric cancer studies are now grouped and highlighted in the query page
![image](https://user-images.githubusercontent.com/840895/84318659-c897cb00-ab3c-11ea-8209-93c940321a0f.png)

## May 6, 2020
*   **Added data** consisting of 574 samples from 3 studies:
     * [Uterine Sarcoma/Mesenchymal (MSK, Clin Cancer Res 2020)](https://www.cbioportal.org/study/summary?id=usarc_msk_2020) *108 samples* 
     * [Metastatic castration-sensitive prostate cancer (MSK, Clin Cancer Res 2020)](https://www.cbioportal.org/study/summary?id=prad_mcspc_mskcc_2020) *424 samples* 
    *  [Glioblastoma (Columbia, Nat Med. 2019)](https://www.cbioportal.org/study/summary?id=gbm_columbia_2019) *42 samples* 
*   **Updated one study:**
     *  Expression data was added to [The Metastatic Breast Cancer Project (Provisional, February 2020)](http://www.cbioportal.org/study?id=brca_mbcproject_wagle_2017).

## April 24, 2020
* **New Feature**: Add a new chart on the _Study View_ for selecting samples based on pre-defined case lists: 

    <img width="333" alt="Screen Shot 2020-04-24 at 9 18 25 AM" src="https://user-images.githubusercontent.com/1334004/80216807-a1c51a00-860c-11ea-8ba5-3781bd603ee5.png">

## April 10, 2020
*   **New Feature**: Make cohorts on the _Study View_ using continuous molecular profiles of one or more gene(s), such as mRNA expression, methylation, RPPA and continuous CNA. [example](https://www.cbioportal.org/study/summary?id=brca_tcga#filterJson={"genomicDataFilters":[{"hugoGeneSymbol":"ERBB2","profileType":"mrna_median_Zscores","values":[{"start":1.5,"end":2},{"start":2,"end":2.5},{"start":2.5,"end":3},{"start":3,"end":3.5},{"start":3.5,"end":4},{"start":4}]}],"genomicProfiles":[["mrna_median_Zscores"]],"studyIds":["brca_tcga"]})

    <img src="https://user-images.githubusercontent.com/1334004/79270846-da2a6280-7e6c-11ea-8cb9-b40e7d201ea1.png" width=500 />
    
    Combine this with the group comparison feature to compare e.g. all quartiles of expression:
    
    <img src="https://user-images.githubusercontent.com/1334004/79270618-79028f00-7e6c-11ea-8a30-9eaedee948ca.png" width=400 />

*   **New Feature**: Annotate mutations using the _Mutation Mapper Tool_ on the GRCh38 reference genome:

    [![mutation_mapper_tool_grch38](https://user-images.githubusercontent.com/1334004/79233555-07f4b480-7e37-11ea-9d0a-0cafff434fa5.png)](https://www.cbioportal.org/mutation_mapper)

## April 3, 2020
*   **New Feature**: Extended the _Comparison_ tab to support the comparison of altered samples per gene or alteration. This [example query](https://www.cbioportal.org/results/comparison?Action=Submit&RPPA_SCORE_THRESHOLD=2.0&Z_SCORE_THRESHOLD=2.0&cancer_study_list=msk_impact_2017&case_set_id=msk_impact_2017_Non-Small_Cell_Lung_Cancer&comparison_createdGroupsSessionId=5e8a2d4ae4b0ff7ef5fdd7d0&comparison_selectedGroups=%5B%22EGFR%3A%20AMP%20%26%20MUT%22%2C%22EGFR%3A%20MUT%20only%22%2C%22EGFR%3A%20AMP%20only%22%5D&comparison_subtab=survival&data_priority=0&gene_list=EGFR%253AAMP%250AEGFR%253AMUT_DRIVER&geneset_list=%20&genetic_profile_ids_PROFILE_COPY_NUMBER_ALTERATION=msk_impact_2017_cna&genetic_profile_ids_PROFILE_MUTATION_EXTENDED=msk_impact_2017_mutations&profileFilter=0&tab_index=tab_visualize) compares NSCLC patients with 1) both mutated and amplified EGFR, 2) mutated EGFR only, and 3) amplified EGFR only.

![image](https://user-images.githubusercontent.com/840895/78508085-3ea13f80-7752-11ea-8e87-e486061def12.png)

## March 27, 2020
*   **Enhancement**: User selections in the Plots tab are now saved in the URL. [example](https://www.cbioportal.org/results/plots?Action=Submit&RPPA_SCORE_THRESHOLD=2.0&Z_SCORE_THRESHOLD=2.0&cancer_study_id=coadread_tcga_pub&cancer_study_list=ov_tcga_pan_can_atlas_2018&case_set_id=ov_tcga_pan_can_atlas_2018_cnaseq&data_priority=0&gene_list=TP53&gene_set_choice=user-defined-list&geneset_list=%20&genetic_profile_ids_PROFILE_COPY_NUMBER_ALTERATION=ov_tcga_pan_can_atlas_2018_gistic&genetic_profile_ids_PROFILE_MUTATION_EXTENDED=ov_tcga_pan_can_atlas_2018_mutations&plots_horz_selection=%7B%22dataType%22%3A%22MUTATION_EXTENDED%22%7D&profileFilter=0&tab_index=tab_visualize)

*   **New Feature**: Added table of data availability per profile in the _Study View_. [example](https://www.cbioportal.org/study/summary?id=ucec_tcga_pub)

<img src="https://user-images.githubusercontent.com/840895/78508182-e454ae80-7752-11ea-914b-bac17f725a9f.png" width="300">

## March 20, 2020
*   **Enhancement**: Extended _Survival Analysis_ to support more outcome measures. [example](https://www.cbioportal.org/results/comparison?Action=Submit&RPPA_SCORE_THRESHOLD=2.0&Z_SCORE_THRESHOLD=2.0&cancer_study_list=luad_tcga_pan_can_atlas_2018&case_set_id=luad_tcga_pan_can_atlas_2018_cnaseq&comparison_selectedGroups=%5B%22Unaltered%20group%22%2C%22Altered%20group%22%5D&comparison_subtab=survival&data_priority=0&gene_list=KRAS%250ATP53&geneset_list=%20&genetic_profile_ids_PROFILE_COPY_NUMBER_ALTERATION=luad_tcga_pan_can_atlas_2018_gistic&genetic_profile_ids_PROFILE_MUTATION_EXTENDED=luad_tcga_pan_can_atlas_2018_mutations&profileFilter=0&tab_index=tab_visualize)

![image](https://user-images.githubusercontent.com/840895/78508508-5c23d880-7755-11ea-9e16-36bff32678ad.png)

## March 18, 2020
*   **Added data** consisting of 1,393 samples from 3 studies:
     * [Breast Cancer (Alpelisib plus AI, Nature Cancer 2020)](https://www.cbioportal.org/study/summary?id=breast_alpelisib_2020) *141 samples* 
     * [Glioma (MSKCC, Clin Cancer Res 2019)](https://www.cbioportal.org/study/summary?id=glioma_mskcc_2019) *1,004 samples* 
     * [Mixed cfDNA (MSK, Nature Medicine 2019)](https://www.cbioportal.org/study/summary?id=cfdna_msk_2019) *248 samples* 
     
## March 3, 2020
* **New Feature**: Added Pathways tab to the _Results View_ page, which visualizes the alteration frequencies of genes in pathways of interest. The pathways are pulled from https://www.pathwaymapper.org and shown in a read only view. One can edit these pathways in the PathwayMapper editor. For more information see the [tutorial](https://www.cbioportal.org/tutorials#pathways).

    [![pathwaymapper_screenshot](https://user-images.githubusercontent.com/1334004/76771001-133fbc00-6775-11ea-8b36-82edb1e7be09.png)](https://www.cbioportal.org/results/pathways?Action=Submit&Z_SCORE_THRESHOLD=1.0&cancer_study_id=gbm_tcga_pub&cancer_study_list=gbm_tcga_pub&case_set_id=gbm_tcga_pub_sequenced&gene_list=TP53%20MDM2%20MDM4&gene_set_choice=user-defined_list&genetic_profile_ids_PROFILE_COPY_NUMBER_ALTERATION=gbm_tcga_pub_cna_rae&genetic_profile_ids_PROFILE_MUTATION_EXTENDED=gbm_tcga_pub_mutations)

## Feb 12, 2020
*   **Added data** consisting of 1,605 samples from 3 studies:
     * [Tumors with TRK fusions (MSK, 2019)](https://www.cbioportal.org/study/summary?id=ntrk_msk_2019) *106 samples* 
     * [Lymphoma Cell Lines (MSKCC, 2020)](https://www.cbioportal.org/study/summary?id=lymphoma_cellline_msk_2020) *34 samples* 
     * [Prostate Adenocarcinoma (MSKCC, 2020)](https://www.cbioportal.org/study/summary?id=prad_cdk12_mskcc_2020) *1,465 samples* 

## Feb 6, 2020
* **New Feature**: Extend the [recent group comparison feature](https://www.cbioportal.org/tutorials#group-comparison) by allowing comparisons inside the _Results View_ page. The new tab allows for quick comparison of altered vs unaltered cases by survival, clinical information, mutation, copy number events and mRNA expression:

    [![group_results640px](https://user-images.githubusercontent.com/1334004/74002155-2a041f00-493c-11ea-9867-14740202c368.gif)](https://www.cbioportal.org/results/comparison?Z_SCORE_THRESHOLD=2.0&cancer_study_id=coadread_tcga_pub&cancer_study_list=coadread_tcga_pub&case_set_id=coadread_tcga_pub_nonhypermut&gene_list=KRAS%20NRAS%20BRAF&gene_set_choice=user-defined-list&genetic_profile_ids_PROFILE_COPY_NUMBER_ALTERATION=coadread_tcga_pub_gistic&genetic_profile_ids_PROFILE_MUTATION_EXTENDED=coadread_tcga_pub_mutations)

* **Performance enhancement**: the _Study View_'s mutation table now loads faster for studies with multiple gene panels. For the [genie portal](https://genie.cbioportal.org), which has a study with many different gene panels this resulted in a speed-up from ~90-120 seconds to 5 seconds. 
* Read more about the v3.2.2 release [here](https://github.com/cBioPortal/cbioportal/releases/tag/v3.2.2)

## Jan 30, 2020
* **Enhancement**: Show HGVSg in mutations table and linkout to [Genome Nexus](https://www.genomenexus.org):

    ![hgvsg genome nexus](https://user-images.githubusercontent.com/1334004/73494837-4d026200-4383-11ea-8968-f9a8a6e00675.png)
    
    
* **Enhancement**: Add a pencil button near gene list in results page which opens interface for quickly modifying the oql of the query:

    ![edit query pencil](https://user-images.githubusercontent.com/1334004/73494684-f72dba00-4382-11ea-8bd0-001fdf0cb681.png)


* See more updates [here](https://github.com/cBioPortal/cbioportal/releases/tag/v3.2.1)

## Jan 29, 2020
*   **Added data** consisting of 197 samples from 2 studies:
     * [Bladder/Urinary Tract Cancer (MSK, 2019)](https://www.cbioportal.org/study/summary?id=utuc_pdx_msk_2019) *78 samples* 
     * [Upper Tract Urothelial Carcinoma (MSK, 2019)](https://www.cbioportal.org/study/summary?id=utuc_msk_2019) *119 samples* 

## Dec 19, 2019
* **Enhancement**: We restored support for submitting large queries from external applications using HTTP POST requests.  Accepted parameters are the same
as appear in the url of a query submitted from the homepage.

* See more updates [here](https://github.com/cBioPortal/cbioportal/releases/tag/v3.1.8)

## Dec 12, 2019
* **Enhancement**: Several enhancements to the display of gene panels on the _Patient View_ page, by [The Hyve](https://www.thehyve.nl/), described in more detail [here](https://blog.thehyve.nl/blog/gene-panels-patient-view)

    ![image](https://blog.thehyve.nl/hubfs/gene-panels-patient-view-cbioportal.png)    
* **Enhancement**: Add Count Bubbles to Oncoprint Toolbar

    ![Screenshot from 2019-12-06 11-36-21](https://user-images.githubusercontent.com/20069833/70339336-aa16e700-181c-11ea-94ac-c4acff272e4f.png)
* See more updates [here](https://github.com/cBioPortal/cbioportal/releases/tag/v3.1.7)

## Nov 29, 2019
* **Enhancement**: Support group comparison for custom charts in _Study View_ page
* **Enhancement**: Performance improvement of Co-Expression analysis.
* **Enhancement**: Kaplan-Meier plots now supports custom time range.
* See more updates [here](https://github.com/cBioPortal/cbioportal/releases/tag/v3.1.6)

<img src="https://user-images.githubusercontent.com/840895/69887458-02963380-12b5-11ea-8151-4d531db0fcc5.png" width="500">

## Nov 22, 2019

* **New Feature**: Support for Treatment response data in the Oncoprint and Plots tab, including new *Waterfall* plot type. Read more in [The Hyve's blog post](https://blog.thehyve.nl/blog/using-treatment-response-data-to-find-targeted-therapies-in-cbioportal)

![image](https://blog.thehyve.nl/hubfs/Waterfall%20plots%20cbioportal.png)

## Nov 15, 2019
* **Enhancement**: heatmap tracks in OncoPrint now has separate headers and sub-menus. [example](https://www.cbioportal.org/results/oncoprint?Action=Submit&RPPA_SCORE_THRESHOLD=2.0&Z_SCORE_THRESHOLD=2.0&cancer_study_list=brca_tcga&case_set_id=brca_tcga_protein_quantification&clinicallist=PROFILED_IN_brca_tcga_mutations&data_priority=0&gene_list=ESR1%2520PGR%2520ERBB2&geneset_list=%20&genetic_profile_ids_PROFILE_COPY_NUMBER_ALTERATION=brca_tcga_gistic&genetic_profile_ids_PROFILE_MRNA_EXPRESSION=brca_tcga_mrna_median_Zscores&genetic_profile_ids_PROFILE_MUTATION_EXTENDED=brca_tcga_mutations&genetic_profile_ids_PROFILE_PROTEIN_EXPRESSION=brca_tcga_rppa_Zscores&heatmap_track_groups=brca_tcga_rna_seq_v2_mrna_median_Zscores%2CESR1%2CPGR%2CERBB2%3Bbrca_tcga_protein_quantification_zscores%2CESR1%2CPGR%2CERBB2&tab_index=tab_visualize)

![image](https://user-images.githubusercontent.com/840895/69502724-7fee2c80-0ee0-11ea-81b7-2ada01fc99a2.png)

* **Enhancement**: global settings for query session

<img src="https://user-images.githubusercontent.com/840895/69502843-b6787700-0ee1-11ea-927b-049635a5a8ac.png" width="300">


## Nov 7, 2019

*   **Added data** consisting of 212 samples from 3 studies:
     * [Metastatic Melanoma (DFCI, Science 2015)](https://www.cbioportal.org/study/summary?id=skcm_dfci_2015) *110 samples* 
     * [Melanoma (MSKCC, NEJM 2014)](https://www.cbioportal.org/study/summary?id=skcm_mskcc_2014) *64 samples* 
     * [Metastatic Melanoma (UCLA, Cell 2016)](https://www.cbioportal.org/study/summary?id=mel_ucla_2016) *38 samples* 
     
## Oct 30, 2019

*   **Added data** consisting of 178 samples from 2 studies:
     * [Intrahepatic Cholangiocarcinoma (Shanghai, Nat Commun 2014)](https://www.cbioportal.org/study/summary?id=ihch_smmu_2014) *103 samples* 
     * [Non-Small Cell Lung Cancer (MSK, Cancer Cell 2018)](https://www.cbioportal.org/study/summary?id=nsclc_mskcc_2018) *75 samples* 

## October 23, 2019
* **Enhancement**: Quick example links in Plots tab. [example](https://www.cbioportal.org/results/plots?session_id=5ba11ce6498eb8b3d567e7ed)

## October 14, 2019

* **New Feature**: Fusion Genes table in `Study View`. [example](https://www.cbioportal.org/study/summary?id=msk_impact_2017)

![image](https://user-images.githubusercontent.com/840895/68320157-6ef28e00-008d-11ea-8cb3-e4b54aa1d848.png)

## October 11, 2019
* **Enhancement**: The Download interface on the homepage has been removed. Enhanced download functionality is now available after querying on the results page.

    _Home page_:
    
    ![homepage download tab removed](https://user-images.githubusercontent.com/1334004/66735303-48627e00-ee66-11e9-863f-5ec713ea444a.png)
    
    _Results page_:
    
    ![results page download tab](https://user-images.githubusercontent.com/1334004/66736057-7ea0fd00-ee68-11e9-942b-e123b9659126.png)
    
    Note that as before one can always download the full raw data on the [Data Sets page](https://www.cbioportal.org/datasets) or from [Datahub](https://github.com/cBioPortal/datahub/).
 
## October 9, 2019
*  **Added data** consisting of  2725 samples from 4 studies:
   
     * [Cancer Cell Line Encyclopedia (Broad, 2019)](https://www.cbioportal.org/study/summary?id=ccle_broad_2019) *1739 samples*
    * [Chronic Lymphocytic Leukemia (Broad, Nature 2015)](https://www.cbioportal.org/study/summary?id=cll_broad_2015#summary) *537 samples* 
    * [Rectal Cancer (MSK,Nature Medicine 2019)](https://www.cbioportal.org/study/summary?id=rectal_msk_2019) *339 samples* 
    * [Colon Cancer (CPTAC-2 Prospective, Cell 2019)](https://www.cbioportal.org/study/summary?id=coad_cptac_2019) *110 samples*

  *  **Updated**  [Esophageal Carcinoma (TCGA, Nature 2017)](https://www.cbioportal.org/study/summary?id=stes_tcga_pub#summary) with addition of CNA data for Esophageal Squamous Cell Carcinoma cases *90 samples*. 

## September 18, 2019
* **New Feature**: The list and order of charts of a study will be automatically saved now as a user preference on the _study view_ page.

## September 6, 2019
*   **Added data** consisting of 1216 samples from 3 studies:
    * [Breast Cancer (MSKCC, 2019)](https://www.cbioportal.org/study/summary?id=brca_mskcc_2019) *70 samples* 
    * [Brain Tumor PDXs (Mayo Clinic, 2019)](https://www.cbioportal.org/study/summary?id=gbm_mayo_pdx_sarkaria_2019)*97 samples*
    * [Adenoid Cystic Carcinoma Project (2019)](https://www.cbioportal.org/study/summary?id=acc_2019) *1049 samples* 

## August 13, 2019
*   **Added data** consisting of 295 samples from 3 studies:
    *  [Pediatric Preclinical Testing Consortium (PPTC, 2019)](https://www.cbioportal.org/study/summary?id=pptc_2019) *261 samples*
    *  [Non-small cell lung cancer (MSK, Science 2015)](https://www.cbioportal.org/study/summary?id=nsclc_mskcc_2015) *16 samples*
    *  [Prostate Cancer (MSK, 2019)](https://www.cbioportal.org/study/summary?id=prad_msk_2019) *18 samples*

## July 26, 2019
*   **Added data** consisting of 35 samples from 1 study:
    *  [Clear Cell Renal Cell Carcinoma (DFCI, Science 2019)](https://www.cbioportal.org/study/summary?id=ccrcc_dfci_2019) *35 samples*
*   Added Hypoxia data for:
    *  [Breast Invasive Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study/summary?id=brca_tcga_pan_can_atlas_2018)
    *  [Colorectal Adenocarcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study/summary?id=coadread_tcga_pan_can_atlas_2018)
    *  [Cervical Squamous Cell Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study/summary?id=cesc_tcga_pan_can_atlas_2018)
    *  [Glioblastoma Multiforme (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study/summary?id=gbm_tcga_pan_can_atlas_2018)
    *  [Head and Neck Squamous Cell Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study/summary?id=hnsc_tcga_pan_can_atlas_2018)
    *  [Kidney Renal Clear Cell Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study/summary?id=kirc_tcga_pan_can_atlas_2018)
    *  [Kidney Renal Papillary Cell Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study/summary?id=kirp_tcga_pan_can_atlas_2018)
    *  [Brain Lower Grade Glioma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study/summary?id=lgg_tcga_pan_can_atlas_2018)
    *  [Liver Hepatocellular Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study/summary?id=lihc_tcga_pan_can_atlas_2018)
    *  [Lung Adenocarcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study/summary?id=luad_tcga_pan_can_atlas_2018)
    *  [Lung Squamous Cell Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study/summary?id=lusc_tcga_pan_can_atlas_2018)
    *  [Ovarian Serous Cystadenocarcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study/summary?id=ov_tcga_pan_can_atlas_2018)
    *  [Pancreatic Adenocarcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study/summary?id=paad_tcga_pan_can_atlas_2018)
    *  [Pheochromocytoma and Paraganglioma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study/summary?id=pcpg_tcga_pan_can_atlas_2018)
    *  [Prostate Adenocarcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study/summary?id=prad_tcga_pan_can_atlas_2018)
    *  [Skin Cutaneous Melanoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study/summary?id=skcm_tcga_pan_can_atlas_2018)
    *  [Thyroid Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study/summary?id=thca_tcga_pan_can_atlas_2018)
    *  [Uterine Corpus Endometrial Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study/summary?id=ucec_tcga_pan_can_atlas_2018)
## July 24, 2019
*   **Added data** consisting of 151 samples from 1 study:
    *  [Myeloproliferative Neoplasms (CIMR, NEJM 2013)](https://www.cbioportal.org/study/summary?id=mpn_cimr_2013) *151 samples*
## July 13, 2019
 *   **Public Release 6.1 of AACR Project GENIE:**
     *   The sixth data set, GENIE 6.0-public, was released in early July 2019.  A patch to GENIE 6.0-public, GENIE 6.1-pubic, was subsequently released on July 13, 2019.  The combined data set now includes nearly 70,000 de-identified genomic records collected from patients who were treated at each of the consortium's participating institutions, making it among the largest fully public cancer genomic data sets released to date. The combined data set now includes data for nearly 80 major cancer types, including data from nearly 11,000 patients with lung cancer, greater than  9,700 patients with breast cancer, and nearly 7,000 patients with colorectal cancer.
 *   More detailed information can be found in the [AACR GENIE Data Guide](http://www.aacr.org/Research/Research/Documents/GENIE%20Data%20Guide.pdf).  In addition to accessing the data via the cBioPortal, users can download the data directly from [Sage Bionetworks](http://synapse.org/genie). Users will need to create an account for either site and agree to the [terms of access](http://www.aacr.org/Documents/Terms%20of%20Access.pdf).
 *   For frequently asked questions, visit the [AACR FAQ page](http://www.aacr.org/Research/Research/Pages/aacr-project-genie.aspx).
## July 2, 2019
*   **Added data** consistng of 785 samples from 4 studies:
    *  [Non-Small Cell Lung Cancer (TRACERx, NEJM 2017)](https://www.cbioportal.org/study/summary?id=nsclc_tracerx_2017) *327 samples* 
    *  [Acute myeloid leukemia or myelodysplastic syndromes (WashU, 2016)](https://www.cbioportal.org/study/summary?id=mnm_washu_2016) *136 samples* 
    *  [Basal Cell Carcinoma (UNIGE, Nat Genet 2016)](https://www.cbioportal.org/study/summary?id=bcc_unige_2016) *293 samples* 
    *  [Colon Adenocarcinoma (CaseCCC, PNAS 2015)](https://www.cbioportal.org/study/summary?id=coad_caseccc_2015) *29 samples* 

## June 19, 2019
* **New Feature**: Show *Genome Aggregation Database (gnomAD)* population frequencies in the mutations table - see [example](http://bit.ly/2ISHgiu):
    
    ![gnomad feature news](https://user-images.githubusercontent.com/1334004/59794400-e07c9c00-92a6-11e9-97ea-a79bfc8f3885.gif)

## June 12, 2019
 *   **Added data**  of 1350 samples from 3 studies:
     *  [Pheochromocytoma and Paraganglioma (TCGA, Cell 2017)](https://www.cbioportal.org/study?id=pcpg_tcga_pub) *178 samples*
     *  [Metastatic Solid Cancers (UMich, Nature 2017)](https://www.cbioportal.org/study?id=metastatic_solid_tumors_mich_2017) *500 samples*
     *   [Acute Myeloid Leukemia (OHSU, Nature 2018)](https://www.cbioportal.org/study/summary?id=aml_ohsu_2018) *672 samples*
 *    Added survival data for TCGA PanCan Atlas Cohorts (>10,000 samples across 33 tumor types).
 *    Added hypoxia data for [Bladder Urothelial Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study/summary?id=blca_tcga_pan_can_atlas_2018)
## June 7, 2019
* **New Group Comparison Feature**: Compare clinical and genomic features of user-defined groups of samples/patients. [View Tutorial](https://www.cbioportal.org/tutorials#group-comparison)

[![group-comparison](https://user-images.githubusercontent.com/840895/59052073-40f9eb00-885c-11e9-9ddb-6d036533e5f5.png)](https://www.cbioportal.org/comparison/mutations?sessionId=5cf92520e4b0ab4137874376)


## May 8, 2019
* **New Feature**: Show *Post Translational Modification (PTM)* information from [dbPTM](http://dbptm.mbc.nctu.edu.tw/) on the _Mutation Mapper_ - see [example](https://bit.ly/2VVrWdi):
    
    ![ptm feature_news](https://user-images.githubusercontent.com/1334004/57391288-34667200-718c-11e9-9fcc-b849542c74b3.gif)

## April 26, 2019
 *   **Added data**  of 568 samples from 4 studies:
     *  [Adenoid Cystic Carcinoma (JHU, Cancer Prev Res 2016)](https://www.cbioportal.org/study?id=acyc_jhu_2016) *25 samples*
     *  [Histiocytosis Cobimetinib (MSK, Nature 2019)](https://www.cbioportal.org/study?id=histiocytosis_cobi_msk_2019) *52 samples*
     *  [Upper Tract Urothelial Carcinoma (Cornell/Baylor/MDACC, Nat Comm 2019)](https://www.cbioportal.org/study?id=utuc_cornell_baylor_mdacc_2019) *47 samples*
     *  [Metastatic Prostate Adenocarcinoma (SU2C/PCF Dream Team, PNAS 2019)](https://www.cbioportal.org/study?id=prad_su2c_2019) *444 samples*

## March 29, 2019
* **New Feature**: Use the new quick search tab on the homepage to more easily navigate to a study, gene or patient:

    ![quick_search_news](https://user-images.githubusercontent.com/1334004/55113078-8f4c7a00-50b4-11e9-9d95-e9a6e1dcda52.gif)
   
## March 15, 2019
 *   **Added data**  of 338 samples from 4 studies:  
     *  [Adenoid Cystic Carcinoma (MGH, Nat Gen  2016)](https://www.cbioportal.org/study?id=acyc_mgh_2016) *10 samples*
     *   [Gallbladder Cancer (MSK, Cancer 2018)](https://www.cbioportal.org/study?id=gbc_msk_2018) *103 samples*
     *   [The Metastatic Prostate Cancer Project (Provisional, December 2018)](https://www.cbioportal.org/study?id=prad_mpcproject_2018) *19 samples*
     *   [Adult Soft Tissue Sarcomas (TCGA, Cell 2017)](https://www.cbioportal.org/study?id=sarc_tcga_pub) *206 samples*

## February 22, 2019
* **Enhancement**: Exon number and HGVSc annotations are available in optional columns in the Mutations tab on the _Results_ page and in the _Patient View_.
* **New feature**: option to a show regression line in the scatter plot in the Plots tab on the _Results_ page

![image](https://user-images.githubusercontent.com/840895/53193265-99271d00-35de-11e9-8ffc-352afc2ba906.png)

## February 19, 2019
* **New feature**: *Copy-Number Segments tab* on the _Study View_ page using [igv.js v2](https://github.com/igvteam/igv.js) - see [example](https://www.cbioportal.org/study?id=ucec_tcga_pub&tab=cnSegments)
* Improved *Copy-Number Segments* tab on the _Results_ page
* **New feature**: *OncoKB* and *Cancer Hotspots tracks* in the Mutations tab on the _Results_ page

![image](https://user-images.githubusercontent.com/840895/53054860-d1f6b300-3473-11e9-8178-1df10eed36fc.png)


## January 24, 2019
 *   **Added data**  of 2328 samples from 8 studies:
     *   [Uveal Melanoma (QIMR, Oncotarget 2016)](https://www.cbioportal.org/study?id=um_qimr_2016#summary) *28 samples*
     *   [Squamous Cell Carcinoma of the Vulva (CUK, Exp Mol Med 2018)](https://www.cbioportal.org/study?id=vsc_cuk_2018) *15 samples*
     *   [TMB and Immunotherapy (MSKCC, Nat Genet 2019)](https://www.cbioportal.org/study?id=tmb_mskcc_2018#summary) *1661 samples*
     *   [Glioma (MSK, 2018)](https://www.cbioportal.org/study?id=glioma_msk_2018) *91 samples*
     *   [Urothelial Carcinoma (Cornell/Trento, Nat Gen 2016)](https://www.cbioportal.org/study?id=blca_cornell_2016) *72 samples*
     *   [Hepatocellular Carcinoma (MSK, Clin Cancer Res 2018)](https://www.cbioportal.org/study?id=hcc_mskimpact_2018) *127 samples*
     *   [MSK Thoracic PDX (MSK, Provisional)](https://www.cbioportal.org/study?id=lung_msk_pdx) *139 samples*
     *   [Cholangiocarcinoma (MSK, Clin Cancer Res 2018)](https://www.cbioportal.org/study?id=chol_msk_2018) *195 samples*
 *  **Updated data** for [The Metastatic Breast Cancer Project (Provisional, October 2018)](https://www.cbioportal.org/study?id=brca_mbcproject_wagle_2017) *237 samples*
## January 10, 2019
* cBioPortal now supports queries for driver mutations, fusions and copy number alterations as well as germline/somatic mutations using [Onco Query Language (OQL)](https://www.cbioportal.org/oql) -- see [example](https://www.cbioportal.org/results/oncoprint?session_id=5c23ea81e4b05228701f9d44)
* A new [tutorial](https://www.cbioportal.org/tutorials) explores OQL and provides examples of how OQL can be a powerful tool to refine queries.


## December 17, 2018
* The 10th phase of cBioPortal architectural upgrade is now complete: the _Study View_ has been moved to the new architecture with numerous improvements. This marks the completion of the cBioPortal architectural refactoring! 🎉🎉🎉

[![image](https://user-images.githubusercontent.com/840895/50120599-11aadf80-0224-11e9-9f9f-adae0ed42c72.png)](https://www.cbioportal.org/study?id=msk_impact_2017)

## October 29, 2018
* The ninth phase of the cBioPortal architectural upgrade is now complete: the _results page_ is now a single-page application with better performance.
* Supported plotting mutations by type in _Plots_ tab

![image](https://user-images.githubusercontent.com/840895/47598695-d996c700-d96d-11e8-9371-9e591cf25ea3.png)

## October 19, 2018

* Support selection of transcript of interest in the [MutationMapper tool](https://www.cbioportal.org/mutation_mapper.jsp) via [Genome Nexus](https://www.genomenexus.org).

![mutation_mapper_dropdown](https://user-images.githubusercontent.com/1334004/47240017-f99a1980-d3b4-11e8-99a4-e8c73b25ecec.png)

## October 17, 2018
 *   **Added data**  of 3578 samples from 8 studies:
     *   [Rhabdoid Cancer (BCGSC, Cancer Cell 2016)](https://www.cbioportal.org/study?id=mrt_bcgsc_2016#summary) *40 samples*
     *   [Diffuse Large B-Cell Lymphoma (Duke, Cell 2017)](https://www.cbioportal.org/study?id=dlbcl_duke_2017#summary) *1001 samples*
     *   [Diffuse Large B cell Lymphoma (DFCI, Nat Med 2018)](https://www.cbioportal.org/study?id=dlbcl_dfci_2018#summary) *135 samples*
     *   [Breast Fibroepithelial Tumors (Duke-NUS, Nat Genet 2015)](https://www.cbioportal.org/study?id=bfn_duke_nus_2015#summary) *22 samples*
     *   [Uterine Clear Cell Carcinoma (NIH, Cancer 2017)](https://www.cbioportal.org/study?id=uccc_nih_2017#summary) *16 samples*
     *   [Endometrial Cancer (MSK, 2018)](https://www.cbioportal.org/study?id=ucec_msk_2018#summary) *197 samples*
     *   [Breast Cancer (MSK, Cancer Cell 2018)](https://www.cbioportal.org/study?id=breast_msk_2018#summary) *1918 samples*
     *   [MSS Mixed Solid Tumors (Van Allen, 2018)](https://www.cbioportal.org/study?id=mixed_allen_2018#summary) *249 samples*
*   Updated data for [The Angiosarcoma Project (Provisional, September 2018)](https://www.cbioportal.org/study?id=angs_project_painter_2018#summary) *48 samples*
## Auguest 20, 2018
 *   Now you can log in on the public cBioPortal with your Google account and save your virtual studies for quick analysis.

![image](https://user-images.githubusercontent.com/840895/44370286-902a8700-a4a7-11e8-9c8f-dfda87fbd66b.png)

## August 7, 2018
 *   The eighth phase of the cBioPortal architectural upgrade is now complete: The _Plots_, _Expression_, _Network_, and _Bookmarks_ tabs, and therefore all analysis tabs in the _results page_, have been moved to the new architecture.
 *   Updated the [MutationMapper tool](https://www.cbioportal.org/mutation_mapper.jsp), now connecting to [Genome Nexus](https://genomenexus.org/) for annotating mutations on the fly.
 *   _Total Mutations_ and _Fraction Genome Altered_ are now available in _Plots_ tab for visualization and analysis.
 *   Enhanced clinical attribute selector for OncoPrint, now showing sample counts per attribute.
 
 ![image](https://user-images.githubusercontent.com/840895/43609786-ec946d64-9672-11e8-8218-4e63bd5007e8.png)


## July 27, 2018
 *   **Added data**  of 2787 samples from 10 studies:
     *   [Mixed Tumors (PIP-Seq 2017)](https://www.cbioportal.org/study?id=mixed_pipseq_2017#summary) *103 samples*
     *   [Nonmuscle Invasive Bladder Cancer (MSK Eur Urol 2017)](https://www.cbioportal.org/study?id=blca_nmibc_2017#summary) *105 samples*
     *   [Pediatric Neuroblastoma (TARGET, 2018)](https://www.cbioportal.org/study?id=nbl_target_2018_pub) *1089 samples*
     *   [Pediatric Pan-Cancer (DKFZ - German Cancer Consortium, 2017)](https://www.cbioportal.org/study?id=pediatric_dkfz_2017) *961 samples*
     *   [Skin Cutaneous Melanoma (Broad, Cancer Discov 2014)](https://www.cbioportal.org/study?id=skcm_broad_brafresist_2012) *78 samples*
     *   [Cutaneous Squamous Cell Carcinoma (MD Anderson, Clin Cancer Res 2014)](https://www.cbioportal.org/study?id=cscc_hgsc_bcm_2014#summary) *39 samples*
     *   [Diffuse Large B-cell Lymphoma (BCGSC, Blood 2013)](https://www.cbioportal.org/study?id=nhl_bcgsc_2013#summary) *53 samples*
     *   [Non-Hodgkin Lymphoma (BCGSC, Nature 2011)](https://www.cbioportal.org/study?id=nhl_bcgsc_2011#summary) *14 samples* 
     *   [Chronic lymphocytic leukemia (ICGA, Nat 2011)](https://www.cbioportal.org/study?id=cllsll_icgc_2011#summary) *105 samples* 
     *   [Neuroblastoma (Broad Institute 2013)](https://www.cbioportal.org/study?id=nbl_broad_2013#summary) *240 samples* 

## June 20, 2018
 *   The seventh phase of the cBioPortal architectural upgrade is now complete: The _Enrichments_ and _Co-Expression_ tabs have been moved to the new architecture.
 *   Supported merged gene tracks in OncoPrint and [Onco Query Language](https://www.cbioportal.org/onco_query_lang_desc.jsp) -- see [example](https://www.cbioportal.org/index.do?session_id=5b2bedca498eb8b3d566ab60)
 
![image](https://user-images.githubusercontent.com/840895/41738366-def14850-755f-11e8-8a7c-deb789dc03da.png)

## May 10, 2018
 *   Enhanced OncoPrint to show germline mutations -- see [example](https://www.cbioportal.org/index.do?session_id=5af5a304498eb8b3d56615b6)
 
 ![image](https://user-images.githubusercontent.com/840895/39926636-c3304d8c-54fd-11e8-9cb7-76430c8772d7.png)

## April 17, 2018
 *   **Added data**  of 3732 samples from 4 TARGET studies:
     *   [Pediatric Acute Lymphoid Leukemia - Phase II (TARGET, 2018)](https://www.cbioportal.org/study?id=all_phase2_target_2018_pub#summary) *1978 samples*
     *   [Pediatric Acute Myeloid Leukemia (TARGET, 2018)](https://www.cbioportal.org/study?id=aml_target_2018_pub#summary) *1025 samples*
     *   [Pediatric Rhabdoid Tumor (TARGET, 2018)](https://www.cbioportal.org/study?id=rt_target_2018_pub#summary) *72 samples*
     *   [Pediatric Wilms' Tumor (TARGET, 2018)](https://www.cbioportal.org/study?id=wt_target_2018_pub#summary) *657 samples*    
 *   **Added data**  of 3416 samples from 10 published studies:
     *    [Prostate Adenocarcinoma (MSKCC/DFCI, Nature Genetics 2018)](https://www.cbioportal.org/study?id=prad_p1000#summary) *1013 samples*
     *    [Prostate Adenocarcinoma (EurUrol, 2017)](https://www.cbioportal.org/study?id=prad_eururol_2017#summary) *65 samples*
     *    [Non-Small Cell Lung Cancer (MSK, JCO 2018)](https://www.cbioportal.org/study?id=nsclc_pd1_msk_2018#summary) *240 samples*
     *    [Small-Cell Lung Cancer (Multi-Institute 2017)](https://www.cbioportal.org/study?id=sclc_cancercell_gardner_2017#summary) *20 samples*
     *    [The Angiosarcoma Project (Provisional, February 2018)](https://www.cbioportal.org/study?id=angs_project_painter_2018#summary) *14 samples*
     *    [Acute Lymphoblastic Leukemia (St Jude, Nat Genet 2016)](https://www.cbioportal.org/study?id=all_stjude_2016#summary) *73 samples*
     *    [Updated Segment data and Allele Frequencies for The Metastatic Breast Cancer Project (Provisional, October 2017)](https://www.cbioportal.org/study?id=brca_mbcproject_wagle_2017#summary) *103 samples*
     *    [Colorectal Cancer (MSK, Cancer Cell 2018)](https://www.cbioportal.org/study?id=crc_msk_2018#summary) *1134 samples*
     *    [Metastatic Esophagogastric Cancer (MSK,Cancer Discovery 2017)](https://www.cbioportal.org/study?id=egc_msk_2017#summary) *341 samples*
     * [Bladder Cancer (TCGA, Cell 2017)](https://www.cbioportal.org/study?id=blca_tcga_pub_2017#summary) *413 samples*

## April 05, 2018
 *   **Added data** from the TCGA PanCanAtlas project with >10,000 samples from 33 tumor types:
     *   [Adrenocortical Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=acc_tcga_pan_can_atlas_2018)
     *   [Bladder Urothelial Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=blca_tcga_pan_can_atlas_2018)
     *   [Breast Invasive Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=brca_tcga_pan_can_atlas_2018)
     *   [Cervical Squamous Cell Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=cesc_tcga_pan_can_atlas_2018)
     *   [Colon Adenocarcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=coad_tcga_pan_can_atlas_2018)
     *   [Cholangiocarcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=chol_tcga_pan_can_atlas_2018)
     *   [Diffuse Large B-Cell Lymphoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=dlbc_tcga_pan_can_atlas_2018)
     *   [Esophageal Adenocarcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=esca_tcga_pan_can_atlas_2018)
     *   [Glioblastoma Multiforme (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=gbm_tcga_pan_can_atlas_2018)
     *   [Head and Neck Squamous Cell Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=hnsc_tcga_pan_can_atlas_2018)
     *   [Kidney Chromophobe (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=kich_tcga_pan_can_atlas_2018)
     *   [Kidney Renal Clear Cell Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=kirc_tcga_pan_can_atlas_2018)
     *   [Kidney Renal Papillary Cell Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=kirp_tcga_pan_can_atlas_2018)
     *   [Acute Myeloid Leukemia (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=laml_tcga_pan_can_atlas_2018)
     *   [Brain Lower Grade Glioma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=lgg_tcga_pan_can_atlas_2018)
     *   [Liver Hepatocellular Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=lihc_tcga_pan_can_atlas_2018)
     *   [Lung Adenocarcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=luad_tcga_pan_can_atlas_2018)
     *   [Lung Squamous Cell Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=lusc_tcga_pan_can_atlas_2018)
     *   [Mesothelioma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=meso_tcga_pan_can_atlas_2018)
     *   [Ovarian Serous Cystadenocarcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=ov_tcga_pan_can_atlas_2018)
     *   [Pancreatic Adenocarcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=paad_tcga_pan_can_atlas_2018)
     *   [Pheochromocytoma and Paraganglioma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=pcpg_tcga_pan_can_atlas_2018)
     *   [Prostate Adenocarcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=prad_tcga_pan_can_atlas_2018)
     *   [Rectum Adenocarcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=read_tcga_pan_can_atlas_2018)
     *   [Sarcoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=sarc_tcga_pan_can_atlas_2018)
     *   [Skin Cutaneous Melanoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=skcm_tcga_pan_can_atlas_2018)
     *   [Stomach Adenocarcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=stad_tcga_pan_can_atlas_2018)
     *   [Testicular Germ Cell Tumors (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=tgct_tcga_pan_can_atlas_2018)
     *   [Thyroid Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=thca_tcga_pan_can_atlas_2018)
     *   [Thymoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=thym_tcga_pan_can_atlas_2018)
     *   [Uterine Corpus Endometrial Carcinoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=ucec_tcga_pan_can_atlas_2018)
     *   [Uterine Carcinosarcoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=ucs_tcga_pan_can_atlas_2018)
     *   [Uveal Melanoma (TCGA, PanCancer Atlas)](https://www.cbioportal.org/study?id=uvm_tcga_pan_can_atlas_2018)

## March 20, 2018
 *   The sixth phase of the cBioPortal architectural upgrade is now complete: The _Download_ tab has been moved to the new architecture.
 *   Data can now be downloaded in [tabular format](http://blog.thehyve.nl/blog/downloading-data-from-the-cbioportal-oncoprint-view) from OncoPrint.
 *   Added an option to download an SVG file on the _Cancer Type Summary_ tab.

 ![image](https://ptpb.pw/jMli.png)

## January 15, 2018
 *   The fifth phase of the cBioPortal architectural upgrade is now complete: The OncoPrint and Survival tabs have been moved to the new architecture.

## November 20, 2017
 *   You can now combine multiple studies and view them on the study summary page. [Example: liver cancer studies](https://www.cbioportal.org/study?id=liad_inserm_fr_2014%2Chcc_inserm_fr_2015%2Clihc_amc_prv%2Clihc_riken%2Clihc_tcga)
 *   You can now bookmark or share your selected samples as virtual studies with the _share_ icon on the study summary page. [Example: a virtual study of breast tumors](https://www.cbioportal.org/study?id=5a12fd57498eb8b3d5605cd4)
 *   Cross-study query reimplemented: Now you can view an OncoPrint of multiple studies. [Example: querying NSCLC tumors from 5 studies](https://www.cbioportal.org/index.do?session_id=5a135bee498eb8b3d5605d01)
 
 ![image](https://user-images.githubusercontent.com/840895/33045546-3c8a636c-ce1b-11e7-9f52-7060c89a8dfd.png)
 
## October 17, 2017
 *   The fourth phase of the cBioPortal architectural upgrade is now complete: The _Mutual Exclusivity_ and _Cancer Type Summary_ tabs have been moved to the new architecture.
 *   Updated protein structure alignment data in Mutations tab are now retrieved from Genome Nexus via the [G2S web service](https://g2s.genomenexus.org/).

## October 2, 2017
 *   **Added data** of 1646 samples from 7 published studies:
     *   [NGS in Anaplastic Oligodendroglioma and Anaplastic Oligoastrocytomas tumors (MSK, Neuro Oncol 2017)](https://www.cbioportal.org/study?id=odg_msk_2017#summary)   *22 samples*
     *   [MSK-IMPACT Clinical Sequencing Cohort for Non-Small Cell Cancer (MSK, Cancer Discovery 2017)](https://www.cbioportal.org/study?id=lung_msk_2017) *915 samples*
     *   [Paired-exome sequencing of acral melanoma (TGEN, Genome Res 2017)](https://www.cbioportal.org/study?id=mel_tsam_liang_2017#summary) *38 samples*
     *   [MSK-IMPACT Clinical Sequencing Cohort in Prostate Cancer (MSK, JCO Precision Oncology 2017)](https://www.cbioportal.org/study?id=prad_mskcc_2017#summary) *504 samples*
     *   [Whole-exome sequences (WES) of pretreatment melanoma tumors (UCLA, Cell 2016)](https://www.cbioportal.org/study?id=skcm_ucla_2016#summary) *39 samples*
     *   [Next generation sequencing (NGS) of pre-treatment metastatic melanoma samples (MSK, JCO Precision Oncology 2017)](https://www.cbioportal.org/study?id=skcm_vanderbilt_mskcc_2015#summary) *66 samples*
     *   [Targeted gene sequencing in 62 high-grade primary Unclassified Renal Cell Carcinoma (MSK, Nature 2016)](https://www.cbioportal.org/study?id=urcc_mskcc_2016#summary) *62 samples*
 *   **Updated data** for [MSK-IMPACT Clinical Sequencing Cohort (MSK, Nat Med 2017)](https://www.cbioportal.org/study?id=msk_impact_2017) with overall survival data.
## August 3, 2017
 *   The third phase of the cBioPortal architectural upgrade is now complete: The _Mutations_ tab now has a fresh look and faster performance -- see [example](https://www.cbioportal.org/index.do?session_id=598386e5498e5df2e29376ab&show_samples=false&#mutation_details&)
 
 ![image](https://user-images.githubusercontent.com/840895/28942244-9ea43bee-7868-11e7-9b24-0aaf0f9c010d.png)
 
 *   Variant interpretations from the [CIViC database](https://civic.genome.wustl.edu) are now integrated into the annotation columns on the Mutations tab and in the patient view pages
 *   New summary graph for all cancer studies and samples on the front page
 

## June 26, 2017
 *   The second phase of the cBioPortal architectural upgrade is now complete: The query interface now has a fresh look and faster performance. 
 
  ![image](https://user-images.githubusercontent.com/840895/27399281-3ba5357a-568a-11e7-9730-d230285d7805.png)

## May 12, 2017
 *   **Added data** of 12,211 samples from 11 published studies:
     *   [MSK-IMPACT Clinical Sequencing Cohort (MSK, Nat Med 2017)](https://www.cbioportal.org/study?id=msk_impact_2017)   *10,945 samples*
     *   [Whole-genome sequencing of pilocytic astrocytomasatic (DKFZ, Nat Genetics, 2013)](https://www.cbioportal.org/study?id=past_dkfz_heidelberg_2013) *96 samples*
     *   [Hepatocellular Carcinomas (INSERM, Nat Genet 2015)](https://www.cbioportal.org/study?id=hcc_inserm_fr_2015) *243 samples*
     *   [Cystic Tumor of the Pancreas (Johns Hopkins, PNAS 2011)](https://www.cbioportal.org/study?id=pact_jhu_2011) *32 samples*
     *   [Whole-Genome Sequencing of Pancreatic Neuroendocrine Tumors (ARC- Net, Nature, 2017)](https://www.cbioportal.org/study?id=panet_arcnet_2017) *98 samples*
     *   [Medulloblastoma (Sickkids, Nature 2016)](https://www.cbioportal.org/study?id=mbl_sickkids_2016) *46 samples*
     *   [Genetic Characterization of NSCLC young adult patients (University of Turin, Lung Cancer 2016)](https://www.cbioportal.org/study?id=nsclc_unito_2016) *41 samples*
     *   [Genomic Profile of Patients with Advanced Germ Cell Tumors (MSK, JCO 2016).](https://www.cbioportal.org/study?id=gct_msk_2016) *180 samples*
     *   [Ampullary Carcinoma (Baylor, Cell Reports 2016)](https://www.cbioportal.org/study?id=ampca_bcm_2016) *160 samples*
     *   [Mutational profiles of metastatic breast cancer (INSERM, 2016)](https://www.cbioportal.org/study?id=brca_igr_2015) *216 samples*
     *   [Prostate Adenocarcinoma (Fred Hutchinson CRC, Nat Med 2016)](https://www.cbioportal.org/study?id=prad_fhcrc) *154 samples*


## May 5, 2017
*   First phase of cBioPortal architectural upgrade complete: Patient view now has fresh look and faster performance. [example](http://bit.ly/2pNY961)

## March 28, 2017
*   **New features**:
    * Per-sample mutation spectra are now available in OncoPrints -- see [example](https://www.cbioportal.org/index.do?cancer_study_list=ucec_tcga_pub&cancer_study_id=ucec_tcga_pub&genetic_profile_ids_PROFILE_MUTATION_EXTENDED=ucec_tcga_pub_mutations&genetic_profile_ids_PROFILE_COPY_NUMBER_ALTERATION=ucec_tcga_pub_gistic&Z_SCORE_THRESHOLD=2.0&data_priority=0&case_set_id=ucec_tcga_pub_manuscript&case_ids=&patient_case_select=sample&gene_set_choice=user-defined-list&gene_list=POLE%0D%0AERBB2%0D%0AKRAS%0D%0ACTNNB1&clinical_param_selection=null&tab_index=tab_visualize&Action=Submit&show_samples=false&clinicallist=SUBTYPE,NO_CONTEXT_MUTATION_SIGNATURE,%23%20mutations&)
    
    ![image](https://cloud.githubusercontent.com/assets/840895/24209131/d34e33b2-0efb-11e7-945a-fd2b3c66d195.png)
    
    * mRNA heat map clustering is now supported in OncoPrints
    * MDACC Next-Generation Clustered Heat Maps are now available in the patient view
    * cBioPortal web site style change


## Feburary 2, 2017
*   **New features**:
    * 3D hotspot mutation annotations are now available from 3dhotspots.org
*   **New data**:
    * CPTAC proteomics data have been integrated for TCGA breast, ovarian, and colorectal provisional studies
    

## December 23, 2016
*   **New features**:
    * Heat map visualization of gene expression data in the OncoPrint

    ![OncoPrint Heatmap](https://cloud.githubusercontent.com/assets/840895/21407479/81f8c008-c79e-11e6-9b82-3f4eb8f8e1ea.png)
    
    * Heat map visualization of gene expression data in the Study View page connecting to MDACC's TCGA Next-Generation Clustered Heat Map Compendium

## October 7, 2016
*   **New features**:
    * All data sets can now be downloaded as flat files from the new [Data Hub](https://github.com/cBioPortal/datahub)
    * Annotation of putative driver missense mutations in OncoPrints, based on [OncoKB](https://www.oncokb.org), mutation hotspots, and recurrence in cBioPortal and COSMIC
    
    ![OncoPrint-OncoKB](https://cloud.githubusercontent.com/assets/840895/19208804/094dd320-8ccd-11e6-8012-f30104b62ff4.png)
    
    * Copy number segments visualization directly in the browser in a new *CN Segments* tab via [IGV.js](http://igv.org/)
    
    ![image](https://cloud.githubusercontent.com/assets/840895/19200747/ad03c824-8c98-11e6-8051-91fc40e1dc56.png)
    
*   **Improvements**:
    * Improved cancer study view page (bug fixes and increased performance)

## July 24, 2016
*   **Added data** of 4,375 samples from 21 published studies:
    *   [Adenoid Cystic Carcinoma (MDA, Clin Cancer Res 2015)](https://www.cbioportal.org/study?id=acyc_mda_2015) *102 samples*
    *   [Adenoid Cystic Carcinoma (FMI, Am J Surg Pathl. 2014)](https://www.cbioportal.org/study?id=acyc_fmi_2014) *28 samples*
    *   [Adenoid Cystic Carcinoma (Sanger/MDA, JCI 2013)](https://www.cbioportal.org/study?id=acyc_sanger_2013) *24 samples*
    *   [Adenoid Cystic Carcinoma of the Breast (MSKCC, J Pathol. 2015)](https://www.cbioportal.org/study?id=acbc_mskcc_2015) *12 samples*
    *   [Bladder Cancer, Plasmacytoid Variant (MSKCC, Nat Genet 2016)](https://www.cbioportal.org/study?id=blca_plasmacytoid_mskcc_2016) *34 samples*
    *   [Breast Cancer (METABRIC, Nat Commun 2016)](https://www.cbioportal.org/study?id=brca_metabric) *1980 samples*
    *   [Chronic Lymphocytic Leukemia (Broad, Cell 2013)](https://www.cbioportal.org/study?id=lcll_broad_2013) *160 samples*
    *   [Chronic Lymphocytic Leukemia (IUOPA, Nature 2015)](https://www.cbioportal.org/study?id=cll_iuopa_2015) *506 samples*
    *   [Colorectal Adenocarcinoma (DFCI, Cell Reports 2016)](https://www.cbioportal.org/study?id=coadread_dfci_2016) *619 samples*
    *   [Cutaneous T Cell Lymphoma (Columbia U, Nat Genet 2015)](https://www.cbioportal.org/study?id=ctcl_columbia_2015) *42 samples*
    *   [Diffuse Large B-Cell Lymphoma (Broad, PNAS 2012)](https://www.cbioportal.org/study?id=dlbc_broad_2012) *58 samples*
    *   [Hepatocellular Adenoma (Inserm, Cancer Cell 2014)](https://www.cbioportal.org/study?id=liad_inserm_fr_2014) *46 samples*
    *   [Hypodiploid Acute Lymphoid Leukemia (St Jude, Nat Genet 2013)](https://www.cbioportal.org/study?id=all_stjude_2013) *44 samples*
    *   [Insulinoma (Shanghai, Nat Commun 2013)](https://www.cbioportal.org/study?id=panet_shanghai_2013) *10 samples*
    *   [Malignant Pleural Mesothelioma (NYU, Cancer Res 2015)](https://www.cbioportal.org/study?id=plmeso_nyu_2015) *22 samples*
    *   [Mantle Cell Lymphoma (IDIBIPS, PNAS 2013)](https://www.cbioportal.org/study?id=mcl_idibips_2013) *29 samples*
    *   [Myelodysplasia (Tokyo, Nature 2011)](https://www.cbioportal.org/study?id=mds_tokyo_2011) *29 samples*
    *   [Neuroblastoma (Broad, Nat Genet 2013)](https://www.cbioportal.org/study?id=nbl_ucologne_2015) *56 samples*
    *   [Oral Squamous Cell Carcinoma (MD Anderson, Cancer Discov 2013)](https://www.cbioportal.org/study?id=hnsc_mdanderson_2013) *40 samples*
    *   [Pancreatic Adenocarcinoma (QCMG, Nature 2016)](https://www.cbioportal.org/study?id=paad_qcmg_uq_2016) *383 samples*
    *   [Recurrent and Metastatic Head & Neck Cancer (JAMA Oncology, 2016)](https://www.cbioportal.org/study?id=hnc_mskcc_2016) *151 samples*
*   **New TCGA study**:
    *   [Pan-Lung Cancer (TCGA, Nat Genet 2016)](https://www.cbioportal.org/study?id=nsclc_tcga_broad_2016) *1144 samples*
* Updated **TCGA provisional studies**
    *   updated to the Firehose run of January 28, 2016
    *   RPPA data updated with the latest data from MD Anderson
    *   [OncoTree](http://oncotree.mskcc.org/) codes assigned per sample

## June 6, 2016
*   **New features**:
    * Annotation of mutation effect and drug sensitivity on the Mutations tab and the patient view pages (via [OncoKB](https://www.oncokb.org))  
    ![oncokb-screenshot](https://cloud.githubusercontent.com/assets/840895/15825344/54bbbc44-2bd1-11e6-8b65-e265e03f453a.png)
*   **Improvements**:
    * Improved OncoPrint visualization using WebGL: faster, more zooming flexibility, visualization of recurrent variants
    * Improved Network tab with SBGN view for a single interaction
    * Performance improvement of tables in the study view page
    * Mutation type summary on the Mutations tab

## March 31, 2016
*   **New features**:
    *   Visualization of "Enrichments Analysis" results via volcano plots
    *   Improved performance of the cross cancer expression view  by switching to Plot.ly graphs
    *   Improvements to the "Clinical Data" tab on the study view page
    *   More customization options for the cross-cancer histograms
    *   Performance improvements in the study view and query result tabs
*   **Added data** of 1235 samples from 3 published studies:
    *   [Merged Cohort of LGG and GBM (TCGA, 2016)](https://www.cbioportal.org/study?id=lgggbm_tcga_pub)
    *   [Lung Adenocarcinoma (MSKCC, 2015)](https://www.cbioportal.org/study?id=luad_mskcc_2015)
    *   [Poorly-Differentiated and Anaplastic Thyroid Cancers (MSKCC, JCI 2016)](https://www.cbioportal.org/study?id=thyroid_mskcc_2016)

## January 12, 2016
*   **New features**:
    *   Visualization of multiple samples in a patient
    *   Visualization of timeline data of a patient ([example](https://www.cbioportal.org/patient?studyId=lgg_ucsf_2014&caseId=P04))<br/>
        ![timeline-example](https://cloud.githubusercontent.com/assets/840895/12055606/cca26160-aefc-11e5-93f9-2ecfe7e95caf.png)
*   All **TCGA data** updated to the latest Firehose run of August 21, 2015
*   **New TCGA studies**:
    *   [Cholangiocarcinoma (TCGA, Provisional)](https://www.cbioportal.org/study?id=chol_tcga)
    *   [Mesothelioma (TCGA, Provisional)](https://www.cbioportal.org/study?id=meso_tcga)
    *   [Testicular Germ Cell Cancer (TCGA, Provisional)](https://www.cbioportal.org/study?id=tgct_tcga)
    *   [Thymoma (TCGA, Provisional)](https://www.cbioportal.org/study?id=thym_tcga)
*   **Added data** of 650 samples from 10 published studies:
    *   [Neuroblastoma (AMC Amsterdam, Nature 2012)](https://www.cbioportal.org/study?id=nbl_amc_2012)
    *   [Clear Cell Renal Cell Carcinoma (U Tokyo, Nat Genet 2013)](https://www.cbioportal.org/study?id=ccrcc_utokyo_2013)
    *   [Multiregion Sequencing of Clear Cell Renal Cell Carcinoma (IRC, Nat Genet 2014)](https://www.cbioportal.org/study?id=ccrcc_irc_2014)
    *   [Bladder Urothelial Carcinoma (Dana Farber & MSKCC, Cancer Discovery 2014)](https://www.cbioportal.org/study?id=blca_dfarber_mskcc_2014)
    *   [Low-Grade Gliomas (UCSF, Science 2014)	](https://www.cbioportal.org/study?id=lgg_ucsf_2014)
    *   [Esophageal Squamous Cell Carcinoma (UCLA, Nat Genet 2014)](https://www.cbioportal.org/study?id=escc_ucla_2014)
    *   [Acinar Cell Carcinoma of the Pancreas (Johns Hopkins, J Pathol 2014)](https://www.cbioportal.org/study?id=paac_jhu_2014)
    *   [Gastric Adenocarcinoma (TMUCIH, PNAS 2015)](https://www.cbioportal.org/study?id=egc_tmucih_2015)
    *   [Primary Central Nervous System Lymphoma (Mayo Clinic, Clin Cancer Res 2015)](https://www.cbioportal.org/study?id=pcnsl_mayo_2015)
    *   [Desmoplastic Melanoma (Broad Institute, Nat Genet 2015)](https://www.cbioportal.org/study?id=desm_broad_2015)
*   All mutation data mapped to [UniProt canonical isoforms](http://www.uniprot.org/help/canonical_and_isoforms)

## December 23, 2015
*   **New features**:
    *   Visualization of RNA-seq expression levels across TCGA studies (cross-cancer queries)<br/>
        ![cross cancer expression](https://cloud.githubusercontent.com/assets/840895/11821643/8c9a6de4-a338-11e5-83d1-a9e92cb6dfb6.png)
    *   Selection of genes in the study view to initiate queries<br/>
        ![query gene in study view](https://cloud.githubusercontent.com/assets/840895/11977489/5af87ca8-a951-11e5-87ad-5c31b0451cc1.png)
*   **Improvement**:
    *   3-D structures in the "Mutations" tab are now rendered by 3Dmol.js (previously JSmol)
    *   Improved performance by code optimization and compressing large data by gzip


## December 1, 2015
*   **New feature**: Annotated statistically recurrent hotspots, via new algorithm by [Chang et al. 2015](http://www.nature.com/nbt/journal/vaop/ncurrent/full/nbt.3391.html)</br>
        ![Annotate recurrent hotspots](https://cloud.githubusercontent.com/assets/840895/11794851/7729839e-a281-11e5-9413-12dc885b947d.png)


## November 9, 2015
*   **New features**:
    *   Links to MyCancerGenome.org for mutations<br/>
        ![Link to MyCancerGenome.org](https://cloud.githubusercontent.com/assets/11892704/11050295/9745f78c-8712-11e5-8690-a9a04c4455fb.png)
    *   Improved display of selection samples on the study view page
*   **Improvements**:
    *   "Enrichments" analysis is now run across all genes
    *   The "Network" tab is now using Cytoscape.js (Adobe Flash is no longer required)


## October 6, 2015

*   **New TCGA data**:
    *   [Breast Invasive Carcinoma (TCGA, Cell 2015)](https://www.cbioportal.org/study?id=brca_tcga_pub2015)
    *   [Prostate Adenocarcinoma (TCGA, in press)](https://www.cbioportal.org/study?id=prad_tcga_pub)
    *   [Uveal Melanoma (TCGA, Provisional)](https://www.cbioportal.org/study?id=uvm_tcga)
*   **Added data** of 763 samples from 12 published studies:
    *   [Small Cell Lung Cancer (U Cologne, Nature 2015)](https://www.cbioportal.org/study?id=sclc_ucologne_2015)
    *   [Uterine Carcinosarcoma (JHU, Nat Commun 2014)](https://www.cbioportal.org/study?id=ucs_jhu_2014)
    *   [Microdissected Pancreatic Cancer Whole Exome Sequencing (UTSW, Nat Commun 2015)](https://www.cbioportal.org/study?id=paad_utsw_2015)
    *   [Pancreatic Neuroendocrine Tumors (JHU, Science 2011)](https://www.cbioportal.org/https://www.cbioportal.org/study?id=panet_jhu_2011)
    *   [Renal Non-Clear Cell Carcinoma (Genentech, Nat Genet 2014)](https://www.cbioportal.org/study?id=nccrcc_genentech_2014)
    *   [Infant MLL-Rearranged Acute Lymphoblastic Leukemia (St Jude, Nat Genet 2015)](https://www.cbioportal.org/study?id=all_stjude_2015)
    *   [Rhabdomyosarcoma (NIH, Cancer Discov 2014)](https://www.cbioportal.org/study?id=rms_nih_2014)
    *   [Thymic epithelial tumors (NCI, Nat Genet 2014)](https://www.cbioportal.org/study?id=tet_nci_2014)
    *   [Pediatric Ewing Sarcoma (DFCI, Cancer Discov 2014)](https://www.cbioportal.org/study?id=es_dfarber_broad_2014)
    *   [Ewing Sarcoma (Institut Cuire, Cancer Discov 2014)](https://www.cbioportal.org/study?id=es_iocurie_2014)
    *   [Cutaneous squamous cell carcinoma (DFCI, Clin Cancer Res 2015)](https://www.cbioportal.org/study?id=cscc_dfarber_2015)
    *   [Gallbladder Carcinoma (Shanghai, Nat Genet 2014)](https://www.cbioportal.org/study?id=gbc_shanghai_2014)

## August 21, 2015

*   All **TCGA data** updated to the Firehose run of April 16, 2015.
*   **New feature**: Enrichments Analysis finds alterations that are enriched in either altered or unaltered samples.
*   **Improvement**: improved OncoPrint with better performance.

## June 3, 2015

*   **Improvements**:
    *   Allowed downloading data in each chart/table in study summary page.
    *   Added log-rank test _p_-values to the survival plots in study summary page.
    *   Improved visualization of patient clinical data in patient-centric view.
    *   Added option to merge multiple samples for the same patient in OncoPrint.

## April 28, 2015

*   **New features**:
    *   Redesigned query interface to allow selecting multiple cancer studies
    *   Redesigned **Plots** tab

## January 20, 2015

*   All **TCGA data** updated to the Firehose run of October 17, 2014
*   **COSMIC data** updated to V71
*   **New features**:
    *   Query page: better search functions to find cancer studies
    *   OncoPrints now support color coding of different mutation types
    *   OncoPrints now support multiple clinical annotation tracks
    *   [**OncoPrinter tool**](https://www.cbioportal.org/oncoprinter.jsp) now supports mRNA expression changes  
        ![Oncoprint with multiple clinical tracks](https://user-images.githubusercontent.com/1334004/47188323-9f8e4b00-d305-11e8-8fe9-972104518545.png)

## January 6, 2015

*   **New feature**: You can now view **frequencies of mutations and copy-number alterations** in the study view. These tables are updated dynamically when selecting subsets of samples.  
    ![Alterations in heavily copy-number altered endometrial cancer cases](https://user-images.githubusercontent.com/1334004/47188333-9f8e4b00-d305-11e8-8c64-de5cbda0fa54.png)

## December 9, 2014

*   **New TCGA data**:
    *   Added complete and up-to-date **clinical data** for all **TCGA** provisional studies
    *   All TCGA data updated to the Firehose run of July 15, 2014
    *   New TCGA provisional studies: Esophageal cancer, Pheochromocytoma and Paraganglioma (PCPG)
    *   New published TCGA studies: [Thyroid Cancer](https://www.cbioportal.org/study?id=thca_tcga_pub) and [Kidney Chromophobe](https://www.cbioportal.org/study?id=kich_tcga_pub)
*   **Added data** of 172 samples from 4 published studies:
    *   [Cholangiocarcinoma (National University of Singapore, Nature Genetics 2012)](https://www.cbioportal.org/study?id=chol_nus_2012)
    *   [Cholangiocarcinoma (National Cancer Centre of Singapore, Nature Genetics 2013)](https://www.cbioportal.org/study?id=chol_nccs_2013)
    *   [Intrahepatic Cholangiocarcinoma (Johns Hopkins University, Nature Genetics 2013)](https://www.cbioportal.org/study?id=chol_jhu_2013)
    *   [Bladder Cancer (MSKCC, Eur Urol 2014)](https://www.cbioportal.org/study?id=blca_mskcc_solit_2014)
*   **New features**:
    *   Redesigned **Mutual Exclusivity** tab
    *   Added **correlation scores** for scatter plots on the Plots tab
    *   Download links to [**GenomeSpace**](http://www.genomespace.org/)

## October 24, 2014

*   Added data of 885 samples from 11 published studies:
    *   [Colorectal Adenocarcinoma Triplets (MSKCC, Genome Biology 2014)](https://www.cbioportal.org/study?id=coadread_mskcc)
    *   [Esophageal Squamous Cell Carcinoma (ICGC, Nature 2014)](https://www.cbioportal.org/study?id=escc_icgc)
    *   [Malignant Peripheral Nerve Sheath Tumor (MSKCC, Nature Genetics 2014)](https://www.cbioportal.org/study?id=mpnst_mskcc)
    *   [Melanoma (Broad/Dana Farber, Nature 2012)](https://www.cbioportal.org/study?id=skcm_broad_dfarber)
    *   [Nasopharyngeal Carcinoma (National University Singapore, Nature Genetics 2014)](https://www.cbioportal.org/study?id=npc_nusingapore)
    *   [Prostate Adenocarcinoma CNA study (MSKCC, PNAS 2014)](https://www.cbioportal.org/study?id=prad_mskcc_2014)
    *   [Prostate Adenocarcinoma Organoids (MSKCC, Cell 2014)](https://www.cbioportal.org/study?id=prad_mskcc_cheny1_organoids_2014)
    *   [Stomach Adenocarcinoma (TCGA, Nature 2014)](https://www.cbioportal.org/study?id=stad_tcga_pub)
    *   [Stomach Adenocarcinoma (Pfizer and University of Hong Kong, Nature Genetics 2014)](https://www.cbioportal.org/study?id=stad_pfizer_uhongkong)
    *   [Stomach Adenocarcinoma (University of Hong Kong, Nature Genetics 2011)](https://www.cbioportal.org/study?id=stad_uhongkong)
    *   [Stomach Adenocarcinoma (University of Tokyo, Nature Genetics 2014)](https://www.cbioportal.org/study?id=stad_utokyo)

## August 8, 2014

*   Released two new tools
    *   [Oncoprinter](https://www.cbioportal.org/oncoprinter.jsp) lets you create Oncoprints from your own, custom data
    *   [MutationMapper](https://www.cbioportal.org/mutation_mapper.jsp) draws mutation diagrams (lollipop plots) from your custom data

## May 21, 2014

*   All TCGA data updated to the Firehose run of April 16, 2014

## May 12, 2014

*   Improved study summary page including survival analysis based on clinical attributes  
    e.g. [TCGA Endometrial Cancer cohort](https://www.cbioportal.org/study?id=ucec_tcga_pub)  
    ![Study view](https://user-images.githubusercontent.com/1334004/47188334-a026e180-d305-11e8-964c-6c417bb06ca2.png)

## March 27, 2014

*   New features:
    *   Visualizing of mutations mapped on 3D structures (individual or multiple mutations, directly in the browser)
    *   Gene expression correlation analysis (find all genes with expression correlation to your query genes)
    *   The Patient-Centric View now displays mutation frequencies across all cohorts in cBioPortal for each mutation
    *   The Mutation Details Tab and the Patient-Centric View now display the copy-number status of each mutation  
        ![3D viewer & Co-expression](https://user-images.githubusercontent.com/1334004/47188325-9f8e4b00-d305-11e8-96db-67936cd89f8f.png)

## March 18, 2014

*   All TCGA data updated to the Firehose run of January 15, 2014
*   Updated to the latest COSMIC data (v68)
*   Added two new provisional TCGA studies:
    *   Adrenocortical Carcinoma
    *   Uterine Carcinosarcoma
*   Added mutation data of 898 samples from 11 published studies:
    *   Hepatocellular Carcinoma (RIKEN, Nature Genetics 2012)
    *   Hepatocellular Carcinoma (AMC, Hepatology in press)
    *   Medulloblastoma (Broad, Nature 2012)
    *   Medulloblastoma (ICGC, Nature 2012)
    *   Medulloblastoma (PCGP, Nature 2012)
    *   Multiple Myeloma (Broad, Cancer Cell 2014)
    *   Pancreatic Adenocarcinoma (ICGC, Nature 2012)
    *   Small Cell Carcinoma of the Ovary (MSKCC, Nature Genetics in press)
    *   Small Cell Lung Cancer (CLCGP, Nature Genetics 2012)
    *   Small Cell Lung Cancer (Johns Hopkins, Nature Genetics 2012)
    *   NCI-60 Cell Lines (NCI, Cancer Res. 2012)

## December 9, 2013

*   Added mutation data of 99 bladder cancer samples (BGI, Nature Genetics 2013)

## December 6, 2013

*   Data sets matching four recently submitted or published TCGA studies are now available
    *   Glioblastoma (Cell 2013)
    *   Bladder carcinoma (Nature, in press)
    *   Head & neck squamous cell carcinoma (submitted)
    *   Lung adenocarcinoma (submitted)

## November 8, 2013

*   All TCGA data updated to the Firehose run of September 23, 2013.
*   Updated to the latest COSMIC data (v67).
*   Added mutation data of 792 samples from 9 published cancer studies:
    *   Esophageal Adenocarcinoma (Broad, Nature Genetics 2013)
    *   Head and Neck Squamous Cell Carcinoma (Broad, Science 2011)
    *   Head and Neck Squamous Cell Carcinoma (Johns Hopkins, Science 2011)
    *   Kidney Renal Clear Cell Carcinoma (BGI, Nature Genetics 2012)
    *   Prostate Adenocarcinoma, Metastatic (Michigan, Nature 2012)
    *   Prostate Adenocarcinoma (Broad/Cornell, Nature Genetics 2012)
    *   Prostate Adenocarcinoma (Broad/Cornell, Cell 2013)
    *   Skin Cutaneous Melanoma (Yale, Nature Genetics 2012)
    *   Skin Cutaneous Melanoma (Broad, Cell 2012)

## October 21, 2013

*   Improved interface for survival plots, including information on individual samples via mouse-over
*   New fusion glyph in OncoPrints    [![FGFR3 fusions in head and neck carcinoma](https://user-images.githubusercontent.com/1334004/47188320-9f8e4b00-d305-11e8-9d8c-09a7ec28d921.png)](https://www.cbioportal.org/index.do?cancer_study_id=hnsc_tcga_pub&genetic_profile_ids_PROFILE_MUTATION_EXTENDED=hnsc_tcga_pub_mutations&genetic_profile_ids_PROFILE_COPY_NUMBER_ALTERATION=hnsc_tcga_pub_gistic&Z_SCORE_THRESHOLD=2.0&RPPA_SCORE_THRESHOLD=2.0&data_priority=0&case_set_id=hnsc_tcga_pub_sequenced&case_ids=&gene_set_choice=user-defined-list&gene_list=FGFR3%3A+AMP+MUT%3B%0D%0A%0D%0A%0D%0A%0D%0A%0D%0A%0D%0A&clinical_param_selection=null&tab_index=tab_visualize&Action=Submit)
*   Improved cross-cancer query: new alteration frequency histogram (example below - query gene: CDKN2A) and mutation diagram
    ![Cross Cancer Query](https://user-images.githubusercontent.com/1334004/47188319-9ef5b480-d305-11e8-9281-fb31993b7b42.png)

## September 9, 2013

*   Updated COSMIC data (v66 Release)
*   Improved / interactive visualization on the "Protein changes" tab
*   Enhanced mutation diagrams: color-coding by mutation time and syncing with table filters
*   Addition of DNA cytoband information in the patient view of copy-number changes
*   OncoPrints now allow the display of an optional track with clinical annotation (Endometrial cancer example below)
    ![Oncoprint with clinical track](https://user-images.githubusercontent.com/1334004/47188326-9f8e4b00-d305-11e8-9cf5-52e459d56310.png)

## July 25, 2013

*   Multi-gene correlation plots.
*   Variant allele frequency distribution plots for individual tumor samples.
*   Tissue images for TCGA samples in the patient view, via [Digital Slide Archive](http://cancer.digitalslidearchive.net/). [Example](https://www.cbioportal.org/patient?studyId=ucec_tcga&caseId=TCGA-BK-A0CC&tab=tissueImageTab).

## July 16, 2013

*   All TCGA data updated to the May Firehose run (May 23, 2013).
    *   TCGA Pancreatic Cancer study (provisional) added.

## July 4, 2013

*   Improved rendering of mutation diagrams, including ability to download in PDF format.
*   Improved home page: Searchable cancer study & gene set selectors, data sets selector.

## June 17, 2013

*   Improved interface for correlation plots, including information on individual samples via mouse-over.
*   Gene Details from Biogene are now available in the Network view.
*   Added mutation and copy number data from a new adenoid cystic carcinoma study: Ho et al., Nature Genetics 2013.
*   Added mutation data from 6 cancer studies.
    *   Breast Invasive Carcinoma (Shah et al., Nature 2012)
    *   Breast Invasive Carcinoma (Banerji et al., Nature 2012)
    *   Breast Invasive Carcinoma (Stephens et al., Nature 2012)
    *   Lung Adenocarcinoma (Imielinksi et al., Cell 2012)
    *   Lung Adenocarcinoma (Ding et al., Nature 2008)
    *   Colorectal Cancer (Seshagiri et al., Nature 2012)

## June 4, 2013

*   All TCGA data updated to the April Firehose run (April 21, 2012).

## May 14, 2013

*   Added a published TCGA study: Acute Myeloid Leukemia (TCGA, NEJM 2013).

## April 28, 2013

*   All TCGA data updated to the March Firehose run (March 26, 2012).
*   mRNA percentiles for altered genes shown in patient view.

## April 2, 2013

*   All TCGA data updated to the February Firehose run (February 22, 2012).

## March 28, 2013

*   All TCGA data updated to the January Firehose run (January 16, 2012).
*   Data from a new bladder cancer study from MSKCC has been added (97 samples, Iyer et al., JCO in press).

## February 16, 2013

*   The cBio Portal now contains mutation data from all provisional TCGA projects. Please adhere to [the TCGA publication guidelines](http://cancergenome.nih.gov/abouttcga/policies/publicationguidelines) when using these and any TCGA data in your publications.
*   All data updated to the October Firehose run (October 24, 2012).
*   **Sequencing read counts and frequencies** are now shown in the Mutation Details table when available.
*   Improved OncoPrints, resulting in performance improvements.

## November 21, 2012

*   Major new feature: Users can now visualize **genomic alterations and clinical data of individual tumors**, including:
    *   Summary of **mutations** and **copy-number alterations** of interest
    *   **Clinical trial** information
    *   TCGA **Pathology Reports**
*   New **cancer summary view** (Example [Endometrial Cancer](https://www.cbioportal.org/study?id=ucec_tcga))
*   **Updated drug data** from KEGG DRUG and NCI Cancer Drugs (aggregated by [PiHelper](https://bitbucket.org/armish/pihelper))

## October 22, 2012

*   All data updated to the **Broad Firehose** run from July 25, 2012.
*   **COSMIC data** added to Mutation Details (via Oncotator).
*   All predicted functional impact scores are updated to **Mutation Assessor 2.0**.
*   Users can now base queries on genes in recurrent regions of copy-number alteration (from **GISTIC** via Firehose).
*   The [Onco Query Language (OQL)](https://www.cbioportal.org/onco_query_lang_desc.jsp) now supports queries for specific mutations or mutation types.
*   Data sets added that match the data of all TCGA publications (GBM, ovarian, colorectal, and lung squamous).

## July 18, 2012

*   Mutation data for the TCGA lung squamous cell carcinoma and breast cancer projects (manuscripts in press at Nature).
*   All data updated to the **latest Broad Firehose run** (May 25, 2012).
*   **Drug information** added to the network view (via Drugbank).
*   **Improved cross-cancer** queries: Option to select data types, export of summary graphs.
*   Users can now base queries on frequently mutated genes (from **MutSig** via Firehose).

## May 16, 2012

*   All data updated to the **latest Broad Firehose run** (March 21, 2012).
*   Extended **cross-cancer** functionality, enabling users to query across all cancer studies in our database.
*   New **"build a case"** functionality, enabling users to generate custom case sets, based on one or more clinical attributes.
*   New OncoPrint features, including more **compact OncoPrints**, and support for **RPPA** visualization.

## February 27, 2012

*   All data updated to the **latest Broad Firehose run** (January 24, 2012).
*   Validated mutation data for colorectal cancer.
*   New feature: **Mutation Diagrams** that show mutations in the context of protein domains.
    ![TP53 Mutations in Ovarian Cancer](https://user-images.githubusercontent.com/1334004/47188347-a0bf7800-d305-11e8-8c92-7c8763711338.png)

## January 30, 2012

*   Updated data for several TCGA cancer studies.
*   Some small bug-fixes.

## December 22, 2011

*   **Fourteen new [TCGA](http://cancergenome.nih.gov/) cancer studies**: This includes complete data for TCGA Colorectal Carcinoma and provisional data for thirteen other cancer types in the TCGA production pipeline. Please note that data from these thirteen new cancer types are provisional, not final and do not yet include mutation data. As per NCI guidelines, preliminary mutation data cannot be redistributed until they have been validated.
    ![TCGA](http://cancergenome.nih.gov/PublishedContent/Images/SharedItems/Images/TCGA_54px-Logo.png)

*   **Four new data types**:
    *   Reverse-phase protein array (RPPA) data.
    *   microRNA expression and copy-number (including support for multiple loci)
    *   RNA-Seq based expression data.
    *   log2 copy-number data.
*   Updated TCGA GBM copy-number, expression, and methylation data.
*   New **gene symbol validation** service. You can now use gene aliases and/or Entrez Gene IDs within your gene sets.
*   **Links to IGV** for visualization of DNA copy-number changes.
*   Background information from the [Sanger Cancer Gene Census](http://www.sanger.ac.uk/genetics/CGP/Census/).
*   Two **new [Tutorials](https://www.cbioportal.org/tutorials)** to get you quickly started in using the portal.  

## November 14, 2011

*   New and **improved mutation details**, with sorting and filtering capabilities.
*   In collaboration with Bilkent University, we have added a **new Network tab** to our results pages. The network tab enables users to visualize, analyze and filter cancer genomic data in the context of pathways and interaction networks derived from [Pathway Commons](http://www.pathwaycommons.org).
    ![GBM Network](https://user-images.githubusercontent.com/1334004/47188328-9f8e4b00-d305-11e8-87d7-6336285f7572.png)

## September 3, 2011

*   You can now query across different cancer studies (feature available directly from the home page).
*   Our [MATLAB CGDS Cancer Genomics Toolbox](https://www.cbioportal.org/cgds_r.jsp) is now available. The toolbox enables you to download data from the cBio Portal, and import it directly into MATLAB.
*   The code for the cBio Portal has now been fully open sourced, and made available at [Google Code](http://code.google.com/p/cbio-cancer-genomics-portal/). If you would like to join our open source efforts and make the portal even better, drop us an email.  

## March 2, 2011

New plotting features and other improvements:

*   Correlation plots that show the relationship between different data types for individual genes.
*   Survival analysis - assess survival differences between altered and non-altered patient sets.
*   Updated [R Package](https://www.cbioportal.org/cgds_r.jsp) with support for correlation plots and general improvements for retrieving and accessing data in R data frames.
*   The [Web Interface](https://www.cbioportal.org/web_api.jsp) now supports basic clinical data, e.g. survival data.
*   [Networks](https://www.cbioportal.org/networks.jsp) for pathway analysis are now available for download.
    ![Survival Analysis](https://user-images.githubusercontent.com/1334004/47188329-9f8e4b00-d305-11e8-83e2-0ad816431888.png)

## December 15, 2010

Several new features, including:

*   Redesigned and streamlined user interface, based on user feedback and usability testing.
*   Advanced support for gene-specific alterations. For example, users can now view mutations within TP53, and ignore copy number alterations, or only view amplifications of EGFR, and ignore deletions.
*   Improved performance.
*   [Frequently Asked Questions](FAQ) document released.
*   Updated ~~Video Tutorial~~ (update: old link no longer functional. Now see: [YouTube](https://www.youtube.com/results?search_query=cbioportal)

## November 4, 2010

*   Enhanced [Oncoprints](https://www.cbioportal.org/faq.jsp#what-are-oncoprints), enabling users to quickly visualize genomic alterations across many cases. Oncoprints now also work in all major browsers, including Firefox, Chrome, Safari, and Internet Explorer.
*   Official release of our [Web Interface](https://www.cbioportal.org/web_api.jsp), enabling programmatic access to all data.
*   Official release of our [R Package](https://www.cbioportal.org/cgds_r.jsp), enabling programmatic access to all data from the R platform for statistical computing.
    ![OncoPrints](https://user-images.githubusercontent.com/1334004/47188322-9f8e4b00-d305-11e8-9fae-ca188de91267.png)
