/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.maf;

/**
 * Encapsulates Details Regarding a Single MAF Record.
 */
public class MafRecord
{
	// standard MAF cols
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
    
    private String mannualAminoAcidChange;

	// allele frequency cols
    private int tumorAltCount;
    private int tumorRefCount;
    private int normalAltCount;
    private int normalRefCount;
    private int tTotCov;
    private int tVarCov;
    private int nTotCov;
    private int nVarCov;
    private int tumorDepth;
    private float tumorVaf;
    private int normalDepth;
    private float normalVaf;

	// Mutation Assessor cols
	private String maFuncImpact;
	private float maFIS;
	private String maLinkVar;
	private String maLinkMsa;
	private String maLinkPdb;
	private String maProteinChange;

	// Oncotator cols
	private String oncotatorCosmicOverlapping;
	private String oncotatorDbSnpRs;
	private String oncotatorDbSnpValStatus;
	private String oncotatorProteinChange;
    private String oncotatorVariantClassification;
	private String oncotatorGeneSymbol;
	private String oncotatorRefseqMrnaId;
	private String oncotatorRefseqProtId;
	private int oncotatorExonAffected;
	private String oncotatorTranscriptChange;
	private String oncotatorUniprotName;
	private String oncotatorUniprotAccession;
	private String oncotatorCodonChange;
	private int oncotatorProteinPosStart;
	private int oncotatorProteinPosEnd;
	private String oncotatorProteinChangeBestEffect;
	private String oncotatorVariantClassificationBestEffect;
	private String oncotatorGeneSymbolBestEffect;
	private String oncotatorRefseqMrnaIdBestEffect;
	private String oncotatorRefseqProtIdBestEffect;
	private int oncotatorExonAffectedBestEffect;
	private String oncotatorTranscriptChangeBestEffect;
	private String oncotatorUniprotNameBestEffect;
	private String oncotatorUniprotAccessionBestEffect;
	private String oncotatorCodonChangeBestEffect;
	private int oncotatorProteinPosStartBestEffect;
	private int oncotatorProteinPosEndBestEffect;

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

    public String getMannualAminoAcidChange() {
        return mannualAminoAcidChange;
    }

