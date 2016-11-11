package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.MutationEvent;
import org.cbioportal.model.Sample;

public class MutationMixin {

    @JsonIgnore
    private Integer mutationEventId;
    @JsonIgnore
    private Integer geneticProfileId;
    @JsonProperty("geneticProfileId")
    private String geneticProfileStableId;
    @JsonIgnore
    private Integer sampleId;
    @JsonProperty("sampleId")
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
    private GeneticProfile geneticProfile;
    private Sample sample;
    private Gene gene;
}
