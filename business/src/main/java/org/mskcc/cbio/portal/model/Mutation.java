/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.model;

import org.cbioportal.model.Gene;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;

import java.io.Serializable;

public class Mutation implements Serializable {

    private Integer mutationEventId;
    private Integer geneticProfileId;
    private String geneticProfileStableId;
    private Integer sampleId;
    private String sampleStableId;
    private Integer entrezGeneId;
    private String center;
    private String sequencer;
    private String mutationStatus;
    private String validationStatus;
    private String tumorSeqAllele1;
    private String tumorSeqAllele2;
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
    private Integer tumorAltCount;
    private Integer tumorRefCount;
    private Integer normalAltCount;
    private Integer normalRefCount;
    private String aminoAcidChange;
    private MutationEvent mutationEvent;
    private MolecularProfile geneticProfile;
    private Sample sample;
    private Gene gene;
    private Float dipLogR;
    private Float cellularFraction;
    private Integer totalCopyNumber;
    private Integer minorCopyNumber;
    private Float cellularFractionEm;
    private Integer totalCopyNumberEm;
    private Integer minorCopyNumberEm;
    private Float purity;
    private Float ploidy;
    private Float ccfMCopies;
    private Float ccfMCopiesLower;
    private Float ccfMCopiesUpper;
    private Float ccfMCopiesProb95;
    private Float ccfMCopiesProb90;
    private Float ccfMCopiesEm;
    private Float ccfMCopiesLowerEm;
    private Float ccfMCopiesUpperEm;
    private Float ccfMCopiesProb95Em;
    private Float ccfMCopiesProb90Em;

    public Integer getMutationEventId() {
        return mutationEventId;
    }

    public void setMutationEventId(Integer mutationEventId) {
        this.mutationEventId = mutationEventId;
    }

    public Integer getGeneticProfileId() {
        return geneticProfileId;
    }

    public void setGeneticProfileId(Integer geneticProfileId) {
        this.geneticProfileId = geneticProfileId;
    }

    public String getGeneticProfileStableId() {
        return geneticProfileStableId;
    }

    public void setGeneticProfileStableId(String geneticProfileStableId) {
        this.geneticProfileStableId = geneticProfileStableId;
    }

