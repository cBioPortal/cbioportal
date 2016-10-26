package org.cbioportal.web.mixin.summary;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ClinicalAttributeSummaryMixin {

    private String attrId;
    private String displayName;
    private String description;
    private String datatype;
    private Boolean patientAttribute;
    private String priority;
    @JsonIgnore
    private Integer cancerStudyId;
}
