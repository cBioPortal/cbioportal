package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.cbioportal.model.TypeOfCancer;

import java.util.Date;

public class CancerStudyMixin {

    private Integer cancerStudyId;
    private String cancerStudyIdentifier;
    private String typeOfCancerId;
    private String name;
    private String shortName;
    private String description;
    private Boolean publicStudy;
    private String pmid;
    private String citation;
    private String groups;
    private Integer status;
    private Date importDate;

    @JsonUnwrapped
    private TypeOfCancer typeOfCancer;
}
