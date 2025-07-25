package org.cbioportal.legacy.model;

import java.io.Serializable;

public class NamespaceDataCount implements Serializable {

  private String value;
  private Integer count;
  private Integer totalCount;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public Integer getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(Integer totalCount) {
    this.totalCount = totalCount;
  }
}
