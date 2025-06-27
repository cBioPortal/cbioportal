package org.cbioportal.legacy.model;

import java.util.Objects;

public class StudyScopedId {
  private String studyStableId;
  private String stableId;

  public StudyScopedId() {
    super();
  }

  public StudyScopedId(String studyStableId, String stableId) {
    this.studyStableId = studyStableId;
    this.stableId = stableId;
  }

  public String getStableId() {
    return stableId;
  }

  public void setStableId(String stableId) {
    this.stableId = stableId;
  }

  public String getStudyStableId() {
    return studyStableId;
  }

  public void setStudyStableId(String studyStableId) {
    this.studyStableId = studyStableId;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    StudyScopedId that = (StudyScopedId) o;
    return Objects.equals(studyStableId, that.studyStableId)
        && Objects.equals(stableId, that.stableId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(studyStableId, stableId);
  }

  @Override
  public String toString() {
    return "StudyScopedId{"
        + "studyStableId='"
        + studyStableId
        + '\''
        + ", stableId='"
        + stableId
        + '\''
        + '}';
  }
}
