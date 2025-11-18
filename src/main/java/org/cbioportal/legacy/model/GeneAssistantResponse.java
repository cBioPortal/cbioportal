package org.cbioportal.legacy.model;

import java.io.Serializable;

public class GeneAssistantResponse implements Serializable {

  private String aiResponse;

  public String getAiResponse() {
    return aiResponse;
  }

  public void setAiResponse(String aiResponse) {
    this.aiResponse = aiResponse;
  }
}
