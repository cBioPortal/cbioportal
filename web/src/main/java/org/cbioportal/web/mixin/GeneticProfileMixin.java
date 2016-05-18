package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.cbioportal.model.CancerStudy;

public class GeneticProfileMixin {

    private Integer geneticProfileId;
    private String stableId;
    private String studyId;
    private String geneticAlterationType;
    private String datatype;
    private String name;
    private String description;
    private Boolean showProfileInAnalysisTab;

    @JsonUnwrapped
    private CancerStudy cancerStudy;
}
