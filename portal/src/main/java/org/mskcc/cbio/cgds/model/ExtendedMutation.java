/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.cgds.model;

import org.codehaus.jackson.annotate.JsonIgnore;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Encapsules Details regarding a Single Mutation.
 *
 * @author Ethan Cerami.
 */
public class ExtendedMutation
{
	private static final String GERMLINE = "germline";

	private int geneticProfileId;
	private String caseId;
	private CanonicalGene gene;
	private String sequencingCenter;
	private String sequencer;
	private String mutationStatus;
	private String validationStatus;
	private String chr;
	private long startPosition;
	private long endPosition;
	private String proteinChange; // amino acid change
	private String mutationType; // variant classification
	private String functionalImpactScore;
	private String linkXVar;
	private String linkPdb;
	private String linkMsa;

	private String ncbiBuild;
	private String strand;
	private String variantType;
	private String referenceAllele;
	private String tumorSeqAllele1;
	private String tumorSeqAllele2;
	private String dbSnpRs;
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

	private String oncotatorCosmicOverlapping;
	private String oncotatorDbSnpRs;

	public ExtendedMutation() {
	}

	/**
	 * Constructor.
	 *
	 * @param gene              Gene Object.
	 * @param validationStatus  Validation Status,  e.g. Valid or Unknown.
	 * @param mutationStatus    Mutation Status, e.g. Somatic or Germline.
	 * @param mutationType      Mutation Type, e.g. Nonsense_Mutation, Frame_Shift_Del, etc.
	 */
	public ExtendedMutation(CanonicalGene gene, String validationStatus, String mutationStatus,
			String mutationType) {
		this.gene = gene;
		this.mutationStatus = mutationStatus;
		this.validationStatus = validationStatus;
		this.mutationType = mutationType;
	}

	/**
	 * Sets the Sequencing Center which performed the sequencing.
	 * @param center sequencing center, e.g. WashU, Broad, etc.
	 */
	public void setSequencingCenter(String center) {
		this.sequencingCenter = center;
	}

	/**
	 * Gets the Sequencing Center which performed the sequencing.
	 * @return sequencing center, e.g. WashU, Broad, etc.
	 */
	public String getSequencingCenter() {
		return sequencingCenter;
	}

	/**
	 * Gets the Mutations Status, e.g. Somatic or Germline.
	 * @return mutation status, e.g. Somatic or Germline.
	 */
	public String getMutationStatus() {
		return mutationStatus;
	}

	@JsonIgnore
	public boolean isGermlineMutation() {
		return getMutationStatus() != null && getMutationStatus().equalsIgnoreCase(GERMLINE);
	}

	/**
	 * Sets the Mutation Status, e.g. Somatic or Germline.
	 * @param mutationStatus mutation status, e.g. Somatic or Germline.
	 */
	public void setMutationStatus(String mutationStatus) {
		this.mutationStatus = mutationStatus;
	}

	/**
	 * Sets the Validation Status, e.g. Valid or Unknown.
	 * @param validationStatus validation status, e.g. Valid or Unknown.
	 */
	public void setValidationStatus(String validationStatus) {
		this.validationStatus = validationStatus;
	}

	/**
	 * Gets the Validation Status, e.g. Valid or Unknown.
	 * @return validation status, e.g. Valid or Unknown.
	 */
	public String getValidationStatus() {
		return validationStatus;
	}

	/**
	 * Sets the Mutation Type, e.g. Nonsense_Mutation, Frame_Shift_Del, etc.
	 * @param mutationType mutation type, e.g. Nonsense_Mutation, Frame_Shift_Del, etc.
	 */
	public void setMutationType(String mutationType) {
		this.mutationType = mutationType;
	}

	/**
	 * Gets the Mutation Type, e.g. Nonsense_Mutation, Frame_Shift_Del, etc.
	 * @return mutation type, e.g. Nonsense_Mutation, Frame_Shift_Del, etc.
	 */
	public String getMutationType() {
		return mutationType;
	}

	public int getGeneticProfileId() {
		return geneticProfileId;
	}

	public void setGeneticProfileId(int geneticProfileId) {
		this.geneticProfileId = geneticProfileId;
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public String getChr() {
		return chr;
	}

	public void setChr(String chr) {
		this.chr = chr;
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

	public String getProteinChange() {
		return proteinChange;
	}

	public void setProteinChange(String proteinChange) {
		this.proteinChange = proteinChange;
	}

	public String getFunctionalImpactScore() {
		return functionalImpactScore;
	}

	public void setFunctionalImpactScore(String fImpact) {
		this.functionalImpactScore = fImpact;
	}

	public String getLinkXVar() {
		return linkXVar;
	}

	public void setLinkXVar(String linkXVar) {
		this.linkXVar = linkXVar;
	}

	public String getLinkPdb() {
		return linkPdb;
	}

	public void setLinkPdb(String linkPdb) {
		this.linkPdb = linkPdb;
	}

	public String getLinkMsa() {
		return linkMsa;
	}

	public void setLinkMsa(String linkMsa) {
		this.linkMsa = linkMsa;
	}

	public String getSequencer() {
		return sequencer;
	}

	public void setSequencer(String sequencer) {
		this.sequencer = sequencer;
	}

	public String getNcbiBuild() {
		return ncbiBuild;
	}

	public void setNcbiBuild(String ncbiBuild) {
		this.ncbiBuild = ncbiBuild;
	}

	public String getStrand() {
		return strand;
	}

	public void setStrand(String strand) {
		this.strand = strand;
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

	@JsonIgnore
	public void setGene(CanonicalGene gene) {
		this.gene = gene;
	}

	@JsonIgnore
	public CanonicalGene getGene() {
		return gene;
	}

	@JsonIgnore
	public long getEntrezGeneId() {
		return gene.getEntrezGeneId();
	}

	@JsonIgnore
	public String getGeneSymbol() {
		return gene.getHugoGeneSymbolAllCaps();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
