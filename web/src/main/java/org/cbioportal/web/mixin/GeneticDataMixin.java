package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.Sample;

public class GeneticDataMixin {

    @JsonIgnore
    private Integer geneticProfileId;
    @JsonProperty("geneticProfileId")
    private String geneticProfileStableId;
    private Integer entrezGeneId;
    @JsonIgnore
    private Integer sampleId;
    @JsonProperty("sampleStableId")
    private String sampleStableId;
    private String value;
    private GeneticProfile geneticProfile;
    private Gene gene;
    private Sample sample;
}
