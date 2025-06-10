# Study Data Export

This package contains the code for exporting study data from the database to a file format.  The export process involves several steps, including:
1. Retrieving the study data from the database.
2. Transforming the data into a suitable format for export.
3. Writing the transformed data to a file.

The implementation is done with minimum dependencies on the rest of the code to ensure that the code is lightweight, performant and easy to move to a separate web application if needed.
To make export process take less RAM, the code uses a streaming approach to read and write data. On the database side, the code uses a cursor to read data in chunks, and on the web controller side, the code uses a streaming response to write data in chunks.
This allows the code to handle large datasets without running out of memory. 

## Usage

Set `dynamic_study_export_mode` to `true` in the application properties file to enable the dynamic study export mode. 
This mode allows the user to export study with `/export/study/{studyId}.zip` link.

## 10 minute timeout

The export process is designed to complete within 10 minutes. If the export takes longer than that, it will be terminated. This is to ensure that the export process does not block the server for too long and to prevent resource exhaustion.
If you want to increase the timeout, you can set the `dynamic_study_export_mode.timeout_ms` property in the application properties file. The value is in milliseconds, and the default value is `600000` (10 minutes).
Setting it to `-1` will disable the timeout and allow the export process to run indefinitely. However, this is not recommended as it can lead to resource exhaustion and performance issues.

## Supported Formats

The following formats are supported for export:

| GENETIC_ALTERATION_TYPE                                 | DATATYPE | SUPPORTED |
|---------------------------------------------------------|---|---|
| CANCER_TYPE                                             | CANCER_TYPE | Yes |
| CLINICAL                                                | PATIENT_ATTRIBUTES | Yes |
| CLINICAL                                                | SAMPLE_ATTRIBUTES | Yes |
| CLINICAL                                                | TIMELINE | Yes |
| PROTEIN_LEVEL                                           | LOG2-VALUE | Yes |
| PROTEIN_LEVEL                                           | Z-SCORE | Yes |
| PROTEIN_LEVEL                                           | CONTINUOUS | Yes |
| COPY_NUMBER_ALTERATION                                  | DISCRETE | Yes |
| COPY_NUMBER_ALTERATION                                  | CONTINUOUS | Yes |
| COPY_NUMBER_ALTERATION                                  | DISCRETE_LONG | No |
| COPY_NUMBER_ALTERATION                                  | LOG2-VALUE | Yes |
| COPY_NUMBER_ALTERATION                                  | SEG | Yes |
| MRNA_EXPRESSION                                         | CONTINUOUS | Yes |
| MRNA_EXPRESSION                                         | Z-SCORE | Yes |
| MRNA_EXPRESSION                                         | DISCRETE | Yes |
| MUTATION_EXTENDED                                       | MAF | Yes |
| MUTATION_UNCALLED                                       | MAF | Yes |
| METHYLATION                                             | CONTINUOUS | Yes |
| GENE_PANEL_MATRIX                                       | GENE_PANEL_MATRIX | Yes |
| STRUCTURAL_VARIANT                                      | SV | Yes |
| GENERIC_ASSAY (sample level only, PATIENT_LEVEL: false) | LIMIT-VALUE | Yes |
| GENERIC_ASSAY (sample level only, PATIENT_LEVEL: false) | BINARY | Yes |
| GENERIC_ASSAY (sample level only, PATIENT_LEVEL: false) | CATEGORICAL | Yes |
| Cancer study meta file                                  | | Yes |
| Case lists                                              | | Yes |
| GISTIC_GENES_AMP                                        | Q-VALUE | No |
| GISTIC_GENES_DEL                                        | Q-VALUE | No |
| MUTSIG                                                  | Q-VALUE | No |
| GENESET_SCORE                                           | GSVA-SCORE | No |
| GENESET_SCORE                                           | P-VALUE | No |
| Study tags                                              | | No |
| Resource Definition                                     | | No |
| Study Resource                                          | | No |
| Patient Resrouce                                        | | No |
| Sample Resource                                         | | No |

## Caveats

The exported study data files won't look exactly the same as the original study data files.
## What's lost in translation?
- The exported files will not contain the original file names, but rather the file names will be generated based on the data type.
- `DISCRETE_LONG` will not be exported as such as there is no information in the database that marks the data as long. Instead, it will be exported as `DISCRETE`.
- `HGVSp_Short` of the MAF file will be computed from `mutation_event`.`PROTEIN_CHANGE` by adding the `p.` prefix (if it's not `MUTATED`).
  - The protein change could be read from `Amino_Acid_Change` as fallback field in the original files, but there is no way of knowing where the protein change has been parsed originally from.
  - As `Amino_Acid_Change` can contain not valid HGVSp value, you might end up with `HGVSp_Short` that is not valid HGVSp value. Although, it should not stop you from loading the file into cBioPortal and get the protein change parsed correctly.