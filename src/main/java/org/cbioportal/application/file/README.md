# Study Data Export

This package contains the code for exporting study data from the database to a file format.  The export process involves several steps, including:
1. Retrieving the study data from the database.
2. Transforming the data into a suitable format for export.
3. Writing the transformed data to a file.

The implementation is done with minimum dependencies on the rest of the code to ensure that the code is lightweight, performant and easy to move to a separate web application if needed.
To make export process take less RAM, the code uses a streaming approach to read and write data. On the database side, the code uses a cursor to read data in chunks, and on the web controller side, the code uses a streaming response to write data in chunks.
This allows the code to handle large datasets without running out of memory. 

## Usage

Set `feature.study.export` to `true` in the application properties file to enable the dynamic study export mode. 
This mode allows the user to export study with `/export/study/{studyId}.zip` link.

## 10 minute timeout

The export process is designed to complete within 10 minutes. If the export takes longer than that, it will be terminated. This is to ensure that the export process does not block the server for too long and to prevent resource exhaustion.
If you want to increase the timeout, you can set the `feature.study.export.timeout_ms` property in the application properties file. The value is in milliseconds, and the default value is `600000` (10 minutes).
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

### namespaces meta property is not supported

Mutations, CNA and SV data has `namespaces` meta property that provide a way to load arbitrary data into cBioPortal.
We do not support exporting this data atm. It can be added later if needed.

## Caveats

The exported study data files won't look exactly the same as the original study data files.
## What's lost in translation?
- If your data includes `Hugo_Symbol` but not `Entrez_Gene_Id`, cBioPortal will try to find the matching gene using its database. As a result, the exported data might include `Hugo_Symbol` values that weren’t in your original file, these could be related gene names that replace gene aliases found in your data.
  - The export always adds both `Hugo_Symbol` and `Entrez_Gene_Id` with complete values, even if the original file had only one column or was missing some values.
- The cBioPortal loader filters out certain mutations (e.g. not coding mutations), so the exported MAF file may not include all mutations from the original file.
- The exported files will not contain the original file names, but rather the file names will be generated based on the data type.
- `TIMELINE` data will be exported file per `EVENT_TYPE` despite how original files were structured.
  - If `STYLE_COLOR` or `STYLE_SHAPE` columns are present in the timeline data, in case of no value for some events, the default values will be used:
    - `STYLE_COLOR` will be set to `#1f77b4` (light blue).
    - `STYLE_SHAPE` will be set to `circle`.
    - These values are used by default by cBioPortal to render the timeline events in the UI.
- `DISCRETE_LONG` will not be exported as such as there is no information in the database that marks the data as long. Instead, it will be exported as `DISCRETE`.
- `HGVSp_Short` of the MAF file will be computed from `mutation_event`.`PROTEIN_CHANGE` by adding the `p.` prefix (if it's not `MUTATED`).
  - The protein change could be read from `Amino_Acid_Change` as fallback field in the original files, but there is no way of knowing where the protein change has been parsed originally from.
  - As `Amino_Acid_Change` can contain not valid HGVSp value, you might end up with `HGVSp_Short` that is not valid HGVSp value. Although, it should not stop you from loading the file into cBioPortal and get the protein change parsed correctly.