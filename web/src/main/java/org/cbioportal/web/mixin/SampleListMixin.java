package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.cbioportal.model.CancerStudy;

public class SampleListMixin {

    @JsonIgnore
    private Integer listId;
    @JsonProperty("sampleListId")
    private String stableId;
    private String category;
    @JsonIgnore
    private Integer cancerStudyId;
    @JsonProperty("cancerStudyId")
    private String cancerStudyIdentifier;
    private CancerStudy cancerStudy;
    private String name;
    private String description;
}