    public Integer getSampleId() {
        return sampleId;
    }

    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }

    public String getSampleStableId() {
        return sampleStableId;
    }

    public void setSampleStableId(String sampleStableId) {
        this.sampleStableId = sampleStableId;
    }

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public String getSequencer() {
        return sequencer;
    }

    public void setSequencer(String sequencer) {
        this.sequencer = sequencer;
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

    public Integer getTumorAltCount() {
        return tumorAltCount;
    }

    public void setTumorAltCount(Integer tumorAltCount) {
        this.tumorAltCount = tumorAltCount;
    }

    public Integer getTumorRefCount() {
        return tumorRefCount;
    }

    public void setTumorRefCount(Integer tumorRefCount) {
        this.tumorRefCount = tumorRefCount;
    }

    public Integer getNormalAltCount() {
        return normalAltCount;
    }

    public void setNormalAltCount(Integer normalAltCount) {
        this.normalAltCount = normalAltCount;
    }

    public Integer getNormalRefCount() {
        return normalRefCount;
    }

    public void setNormalRefCount(Integer normalRefCount) {
        this.normalRefCount = normalRefCount;
    }

    public String getAminoAcidChange() {
        return aminoAcidChange;
    }

    public void setAminoAcidChange(String aminoAcidChange) {
        this.aminoAcidChange = aminoAcidChange;
    }

    public MutationEvent getMutationEvent() {
        return mutationEvent;
    }

    public void setMutationEvent(MutationEvent mutationEvent) {
        this.mutationEvent = mutationEvent;
    }

    public MolecularProfile getGeneticProfile() {
        return geneticProfile;
    }

    public void setGeneticProfile(MolecularProfile geneticProfile) {
        this.geneticProfile = geneticProfile;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public Gene getGene() {
        return gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }
    
    public Float getDipLogR() {
        return dipLogR;
    }

    public void setDipLogR(Float dipLogR) {
        this.dipLogR = dipLogR;
    }

    public Float getCellularFraction() {
        return cellularFraction;
    }

    public void setCellularFraction(Float cellularFraction) {
        this.cellularFraction = cellularFraction;
    }

    public Integer getTotalCopyNumber() {
        return totalCopyNumber;
    }

    public void setTotalCopyNumber(Integer totalCopyNumber) {
        this.totalCopyNumber = totalCopyNumber;
    }

    public Integer getMinorCopyNumber() {
        return minorCopyNumber;
    }

    public void setMinorCopyNumber(Integer minorCopyNumber) {
        this.minorCopyNumber = minorCopyNumber;
    }

    public Float getCellularFractionEm() {
        return cellularFractionEm;
    }

    public void setCellularFractionEm(Float cellularFractionEm) {
        this.cellularFractionEm = cellularFractionEm;
    }

    public Integer getTotalCopyNumberEm() {
        return totalCopyNumberEm;
    }

    public void setTotalCopyNumberEm(Integer totalCopyNumberEm) {
        this.totalCopyNumberEm = totalCopyNumberEm;
    }

    public Integer getMinorCopyNumberEm() {
        return minorCopyNumberEm;
    }

    public void setMinorCopyNumberEm(Integer minorCopyNumberEm) {
        this.minorCopyNumberEm = minorCopyNumberEm;
    }

    public Float getPurity() {
        return purity;
    }

    public void setPurity(Float purity) {
        this.purity = purity;
    }

    public Float getPloidy() {
        return ploidy;
    }

    public void setPloidy(Float ploidy) {
        this.ploidy = ploidy;
    }

    public Float getCcfMCopies() {
        return ccfMCopies;
    }

    public void setCcfMCopies(Float ccfMCopies) {
        this.ccfMCopies = ccfMCopies;
    }

    public Float getCcfMCopiesLower() {
        return ccfMCopiesLower;
    }

    public void setCcfMCopiesLower(Float ccfMCopiesLower) {
        this.ccfMCopiesLower = ccfMCopiesLower;
    }

    public Float getCcfMCopiesUpper() {
        return ccfMCopiesUpper;
    }

    public void setCcfMCopiesUpper(Float ccfMCopiesUpper) {
        this.ccfMCopiesUpper = ccfMCopiesUpper;
    }

    public Float getCcfMCopiesProb95() {
        return ccfMCopiesProb95;
    }

    public void setCcfMCopiesProb95(Float ccfMCopiesProb95) {
        this.ccfMCopiesProb95 = ccfMCopiesProb95;
    }

    public Float getCcfMCopiesProb90() {
        return ccfMCopiesProb90;
    }

    public void setCcfMCopiesProb90(Float ccfMCopiesProb90) {
        this.ccfMCopiesProb90 = ccfMCopiesProb90;
    }

    public Float getCcfMCopiesEm() {
        return ccfMCopiesEm;
    }

    public void setCcfMCopiesEm(Float ccfMCopiesEm) {
        this.ccfMCopiesEm = ccfMCopiesEm;
    }

    public Float getCcfMCopiesLowerEm() {
        return ccfMCopiesLowerEm;
    }

    public void setCcfMCopiesLowerEm(Float ccfMCopiesLowerEm) {
        this.ccfMCopiesLowerEm = ccfMCopiesLowerEm;
    }

    public Float getCcfMCopiesUpperEm() {
        return ccfMCopiesUpperEm;
    }

    public void setCcfMCopiesUpperEm(Float ccfMCopiesUpperEm) {
        this.ccfMCopiesUpperEm = ccfMCopiesUpperEm;
    }

    public Float getCcfMCopiesProb95Em() {
        return ccfMCopiesProb95Em;
    }

    public void setCcfMCopiesProb95Em(Float ccfMCopiesProb95Em) {
        this.ccfMCopiesProb95Em = ccfMCopiesProb95Em;
    }

    public Float getCcfMCopiesProb90Em() {
        return ccfMCopiesProb90Em;
    }

    public void setCcfMCopiesProb90Em(Float ccfMCopiesProb90Em) {
        this.ccfMCopiesProb90Em = ccfMCopiesProb90Em;
    }
}
