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
}
