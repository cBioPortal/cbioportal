package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.cbioportal.model.CancerStudy;

public class GeneticProfileMixin {

    private Integer geneticProfileId;

    private String datatype;

    private String description;

    private String geneticAlterationType;

    private String name;

    private Boolean showProfileInAnalysisTab;

    private String stableId;

    @JsonUnwrapped
    private CancerStudy cancerStudy;
}
