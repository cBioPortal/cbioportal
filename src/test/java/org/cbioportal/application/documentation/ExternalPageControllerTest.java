package org.cbioportal.application.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ExternalPageControllerTest {

  private final ExternalPageController controller = new ExternalPageController("");

  private void assertRejected(String sourceURL) throws Exception {
    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> controller.getExternalPage(sourceURL));
    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
  }

  @Test
  void rejectsFileScheme() throws Exception {
    assertRejected("file:///etc/passwd");
  }

  @Test
  void rejectsNonHttpSchemes() throws Exception {
    assertRejected("ftp://example.com/file");
    assertRejected("gopher://example.com/");
    assertRejected("jar:file:///etc/passwd!/");
  }

  @Test
  void rejectsLoopback() throws Exception {
    assertRejected("http://localhost:8080/api/health");
    assertRejected("http://127.0.0.1/");
    assertRejected("http://[::1]/");
  }

  @Test
  void rejectsLinkLocalMetadataEndpoint() throws Exception {
    assertRejected("http://169.254.169.254/latest/meta-data/iam/security-credentials/");
  }

  @Test
  void rejectsPrivateNetwork() throws Exception {
    assertRejected("http://10.0.0.5/");
    assertRejected("http://192.168.1.1/");
    assertRejected("http://172.16.0.1/");
  }

  @Test
  void rejectsMissingHost() throws Exception {
    assertRejected("http:///etc/passwd");
  }

  @Test
  void rejectsHostNotOnAllowlistWhenConfigured() throws Exception {
    ExternalPageController restricted = new ExternalPageController("docs.cbioportal.org");
    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> restricted.getExternalPage("http://attacker.example.com/"));
    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
  }
}
