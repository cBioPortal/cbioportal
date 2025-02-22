package org.cbioportal.legacy.service.exception;

public class GenePanelNotFoundException extends Exception {

  private String genePanelId;

  public GenePanelNotFoundException(String genePanelId) {
    super();
    this.genePanelId = genePanelId;
  }

  public String getGenePanelId() {
    return genePanelId;
  }

  public void setGenePanelId(String genePanelId) {
    this.genePanelId = genePanelId;
  }
}
