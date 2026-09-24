package org.cbioportal.application.rest.availability;

/** Thrown when a request names a study, or data owned by a study, that is being (re)imported. */
public class StudyUnavailableException extends RuntimeException {

  private final String studyId;

  public StudyUnavailableException(String studyId) {
    super("Study " + studyId + " is unavailable");
    this.studyId = studyId;
  }

  public String getStudyId() {
    return studyId;
  }
}
