package org.cbioportal.application.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * The open /api/chat/oql endpoints, proxied against a stub upstream on loopback. Which user header
 * reaches the upstream is decided in the controller, so it is checked here; the @PreAuthorize rules
 * themselves need a Spring Security context and are guarded by EndpointAuthorizationArchTest.
 */
public class ChatProxyControllerOqlTest {

  private static final HttpServer UPSTREAM;
  private static final int UPSTREAM_PORT;

  /** What the upstream saw on the latest request. */
  private static volatile String lastPath;

  private static volatile String lastChatUser;

  static {
    try {
      UPSTREAM = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    } catch (IOException e) {
      throw new IllegalStateException("could not start the stub upstream", e);
    }
    UPSTREAM_PORT = UPSTREAM.getAddress().getPort();
    UPSTREAM.createContext(
        "/",
        exchange -> {
          lastPath = exchange.getRequestURI().getPath();
          lastChatUser = exchange.getRequestHeaders().getFirst("X-Chat-User");
          byte[] bytes = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
          }
        });
    UPSTREAM.start();
  }

  @AfterClass
  public static void stopUpstream() {
    UPSTREAM.stop(0);
  }

  private MockMvc mockMvc;

  @Before
  public void setUp() {
    lastPath = null;
    lastChatUser = null;
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new ChatProxyController(
                    "http://127.0.0.1:" + UPSTREAM_PORT, 2000, 5000, 5000, 5000))
            .build();
  }

  private MvcResult postAs(String path, String user, String body) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post(path)
                    .principal(() -> user)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(request().asyncStarted())
            .andReturn();
    mockMvc.perform(asyncDispatch(result)).andReturn();
    return result;
  }

  @Test
  public void oqlRequestsReachUpstreamWithoutTheUser() throws Exception {
    MvcResult result =
        postAs("/api/chat/oql/translate", "someone@example.org", "{\"prompt\":\"KRAS G12C\"}");

    assertEquals(200, result.getResponse().getStatus());
    assertEquals("/api/chat/oql/translate", lastPath);
    assertNull(lastChatUser);
  }

  @Test
  public void chatRequestsStillCarryTheUser() throws Exception {
    postAs("/api/chat/threads", "someone@example.org", "{\"id\":\"t1\"}");

    assertEquals("/api/chat/threads", lastPath);
    assertEquals("someone@example.org", lastChatUser);
  }

  @Test
  public void oversizedTranslateRequestsAreRefused() throws Exception {
    mockMvc
        .perform(
            post("/api/chat/oql/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"prompt\":\"" + "x".repeat(16 * 1024) + "\"}"))
        .andExpect(status().isPayloadTooLarge());
    assertNull(lastPath);
  }

  @Test
  public void largeQueriesStillReachValidate() throws Exception {
    postAs(
        "/api/chat/oql/validate",
        "someone@example.org",
        "{\"dsl\":\"" + "x".repeat(32 * 1024) + "\"}");

    assertEquals("/api/chat/oql/validate", lastPath);
  }

  @Test
  public void oversizedQueriesAreRefused() throws Exception {
    mockMvc
        .perform(
            post("/api/chat/oql/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"dsl\":\"" + "x".repeat(64 * 1024) + "\"}"))
        .andExpect(status().isPayloadTooLarge());
    assertNull(lastPath);
  }
}
