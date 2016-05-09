This page contains an overview to help you transition to the new file formats. As this should be a one-time operation for all users, this page is only temporary. 

1. (recommended) Update your genes table, see issues [#799](https://github.com/cBioPortal/cbioportal/issues/799) and [#805](https://github.com/cBioPortal/cbioportal/issues/805) on how to do this
    - Reason: cBioPortal in the past accidentally imported the wrong column as HUGO symbol. This will cause many warnings about invalid genes during the validation process. 

2. Be aware: now there is a strict validation of the file column header names for all data files that have Entrez Id *and* Hugo gene symbol columns. The column names have to be `Entrez_Gene_Id` and `Hugo_Symbol`. This can be a change if you are expecting the *position* of the column to be important rather than the name. The columns still should be placed *before* any of the **samples** columns, though (i.e. only the columns *after* `Entrez_Gene_Id` and `Hugo_Symbol` columns are considered as sample columns). The new validator will warn you when your file does not comply to at least having the `Entrez_Gene_Id` column (which is the recommended column to use for gene identifiers).
3. Be aware: now there is a strict validation on `datatype` in the **meta** files, now also documented in the updated [File formats](File-Formats.md) page (and in table below)

4. Other changes: check the following table for your data types:

DataType | What you have to do
--- | ---
Cancer Study | (optionally) Add `add_global_case_list`
Cancer Type | [Create](File-Formats.md#cancer-type) the meta file
Discrete Copy Number Data | Update meta file: <ul><li>change `stable_id` to `gistic`, `cna`, `cna_rae` or `cna_consensus`</li><li>add `data_filename`</li></ul>Remark: copynumber profiles used by the cross-cancer histogram no longer use the name to check whether the data is GISTIC or RAE; this is now based on the stable_id.
Copy Number Data | Update meta file: <ul><li>if `datatype` is `LOG-VALUE` change it to `LOG2-VALUE`</li><li>if `datatype` is `CONTINUOUS`, change `stable_id` to `linear_CNA`</li> <li>add `data_filename`</li></ul>
Segmented Data | Update meta file:  <ul><li>change `genetic_alteration_type` to `COPY_NUMBER_ALTERATION`</li><li>change `datatype` to `SEG`</li><li>remove: `stable_id`, `show_profile_in_analysis_tab`, `profile_name`, `profile_description`</li><li>add: `description`, `data_filename`</li></ul>
Expression Data | Update meta file:<ul><li>check your `stable_id` against the [table](File-Formats.md#supported-stable_id-values-for-mrna_expression)</li><li>add `data_filename`</li></ul>
Mutation Data | Update meta file: <ul><li>change your `stable id` to `mutations`</li><li>add `data_filename`</li></ul>
Fusion Data (TODO) | Update meta file:<ul><li>add `data_filename`</li></ul>
Methylation Data | Update meta file:<ul><li>change `stable_id` to `methylation_hm27` or `methylation_hm450`</li><li>add `data_filename`</li></ul>
RPPA Data | Update meta file:<ul><li>change `genetic_alteration_type` to `PROTEIN_LEVEL`</li> <li>change `datatype` to `LOG2-VALUE` or `Z-SCORE`</li> <li>change `stable_id` to `rppa` or `rppa_Zscores`</li> <li>add `data_filename`</li></ul>
Clinical Data | <ul><li>Create two separate meta files, one for samples and one for patients</li><li>Create two separate data files, one for samples and one for patients</li><ul><li>remove the row describing whether an attribute is a SAMPLE or a PATIENT attribute</li></ul></ul>For full instructions, check the [file formats](File-Formats.md#clinical-data)
Case Lists | -
Timeline Data | Update meta file(s):<ul><li>remove: `stable_id`, `show_profile_in_analysis_tab`, `profile_name`, `profile_description`</li><li>add: `data_filename`</li></ul>
Gistic Data | [Create](File-Formats.md#gistic-data) the meta file
MutSig Data | [Create](File-Formats.md#mutsig-data) the meta file

