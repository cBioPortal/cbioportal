package org.cbioportal.application.file.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.function.Function;

/**
 * Represents a record in a Mutation Annotation Format (MAF) file.
 */
public class MafRecord implements TableRow {
    private static final LinkedHashMap<String, Function<MafRecord, String>> MAF_ROW = new LinkedHashMap<>();

    static {
        MAF_ROW.put("Hugo_Symbol", MafRecord::getHugoSymbol);
        MAF_ROW.put("Entrez_Gene_Id", MafRecord::getEntrezGeneId);
        MAF_ROW.put("Center", MafRecord::getCenter);
        MAF_ROW.put("NCBI_Build", MafRecord::getNcbiBuild);
        MAF_ROW.put("Chromosome", MafRecord::getChromosome);
        MAF_ROW.put("Start_Position", (mafRecord -> mafRecord.getStartPosition() == null ? null : mafRecord.getStartPosition().toString()));
        MAF_ROW.put("End_Position", mafRecord -> mafRecord.getEndPosition() == null ? null : mafRecord.getEndPosition().toString());
        MAF_ROW.put("Strand", MafRecord::getStrand);
        MAF_ROW.put("Variant_Classification", MafRecord::getVariantClassification);
        MAF_ROW.put("Variant_Type", MafRecord::getVariantType);
        MAF_ROW.put("Reference_Allele", MafRecord::getReferenceAllele);
        MAF_ROW.put("Tumor_Seq_Allele1", MafRecord::getTumorSeqAllele1);
        MAF_ROW.put("Tumor_Seq_Allele2", MafRecord::getTumorSeqAllele2);
        MAF_ROW.put("dbSNP_RS", MafRecord::getDbSnpRs);
        MAF_ROW.put("dbSNP_Val_Status", MafRecord::getDbSnpValStatus);
        MAF_ROW.put("Tumor_Sample_Barcode", MafRecord::getTumorSampleBarcode);
        MAF_ROW.put("Matched_Norm_Sample_Barcode", MafRecord::getMatchedNormSampleBarcode);
        MAF_ROW.put("Match_Norm_Seq_Allele1", MafRecord::getMatchNormSeqAllele1);
        MAF_ROW.put("Match_Norm_Seq_Allele2", MafRecord::getMatchNormSeqAllele2);
        MAF_ROW.put("Tumor_Validation_Allele1", MafRecord::getTumorValidationAllele1);
        MAF_ROW.put("Tumor_Validation_Allele2", MafRecord::getTumorValidationAllele2);
        MAF_ROW.put("Match_Norm_Validation_Allele1", MafRecord::getMatchNormValidationAllele1);
        MAF_ROW.put("Match_Norm_Validation_Allele2", MafRecord::getMatchNormValidationAllele2);
        MAF_ROW.put("Verification_Status", MafRecord::getVerificationStatus);
        MAF_ROW.put("Validation_Status", MafRecord::getValidationStatus);
        MAF_ROW.put("Mutation_Status", MafRecord::getMutationStatus);
        MAF_ROW.put("Sequencing_Phase", MafRecord::getSequencingPhase);
        MAF_ROW.put("Sequence_Source", MafRecord::getSequenceSource);
        MAF_ROW.put("Validation_Method", MafRecord::getValidationMethod);
        MAF_ROW.put("Score", MafRecord::getScore);
        MAF_ROW.put("BAM_File", MafRecord::getBamFile);
        MAF_ROW.put("Sequencer", MafRecord::getSequencer);
        MAF_ROW.put("HGVSp_Short", MafRecord::getHgvspShort);
        MAF_ROW.put("t_alt_count", (mafRecord -> mafRecord.gettAltCount() == null ? null : mafRecord.gettAltCount().toString()));
        MAF_ROW.put("t_ref_count", (mafRecord -> mafRecord.gettRefCount() == null ? null : mafRecord.gettRefCount().toString()));
        MAF_ROW.put("n_alt_count", (mafRecord -> mafRecord.getnAltCount() == null ? null : mafRecord.getnAltCount().toString()));
        MAF_ROW.put("n_ref_count", (mafRecord -> mafRecord.getnRefCount() == null ? null : mafRecord.getnRefCount().toString()));
    }

