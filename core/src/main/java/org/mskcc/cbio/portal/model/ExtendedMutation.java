/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.mskcc.cbio.maf.MafUtil;

/**
 * Encapsules Details regarding a Single Mutation.
 *
 * @author Ethan Cerami.
 */
public final class ExtendedMutation
{
    public final static class MutationEvent {
        private long mutationEventId;
        private CanonicalGene gene;
        private String chr;
            private long startPosition;
        private long endPosition;
        private String proteinChange; // amino acid change
        private String mutationType; // variant classification
        private String functionalImpactScore;
        private float fisValue;
        private String linkXVar;
        private String linkPdb;
        private String linkMsa;
            private String keyword;
        private String ncbiBuild;
        private String strand;
        private String variantType;
        private String referenceAllele;
        private String tumorSeqAllele;
        private String dbSnpRs;
        private String dbSnpValStatus;
        private String oncotatorDbSnpRs;
        private String oncotatorRefseqMrnaId;
        private String oncotatorUniprotName;
        private String oncotatorUniprotAccession;
        private String oncotatorCodonChange;
        private int oncotatorProteinPosStart;
        private int oncotatorProteinPosEnd;
        private boolean canonicalTranscript;

        public long getMutationEventId() {
            return mutationEventId;
        }

        public void setMutationEventId(long mutationEventId) {
            this.mutationEventId = mutationEventId;
        }

        @JsonIgnore
        public CanonicalGene getGene() {
            return gene;
        }

