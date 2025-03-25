package org.cbioportal.legacy.web.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResourceDefinitionMixin {

    @JsonProperty("studyId")
    private String cancerStudyIdentifier;
}
