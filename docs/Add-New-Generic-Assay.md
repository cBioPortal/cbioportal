# Add New Generic Assay

This page describes how to add a new type of Generic Assay.

## Backend modification
To add a new type of Generic Assay, the following modification should be performed. Take type `NEW_TYPE` as example
1. **core/src/main/java/org/mskcc/cbio/portal/model/GeneticAlterationType.java**: Add new type `NEW_TYPE` into the `GeneticAlterationType`.
2. **model/src/main/java/org/cbioportal/model/MolecularProfile.java**: Add new type `NEW_TYPE` into the `MolecularAlterationType`.
3. **model/src/main/java/org/cbioportal/model/EntityType.java**: Add new type `NEW_TYPE` into the `EntityType`.
4. **core/src/main/scripts/importer/cbioportal_common.py**: Add a new line to `alt_type_datatype_to_meta`      
    ``` python
    ("NEW_TYPE", "LIMIT-VALUE"): MetaFileTypes.GENERIC_ASSAY
    ```
5. **core/src/main/scripts/importer/allowed_data_types.txt**: Add a new line
    ```
    NEW_TYPE	LIMIT-VALUE	*
    ```
6. **core/src/main/java/org/mskcc/cbio/portal/scripts/ImportTabDelimData.java**: add condition for `genericAssayProfile`
    ```java
        boolean genericAssayProfile = geneticProfile!=null
                                && (geneticProfile.getGeneticAlterationType() == GeneticAlterationType.MUTATIONAL_SIGNATURE || geneticProfile.getGeneticAlterationType() == GeneticAlterationType.NEW_TYPE)
                                && parts[0].equalsIgnoreCase("entity_stable_id");
    ```
7. **stable_id**: 
    ```java
    else if (geneticProfile.getGeneticAlterationType() == GeneticAlterationType.TREATMENT || geneticProfile.getGeneticAlterationType() == GeneticAlterationType.MUTATIONAL_SIGNATURE || geneticProfile.getGeneticAlterationType() == GeneticAlterationType.NEW_TYPE)
    ```
8. **meta file**: Prepare the meta file described in [File-Formats](File-Formats.md)
9. **data file**: Prepare the data file described in [File-Formats](File-Formats.md)