        @JsonIgnore
        public void setGene(CanonicalGene gene) {
            this.gene = gene;
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

        public String getMutationType() {
            return mutationType;
        }

        public void setMutationType(String mutationType) {
            this.mutationType = mutationType;
        }

        public String getFunctionalImpactScore() {
            return functionalImpactScore;
        }

        public void setFunctionalImpactScore(String functionalImpactScore) {
            this.functionalImpactScore = functionalImpactScore;
        }

        public float getFisValue() {
            return fisValue;
        }

        public void setFisValue(float fisValue) {
            this.fisValue = fisValue;
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

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
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

        public String getTumorSeqAllele() {
            return tumorSeqAllele;
        }

        public void setTumorSeqAllele(String tumorSeqAllele) {
            this.tumorSeqAllele = tumorSeqAllele;
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

        public String getOncotatorDbSnpRs() {
            return oncotatorDbSnpRs;
        }

        public void setOncotatorDbSnpRs(String oncotatorDbSnpRs) {
            this.oncotatorDbSnpRs = oncotatorDbSnpRs;
        }

        public String getOncotatorRefseqMrnaId() {
            return oncotatorRefseqMrnaId;
        }

        public void setOncotatorRefseqMrnaId(String oncotatorRefseqMrnaId) {
            this.oncotatorRefseqMrnaId = oncotatorRefseqMrnaId;
        }

        public String getOncotatorUniprotName() {
            return oncotatorUniprotName;
        }

        /**
         * Set the UniprotKB name (formerly known as ID) of the protein record.
         *
         * @param oncotatorUniprotName the UniprotKB name
         * @deprecated set the accession with
         *             {@link #setOncotatorUniprotAccession(String)} instead
         */
        @Deprecated
        public void setOncotatorUniprotName(String oncotatorUniprotName) {
            this.oncotatorUniprotName = oncotatorUniprotName;
        }

        public String getOncotatorUniprotAccession() {
            return oncotatorUniprotAccession;
        }

        public void setOncotatorUniprotAccession(String oncotatorUniprotAccession) {
            this.oncotatorUniprotAccession = oncotatorUniprotAccession;
        }

        public String getOncotatorCodonChange() {
            return oncotatorCodonChange;
        }

        public void setOncotatorCodonChange(String oncotatorCodonChange) {
            this.oncotatorCodonChange = oncotatorCodonChange;
        }

        public int getOncotatorProteinPosStart() {
            return oncotatorProteinPosStart;
        }

        public void setOncotatorProteinPosStart(int oncotatorProteinPosStart) {
            this.oncotatorProteinPosStart = oncotatorProteinPosStart;
        }

        public int getOncotatorProteinPosEnd() {
            return oncotatorProteinPosEnd;
        }

        public void setOncotatorProteinPosEnd(int oncotatorProteinPosEnd) {
            this.oncotatorProteinPosEnd = oncotatorProteinPosEnd;
        }

        public boolean isCanonicalTranscript() {
            return canonicalTranscript;
        }

        public void setCanonicalTranscript(boolean canonicalTranscript) {
            this.canonicalTranscript = canonicalTranscript;
        }

        // The fields used here have to be the same as in sql file.
        // The comparison has to be case insensitive, since this is how MYSQL will
        // look at the values for the UQ constraint in DB.
        @Override
        public int hashCode() {
            int hash = 3;
            hash = 37 * hash + (this.gene != null ? this.gene.hashCode() : 0);
            hash = 37 * hash + (this.chr != null ? this.chr.toUpperCase().hashCode() : 0);
            hash = 37 * hash + (int) (this.startPosition ^ (this.startPosition >>> 32));
            hash = 37 * hash + (int) (this.endPosition ^ (this.endPosition >>> 32));
            hash = 37 * hash + (this.proteinChange != null ? this.proteinChange.toUpperCase().hashCode() : 0);
            hash = 37 * hash + (this.tumorSeqAllele != null ? this.tumorSeqAllele.toUpperCase().hashCode() : 0);
            hash = 37 * hash + (this.mutationType != null ? this.mutationType.toUpperCase().hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MutationEvent other = (MutationEvent) obj;
            if (this.gene != other.gene && (this.gene == null || !this.gene.equals(other.gene))) {
                return false;
            }
            if ((this.chr == null) ? (other.chr != null) : !this.chr.equalsIgnoreCase(other.chr)) {
                return false;
            }
            if (this.startPosition != other.startPosition) {
                return false;
            }
            if (this.endPosition != other.endPosition) {
                return false;
            }
            if ((this.proteinChange == null) ? (other.proteinChange != null) : !this.proteinChange.equalsIgnoreCase(other.proteinChange)) {
                return false;
            }
            if ((this.tumorSeqAllele == null) ? (other.tumorSeqAllele != null) : !this.tumorSeqAllele.equalsIgnoreCase(other.tumorSeqAllele)) {
                return false;
            }
            if ((this.mutationType == null) ? (other.mutationType != null) : !this.mutationType.equalsIgnoreCase(other.mutationType)) {
                return false;
            }
            return true;
        }
    }
    private static final String GERMLINE = "germline";

    private MutationEvent event;
    private int geneticProfileId;
    private int sampleId;
    private String sequencingCenter;
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
	private String aminoAcidChange;
    private Integer tumorAltCount;
    private Integer tumorRefCount;
    private Integer normalAltCount;
    private Integer normalRefCount;
    private String driverFilter;
    private String driverFilterAnn;
    private String driverTiersFilter;
    private String driverTiersFilterAnn;
    private String annotationJson;

    public ExtendedMutation() {
        this(new MutationEvent());
    }
    
    public ExtendedMutation(MutationEvent event) {
         this.event = event;
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
        this();
        this.setGene(gene);
        this.mutationStatus = mutationStatus;
        this.validationStatus = validationStatus;
        this.setMutationType(mutationType);
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
        event.setMutationType(mutationType);
    }

    /**
     * Gets the Mutation Type, e.g. Nonsense_Mutation, Frame_Shift_Del, etc.
     * @return mutation type, e.g. Nonsense_Mutation, Frame_Shift_Del, etc.
     */
    public String getMutationType() {
        return event.getMutationType();
    }

    public int getGeneticProfileId() {
        return geneticProfileId;
    }

    public void setGeneticProfileId(int geneticProfileId) {
        this.geneticProfileId = geneticProfileId;
    }

    public int getSampleId() {
        return sampleId;
    }

    public void setSampleId(int sampleId) {
        this.sampleId = sampleId;
    }

    public String getChr() {
        return event.getChr();
    }

    public void setChr(String chr) {
        event.setChr(chr);
    }

    public long getStartPosition() {
        return event.getStartPosition();
    }

    public void setStartPosition(long startPosition) {
        event.setStartPosition(startPosition);
    }

    public long getEndPosition() {
        return event.getEndPosition();
    }

    public void setEndPosition(long endPosition) {
        event.setEndPosition(endPosition);
    }

    public String getProteinChange() {
        return event.getProteinChange();
    }

    public void setProteinChange(String proteinChange) {
        event.setProteinChange(proteinChange);
    }

	public String getAminoAcidChange()
	{
		return aminoAcidChange;
	}

	public void setAminoAcidChange(String aminoAcidChange)
	{
		this.aminoAcidChange = aminoAcidChange;
	}

	public String getFunctionalImpactScore() {
        return event.getFunctionalImpactScore();
    }

    public void setFunctionalImpactScore(String fImpact) {
        event.setFunctionalImpactScore(fImpact);
    }

    public float getFisValue() {
        return event.getFisValue();
    }

    public void setFisValue(Float fisValue) {
        event.setFisValue(fisValue);
    }

    public String getLinkXVar() {
        return event.getLinkXVar();
    }

    public void setLinkXVar(String linkXVar) {
        event.setLinkXVar(linkXVar);
    }

    public String getLinkPdb() {
        return event.getLinkPdb();
    }

    public void setLinkPdb(String linkPdb) {
        event.setLinkPdb(linkPdb);
    }

    public String getLinkMsa() {
        return event.getLinkMsa();
    }

    public void setLinkMsa(String linkMsa) {
        event.setLinkMsa(linkMsa);
    }

    public String getSequencer() {
        return sequencer;
    }

    public void setSequencer(String sequencer) {
        this.sequencer = sequencer;
    }

    public String getNcbiBuild() {
        return event.getNcbiBuild();
    }

    public void setNcbiBuild(String ncbiBuild) {
        event.setNcbiBuild(ncbiBuild);
    }

    public String getStrand() {
        return event.getStrand();
    }

    public void setStrand(String strand) {
        event.setStrand(strand);
    }

    public String getVariantType() {
        return event.getVariantType();
    }

    public void setVariantType(String variantType) {
        event.setVariantType(variantType);
    }

    public String getReferenceAllele() {
        return event.getReferenceAllele();
    }

    public void setReferenceAllele(String referenceAllele) {
        event.setReferenceAllele(referenceAllele);
    }

    public String getTumorSeqAllele() {
        return event.getTumorSeqAllele();
    }

    public void setTumorSeqAllele(String tumorSeqAllele) {
        event.setTumorSeqAllele(tumorSeqAllele);
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
        
        
        
    /**
     * Set alleles. 
     * For variant allele: one of the tumor sequence alleles is selected.
     * @see MafUtil#resolveTumorSeqAllele(String referenceAllele, String tumorSeqAllele1, String tumorSeqAllele2)
     *
     * @param varAllele1  the first variant allele
     * @param varAllele2  the second variant allele
     * @param refAllele  the reference allele
     * @return tumor sequence allele
     */
    public void setAllele(String varAllele1, String varAllele2, String refAllele)
    {
        this.setReferenceAllele(refAllele);
        this.setTumorSeqAllele1(varAllele1);
        this.setTumorSeqAllele2(varAllele2);
        this.setTumorSeqAllele(MafUtil.resolveTumorSeqAllele(refAllele, varAllele1, varAllele2));
    }

    public String getDbSnpRs() {
        return event.getDbSnpRs();
    }

    public void setDbSnpRs(String dbSnpRs) {
        event.setDbSnpRs(dbSnpRs);
    }

    public String getDbSnpValStatus() {
        return event.getDbSnpValStatus();
    }

    public void setDbSnpValStatus(String dbSnpValStatus) {
        event.setDbSnpValStatus(dbSnpValStatus);
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

    public String getOncotatorDbSnpRs() {
        return event.getOncotatorDbSnpRs();
    }

    public void setOncotatorDbSnpRs(String oncotatorDbSnpRs) {
        event.setOncotatorDbSnpRs(oncotatorDbSnpRs);
    }

    public String getOncotatorRefseqMrnaId()
    {
        return event.getOncotatorRefseqMrnaId();
    }

    public void setOncotatorRefseqMrnaId(String oncotatorRefseqMrnaId)
    {
        event.setOncotatorRefseqMrnaId(oncotatorRefseqMrnaId);
    }

    public String getOncotatorUniprotName()
    {
        return event.getOncotatorUniprotName();
    }

    /**
     * Set the UniprotKB name (formerly known as ID) of the protein record.
     *
     * @param oncotatorUniprotName the UniprotKB name
     * @deprecated set the accession with
     *             {@link #setOncotatorUniprotAccession(String)} instead
     */
    @Deprecated
    public void setOncotatorUniprotName(String oncotatorUniprotName)
    {
        event.setOncotatorUniprotName(oncotatorUniprotName);
    }

    public String getOncotatorUniprotAccession()
    {
        return event.getOncotatorUniprotAccession();
    }

    public void setOncotatorUniprotAccession(String oncotatorUniprotAccession)
    {
        event.setOncotatorUniprotAccession(oncotatorUniprotAccession);
    }

    public String getOncotatorCodonChange()
    {
        return event.getOncotatorCodonChange();
    }

    public void setOncotatorCodonChange(String oncotatorCodonChange)
    {
        event.setOncotatorCodonChange(oncotatorCodonChange);
    }

    public int getOncotatorProteinPosStart()
    {
        return event.getOncotatorProteinPosStart();
    }

    public void setOncotatorProteinPosStart(int oncotatorProteinPosStart)
    {
        event.setOncotatorProteinPosStart(oncotatorProteinPosStart);
    }

    public int getOncotatorProteinPosEnd()
    {
        return event.getOncotatorProteinPosEnd();
    }

    public void setOncotatorProteinPosEnd(int oncotatorProteinPosEnd)
    {
        event.setOncotatorProteinPosEnd(oncotatorProteinPosEnd);
    }

    public boolean isCanonicalTranscript()
    {
        return event.isCanonicalTranscript();
    }

    public void setCanonicalTranscript(boolean canonicalTranscript)
    {
        event.setCanonicalTranscript(canonicalTranscript);
    }

    @JsonIgnore
    public void setGene(CanonicalGene gene) {
        event.setGene(gene);
    }

    @JsonIgnore
    public CanonicalGene getGene() {
        return event.getGene();
    }

    @JsonIgnore
    public long getEntrezGeneId() {
        return event.getGene().getEntrezGeneId();
    }

    @JsonIgnore
    public String getGeneSymbol() {
        return event.getGene().getHugoGeneSymbolAllCaps();
    }

    public long getMutationEventId() {
        return event.getMutationEventId();
    }

    public void setMutationEventId(long mutationEventId) {
        event.setMutationEventId(mutationEventId);
    }

    public String getKeyword() {
        return event.getKeyword();
    }

    public void setKeyword(String keyword) {
        event.setKeyword(keyword);
    }

    public MutationEvent getEvent() {
        return event;
    }

    public void setEvent(MutationEvent event) {
        this.event = event;
    }
    
    public void setDriverFilter(String driverFilter) {
        this.driverFilter = driverFilter;
    }
	
    public String getDriverFilter() {
        return driverFilter;
    }

    public void setDriverFilterAnn(String driverFilterAnn) {
        this.driverFilterAnn = driverFilterAnn;
    }

    public String getDriverFilterAnn() {
        return driverFilterAnn;
    }

    public void setDriverTiersFilter(String driverTiersFilter) {
        this.driverTiersFilter = driverTiersFilter;
    }

    public String getDriverTiersFilter() {
        return driverTiersFilter;
    }

    public void setDriverTiersFilterAnn(String driverTiersFilterAnn) {
        this.driverTiersFilterAnn = driverTiersFilterAnn;
    }

    public String getDriverTiersFilterAnn() {
        return driverTiersFilterAnn;
    }

    public String getAnnotationJson() {
        return annotationJson;
    }

    public void setAnnotationJson(String annotationJson) {
        this.annotationJson = annotationJson;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.event != null ? this.event.hashCode() : 0);
        hash = 79 * hash + this.geneticProfileId;
        hash = 79 * hash;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExtendedMutation other = (ExtendedMutation) obj;
        if (this.event != other.event && (this.event == null || !this.event.equals(other.event))) {
            return false;
        }
        if (this.geneticProfileId != other.geneticProfileId) {
            return false;
        }
        if (this.sampleId != other.sampleId) {
            return false;
        }
        return true;
    }
}
