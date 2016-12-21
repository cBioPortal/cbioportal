package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cbioportal.model.CancerStudy;

public class ClinicalAttributeMixin {

    private String attrId;
    private String displayName;
    private String description;
    private String datatype;
    private Boolean patientAttribute;
    private String priority;
    @JsonIgnore
    private Integer cancerStudyId;
    @JsonIgnore
    private CancerStudy cancerStudy;
}
