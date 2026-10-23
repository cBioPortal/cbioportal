package org.cbioportal.legacy.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.service.util.SessionServiceRequestHandler;
import org.cbioportal.legacy.utils.removeme.Session;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

class SessionServiceControllerTest {

  private SessionServiceRequestHandler sessionServiceRequestHandler;
  private SessionServiceController sessionServiceController;

  @BeforeEach
  void setUp() {
    sessionServiceRequestHandler = mock(SessionServiceRequestHandler.class);
    sessionServiceController =
        new SessionServiceController(
            sessionServiceRequestHandler, new ObjectMapper(), mock(VirtualStudyService.class));

    when(sessionServiceRequestHandler.createSession(
            eq(Session.SessionType.virtual_study), any(Serializable.class)))
        .thenReturn(ResponseEntity.ok(new Session()));
  }

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldRejectAnonymousUserSavingVirtualStudy() throws Exception {
    authenticateAnonymously();

    ResponseEntity<Session> response =
        sessionServiceController.addUserSavedVirtualStudy(virtualStudyRequest());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    verify(sessionServiceRequestHandler, never())
        .createSession(eq(Session.SessionType.virtual_study), any(Serializable.class));
  }

  @Test
  void shouldSaveVirtualStudyForAuthenticatedUserWithServerControlledOwnership() throws Exception {
    authenticateAs("test-user");

    ResponseEntity<Session> response =
        sessionServiceController.addUserSavedVirtualStudy(virtualStudyRequest());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    VirtualStudyData payload = capturedVirtualStudy();
    assertThat(payload.getOwner()).isEqualTo("test-user");
    assertThat(payload.getUsers()).containsExactly("test-user");
  }

  @Test
  void shouldRejectAnonymousUserCreatingVirtualStudy() throws Exception {
    authenticateAnonymously();

    ResponseEntity<Session> response =
        sessionServiceController.addSession(
            Session.SessionType.virtual_study, virtualStudyRequest());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    verify(sessionServiceRequestHandler, never())
        .createSession(eq(Session.SessionType.virtual_study), any(Serializable.class));
  }

  @Test
  void shouldCreateVirtualStudyForAuthenticatedUserWithServerControlledOwnership()
      throws Exception {
    authenticateAs("test-user");

    ResponseEntity<Session> response =
        sessionServiceController.addSession(
            Session.SessionType.virtual_study, virtualStudyRequest());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    VirtualStudyData payload = capturedVirtualStudy();
    assertThat(payload.getOwner()).isEqualTo("test-user");
  }

  @Test
  void shouldContinueAllowingAnonymousUserToCreateNonVirtualStudySession() throws Exception {
    authenticateAnonymously();
    JSONObject request = new JSONObject();
    when(sessionServiceRequestHandler.createSession(Session.SessionType.main_session, request))
        .thenReturn(ResponseEntity.ok(new Session()));

    ResponseEntity<Session> response =
        sessionServiceController.addSession(Session.SessionType.main_session, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(sessionServiceRequestHandler).createSession(Session.SessionType.main_session, request);
  }

  private VirtualStudyData capturedVirtualStudy() {
    ArgumentCaptor<Serializable> payloadCaptor = ArgumentCaptor.forClass(Serializable.class);
    verify(sessionServiceRequestHandler)
        .createSession(eq(Session.SessionType.virtual_study), payloadCaptor.capture());
    return (VirtualStudyData) payloadCaptor.getValue();
  }

  @SuppressWarnings("unchecked")
  private JSONObject virtualStudyRequest() {
    JSONObject request = new JSONObject();
    request.put("name", "Test virtual study");
    request.put("dynamic", false);
    request.put("studies", List.of(Map.of("id", "study_1", "samples", List.of("sample_1"))));
    request.put("owner", "forged-owner");
    request.put("users", List.of("forged-owner", "another-user"));
    return request;
  }

  private void authenticateAnonymously() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "test-key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  private void authenticateAs(String username) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                username, "credentials", AuthorityUtils.createAuthorityList("ROLE_USER")));
  }
}