    /**
     * A HUGO gene symbol.
     */
    private String hugoSymbol;
    /**
     * A Entrez Gene identifier.
     */
    private String entrezGeneId;
    /**
     * The sequencing center.
     */
    private String center;
    /**
     * The Genome Reference Consortium Build used by a variant calling software. It must be "GRCh37" or "GRCh38" for a human, and "GRCm38" for a mouse.
     */
    private String ncbiBuild;
    /**
     * A chromosome number, e.g., "7".
     */
    private String chromosome;
    /**
     * Start position of event.
     */
    private Long startPosition;
    /**
     * End position of event.
     */
    private Long endPosition;
    /**
     * We assume that the mutation is reported for the + strand.
     */
    private String strand;
    /**
     * Translational effect of variant allele, e.g. Missense_Mutation, Silent, etc.
     */
    private String variantClassification;
    /**
     * Variant Type, e.g. SNP, DNP, etc.
     */
    private String variantType;
    /**
     * The plus strand reference allele at this position.
     */
    private String referenceAllele;
    /**
     * Primary data genotype.
     */
    private String tumorSeqAllele1;
    /**
     * Primary data genotype.
     */
    private String tumorSeqAllele2;
    /**
     * Latest dbSNP rs ID.
     */
    private String dbSnpRs;
    /**
     * dbSNP validation status.
     */
    private String dbSnpValStatus;
    /**
     * This is the sample ID. Either a TCGA barcode (patient identifier will be extracted), or for non-TCGA data, a literal SAMPLE_ID as listed in the clinical data file.
     */
    private String tumorSampleBarcode;
    /**
     * The sample ID for the matched normal sample.
     */
    private String matchedNormSampleBarcode;
    /**
     * Primary data.
     */
    private String matchNormSeqAllele1;
    /**
     * Primary data.
     */
    private String matchNormSeqAllele2;
    /**
     * Secondary data from orthogonal technology.
     */
    private String tumorValidationAllele1;
    /**
     * Secondary data from orthogonal technology.
     */
    private String tumorValidationAllele2;
    /**
     * Secondary data from orthogonal technology.
     */
    private String matchNormValidationAllele1;
    /**
     * Secondary data from orthogonal technology.
     */
    private String matchNormValidationAllele2;
    /**
     * Second pass results from independent attempt using same methods as primary data source. "Verified", "Unknown" or "NA".
     */
    private String verificationStatus;
    /**
     * Second pass results from orthogonal technology. "Valid", "Invalid", "Untested", "Inconclusive", "Redacted", "Unknown" or "NA".
     */
    private String validationStatus;
    /**
     * "Somatic" or "Germline" are supported by the UI in Mutations tab. "None", "LOH" and "Wildtype" will not be loaded. Other values will be displayed as text.
     */
    private String mutationStatus;
    /**
     * Indicates current sequencing phase.
     */
    private String sequencingPhase;
    /**
     * Molecular assay type used to produce the analytes used for sequencing.
     */
    private String sequenceSource;
    /**
     * The assay platforms used for the validation call.
     */
    private String validationMethod;
    /**
     * Score
     */
    private String score;
    /**
     * The BAM file used to call the variant.
     */
    private String bamFile;
    /**
     * Instrument used to produce primary data.
     */
    private String sequencer;
    /**
     * Amino Acid Change, e.g. p.V600E.
     */
    private String hgvspShort;
    /**
     * Variant allele count (tumor).
     */
    private Integer tAltCount;
    /**
     * Reference allele count (tumor).
     */
    private Integer tRefCount;
    /**
     * Variant allele count (normal).
     */
    private Integer nAltCount;
    /**
     * Reference allele count (normal).
     */
    private Integer nRefCount;

    public MafRecord() {
    }

    public static SequencedSet<String> getHeader() {
        return new LinkedHashSet<>(MAF_ROW.sequencedKeySet());
    }

    @Override
    public SequencedMap<String, String> toRow() {
        LinkedHashMap<String, String> row = new LinkedHashMap<>();
        MAF_ROW.sequencedEntrySet().forEach(entry -> {
            String value = entry.getValue().apply(this);
            row.put(entry.getKey(), value);
        });
        return row;
    }

