package org.mskcc.cbio.maf;

/**
 * Utility Class for Parsing MAF Files.
 *
 * This utility class handles variable columns and column orderings within MAF Files.
 * (Comments next to data fields indicate corresponding database columns or
 * column headers in the MAF file).
 */
public class MafUtil
{
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
    private int tumorSampleIndex = -1;
    private int mutationStatusIndex = -1; // MUTATION_STATUS
    private int validationStatusIndex = -1; // VALIDATION_STATUS
    private int sequencerIndex = -1; // SEQUENCER_INDEX
    private int dbSnpValStatusIndex = -1; // DB_SNP_VAL_STATUS
    private int matchedNormSampleBarcodeIndex = -1; // MATCHED_NORM_SAMPLE_BARCODE
    private int matchNormSeqAllele1Index = -1; // MATCH_NORM_SEQ_ALLELE1
    private int matchNormSeqAllele2Index = -1; // MATCH_NORM_SEQ_ALLELE2
    private int tumorValidationAllele1Index = -1; // TUMOR_VALIDATION_ALLELE1
    private int tumorValidationAllele2Index = -1; // TUMOR_VALIDATION_ALLELE2
    private int matchNormValidationAllele1Index = -1; // MATCH_NORM_VALIDATION_ALLELE1
    private int matchNormValidationAllele2Index = -1; // MATCH_NORM_VALIDATION_ALLELE2
    private int verificationStatusIndex = -1; // VERIFICATION_STATUS
    private int sequencingPhaseIndex = -1; // SEQUENCING_PHASE
    private int sequenceSourceIndex = -1; // SEQUENCE_SOURCE
    private int validationMethodIndex = -1; // VALIDATION_METHOD
    private int scoreIndex = -1; // SCORE
    private int bamFileIndex = -1; // BAM_FILE
    private int tumorAltCountIndex = -1; // TUMOR_ALT_COUNT
    private int tumorRefCountIndex = -1; // TUMOR_REF_COUNT
    private int normalAltCountIndex = -1; // NORMAL_ALT_COUNT
    private int normalRefCountIndex = -1; // NORMAL_REF_COUNT
    private int oncoProteinChangeIndex = -1; // ONCOTATOR_PROTEIN_CHANGE
    private int oncoVariantClassificationIndex = -1; // ONCOTATOR_VARIANT_CLASSIFICATION
    private int oncoCosmicOverlappingIndex = -1; // ONCOTATOR_DBSNP_RS
    private int oncoDbSnpRsIndex = -1; // ONCOTATOR_COSMIC_OVERLAPPING
    
    private int headerCount; // number of headers in the header line

