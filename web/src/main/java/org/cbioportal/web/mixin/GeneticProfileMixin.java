package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.GeneticProfile;

public class GeneticProfileMixin {

    @JsonIgnore
    private Integer geneticProfileId;
    private String stableId;
    @JsonIgnore
    private Integer cancerStudyId;
    @JsonProperty("cancerStudyId")
    private String cancerStudyIdentifier;
    private GeneticProfile.GeneticAlterationType geneticAlterationType;
    private String datatype;
    private String name;
    private String description;
    private Boolean showProfileInAnalysisTab;
    private CancerStudy cancerStudy;
}
