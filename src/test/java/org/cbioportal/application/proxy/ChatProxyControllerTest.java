package org.cbioportal.application.proxy;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Exercises the proxying itself against a real loopback HTTP server, since the behaviour worth
 * pinning down -- upstream status and Content-Type reaching the browser unchanged -- lives in how
 * the response is assembled, not in anything mockable.
 *
 * <p>Role enforcement is not covered here: {@code @PreAuthorize} is applied by a Spring Security
 * proxy that a controller slice test doesn't stand up, and {@code EndpointAuthorizationArchTest}
 * already fails the build if either endpoint loses its annotation.
 */
public class ChatProxyControllerTest {

  private static final HttpServer UPSTREAM;
  private static final int UPSTREAM_PORT;

  /** Set by each test to decide how the upstream responds to that test's single request. */
  private static volatile Consumer<HttpExchange> responder;

  static {
    try {
      UPSTREAM = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    } catch (IOException e) {
      throw new IllegalStateException("could not start the stub upstream", e);
    }
    UPSTREAM_PORT = UPSTREAM.getAddress().getPort();
    UPSTREAM.createContext("/", exchange -> responder.accept(exchange));
    UPSTREAM.start();
  }

  @AfterClass
  public static void stopUpstream() {
    UPSTREAM.stop(0);
  }

  private MockMvc mockMvc;

  @Before
  public void setUp() {
    mockMvc = mockMvcFor("http://127.0.0.1:" + UPSTREAM_PORT);
  }

  private static MockMvc mockMvcFor(String upstreamBaseUrl) {
    return MockMvcBuilders.standaloneSetup(
            new ChatProxyController(upstreamBaseUrl, 2000, 5000, 5000))
        .build();
  }

  private static void respond(HttpExchange exchange, int status, String contentType, String body) {
    try {
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", contentType);
      exchange.sendResponseHeaders(status, bytes.length);
      try (OutputStream out = exchange.getResponseBody()) {
        out.write(bytes);
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  /** Drains the async result that StreamingResponseBody produces. */
  private String bodyOf(MvcResult result) throws Exception {
    return mockMvc
        .perform(asyncDispatch(result))
        .andReturn()
        .getResponse()
        .getContentAsString(StandardCharsets.UTF_8);
  }

  @Test
  public void relaysTheUpstreamResponseBody() throws Exception {
    responder = exchange -> respond(exchange, 200, "text/event-stream", "data: hello\n\n");

    MvcResult result =
        mockMvc
            .perform(
                post("/api/chat/message")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"messages\":[]}"))
            .andExpect(request().asyncStarted())
            .andReturn();

    assertEquals(200, result.getResponse().getStatus());
    assertEquals("text/event-stream", result.getResponse().getHeader("Content-Type"));
    assertEquals("data: hello\n\n", bodyOf(result));
  }

  @Test
  public void propagatesUpstreamErrorStatus() throws Exception {
    responder = exchange -> respond(exchange, 500, "application/json", "{\"error\":\"boom\"}");

    MvcResult result =
        mockMvc
            .perform(
                post("/api/chat/message")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"messages\":[]}"))
            .andExpect(request().asyncStarted())
            .andReturn();

    // A 500 upstream must not surface as a 200 carrying an error body.
    assertEquals(500, result.getResponse().getStatus());
  }

  @Test
  public void preservesContentTypeOfBundleAssets() throws Exception {
    responder = exchange -> respond(exchange, 200, "text/javascript", "console.log(1);");

    MvcResult result =
        mockMvc
            .perform(get("/chat-sidebar/assets/index-abc123.js"))
            .andExpect(request().asyncStarted())
            .andReturn();

    // Express computes the per-asset type; guessing it here would break the bundle.
    assertEquals("text/javascript", result.getResponse().getHeader("Content-Type"));
    assertEquals("console.log(1);", bodyOf(result));
  }

  @Test
  public void forwardsTheUserNameButNotTheSessionCookie() throws Exception {
    StringBuilder seenCookie = new StringBuilder();
    StringBuilder seenUser = new StringBuilder();
    responder =
        exchange -> {
          String cookie = exchange.getRequestHeaders().getFirst("Cookie");
          if (cookie != null) {
            seenCookie.append(cookie);
          }
          String user = exchange.getRequestHeaders().getFirst("X-Chat-User");
          if (user != null) {
            seenUser.append(user);
          }
          respond(exchange, 200, "application/json", "{}");
        };

    MvcResult result =
        mockMvc
            .perform(
                get("/api/chat/models")
                    .cookie(new Cookie("JSESSIONID", "secret"))
                    .principal(() -> "alice"))
            .andExpect(request().asyncStarted())
            .andReturn();
    bodyOf(result);

    // The upstream has no authentication of its own; handing it the portal session would let
    // anything that can reach it act as the user.
    assertEquals("", seenCookie.toString());
    // The name is still useful upstream for attributing usage in logs.
    assertEquals("alice", seenUser.toString());
  }

  @Test
  public void rejectsPathTraversal() throws Exception {
    responder = exchange -> respond(exchange, 200, "text/plain", "should not be reached");

    mockMvc.perform(get("/chat-sidebar/..%2f..%2fetc/passwd")).andExpect(status().isBadRequest());
  }

  @Test
  public void reportsBadGatewayWhenUpstreamIsDown() throws Exception {
    // Port 1 is reserved and nothing listens on it, so the connect attempt fails immediately.
    MockMvc unreachable = mockMvcFor("http://127.0.0.1:1");

    unreachable.perform(get("/api/chat/health")).andExpect(status().isBadGateway());
  }

  @Test
  public void stripsTrailingSlashFromTheConfiguredBaseUrl() throws Exception {
    responder =
        exchange -> respond(exchange, 200, "application/json", exchange.getRequestURI().getPath());

    MockMvc trailingSlash = mockMvcFor("http://127.0.0.1:" + UPSTREAM_PORT + "/");
    MvcResult result =
        trailingSlash
            .perform(get("/api/chat/health"))
            .andExpect(request().asyncStarted())
            .andReturn();

    String path =
        trailingSlash
            .perform(asyncDispatch(result))
            .andReturn()
            .getResponse()
            .getContentAsString(StandardCharsets.UTF_8);
    assertEquals("/api/chat/health", path);
  }
}
