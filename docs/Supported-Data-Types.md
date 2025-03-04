# Supported Data Types

cBioPortal is a multimodal cancer data visualization tool and supports a variety of data types. For some data types, we have explicit support; for others, you can leverage generic assay or clinical data to assign arbitrary data to either a sample or patient. Beyond including data directly into cBioPortal, one can also link in additional external viewers specialized for particular data types, or even entirely different dashboards or portals. This combination of structured and flexible data formats, as well as the option to link out to other viewers, allows cBioPortal to be used for many different combinations of clinical data and profiling methods.

## Molecular Data

| Assay | Processed Data | cBioPortal Data Type | Example cBioPortal Study |
| --- | --- | --- | --- |
| Bulk DNA Sequencing | Mutations | [Mutations](./File-Formats.md#mutation-data) | [MSK-IMPACT (Nat Med, 2017)](https://www.cbioportal.org/study/summary?id=msk_impact_2017) |
| - | Copy Number Alterations per Gene (CNA) | [CNA](./File-Formats.md#discrete-copy-number-data) | [MSK-IMPACT (Nat Med, 2017)](https://www.cbioportal.org/study/summary?id=msk_impact_2017) |
| - | Arm Level CNA Data | [Generic Assay](./File-Formats.md#arm-level-cna-data) | [TCGA Pancan (Cell 2018)](https://www.cbioportal.org/study/summary?id=acc_tcga_pan_can_atlas_2018) |
| - | Copy Number Segmentation | [Segmented Data](./File-Formats.md#segmented-data) | [MSK-IMPACT (Nat Med, 2017)](https://www.cbioportal.org/patient?studyId=msk_impact_2017&caseId=P-0000004) |
| - | Structural Variants | [Structural Variants](./File-Formats.md#structural-variant-data) | [PCAWG (Nature, 2020)](https://www.cbioportal.org/study/summary?id=pancan_pcawg_2020) |
| - | Mutational Signatures | [Generic Assay](./File-Formats.md#mutational-signature-data) | [PCAWG (Nature, 2020)](https://www.cbioportal.org/study/summary?id=pancan_pcawg_2020) |
| - | Genetic Ancestry | [Generic Assay](./File-Formats.md#generic-assay) | [TCGA Pancan (Cell, 2018)](https://www.cbioportal.org/study/summary?id=acc_tcga_pan_can_atlas_2018) |
| Bulk RNA Sequencing | Gene Expression | [Gene Expression](./File-Formats.md#expression-data) | [TCGA Pancan (Cell, 2018)](https://www.cbioportal.org/study/summary?id=acc_tcga_pan_can_atlas_2018) |
| Bulk DNA Methylation | Methylation per probe | [Generic Assay](./File-Formats.md#methylation-data) | [TCGA Pancan (Cell, 2018)](https://www.cbioportal.org/study/summary?id=acc_tcga_pan_can_atlas_2018) |
| Proteomics | Protein Expression | [Protein Expression](./File-Formats.md#protein-level-data) | [Cancer Cell Line Encyclopedia (Broad, 2019)](https://www.cbioportal.org/study/summary?id=ccle_broad_2019) |
| Other Assays | Any sample-level and/or patient-level attributes (e.g. LHA types, microbiome) | [Generic Assay](./File-Formats.md#generic-assay) | |


### External Viewer Integration

There are several other data types for which there is no native support. However cBioPortal offers a generic way of linking in additional viewers using the [Resource Data Format](./File-Formats#resource-data). Technically this is known as an iframe link. These viewers can be linked at the cohort, patient or individual sample level. Beyond just linking viewers, we also have several suggestions on what kind of derived data can be added from these assays directly into cBioPortal for multimodal analysis. This mainly leverages the [Generic Assay Format](./File-Formats#generic-assay).


| Assay | External Viewer | Derived data to load into cBioPortal | Example cBioPortal Study |
| --- | --- | --- | --- |
| scRNAseq | CellxGene, Vitesssce, Broad Single Cell Portal | Cell fractions per sample, pseudobulk expression per gene per sample | [HTAN Vanderbilt (Cell, 2021)](https://www.cbioportal.org/study/summary?id=msk_spectrum_tme_2022) |
| GeoMx | Minerva | RNA expression per Region of Interest | [Ovarian Cancer (Gray Foundation, Cancer Discov 2024)](https://www.cbioportal.org/study/summary?id=ovary_geomx_gray_foundation_2024) |
| CyCIF/ORION | Minerva | Cell marker density in tumor regions | [Colorectal Adenocarcinoma (DFCI/Orion, 2024)](https://www.cbioportal.org/study/summary?id=crc_orion_2024) |
| H&E | Minerva, CDSA| | [MSK SPECTRUM (Nature, 2022)](https://www.cbioportal.org/patient/openResource_HE?studyId=msk_spectrum_tme_2022&caseId=P-0042164) |
| CT | OHIF | Tumor volume, density, etc. | [TCGA Pancan (Cell, 2021)](https://viewer.imaging.datacommons.cancer.gov/viewer/1.3.6.1.4.1.14519.5.2.1.8421.4017.206944705526266221852495854472) |
| scDNAseq | CellxGene, Vitesssce, Broad Single Cell Portal | | |
| scATACseq | CellxGene, Vitesssce, Broad Single Cell Portal | | |
| Tapestri | TBD | Protein expression per sample | TBD |
| 3D CyCIF | TBD | TBD | TBD |
| Your very custom assay | Your very special viewer | Any sample-level or patient-level data | |

## Clinical Data

The clinical data are free-form de-identified patient and sample attributes. We don't follow a specific ontology, but rely on the authors of the published studies to determine the organization of their clinical data. The only exception is the cancer type categorization which uses the [Oncotree](https://oncotree.info) ontology. We do allow encoding of the data type for each column (e.g. numerical, text, etc).  More details can be found in the [Clinical Data File Formats section](./File-Formats.md#clinical-data). We also have a [timeline format](./File-Formats.md#timeline-data) for incorporating treatment information (see e.g. [this](https://genie.cbioportal.org/patient/summary?studyId=nsclc_public_genie_bpc&caseId=GENIE-DFCI-004022) AACR GENIE lung cancer case).