	/**
     * Constructor.
     * 
     * @param headerLine    Header Line.
     */
    public MafUtil(String headerLine)
    {
        // split header names
    	String parts[] = headerLine.split("\t");
        
    	// update header count
        this.headerCount = parts.length;
        
        // find required header indices
        for (int i=0; i<parts.length; i++)
        {
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
	        } else if(header.equalsIgnoreCase("dbSNP_Val_Status")) {
	        	dbSnpValStatusIndex = i;
	        } else if(header.equalsIgnoreCase("Matched_Norm_Sample_Barcode")) {
	        	matchedNormSampleBarcodeIndex = i;
	        } else if(header.equalsIgnoreCase("Match_Norm_Seq_Allele1")) {
	        	matchNormSeqAllele1Index = i;
	        } else if(header.equalsIgnoreCase("Match_Norm_Seq_Allele2")) {
	        	matchNormSeqAllele2Index = i;
	        } else if(header.equalsIgnoreCase("Tumor_Validation_Allele1")) {
	        	tumorValidationAllele1Index = i;
	        } else if(header.equalsIgnoreCase("Tumor_Validation_Allele2")) {
	        	tumorValidationAllele2Index = i;
	        } else if(header.equalsIgnoreCase("Match_Norm_Validation_Allele1")) {
	        	matchNormValidationAllele1Index = i;
	        } else if(header.equalsIgnoreCase("Match_Norm_Validation_Allele2")) {
	        	matchNormValidationAllele2Index = i;
	        } else if(header.equalsIgnoreCase("Verification_Status")) {
	        	verificationStatusIndex = i;
	        } else if(header.equalsIgnoreCase("Sequencing_Phase")) {
	        	sequencingPhaseIndex = i;
	        } else if(header.equalsIgnoreCase("Sequence_Source")) {
	        	sequenceSourceIndex = i;
	        } else if(header.equalsIgnoreCase("Validation_Method")) {
	        	validationMethodIndex = i;
	        } else if(header.equalsIgnoreCase("Score")) {
	        	scoreIndex = i;
	        } else if(header.equalsIgnoreCase("BAM_file")) {
	        	bamFileIndex = i;
	        } else if(header.equalsIgnoreCase("ONCOTATOR_PROTEIN_CHANGE")) {
	        	oncoProteinChangeIndex = i;
	        } else if(header.equalsIgnoreCase("ONCOTATOR_VARIANT_CLASSIFICATION")) {
	        	oncoVariantClassificationIndex = i;
	        } else if(header.equalsIgnoreCase("ONCOTATOR_COSMIC_OVERLAPPING")) {
	        	oncoCosmicOverlappingIndex = i;
	        } else if(header.equalsIgnoreCase("ONCOTATOR_DBSNP_RS")) {
	        	oncoDbSnpRsIndex = i;
	        }
            // TODO will be decided later...
//	        } else if(header.equalsIgnoreCase("t_ref_count")) { // TODO alternative header names?
//	        	tumorRefCountIndex = i;
//	        } else if(header.equalsIgnoreCase("t_alt_count")) { // TODO alternative header names?
//	        	tumorAltCountIndex = i;
//	        } else if(header.equalsIgnoreCase("i_t_ref_count")) { // TODO is it correct header name?
//	        	normalRefCountIndex= i;
//	        } else if(header.equalsIgnoreCase("i_t_alt_count")) { // TODO is it correct header name?
//	        	normalAltCountIndex = i;
//	        }
        }
    }
    
    public MafRecord parseRecord(String line)
    {
        String parts[] = line.split("\t", -1);
        
        MafRecord record = new MafRecord();
        record.setCenter(getPartString(centerIndex, parts));
        record.setChr(getPartString(chrIndex, parts));
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
        record.setDbSnpValStatus(getPartString(dbSnpValStatusIndex, parts));
        record.setMatchedNormSampleBarcode(getPartString(matchedNormSampleBarcodeIndex, parts));
        record.setMatchNormSeqAllele1(getPartString(matchNormSeqAllele1Index, parts));
        record.setMatchNormSeqAllele2(getPartString(matchNormSeqAllele2Index, parts));
        record.setTumorValidationAllele1(getPartString(tumorValidationAllele1Index, parts));
        record.setTumorValidationAllele2(getPartString(tumorValidationAllele2Index, parts));
        record.setMatchNormValidationAllele1(getPartString(matchNormValidationAllele1Index, parts));
        record.setMatchNormValidationAllele2(getPartString(matchNormValidationAllele2Index, parts));
        record.setVerificationStatus(getPartString(verificationStatusIndex, parts));
        record.setSequencingPhase(getPartString(sequencingPhaseIndex, parts));
        record.setSequenceSource(getPartString(sequenceSourceIndex, parts));
        record.setValidationMethod(getPartString(validationMethodIndex, parts));
        record.setScore(getPartString(scoreIndex, parts));
        record.setBamFile(getPartString(bamFileIndex, parts));
        record.setTumorAltCount(getPartInt(tumorAltCountIndex, parts));
        record.setTumorRefCount(getPartInt(tumorRefCountIndex, parts));
        record.setNormalAltCount(getPartInt(normalAltCountIndex, parts));
        record.setNormalRefCount(getPartInt(normalRefCountIndex, parts));
        record.setOncotatorProteinChange(getPartString(oncoProteinChangeIndex, parts));
        record.setOncotatorVariantClassification(getPartString(oncoVariantClassificationIndex, parts));
        record.setOncotatorCosmicOverlapping(getPartString(oncoCosmicOverlappingIndex, parts));
        record.setOncotatorDbSnpRs(getPartString(oncoDbSnpRsIndex, parts));

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
    
    private Integer getPartInt(int index, String[] parts)
    {
        try {
            String part = parts[index];
            return Integer.parseInt(part);
        } catch (ArrayIndexOutOfBoundsException e) {
            return MafRecord.NA_INT;
        } catch (NumberFormatException e) {
            return MafRecord.NA_INT;
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

	public int getValidationStatusIndex() {
		return validationStatusIndex;
	}

	public int getSequencerIndex() {
		return sequencerIndex;
	}

	public int getDbSnpValStatusIndex() {
		return dbSnpValStatusIndex;
	}

	public int getMatchedNormSampleBarcodeIndex() {
		return matchedNormSampleBarcodeIndex;
	}

	public int getMatchNormSeqAllele1Index() {
		return matchNormSeqAllele1Index;
	}

	public int getMatchNormSeqAllele2Index() {
		return matchNormSeqAllele2Index;
	}

	public int getTumorValidationAllele1Index() {
		return tumorValidationAllele1Index;
	}

	public int getTumorValidationAllele2Index() {
		return tumorValidationAllele2Index;
	}

	public int getMatchNormValidationAllele1Index() {
		return matchNormValidationAllele1Index;
	}

	public int getMatchNormValidationAllele2Index() {
		return matchNormValidationAllele2Index;
	}

	public int getVerificationStatusIndex() {
		return verificationStatusIndex;
	}

	public int getSequencingPhaseIndex() {
		return sequencingPhaseIndex;
	}

	public int getSequenceSourceIndex() {
		return sequenceSourceIndex;
	}

	public int getValidationMethodIndex() {
		return validationMethodIndex;
	}

	public int getScoreIndex() {
		return scoreIndex;
	}

	public int getBamFileIndex() {
		return bamFileIndex;
	}

	public int getTumorAltCountIndex() {
		return tumorAltCountIndex;
	}

	public int getTumorRefCountIndex() {
		return tumorRefCountIndex;
	}

	public int getNormalAltCountIndex() {
		return normalAltCountIndex;
	}

	public int getNormalRefCountIndex() {
		return normalRefCountIndex;
	}

	public int getOncoProteinChangeIndex() {
		return oncoProteinChangeIndex;
	}

	public int getOncoVariantClassificationIndex() {
		return oncoVariantClassificationIndex;
	}

	public int getOncoCosmicOverlappingIndex() {
		return oncoCosmicOverlappingIndex;
	}

	public int getOncoDbSnpRsIndex() {
		return oncoDbSnpRsIndex;
	}
	
    public int getHeaderCount() {
		return headerCount;
	}
}