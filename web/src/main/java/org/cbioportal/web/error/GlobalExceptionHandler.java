package org.cbioportal.web.error;

import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice("org.cbioportal.web")
public class GlobalExceptionHandler {

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedOperation() {

        return new ResponseEntity<>(new ErrorResponse("Requested API is not implemented yet."),
                HttpStatus.NOT_IMPLEMENTED);
    }

    @ExceptionHandler(StudyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStudyNotFound(StudyNotFoundException ex) {

        return new ResponseEntity<>(new ErrorResponse("Study not found: " + ex.getStudyId()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePatientNotFound(PatientNotFoundException ex) {

        return new ResponseEntity<>(new ErrorResponse("Patient not found: " + ex.getPatientId()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SampleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSampleNotFound(SampleNotFoundException ex) {

        return new ResponseEntity<>(new ErrorResponse("Sample not found: " + ex.getSampleId()),
                HttpStatus.NOT_FOUND);
    }
}