    public void setMannualAminoAcidChange(String mannualAminoAcidChange) {
        this.mannualAminoAcidChange = mannualAminoAcidChange;
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

    public int getTTotCov() {
        return tTotCov;
    }

    public void setTTotCov(int tTotCov) {
        this.tTotCov = tTotCov;
    }

    public int getTVarCov() {
        return tVarCov;
    }

    public void setTVarCov(int tVarCov) {
        this.tVarCov = tVarCov;
    }

    public int getNTotCov() {
        return nTotCov;
    }

    public void setNTotCov(int nTotCov) {
        this.nTotCov = nTotCov;
    }

    public int getNVarCov() {
        return nVarCov;
    }

    public void setNVarCov(int nVarCov) {
        this.nVarCov = nVarCov;
    }

    public int getTumorDepth() {
        return tumorDepth;
    }

    public void setTumorDepth(int tumorDepth) {
        this.tumorDepth = tumorDepth;
    }

    public float getTumorVaf() {
        return tumorVaf;
    }

    public void setTumorVaf(float tumorVaf) {
        this.tumorVaf = tumorVaf;
    }

    public int getNormalDepth() {
        return normalDepth;
    }

    public void setNormalDepth(int normalDepth) {
        this.normalDepth = normalDepth;
    }

    public float getNormalVaf() {
        return normalVaf;
    }

    public void setNormalVaf(float normalVaf) {
        this.normalVaf = normalVaf;
    }

	public String getOncotatorProteinChange()
	{
		return oncotatorProteinChange;
	}

	public void setOncotatorProteinChange(String oncotatorProteinChange)
	{
		this.oncotatorProteinChange = oncotatorProteinChange;
	}

	public String getOncotatorVariantClassification()
	{
		return oncotatorVariantClassification;
	}

	public void setOncotatorVariantClassification(String oncotatorVariantClassification)
	{
		this.oncotatorVariantClassification = oncotatorVariantClassification;
	}

	public String getOncotatorCosmicOverlapping()
	{
		return oncotatorCosmicOverlapping;
	}

	public void setOncotatorCosmicOverlapping(String oncotatorCosmicOverlapping)
	{
		this.oncotatorCosmicOverlapping = oncotatorCosmicOverlapping;
	}

	public String getOncotatorDbSnpRs()
	{
		return oncotatorDbSnpRs;
	}

	public void setOncotatorDbSnpRs(String oncotatorDbSnpRs)
	{
		this.oncotatorDbSnpRs = oncotatorDbSnpRs;
	}

	public String getOncotatorGeneSymbol()
	{
		return oncotatorGeneSymbol;
	}

	public void setOncotatorGeneSymbol(String oncotatorGeneSymbol)
	{
		this.oncotatorGeneSymbol = oncotatorGeneSymbol;
	}

	public String getOncotatorDbSnpValStatus()
	{
		return oncotatorDbSnpValStatus;
	}

	public void setOncotatorDbSnpValStatus(String oncotatorDbSnpValStatus)
	{
		this.oncotatorDbSnpValStatus = oncotatorDbSnpValStatus;
	}

	public String getOncotatorRefseqMrnaId()
	{
		return oncotatorRefseqMrnaId;
	}

	public void setOncotatorRefseqMrnaId(String oncotatorRefseqMrnaId)
	{
		this.oncotatorRefseqMrnaId = oncotatorRefseqMrnaId;
	}

	public String getOncotatorRefseqProtId()
	{
		return oncotatorRefseqProtId;
	}

	public void setOncotatorRefseqProtId(String oncotatorRefseqProtId)
	{
		this.oncotatorRefseqProtId = oncotatorRefseqProtId;
	}

	public int getOncotatorExonAffected()
	{
		return oncotatorExonAffected;
	}

	public void setOncotatorExonAffected(int oncotatorExonAffected)
	{
		this.oncotatorExonAffected = oncotatorExonAffected;
	}

	public String getOncotatorTranscriptChange()
	{
		return oncotatorTranscriptChange;
	}

	public void setOncotatorTranscriptChange(String oncotatorTranscriptChange)
	{
		this.oncotatorTranscriptChange = oncotatorTranscriptChange;
	}

	public String getOncotatorUniprotName()
	{
		return oncotatorUniprotName;
	}

	public void setOncotatorUniprotName(String oncotatorUniprotName)
	{
		this.oncotatorUniprotName = oncotatorUniprotName;
	}

	public String getOncotatorUniprotAccession()
	{
		return oncotatorUniprotAccession;
	}

	public void setOncotatorUniprotAccession(String oncotatorUniprotAccession)
	{
		this.oncotatorUniprotAccession = oncotatorUniprotAccession;
	}

	public String getOncotatorCodonChange()
	{
		return oncotatorCodonChange;
	}

	public void setOncotatorCodonChange(String oncotatorCodonChange)
	{
		this.oncotatorCodonChange = oncotatorCodonChange;
	}

	public int getOncotatorProteinPosStart()
	{
		return oncotatorProteinPosStart;
	}

	public void setOncotatorProteinPosStart(int oncotatorProteinPosStart)
	{
		this.oncotatorProteinPosStart = oncotatorProteinPosStart;
	}

	public int getOncotatorProteinPosEnd()
	{
		return oncotatorProteinPosEnd;
	}

	public void setOncotatorProteinPosEnd(int oncotatorProteinPosEnd)
	{
		this.oncotatorProteinPosEnd = oncotatorProteinPosEnd;
	}

	public String getOncotatorProteinChangeBestEffect()
	{
		return oncotatorProteinChangeBestEffect;
	}

	public void setOncotatorProteinChangeBestEffect(String oncotatorProteinChangeBestEffect)
	{
		this.oncotatorProteinChangeBestEffect = oncotatorProteinChangeBestEffect;
	}

	public String getOncotatorVariantClassificationBestEffect()
	{
		return oncotatorVariantClassificationBestEffect;
	}

	public void setOncotatorVariantClassificationBestEffect(String oncotatorVariantClassificationBestEffect)
	{
		this.oncotatorVariantClassificationBestEffect = oncotatorVariantClassificationBestEffect;
	}

	public String getOncotatorGeneSymbolBestEffect()
	{
		return oncotatorGeneSymbolBestEffect;
	}

	public void setOncotatorGeneSymbolBestEffect(String oncotatorGeneSymbolBestEffect)
	{
		this.oncotatorGeneSymbolBestEffect = oncotatorGeneSymbolBestEffect;
	}

	public String getOncotatorRefseqMrnaIdBestEffect()
	{
		return oncotatorRefseqMrnaIdBestEffect;
	}

	public void setOncotatorRefseqMrnaIdBestEffect(String oncotatorRefseqMrnaIdBestEffect)
	{
		this.oncotatorRefseqMrnaIdBestEffect = oncotatorRefseqMrnaIdBestEffect;
	}

	public String getOncotatorRefseqProtIdBestEffect()
	{
		return oncotatorRefseqProtIdBestEffect;
	}

	public void setOncotatorRefseqProtIdBestEffect(String oncotatorRefseqProtIdBestEffect)
	{
		this.oncotatorRefseqProtIdBestEffect = oncotatorRefseqProtIdBestEffect;
	}

	public int getOncotatorExonAffectedBestEffect()
	{
		return oncotatorExonAffectedBestEffect;
	}

	public void setOncotatorExonAffectedBestEffect(int oncotatorExonAffectedBestEffect)
	{
		this.oncotatorExonAffectedBestEffect = oncotatorExonAffectedBestEffect;
	}

	public String getOncotatorTranscriptChangeBestEffect()
	{
		return oncotatorTranscriptChangeBestEffect;
	}

	public void setOncotatorTranscriptChangeBestEffect(String oncotatorTranscriptChangeBestEffect)
	{
		this.oncotatorTranscriptChangeBestEffect = oncotatorTranscriptChangeBestEffect;
	}

	public String getOncotatorUniprotNameBestEffect()
	{
		return oncotatorUniprotNameBestEffect;
	}

	public void setOncotatorUniprotNameBestEffect(String oncotatorUniprotNameBestEffect)
	{
		this.oncotatorUniprotNameBestEffect = oncotatorUniprotNameBestEffect;
	}

	public String getOncotatorUniprotAccessionBestEffect()
	{
		return oncotatorUniprotAccessionBestEffect;
	}

	public void setOncotatorUniprotAccessionBestEffect(String oncotatorUniprotAccessionBestEffect)
	{
		this.oncotatorUniprotAccessionBestEffect = oncotatorUniprotAccessionBestEffect;
	}

	public String getOncotatorCodonChangeBestEffect()
	{
		return oncotatorCodonChangeBestEffect;
	}

	public void setOncotatorCodonChangeBestEffect(String oncotatorCodonChangeBestEffect)
	{
		this.oncotatorCodonChangeBestEffect = oncotatorCodonChangeBestEffect;
	}

	public int getOncotatorProteinPosStartBestEffect()
	{
		return oncotatorProteinPosStartBestEffect;
	}

	public void setOncotatorProteinPosStartBestEffect(int oncotatorProteinPosStartBestEffect)
	{
		this.oncotatorProteinPosStartBestEffect = oncotatorProteinPosStartBestEffect;
	}

	public int getOncotatorProteinPosEndBestEffect()
	{
		return oncotatorProteinPosEndBestEffect;
	}

	public void setOncotatorProteinPosEndBestEffect(int oncotatorProteinPosEndBestEffect)
	{
		this.oncotatorProteinPosEndBestEffect = oncotatorProteinPosEndBestEffect;
	}

	public String getMaFuncImpact() {
		return maFuncImpact;
	}

	public void setMaFuncImpact(String maFuncImpact) {
		this.maFuncImpact = maFuncImpact;
	}

	public float getMaFIS() {
		return maFIS;
	}

	public void setMaFIS(float maFIS) {
		this.maFIS = maFIS;
	}

	public String getMaLinkVar() {
		return maLinkVar;
	}

	public void setMaLinkVar(String maLinkVar) {
		this.maLinkVar = maLinkVar;
	}

	public String getMaLinkMsa() {
		return maLinkMsa;
	}

	public void setMaLinkMsa(String maLinkMsa) {
		this.maLinkMsa = maLinkMsa;
	}

	public String getMaLinkPdb() {
		return maLinkPdb;
	}

	public void setMaLinkPdb(String maLinkPdb) {
		this.maLinkPdb = maLinkPdb;
	}

	public String getMaProteinChange() {
		return maProteinChange;
	}

	public void setMaProteinChange(String maProteinChange) {
		this.maProteinChange = maProteinChange;
	}
}
