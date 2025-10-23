package org.cbioportal.legacy.model;

import java.io.Serializable;

public class InfoDb implements Serializable {

  private String dbSchemaVersion;
  private String genesetVersion;
  private String derivedTableSchemaVersion;
  private String geneTableVersion;

  public String getDbSchemaVersion() {
    return dbSchemaVersion;
  }

  public void setDbSchemaVersion(String dbSchemaVersion) {
    this.dbSchemaVersion = dbSchemaVersion;
  }

  public String getGenesetVersion() {
    return genesetVersion;
  }

  public void setGenesetVersion(String genesetVersion) {
    this.genesetVersion = genesetVersion;
  }

  public String getDerivedTableSchemaVersion() {
    return derivedTableSchemaVersion;
  }

  public void setDerivedTableSchemaVersion(String derivedTableSchemaVersion) {
    this.derivedTableSchemaVersion = derivedTableSchemaVersion;
  }

  public String getGeneTableVersion() {
    return geneTableVersion;
  }

  public void setGeneTableVersion(String geneTableVersion) {
    this.geneTableVersion = geneTableVersion;
  }
}
