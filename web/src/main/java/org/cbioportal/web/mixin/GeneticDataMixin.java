package org.cbioportal.web.mixin;

import org.cbioportal.model.GeneticEntity;
import org.cbioportal.model.GeneticEntity.EntityType;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.Sample;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GeneticDataMixin {

    @JsonIgnore
    private Integer geneticProfileId;
    @JsonProperty("geneticProfileId")
    private String geneticProfileStableId;
    
    @JsonIgnore
    private Integer geneticEntityId;
    @JsonProperty("geneticEntityId")
    private String geneticEntityStableId;
    private EntityType geneticEntityType;
    
    @JsonIgnore
    private Integer sampleId;
    @JsonProperty("sampleId")
    private String sampleStableId;
    
    private String value;
    
    @JsonIgnore
    private GeneticProfile geneticProfile;
    @JsonIgnore
    private GeneticEntity geneticEntity;
    @JsonIgnore
    private Sample sample;
    
}
