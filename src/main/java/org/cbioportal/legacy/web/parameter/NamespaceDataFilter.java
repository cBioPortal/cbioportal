package org.cbioportal.legacy.web.parameter;

import java.io.Serializable;
import java.util.List;

public class NamespaceDataFilter implements Serializable {

  private String outerKey;
  private String innerKey;
  private List<List<DataFilterValue>> values;

  public List<List<DataFilterValue>> getValues() {
    return values;
  }

  public void setValues(List<List<DataFilterValue>> values) {
    this.values = values;
  }

  public String getOuterKey() {
    return outerKey;
  }

  public void setOuterKey(String outerKey) {
    this.outerKey = outerKey;
  }

  public String getInnerKey() {
    return innerKey;
  }

  public void setInnerKey(String innerKey) {
    this.innerKey = innerKey;
  }
}
