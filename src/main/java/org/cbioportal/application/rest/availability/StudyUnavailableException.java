package org.cbioportal.application.rest.availability;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when a request names a study, or data owned by a study, that is being (re)imported. */
@ResponseStatus(HttpStatus.LOCKED)
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
