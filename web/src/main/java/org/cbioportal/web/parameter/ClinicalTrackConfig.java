package org.cbioportal.web.parameter;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
class ClinicalTrackConfig {
    public String stableId;
    public String sortOrder;
    public Boolean gapOn;
}
