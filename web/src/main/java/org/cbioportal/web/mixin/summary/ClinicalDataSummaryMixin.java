package org.cbioportal.web.mixin.summary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cbioportal.model.ClinicalAttribute;

public class ClinicalDataSummaryMixin {

    @JsonIgnore
    private Integer internalId;
    private String attrId;
    private String attrValue;
}
