package org.cbioportal.model;

import java.io.Serializable;

public class MutationWithSampleListId extends Mutation implements Serializable {

    private String sample_list_id;

    public String getSampleListId() {
        return sample_list_id;
    }

    public void setSampleListId(String sampleListId) {
        this.sample_list_id = sampleListId;
    }

    public MutationWithSampleListId(Mutation mutation, String sampleListId) {
        setMutationEventId(mutation.getMutationEventId());
        setMutationEvent(mutation.getMutationEvent());
        setGeneticProfileId(mutation.getGeneticProfileId());
        setGeneticProfile(mutation.getGeneticProfile());
        setSampleId(mutation.getSampleId());
        setSample(mutation.getSample());
        setEntrezGeneId(mutation.getEntrezGeneId());
        setGene(mutation.getGene());
        setSequencingCenter(mutation.getSequencingCenter());
        setSequencer(mutation.getSequencer());
        setMutationStatus(mutation.getMutationStatus());
        setValidationStatus(mutation.getValidationStatus());
        setTumorSeqAllele1(mutation.getTumorSeqAllele1());
        setTumorSeqAllele2(mutation.getTumorSeqAllele2());
        setMatchedNormSampleBarcode(mutation.getMatchedNormSampleBarcode());
        setMatchNormSeqAllele1(mutation.getMatchNormSeqAllele1());
        setMatchNormSeqAllele2(mutation.getMatchNormSeqAllele2());
        setTumorValidationAllele1(mutation.getTumorValidationAllele1());
        setTumorValidationAllele2(mutation.getTumorValidationAllele2());
        setMatchNormValidationAllele1(mutation.getMatchNormValidationAllele1());
        setMatchNormValidationAllele2(mutation.getMatchNormValidationAllele2());
        setVerificationStatus(mutation.getVerificationStatus());
        setSequencingPhase(mutation.getSequencingPhase());
        setSequenceSource(mutation.getSequenceSource());
        setValidationMethod(mutation.getValidationMethod());
        setScore(mutation.getScore());
        setBamFile(mutation.getBamFile());
        setVariantReadCountTumor(mutation.getVariantReadCountTumor());
        setReferenceReadCountTumor(mutation.getReferenceReadCountTumor());
        setVariantReadCountNormal(mutation.getVariantReadCountNormal());
        setReferenceReadCountNormal(mutation.getReferenceReadCountNormal());
        setAminoAcidChange(mutation.getAminoAcidChange());
        //set sample List Id
        this.sample_list_id = sampleListId;
    }

}
