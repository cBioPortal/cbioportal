# Incremental Data Loading

To add or update a few entries (patient/sample/genetic profile) more quickly, especially for larger studies, you can use incremental data loading instead of re-uploading the entire study.

## Granularity of Incremental Data Loading

Think of updating an entry as a complete swap of data for a particular data type for this entry (patient/sample/genetic profile).
When you update an entry, you must provide the complete data for this data type for this entry again.
For example, if you want to add or update the  `Gender` attribute of a patient by incrementally uploading the `PATIENT_ATTRIBUTES` data type, you have to supply **all** other attributes of this patient again.
Note that in this case, you don't have to supply all sample information or molecular data types for this patient again as those are separate data types, and the rule applies to them in their own turn.

**Note:** Although incremental upload will create a genetic profile (name, description, etc.) when you upload molecular data for the first time, it does not update the profile (metadata)attributes on subsequent uploads.
It simply reuses the genetic profile if none of the identifying attributes (`cancer_study_identifier`, `genetic_alteration_type`, `datatype` and `stable_id`) have changed.

## Usage
To load data incrementally, you have to specify `--data_directory` (or `-d`) instead of `--study_directory` (or `-s`) option for the [metaImport script](./Using-the-metaImport-script.md) or `cbioportalImporter.py` scripts.

The data directory follows the same structure and data format as the study directory.
The data files should contain complete information about entries you want to add or update.

## Supported Data Types
Please note that incremental upload is supported for subset of data types only.
Unsupported data types have to be omitted from the directory.

Here is the list of data types as they specified in `datatype` attribute of meta file.

- `CASE_LIST`
- `CNA_CONTINUOUS`
- `CNA_DISCRETE`
- `CNA_DISCRETE_LONG`
- `CNA_LOG2`
- `EXPRESSION`
- `GENERIC_ASSAY_BINARY` (sample level only; `patient_level: false`)
- `GENERIC_ASSAY_CATEGORICAL` (sample level only; `patient_level: false`)
- `GENERIC_ASSAY_CONTINUOUS`  (sample level only; `patient_level: false`)
- `METHYLATION`
- `MUTATION`
- `MUTATION_UNCALLED`
- `PATIENT_ATTRIBUTES`
- `PROTEIN`
- `SAMPLE_ATTRIBUTES`
- `SEG`
- `STRUCTURAL_VARIANT`
- `TIMELINE` (aka clinical events)

You might want to check the `INCREMENTAL_UPLOAD_SUPPORTED_META_TYPES` variable of the `cbioportal_common.py` module of the `cbioportal-core` project to ensure the list is up to date.

These are the known data types for which incremental upload is not currently supported:

- `CANCER_TYPE`
- `GENERIC_ASSAY_BINARY` (patient level; `patient_level: true`)
- `GENERIC_ASSAY_CATEGORICAL` (patient level; `patient_level: true`)
- `GENERIC_ASSAY_CONTINUOUS`  (patient level; `patient_level: true`)
- `GISTIC_GENES`
- `GSVA_PVALUES`
- `GSVA_SCORES`
- `PATIENT_RESOURCES`
- `RESOURCES_DEFINITION`
- `SAMPLE_RESOURCES`
- `STUDY_RESOURCES`
- `STUDY`