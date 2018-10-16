package org.mskcc.portal.model;

/**
 * Encapsulates Details Regarding a Single MAF Record.
 */
public class MafRecord {
    public final static String NA_STRING = "NA";
    public final static long NA_LONG = -1L;
    public final static int NA_INT = -1;

    private String chr;
    private String ncbiBuild;
    private long startPosition;
    private long endPosition;
    private String hugoGeneSymbol;
    private long entrezGeneId;
    private String referenceAllele;
    private String variantClassification; // mutation type
    private String variantType;
    private String center; // sequencing center
    private String strand;
    private String tumorSeqAllele1;
    private String tumorSeqAllele2;
    private String dbSNP_RS;
    private String tumorSampleID;
    private String mutationStatus;
    private String validationStatus;
    
    private String sequencer;
    private String dbSnpValStatus;
    private String matchedNormSampleBarcode;
    private String matchNormSeqAllele1;
    private String matchNormSeqAllele2;
    private String tumorValidationAllele1;
    private String tumorValidationAllele2;
    private String matchNormValidationAllele1;
    private String matchNormValidationAllele2;
    private String verificationStatus;
    private String sequencingPhase;
    private String sequenceSource;
    private String validationMethod;
    private String score;
    private String bamFile;
    
    private int tumorAltCount;
    private int tumorRefCount;
    private int normalAltCount;
    private int normalRefCount;
    
    private String oncotatorProteinChange;
    private String oncotatorVariantClassification;
    private String oncotatorCosmicOverlapping;
    private String oncotatorDbSnpRs;

    //FACETS
    private float dipLogR;
    private float cellularFraction;
    private int totalCopyNumber;
    private int minorCopyNumber;
    private float cellularFractionEm;
    private int totalCopyNumberEm;
    private int minorCopyNumberEm;
    private float purity;
    private float ploidy;
    private float ccfMCopies;
    private float ccfMCopiesLower;
    private float ccfMCopiesUpper;
    private float ccfMCopiesProb95;
    private float ccfMCopiesProb90;
    private float ccfMCopiesEm;
    private float ccfMCopiesLowerEm;
    private float ccfMCopiesUpperEm;
    private float ccfMCopiesProb95Em;
    private float ccfMCopiesProb90Em;

	public String getChr() {
        return chr;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }

    public String getNcbiBuild() {
        return ncbiBuild;
    }

