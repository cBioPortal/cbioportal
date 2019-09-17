package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.cbioportal.model.TypeOfCancer;

import java.util.Date;

public class CancerStudyMixin {

    @JsonIgnore
    private Integer cancerStudyId;
    @JsonProperty("studyId")
    private String cancerStudyIdentifier;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date importDate;
    @JsonProperty("cancerTypeId")
    private String typeOfCancerId;
    @JsonProperty("cancerType")
    private TypeOfCancer typeOfCancer;
    @JsonProperty("referenceGenome")
    private String referenceGenome;
}
