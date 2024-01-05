# Load Mutations Data

This to be done after the tables have been created in ClickHouse.

## Download from MySQL database into file called mutation.csv
```
SELECT
	cs.CANCER_STUDY_ID as cancerStudyId,
	cs.CANCER_STUDY_IDENTIFIER as cancerStudyIdentifier,
	p.STABLE_ID as patientStableId,
	s.STABLE_ID as sampleStableId,
	s.SAMPLE_TYPE as sampleType,
	m.MUTATION_EVENT_ID as mutationEventId,
	m.ENTREZ_GENE_ID as mutationEntrezGeneId,
	gm.HUGO_GENE_SYMBOL as mutationHugoGeneSymbol,
	m.CENTER as mutationCenter,
	m.SEQUENCER as mutationSequencer,
	m.MUTATION_STATUS as mutationStatus,
	m.VALIDATION_STATUS as mutationValidationStatus,
	m.TUMOR_SEQ_ALLELE1 as mutationTumorSeqAllele1,
	m.TUMOR_SEQ_ALLELE2 as mutationTumorSeqAllele2,
	m.MATCHED_NORM_SAMPLE_BARCODE as mutationMatchedNormSampleBarcode,
	m.MATCH_NORM_SEQ_ALLELE1 as mutationMatchNormSeqAllele1,
	m.MATCH_NORM_SEQ_ALLELE2 as mutationMatchNormSeqAllele2,
	m.TUMOR_VALIDATION_ALLELE1 as mutationTumorValidationAllele1,
	m.TUMOR_VALIDATION_ALLELE2 as mutationTumorValidationAllele2,
	m.MATCH_NORM_VALIDATION_ALLELE1 as mutationMatchNormValidationAllele1,
	m.MATCH_NORM_VALIDATION_ALLELE2 as mutationMatchNormValidationAllele2,
	m.VERIFICATION_STATUS as mutationVerificationStatus,
	m.SEQUENCING_PHASE as mutationSequencingPhase,
	m.SEQUENCE_SOURCE as mutationSequenceSource,
	m.VALIDATION_METHOD as mutationValidationMethod,
	m.SCORE as mutationScore,
	m.BAM_FILE as mutationBamFile,
	m.TUMOR_ALT_COUNT as mutationTumorAltCount,
	m.TUMOR_REF_COUNT as mutationTumorRefCount,
	m.NORMAL_ALT_COUNT as mutationNormalAltCount,
	m.NORMAL_REF_COUNT as mutationNormalRefCount,
	m.AMINO_ACID_CHANGE as mutationAminoAcidChange,
	m.ANNOTATION_JSON as mutationAnnotationJSON,
	me.ENTREZ_GENE_ID as mutationEventEntrezGeneId,
	gme.HUGO_GENE_SYMBOL as mutationEventHugoGeneSymbol,
	me.CHR as mutationEventChr,
	me.START_POSITION as mutationEventStartPosition,
	me.END_POSITION as mutationEventEndPosition,
	me.REFERENCE_ALLELE as mutationEventReferenceAllele,
	me.TUMOR_SEQ_ALLELE as mutationEventTumorSeqAllele,
	me.PROTEIN_CHANGE as mutationEventProteinChange,
	me.MUTATION_TYPE as mutationEventMutationType,
	me.FUNCTIONAL_IMPACT_SCORE as mutationEventFunctionalImpactScore,
	me.FIS_VALUE as mutationEventFisValue,
	me.LINK_XVAR as mutationEventLinkXVAR,
	me.LINK_PDB as mutationEventLinkPDB,
	me.LINK_MSA as mutationEventLinkMSA,
	me.NCBI_BUILD as mutationEventNCBIBuild,
	me.STRAND as mutationEventStrand,
	me.VARIANT_TYPE as mutationEventVariantType,
	me.DB_SNP_RS as mutationEventDBSNPRS,
	me.DB_SNP_VAL_STATUS as mutationEventDBSNPValStatus,
	me.ONCOTATOR_DBSNP_RS as mutationEventOncotatorDBSNP_RS,
	me.ONCOTATOR_REFSEQ_MRNA_ID  as mutationEventOncotatorRefSeqMRNAId,
	me.ONCOTATOR_CODON_CHANGE  as mutationEventOncotatorCodonChange,
	me.ONCOTATOR_UNIPROT_ENTRY_NAME  as mutationEventOncotatorUniprotEntryName,
	me.ONCOTATOR_UNIPROT_ACCESSION  as mutationEventOncotatorUniprotAccession,
	me.ONCOTATOR_PROTEIN_POS_START as mutationEventOncotatorProteinPosStart,
	me.ONCOTATOR_PROTEIN_POS_END as mutationEventOncotatorProteinPosEnd,
	me.CANONICAL_TRANSCRIPT as mutationEventCanonicalTranscript,
	me.KEYWORD as mutationEventKeyword,
	gp.GENETIC_PROFILE_ID as geneticProfileId,
	gp.STABLE_ID as geneticProfileStableId,
	gp.GENETIC_ALTERATION_TYPE as geneticProfileGeneticAlterationType,
	gp.GENERIC_ASSAY_TYPE as geneticProfileAssayType,
	gp.DATATYPE as geneticProfileDataType,
	gp.NAME as geneticProfileName,
	gp.DESCRIPTION as geneticProfileDescription,
	gp.SHOW_PROFILE_IN_ANALYSIS_TAB as geneticProfileShowProfileInAnalysisTab,
	gp.PIVOT_THRESHOLD as geneticProfilePivotThreshold,
	gp.SORT_ORDER as geneticProfileSortOrder,
	gp.PATIENT_LEVEL as geneticProfilePatientLevel,
	ada.DRIVER_FILTER as alterationDriverAnnotationDriverFilter,
	ada.DRIVER_FILTER_ANNOTATION  as alterationDriverAnnotationDriverFilterAnnotation,
	ada.DRIVER_TIERS_FILTER  as alterationDriverAnnotationDriverTiersFilter,
	ada.DRIVER_TIERS_FILTER_ANNOTATION  as alterationDriverAnnotationDriverTiersFilterAnnotation
FROM cancer_study cs 
INNER JOIN patient p ON p.CANCER_STUDY_ID = cs.CANCER_STUDY_ID 
INNER JOIN sample s ON s.PATIENT_ID = p.INTERNAL_ID
INNER JOIN mutation m on m.SAMPLE_ID = s.INTERNAL_ID 
INNER JOIN mutation_event me ON m.MUTATION_EVENT_ID = me.MUTATION_EVENT_ID
INNER JOIN genetic_profile gp ON m.GENETIC_PROFILE_ID = gp.GENETIC_PROFILE_ID 
LEFT JOIN gene gm ON m.ENTREZ_GENE_ID = gm.ENTREZ_GENE_ID 
LEFT JOIN gene gme ON me.ENTREZ_GENE_ID = gme.ENTREZ_GENE_ID
LEFT JOIN alteration_driver_annotation ada ON
        m.GENETIC_PROFILE_ID = ada.GENETIC_PROFILE_ID
    and m.SAMPLE_ID = ada.SAMPLE_ID
    and m.MUTATION_EVENT_ID = ada.ALTERATION_EVENT_ID;
```


## Split File

```
cat mutation.csv | parallel --header : --pipe -N99999 'cat > mutation_{#}.csv' 
```

## Create Load Script

Edit and run as needed.

```
# Where 95 is the number of splits
for number in range(0, 95):
   print(f"./clickhouse client -h localhost --port 19000 -q \"INSERT INTO cbioportal.mutation FORMAT CSV\" < sql_query/mutation_{number}.csv")
```

## Run Script

```
python3 load_data.py > load_mutation.sh
chmod u+x load_mutation.sh
./load_mutation.sh
```

Notes: This took less than 10 minutes to run with 9.7 million entries in the CSV
