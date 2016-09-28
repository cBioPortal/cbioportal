package org.cbioportal.web.mixin.summary;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GeneticProfileSummaryMixin {

    @JsonIgnore
    private Integer geneticProfileId;
    private String stableId;
    @JsonIgnore
    private Integer cancerStudyId;
    private String geneticAlterationType;
    private String datatype;
    private String name;
    private String description;
    private Boolean showProfileInAnalysisTab;
}
