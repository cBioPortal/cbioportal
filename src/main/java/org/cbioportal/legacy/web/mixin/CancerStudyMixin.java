package org.cbioportal.legacy.web.mixin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import org.cbioportal.legacy.model.TypeOfCancer;

public class CancerStudyMixin {

  @JsonIgnore private Integer cancerStudyId;

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
