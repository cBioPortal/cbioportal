package org.cbioportal.application.rest.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  // ── AccessForbiddenException ───────────────────────────────────────────────

  @Test
  void handleAccessForbiddenException_returns403Forbidden_notUnauthorized() {
    ResponseEntity<ErrorResponse> response = handler.handleAccessForbiddenException();

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("The access is forbidden.", response.getBody().getMessage());
  }

  // ── ConstraintViolationException: parameter node present ──────────────────

  @Test
  void handleConstraintViolation_withParameterNode_prefixesParameterName() {
    ConstraintViolationException ex =
        buildConstraintViolationException("pageSize", "must be positive", true);

    ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("pageSize must be positive", response.getBody().getMessage());
  }

  // ── ConstraintViolationException: no parameter node (bean-level) ──────────

  @Test
  void handleConstraintViolation_withoutParameterNode_doesNotPrefixNull() {
    ConstraintViolationException ex =
        buildConstraintViolationException(null, "invalid request", false);

    ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    // Must NOT start with "null " — that was the bug
    assertEquals("invalid request", response.getBody().getMessage());
  }

  // ── HttpMessageNotReadableException ───────────────────────────────────────

  @Test
  void handleHttpMessageNotReadable_returns400WithUserFriendlyMessage() {
    HttpMessageNotReadableException ex =
        new HttpMessageNotReadableException("Unexpected character", mock(HttpInputMessage.class));

    ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(
        "There is an error in the JSON format of the request payload",
        response.getBody().getMessage());
  }

  // ── MissingServletRequestParameterException ───────────────────────────────

  @Test
  void handleMissingServletRequestParameter_returns400WithParameterName() {
    MissingServletRequestParameterException ex =
        new MissingServletRequestParameterException("studyId", "String");

    ResponseEntity<ErrorResponse> response = handler.handleMissingServletRequestParameter(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Request parameter is missing: studyId", response.getBody().getMessage());
  }

  // ── MethodArgumentNotValidException ───────────────────────────────────────

  @Test
  void handleMethodArgumentNotValid_withFieldError_returns400WithFieldName() {
    FieldError fieldError =
        new FieldError("survivalRequest", "attributeIdPrefix", "must not be null");
    BindingResult bindingResult = mock(BindingResult.class);
    when(bindingResult.getFieldError()).thenReturn(fieldError);
    MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
    when(ex.getBindingResult()).thenReturn(bindingResult);

    ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValid(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().getMessage().contains("attributeIdPrefix"));
  }

  // ── Other handlers (smoke tests) ──────────────────────────────────────────

  @Test
  void handleStudyNotFound_returns404() {
    ResponseEntity<ErrorResponse> response =
        handler.handleStudyNotFound(new StudyNotFoundException("acc_tcga"));

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Study not found: acc_tcga", response.getBody().getMessage());
  }

  @Test
  void handleAccessDeniedException_returns403() {
    ResponseEntity<ErrorResponse> response =
        handler.handleAccessDeniedException(new AccessDeniedException("denied"));

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void handleUnsupportedOperation_returns501() {
    ResponseEntity<ErrorResponse> response = handler.handleUnsupportedOperation();

    assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  @SuppressWarnings("unchecked")
  private ConstraintViolationException buildConstraintViolationException(
      String parameterName, String message, boolean hasParameterNode) {

    Path.Node paramNode = mock(Path.Node.class);
    when(paramNode.getKind()).thenReturn(ElementKind.PARAMETER);
    if (parameterName != null) {
      when(paramNode.getName()).thenReturn(parameterName);
    }

    Path.Node methodNode = mock(Path.Node.class);
    when(methodNode.getKind()).thenReturn(ElementKind.METHOD);

    Iterator<Path.Node> nodeIterator;
    if (hasParameterNode) {
      nodeIterator = List.of(methodNode, paramNode).iterator();
    } else {
      nodeIterator = List.of(methodNode).iterator();
    }

    Path path = mock(Path.class);
    when(path.iterator()).thenReturn(nodeIterator);

    ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn(message);

    return new ConstraintViolationException(Set.of(violation));
  }
}
