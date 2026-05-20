package org.cbioportal.legacy.web.parameter;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import org.cbioportal.legacy.web.validation.VirtualStudyValidationMessages;

public class VirtualStudySamples {

  @NotBlank(message = VirtualStudyValidationMessages.STUDY_ID_REQUIRED)
  private String id;

  private Set<String> samples;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Set<String> getSamples() {
    return samples;
  }

  public void setSamples(Set<String> samples) {
    this.samples = samples;
  }
}
