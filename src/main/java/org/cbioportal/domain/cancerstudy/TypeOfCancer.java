package org.cbioportal.domain.cancerstudy;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public record TypeOfCancer(
    String id, String name, String dedicatedColor, String shortName, String parent)
    implements Serializable {

  @JsonProperty("cancerTypeId")
  public String cancerTypeId() {
    return id;
  }

  @JsonProperty("typeOfCancerId")
  public String typeOfCancerId() {
    return id;
  }
}
