package org.cbioportal.web.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import org.cbioportal.service.exception.*;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Iterator;
import java.util.NoSuchElementException;


// TODO
// - consider extending extends ResponseEntityExceptionHandler
// - check controllers for not catching exceptions themselves
@ControllerAdvice("org.cbioportal.web")
public class GlobalExceptionHandler {

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedOperation() {
        return new ResponseEntity<>(new ErrorResponse("Requested API is not implemented yet"),
                HttpStatus.NOT_IMPLEMENTED);
    }

    @ExceptionHandler(StudyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStudyNotFound(StudyNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse("Study not found: " + ex.getStudyId()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePatientNotFound(PatientNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse("Patient not found in study " + ex.getStudyId() + ": " +
            ex.getPatientId()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CancerTypeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCancerTypeNotFound(CancerTypeNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse("Cancer type not found: " + ex.getCancerTypeId()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MolecularProfileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMolecularProfileNotFound(MolecularProfileNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse("Molecular profile not found: " + ex.getMolecularProfileId()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SampleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSampleNotFound(SampleNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse("Sample not found in study " + ex.getStudyId() + ": " +
                ex.getSampleId()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(GeneNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGeneNotFound(GeneNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse("Gene not found: " + ex.getGeneId()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(GeneWithMultipleEntrezIdsException.class)
    public ResponseEntity<ErrorResponse> handleGeneWithMultipleEntrezIdsException(
            GeneWithMultipleEntrezIdsException ex) {
        return new ResponseEntity<>(
                new ErrorResponse("Gene data error: multiple Entrez gene IDs for " + ex.getGeneId()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(GenesetNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGenesetNotFound(GenesetNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse("Gene set not found: " + ex.getGenesetId()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SampleListNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSampleListNotFound(SampleListNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse("Sample list not found: " + ex.getSampleListId()),
            HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ClinicalAttributeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClinicalAttributeNotFound(ClinicalAttributeNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse("Clinical attribute not found in study " + ex.getStudyId() +
            ": " + ex.getClinicalAttributeId()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(GenePanelNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGenePanelNotFound(GenePanelNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse("Gene panel not found: " + ex.getGenePanelId()),
            HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex) {
        return new ResponseEntity<>(new ErrorResponse("Request parameter is missing: " + ex.getParameterName()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(TypeMismatchException ex) {
        return new ResponseEntity<>(new ErrorResponse("Request parameter type mismatch: " + ex.getMostSpecificCause()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return new ResponseEntity<>(new ErrorResponse("There is an error in the JSON format of the request payload"),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        ConstraintViolation constraintViolation = ex.getConstraintViolations().iterator().next();
        Iterator<Path.Node> iterator = constraintViolation.getPropertyPath().iterator();
        String parameterName = null;

        while (iterator.hasNext()) {
            Path.Node node = iterator.next();
            if (node.getKind() == ElementKind.PARAMETER) {
                parameterName = node.getName();
                break;
            }
        }
        return new ResponseEntity<>(new ErrorResponse(parameterName + " " + constraintViolation.getMessage()),
            HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {

        FieldError fieldError = ex.getBindingResult().getFieldError();
        return new ResponseEntity<>(new ErrorResponse(fieldError.getField() + " " + fieldError.getDefaultMessage()),
            HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        return new ResponseEntity<>(new ErrorResponse("Access to the specified resource has been forbidden"),
            HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DataAccessTokenNoUserIdentityException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessTokenNoUserIdentityException() {
        ErrorResponse response = new ErrorResponse("No authenticated identity found while processing request");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DataAccessTokenProhibitedUserException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessTokenProhibitedUserException() {
        ErrorResponse response = new ErrorResponse("You are prohibited from using Data Access Tokens");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleAccessForbiddenException() {
        ErrorResponse response = new ErrorResponse("The access is forbidden.");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTokenNotFoundException() {
        ErrorResponse response = new ErrorResponse("Specified token cannot be found");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CacheNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCacheNotFoundException(CacheNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse("No cache found with name " + ex.getCacheName()),
            HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(CacheOperationException.class)
    public ResponseEntity<ErrorResponse> handleCacheOperationException(CacheNotFoundException ex) {
        ErrorResponse response = new ErrorResponse("Error evicting caches. Please try again or validate correct operation of your configured caching implementation.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(GenericAssayNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGenericAssayNotFound(GenericAssayNotFoundException ex) {

        return new ResponseEntity<>(new ErrorResponse("stableId not found: " + ex.getStableId()),
            HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceDefinitionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceDefinitionNotFound(ResourceDefinitionNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse("Resource not found: " + ex.getResourceId()),
                HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(BadSqlGrammarException.class)
    public ResponseEntity<ErrorResponse> handleBadSqlGrammar(BadSqlGrammarException ex) {
        ex.printStackTrace(); // we still want this to show up in the logs
        return new ResponseEntity<>(
            new ErrorResponse("SQL exception. If you are a maintainer of this instance, see logs for details."),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }
}
