package org.cbioportal.legacy.model;

public interface StudyPermission {
  public void setReadPermission(Boolean permission);

  public Boolean getReadPermission();

  public void setDownloadPermission(Boolean permission);

  public Boolean getDownloadPermission();
}
