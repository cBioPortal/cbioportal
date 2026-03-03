package org.cbioportal.application.rest.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;

public class GlobalExceptionHandlerTests {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  public void handleBadSqlGrammarReturnsInternalServerError() {
    BadSqlGrammarException ex =
        new BadSqlGrammarException("task", "SELECT 1", new SQLException("bad sql"));

    ResponseEntity<ErrorResponse> response = handler.handleBadSqlGrammar(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage())
        .isEqualTo(
            "SQL exception. If you are a maintainer of this instance, see logs for details.");
  }

  @Test
  public void handleUncaughtExceptionReturnsInternalServerError() {
    RuntimeException ex = new RuntimeException("something unexpected");

    ResponseEntity<ErrorResponse> response = handler.handleUncaughtException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage())
        .isEqualTo("An unexpected error occurred. Please contact your administrator.");
  }
}
