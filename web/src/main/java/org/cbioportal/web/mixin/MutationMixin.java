package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.MutationEvent;
import org.cbioportal.model.Sample;

public class MutationMixin {

    private String aminoAcidChange;

    private String bamFile;

    private String center;

    private String matchNormSeqAllele1;

    private String matchNormSeqAllele2;

    private String matchNormValidationAllele1;

    private String matchNormValidationAllele2;

    private String matchedNormSampleBarcode;

    private String mutationStatus;

    private Integer normalAltCount;

    private Integer normalRefCount;

    private String score;

    private String sequenceSource;

    private String sequencer;

    private String sequencingPhase;

    private Integer tumorAltCount;

    private Integer tumorRefCount;

    private String tumorSeqAllele1;

    private String tumorSeqAllele2;

    private String tumorValidationAllele1;

    private String tumorValidationAllele2;

    private String validationMethod;

    private String validationStatus;

    private String verificationStatus;

    @JsonUnwrapped
    private MutationEvent mutationEvent;

    @JsonUnwrapped
    private GeneticProfile geneticProfile;

    @JsonUnwrapped
    private Sample sample;

    @JsonUnwrapped
    private Gene gene;
}