    public void setNcbiBuild(String ncbiBuild) {
        this.ncbiBuild = ncbiBuild;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public long getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(long endPosition) {
        this.endPosition = endPosition;
    }

    public String getHugoGeneSymbol() {
        return hugoGeneSymbol;
    }

    public void setHugoGeneSymbol(String hugoGeneSymbol) {
        this.hugoGeneSymbol = hugoGeneSymbol;
    }

    public long getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(long entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getReferenceAllele() {
        return referenceAllele;
    }

    public void setReferenceAllele(String referenceAllele) {
        this.referenceAllele = referenceAllele;
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

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
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

    public String getDbSNP_RS() {
        return dbSNP_RS;
    }

    public void setDbSNP_RS(String dbSNP_RS) {
        this.dbSNP_RS = dbSNP_RS;
    }

    public String getTumorSampleID() {
        return tumorSampleID;
    }

    public void setTumorSampleID(String tumorSampleID) {
        this.tumorSampleID = tumorSampleID;
    }

    public String getMutationStatus() {
        return mutationStatus;
    }

    public void setMutationStatus(String mutationStatus) {
        this.mutationStatus = mutationStatus;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }
    
    public String getSequencer() {
		return sequencer;
	}

	public void setSequencer(String sequencer) {
		this.sequencer = sequencer;
	}

	public String getDbSnpValStatus() {
		return dbSnpValStatus;
	}

	public void setDbSnpValStatus(String dbSnpValStatus) {
		this.dbSnpValStatus = dbSnpValStatus;
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

	public int getTumorAltCount() {
		return tumorAltCount;
	}

	public void setTumorAltCount(int tumorAltCount) {
		this.tumorAltCount = tumorAltCount;
	}

	public int getTumorRefCount() {
		return tumorRefCount;
	}

	public void setTumorRefCount(int tumorRefCount) {
		this.tumorRefCount = tumorRefCount;
	}

	public int getNormalAltCount() {
		return normalAltCount;
	}

	public void setNormalAltCount(int normalAltCount) {
		this.normalAltCount = normalAltCount;
	}

	public int getNormalRefCount() {
		return normalRefCount;
	}

	public void setNormalRefCount(int normalRefCount) {
		this.normalRefCount = normalRefCount;
	}

	public String getOncotatorProteinChange() {
		return oncotatorProteinChange;
	}

	public void setOncotatorProteinChange(String oncotatorProteinChange) {
		this.oncotatorProteinChange = oncotatorProteinChange;
	}

	public String getOncotatorVariantClassification() {
		return oncotatorVariantClassification;
	}

	public void setOncotatorVariantClassification(
			String oncotatorVariantClassification) {
		this.oncotatorVariantClassification = oncotatorVariantClassification;
	}

	public String getOncotatorCosmicOverlapping() {
		return oncotatorCosmicOverlapping;
	}

	public void setOncotatorCosmicOverlapping(String oncotatorCosmicOverlapping) {
		this.oncotatorCosmicOverlapping = oncotatorCosmicOverlapping;
	}

	public String getOncotatorDbSnpRs() {
		return oncotatorDbSnpRs;
	}

	public void setOncotatorDbSnpRs(String oncotatorDbSnpRs) {
		this.oncotatorDbSnpRs = oncotatorDbSnpRs;
	}
        
        public float getDipLogR() {
                return dipLogR;
        }

        public void setDipLogR(float dipLogR) {
                this.dipLogR = dipLogR;
        }

        public float getCellularFraction() {
                return cellularFraction;
        }

        public void setCellularFraction(float cellularFraction) {
                this.cellularFraction = cellularFraction;
        }

        public int getTotalCopyNumber() {
                return totalCopyNumber;
        }

        public void setTotalCopyNumber(int totalCopyNumber) {
                this.totalCopyNumber = totalCopyNumber;
        }

        public int getMinorCopyNumber() {
                return minorCopyNumber;
        }

        public void setMinorCopyNumber(int minorCopyNumber) {
                this.minorCopyNumber = minorCopyNumber;
        }

        public float getCellularFractionEm() {
                return cellularFractionEm;
        }

        public void setCellularFractionEm(float cellularFractionEm) {
                this.cellularFractionEm = cellularFractionEm;
        }

        public int getTotalCopyNumberEm() {
                return totalCopyNumberEm;
        }

        public void setTotalCopyNumberEm(int totalCopyNumberEm) {
                this.totalCopyNumberEm = totalCopyNumberEm;
        }

        public int getMinorCopyNumberEm() {
                return minorCopyNumberEm;
        }

        public void setMinorCopyNumberEm(int minorCopyNumberEm) {
                this.minorCopyNumberEm = minorCopyNumberEm;
        }
        public float getPurity() {
                return purity;
        }

        public void setPurity(float purity) {
                this.purity = purity;
        }

        public float getPloidy() {
                return ploidy;
        }

        public void setPloidy(float ploidy) {
                this.ploidy = ploidy;
        }

        public float getCcfMCopies() {
                return ccfMCopies;
        }

        public void setCcfMCopies(float ccfMCopies) {
                this.ccfMCopies = ccfMCopies; }

        public float getCcfMCopiesLower() {
                return ccfMCopiesLower;
        }

        public void setCcfMCopiesLower(float ccfMCopiesLower) {
                this.ccfMCopiesLower = ccfMCopiesLower;
        }

        public float getCcfMCopiesUpper() {
                return ccfMCopiesUpper;
        }

        public void setCcfMCopiesUpper(float ccfMCopiesUpper) {
                this.ccfMCopiesUpper = ccfMCopiesUpper;
        }

        public float getCcfMCopiesProb95() {
                return ccfMCopiesProb95;
        }

        public void setCcfMCopiesProb95(float ccfMCopiesProb95) {
                this.ccfMCopiesProb95 = ccfMCopiesProb95;
        }

        public float getCcfMCopiesProb90() {
                return ccfMCopiesProb90;
        }

        public void setCcfMCopiesProb90(float ccfMCopiesProb90) {
                this.ccfMCopiesProb90 = ccfMCopiesProb90;
        }

        public float getCcfMCopiesEm() {
                return ccfMCopiesEm;
        }

        public void setCcfMCopiesEm(float ccfMCopiesEm) {
                this.ccfMCopiesEm = ccfMCopiesEm;
        }

        public float getCcfMCopiesLowerEm() {
                return ccfMCopiesLowerEm;
        }

        public void setCcfMCopiesLowerEm(float ccfMCopiesLowerEm) {
                this.ccfMCopiesLowerEm = ccfMCopiesLowerEm;
        }

        public float getCcfMCopiesUpperEm() {
                return ccfMCopiesUpperEm;
        }

        public void setCcfMCopiesUpperEm(float ccfMCopiesUpperEm) {
                this.ccfMCopiesUpperEm = ccfMCopiesUpperEm;
        }

        public float getCcfMCopiesProb95Em() {
                return ccfMCopiesProb95Em;
        }

        public void setCcfMCopiesProb95Em(float ccfMCopiesProb95Em) {
                this.ccfMCopiesProb95Em = ccfMCopiesProb95Em;
        }

        public float getCcfMCopiesProb90Em() {
                return ccfMCopiesProb90Em;
        }

        public void setCcfMCopiesProb90Em(float ccfMCopiesProb90Em) {
                this.ccfMCopiesProb90Em = ccfMCopiesProb90Em;
        }	
	
}