    public String getHugoSymbol() {
        return hugoSymbol;
    }

    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    public String getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(String entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public String getNcbiBuild() {
        return ncbiBuild;
    }

    public void setNcbiBuild(String ncbiBuild) {
        this.ncbiBuild = ncbiBuild;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public Long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(Long startPosition) {
        this.startPosition = startPosition;
    }

    public Long getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(Long endPosition) {
        this.endPosition = endPosition;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public String getVariantClassification() {
        return variantClassification;
    }

    public void setVariantClassification(String variantClassification) {
        this.variantClassification = variantClassification;
    }

    public String getVariantType() {
        return variantType;
    }

    public void setVariantType(String variantType) {
        this.variantType = variantType;
    }

    public String getReferenceAllele() {
        return referenceAllele;
    }

    public void setReferenceAllele(String referenceAllele) {
        this.referenceAllele = referenceAllele;
    }

    public String getTumorSeqAllele1() {
        return tumorSeqAllele1;
    }

    public void setTumorSeqAllele1(String tumorSeqAllele1) {
        this.tumorSeqAllele1 = tumorSeqAllele1;
    }

    public String getTumorSeqAllele2() {
        return tumorSeqAllele2;
    }

    public void setTumorSeqAllele2(String tumorSeqAllele2) {
        this.tumorSeqAllele2 = tumorSeqAllele2;
    }

    public String getDbSnpRs() {
        return dbSnpRs;
    }

    public void setDbSnpRs(String dbSnpRs) {
        this.dbSnpRs = dbSnpRs;
    }

    public String getDbSnpValStatus() {
        return dbSnpValStatus;
    }

    public void setDbSnpValStatus(String dbSnpValStatus) {
        this.dbSnpValStatus = dbSnpValStatus;
    }

    public String getTumorSampleBarcode() {
        return tumorSampleBarcode;
    }

    public void setTumorSampleBarcode(String tumorSampleBarcode) {
        this.tumorSampleBarcode = tumorSampleBarcode;
    }

    public String getMatchedNormSampleBarcode() {
        return matchedNormSampleBarcode;
    }

    public void setMatchedNormSampleBarcode(String matchedNormSampleBarcode) {
        this.matchedNormSampleBarcode = matchedNormSampleBarcode;
    }

    public String getMatchNormSeqAllele1() {
        return matchNormSeqAllele1;
    }

    public void setMatchNormSeqAllele1(String matchNormSeqAllele1) {
        this.matchNormSeqAllele1 = matchNormSeqAllele1;
    }

    public String getMatchNormSeqAllele2() {
        return matchNormSeqAllele2;
    }

    public void setMatchNormSeqAllele2(String matchNormSeqAllele2) {
        this.matchNormSeqAllele2 = matchNormSeqAllele2;
    }

    public String getTumorValidationAllele1() {
        return tumorValidationAllele1;
    }

    public void setTumorValidationAllele1(String tumorValidationAllele1) {
        this.tumorValidationAllele1 = tumorValidationAllele1;
    }

    public String getTumorValidationAllele2() {
        return tumorValidationAllele2;
    }

    public void setTumorValidationAllele2(String tumorValidationAllele2) {
        this.tumorValidationAllele2 = tumorValidationAllele2;
    }

    public String getMatchNormValidationAllele1() {
        return matchNormValidationAllele1;
    }

    public void setMatchNormValidationAllele1(String matchNormValidationAllele1) {
        this.matchNormValidationAllele1 = matchNormValidationAllele1;
    }

    public String getMatchNormValidationAllele2() {
        return matchNormValidationAllele2;
    }

    public void setMatchNormValidationAllele2(String matchNormValidationAllele2) {
        this.matchNormValidationAllele2 = matchNormValidationAllele2;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    public String getMutationStatus() {
        return mutationStatus;
    }

    public void setMutationStatus(String mutationStatus) {
        this.mutationStatus = mutationStatus;
    }

    public String getSequencingPhase() {
        return sequencingPhase;
    }

    public void setSequencingPhase(String sequencingPhase) {
        this.sequencingPhase = sequencingPhase;
    }

    public String getSequenceSource() {
        return sequenceSource;
    }

    public void setSequenceSource(String sequenceSource) {
        this.sequenceSource = sequenceSource;
    }

    public String getValidationMethod() {
        return validationMethod;
    }

    public void setValidationMethod(String validationMethod) {
        this.validationMethod = validationMethod;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getBamFile() {
        return bamFile;
    }

    public void setBamFile(String bamFile) {
        this.bamFile = bamFile;
    }

    public String getSequencer() {
        return sequencer;
    }

    public void setSequencer(String sequencer) {
        this.sequencer = sequencer;
    }

    public String getHgvspShort() {
        return hgvspShort;
    }

    public void setHgvspShort(String hgvspShort) {
        this.hgvspShort = hgvspShort;
    }

    public Integer gettAltCount() {
        return tAltCount;
    }

    public void settAltCount(Integer tAltCount) {
        this.tAltCount = tAltCount;
    }

    public Integer gettRefCount() {
        return tRefCount;
    }

    public void settRefCount(Integer tRefCount) {
        this.tRefCount = tRefCount;
    }

    public Integer getnAltCount() {
        return nAltCount;
    }

    public void setnAltCount(Integer nAltCount) {
        this.nAltCount = nAltCount;
    }

    public Integer getnRefCount() {
        return nRefCount;
    }

    public void setnRefCount(Integer nRefCount) {
        this.nRefCount = nRefCount;
    }
}