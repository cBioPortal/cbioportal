package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.cbioportal.model.TypeOfCancer;

import java.util.Date;

public class CancerStudyMixin {

    private Integer cancerStudyId;

    private String cancerStudyIdentifier;

    private String citation;

    private String description;

    private String groups;

    private Date importDate;

    private String name;

    private String pmid;

    private Boolean publicStudy;

    private String shortName;

    private Integer status;

    @JsonUnwrapped
    private TypeOfCancer typeOfCancer;
}
