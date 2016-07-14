package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.MutationEvent;
import org.cbioportal.model.Sample;

public class MutationMixin {

    private Integer mutationEventId;
    private String geneticProfileId;
    private String sampleId;
    private Integer entrezGeneId;
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
    private Integer variantReadCountTumor;
    private Integer referenceReadCountTumor;
    private Integer variantReadCountNormal;
    private Integer referenceReadCountNormal;
    private String aminoAcidChange;

    @JsonUnwrapped
    private MutationEvent mutationEvent;

    @JsonUnwrapped
    private GeneticProfile geneticProfile;

    @JsonUnwrapped
    private Sample sample;

    @JsonUnwrapped
    private Gene gene;
}
