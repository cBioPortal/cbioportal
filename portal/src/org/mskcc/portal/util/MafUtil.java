package org.mskcc.portal.util;

import org.mskcc.portal.model.MafRecord;

/**
 * Utility Class for Parsing MAF Files.
 *
 * This utility class handles variable columns and column orderings within MAF Files.
 * (Comments next to data fields indicate corresponding database columns or
 * column headers in the MAF file).
 */
public class MafUtil {
    private int chrIndex = -1; // CHR
    private int ncbiIndex = -1; // NCBI_BUILD
    private int startPositionIndex = -1; // START_POSITION
    private int endPositionIndex = -1; // END_POSITION
    private int hugoGeneSymbolIndex = -1; 
    private int entrezGeneIdIndex = -1; // ENTREZ_GENE_ID
    private int referenceAlleleIndex = -1; // REFERENCE_ALLELE
    private int variantClassificationIndex = -1; // MUTATION_TYPE 
    private int variantTypeIndex = -1; // VARIANT_TYPE
    private int centerIndex = -1; // CENTER
    private int strandIndex = -1; // STRAND
    private int tumorSeqAllele1Index = -1; // TUMOR_SEQ_ALLELE1
    private int tumorSeqAllele2Index = -1; // TUMOR_SEQ_ALLELE1
    private int dbSNPIndex = -1; // DB_SNP_RS
    private int tumorSampleIndex = -1; //
    private int mutationStatusIndex = -1; // MUTATION_STATUS
    private int validationStatusIndex = -1; // VALIDATION_STATUS
    private int sequencerIndex = -1; // SEQUENCER_INDEX

    /**
     * Constructor.
     * 
     * @param headerLine    Header Line.
     */
    public MafUtil(String headerLine) {
        String parts[] = headerLine.split("\t");
        for (int i=0; i<parts.length; i++) {
            String header = parts[i];
            if (header.equalsIgnoreCase("Chromosome")) {
                chrIndex = i;        
            } else if(header.equals("NCBI_Build")) {
                ncbiIndex = i;   
            } else if(header.equalsIgnoreCase("Start_Position")) {
                startPositionIndex = i;
            } else if(header.equalsIgnoreCase("End_Position")) {
                endPositionIndex = i;
            } else if(header.equalsIgnoreCase("Hugo_Symbol")) {
                hugoGeneSymbolIndex = i;
            } else if(header.equalsIgnoreCase("Entrez_Gene_Id")) {
                entrezGeneIdIndex = i;
            } else if(header.equalsIgnoreCase("Reference_Allele")) {
                referenceAlleleIndex = i;
            } else if(header.equalsIgnoreCase("Variant_Classification")) {
                variantClassificationIndex = i;
            } else if(header.equalsIgnoreCase("Variant_Type")) {
                variantTypeIndex = i;
            } else if(header.equalsIgnoreCase("Center")) {
                centerIndex = i;
            } else if(header.equals("Strand")) {
                strandIndex = i;
            } else if(header.equalsIgnoreCase("Tumor_Seq_Allele1")) {
                tumorSeqAllele1Index = i;
            } else if(header.equalsIgnoreCase("Tumor_Seq_Allele2")) {
                tumorSeqAllele2Index = i;
            } else if(header.equalsIgnoreCase("dbSNP_RS")) {
                dbSNPIndex = i;
            } else if(header.equalsIgnoreCase("Tumor_Sample_Barcode")) {
                tumorSampleIndex = i;
            } else if(header.equalsIgnoreCase("Mutation_Status")) {
                mutationStatusIndex = i;
            } else if(header.equalsIgnoreCase("Validation_Status")) {
                validationStatusIndex = i;
            } else if(header.equalsIgnoreCase("Sequencer")) {
	            sequencerIndex = i;
	        }
            
        }
    }
    
    public MafRecord parseRecord(String line) {
        String parts[] = line.split("\t"); // TODO line.split("\t", -1); ?
        MafRecord record = new MafRecord();
        record.setCenter(getPartString(centerIndex, parts));
        record.setChr(getPartString(chrIndex, parts));
        record.setDbSNP_RS(getPartString(dbSNPIndex, parts));
        record.setStartPosition(getPartLong(startPositionIndex, parts));
        record.setEndPosition(getPartLong(endPositionIndex, parts));
        record.setEntrezGeneId(getPartLong(entrezGeneIdIndex, parts));
        record.setHugoGeneSymbol(getPartString(hugoGeneSymbolIndex, parts));
        record.setNcbiBuild(getPartString(ncbiIndex, parts));
        record.setReferenceAllele(getPartString(referenceAlleleIndex, parts));
        record.setStrand(getPartString(strandIndex, parts));
        record.setDbSNP_RS(getPartString(dbSNPIndex, parts));
        record.setTumorSampleID(getPartString(tumorSampleIndex, parts));
        record.setTumorSeqAllele1(getPartString(tumorSeqAllele1Index, parts));
        record.setTumorSeqAllele2(getPartString(tumorSeqAllele2Index, parts));
        record.setVariantClassification(getPartString(variantClassificationIndex, parts));
        record.setVariantType(getPartString(variantTypeIndex, parts));
        record.setMutationStatus(getPartString(mutationStatusIndex, parts));
        record.setValidationStatus(getPartString(validationStatusIndex, parts));
        record.setSequencer(getPartString(sequencerIndex, parts));
        return record;
    }
    
    private String getPartString(int index, String[] parts) {
        try {
            return parts[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            return MafRecord.NA_STRING;
        }
    }

    private Long getPartLong(int index, String[] parts) {
        try {
            String part = parts[index];
            return Long.parseLong(part);
        } catch (ArrayIndexOutOfBoundsException e) {
            return MafRecord.NA_LONG;
        } catch (NumberFormatException e) {
            return MafRecord.NA_LONG;
        }
    }    

    public int getChrIndex() {
        return chrIndex;
    }

    public int getNcbiIndex() {
        return ncbiIndex;
    }

    public int getStartPositionIndex() {
        return startPositionIndex;
    }

    public int getEndPositionIndex() {
        return endPositionIndex;
    }

    public int getHugoGeneSymbolIndex() {
        return hugoGeneSymbolIndex;
    }

    public int getEntrezGeneIdIndex() {
        return entrezGeneIdIndex;
    }

    public int getReferenceAlleleIndex() {
        return referenceAlleleIndex;
    }

    public int getVariantClassificationIndex() {
        return variantClassificationIndex;
    }

    public int getVariantTypeIndex() {
        return variantTypeIndex;
    }

    public int getCenterIndex() {
        return centerIndex;
    }

    public int getStrandIndex() {
        return strandIndex;
    }

    public int getTumorSeqAllele1Index() {
        return tumorSeqAllele1Index;
    }

    public int getTumorSeqAllele2Index() {
        return tumorSeqAllele2Index;
    }

    public int getDbSNPIndex() {
        return dbSNPIndex;
    }

    public int getTumorSampleIndex() {
        return tumorSampleIndex;
    }

    public int getMutationStatusIndex() {
        return mutationStatusIndex;
    }
}