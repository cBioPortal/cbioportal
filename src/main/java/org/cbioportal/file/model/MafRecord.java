package org.cbioportal.file.model;

/**
 * Represents a record in a Mutation Annotation Format (MAF) file.
 */
public record MafRecord(
    /**
     * A HUGO gene symbol.
     */
    String hugoSymbol,

    /**
     * A Entrez Gene identifier.
     */
    String entrezGeneId,

    /**
     * The sequencing center.
     */
    String center,

    /**
     * The Genome Reference Consortium Build used by a variant calling software. It must be "GRCh37" or "GRCh38" for a human, and "GRCm38" for a mouse.
     */
    String ncbiBuild,

    /**
     * A chromosome number, e.g., "7".
     */
    String chromosome,

    /**
     * Start position of event.
     */
    Long startPosition,

    /**
     * End position of event.
     */
    Long endPosition,

    /**
     * We assume that the mutation is reported for the + strand.
     */
    String strand,

    /**
     * Translational effect of variant allele, e.g. Missense_Mutation, Silent, etc.
     */
    String variantClassification,

    /**
     * Variant Type, e.g. SNP, DNP, etc.
     */
    String variantType,

    /**
     * The plus strand reference allele at this position.
     */
    String referenceAllele,

    /**
     * Primary data genotype.
     */
    String tumorSeqAllele1,

    /**
     * Primary data genotype.
     */
    String tumorSeqAllele2,

    /**
     * Latest dbSNP rs ID.
     */
    String dbSnpRs,

    /**
     * dbSNP validation status.
     */
    String dbSnpValStatus,

    /**
     * This is the sample ID. Either a TCGA barcode (patient identifier will be extracted), or for non-TCGA data, a literal SAMPLE_ID as listed in the clinical data file.
     */
    String tumorSampleBarcode,

    /**
     * The sample ID for the matched normal sample.
     */
    String matchedNormSampleBarcode,

    /**
     * Primary data.
     */
    String matchNormSeqAllele1,

    /**
     * Primary data.
     */
    String matchNormSeqAllele2,

    /**
     * Secondary data from orthogonal technology.
     */
    String tumorValidationAllele1,

    /**
     * Secondary data from orthogonal technology.
     */
    String tumorValidationAllele2,

    /**
     * Secondary data from orthogonal technology.
     */
    String matchNormValidationAllele1,

    /**
     * Secondary data from orthogonal technology.
     */
    String matchNormValidationAllele2,

    /**
     * Second pass results from independent attempt using same methods as primary data source. "Verified", "Unknown" or "NA".
     */
    String verificationStatus,

    /**
     * Second pass results from orthogonal technology. "Valid", "Invalid", "Untested", "Inconclusive", "Redacted", "Unknown" or "NA".
     */
    String validationStatus,

    /**
     * "Somatic" or "Germline" are supported by the UI in Mutations tab. "None", "LOH" and "Wildtype" will not be loaded. Other values will be displayed as text.
     */
    String mutationStatus,

    /**
     * Indicates current sequencing phase.
     */
    String sequencingPhase,

    /**
     * Molecular assay type used to produce the analytes used for sequencing.
     */
    String sequenceSource,

    /**
     * The assay platforms used for the validation call.
     */
    String validationMethod,

    /**
     * Score
     */
    String score,

    /**
     * The BAM file used to call the variant.
     */
    String bamFile,

    /**
     * Instrument used to produce primary data.
     */
    String sequencer,

    /**
     * Amino Acid Change, e.g. p.V600E.
     */
    String hgvspShort,

    /**
     * Variant allele count (tumor).
     */
    Integer tAltCount,

    /**
     * Reference allele count (tumor).
     */
    Integer tRefCount,

    /**
     * Variant allele count (normal).
     */
    Integer nAltCount,

    /**
     * Reference allele count (normal).
     */
    Integer nRefCount
) {}