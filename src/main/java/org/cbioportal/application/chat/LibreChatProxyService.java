package org.cbioportal.application.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cbioportal.application.chat.dto.ChatMessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Forwards chat turns to a LibreChat agent, adding the API key server-side so it never reaches the
 * browser, and translating LibreChat's Open Responses events into the sidebar's own SSE protocol.
 */
@Service
public class LibreChatProxyService {

  private static final Logger LOG = LoggerFactory.getLogger(LibreChatProxyService.class);

  // LibreChat reports which tools ran but not what they returned, so the navigation target has to
  // come out of the answer text. The agent leads with the page it navigated to, so take the first
  // link — but only when a navigation tool actually ran, otherwise any link would hijack the page.
  private static final Pattern FIRST_MARKDOWN_LINK =
      Pattern.compile("\\[[^\\]]*\\]\\((https?://[^)\\s]+)\\)");

  private static final String NAVIGATE_TOOL_MARKER = "navigate_to_";

  private final HttpClient httpClient =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
  private final ObjectMapper objectMapper;

  @Value("${chat.librechat.base-url}")
  private String baseUrl;

  @Value("${chat.librechat.api-key}")
  private String apiKey;

  @Value("${chat.librechat.agent-id}")
  private String agentId;

  public LibreChatProxyService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Async
  public void runTurn(List<ChatMessageDto> messages, SseEmitter emitter) {
    List<String> toolsCalled = new ArrayList<>();
    StringBuilder answer = new StringBuilder();
    try {
      if (messages == null || messages.isEmpty()) {
        throw new IllegalArgumentException("messages must not be empty");
      }
      HttpRequest request =
          HttpRequest.newBuilder(URI.create(baseUrl + "/api/agents/v1/responses"))
              .header("Authorization", "Bearer " + apiKey)
              .header("Content-Type", "application/json")
              .header("Accept", "text/event-stream")
              .timeout(Duration.ofMinutes(10))
              .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(messages)))
              .build();

      HttpResponse<java.io.InputStream> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

      if (response.statusCode() != 200) {
        String body = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
        throw new IllegalStateException(
            "LibreChat responded with " + response.statusCode() + ": " + body);
      }

      readEventStream(response, emitter, toolsCalled, answer);

      String navigateUrl = findNavigationTarget(toolsCalled, answer.toString());
      if (navigateUrl != null) {
        sendEvent(emitter, "navigate", Map.of("url", navigateUrl));
      }
      sendEvent(emitter, "done", Map.of("messages", List.of(assistantMessage(answer.toString()))));
      emitter.complete();
      LOG.info(
          "chat turn done: tools={} answerChars={} navigate={}",
          toolsCalled,
          answer.length(),
          navigateUrl != null ? navigateUrl : "-");
    } catch (Exception e) {
      LOG.error("chat turn failed", e);
      try {
        sendEvent(
            emitter,
            "error",
            Map.of("message", e.getMessage() != null ? e.getMessage() : "Unknown error"));
      } catch (Exception ignored) {
        // emitter already broken
      }
      emitter.completeWithError(e);
    }
  }

  /**
   * Reads the Open Responses event stream, forwarding answer text as it arrives. Tool calls surface
   * as their own events rather than as text, so nothing but the final answer reaches the browser.
   */
  private void readEventStream(
      HttpResponse<java.io.InputStream> response,
      SseEmitter emitter,
      List<String> toolsCalled,
      StringBuilder answer)
      throws Exception {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
      String eventName = null;
      StringBuilder dataBuffer = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.isEmpty()) {
          // Blank line = event boundary: dispatch what's buffered, then reset for the next one.
          if (dataBuffer.length() > 0) {
            handleEvent(eventName, dataBuffer.toString(), emitter, toolsCalled, answer);
          }
          eventName = null;
          dataBuffer.setLength(0);
        } else if (line.startsWith("event:")) {
          eventName = line.substring("event:".length()).trim();
        } else if (line.startsWith("data:")) {
          // Per the SSE spec, multiple data: lines in one event join with '\n'.
          if (dataBuffer.length() > 0) {
            dataBuffer.append('\n');
          }
          dataBuffer.append(line.substring("data:".length()).trim());
        }
      }
      if (dataBuffer.length() > 0) {
        handleEvent(eventName, dataBuffer.toString(), emitter, toolsCalled, answer);
      }
    }
  }

  private void handleEvent(
      String eventName,
      String data,
      SseEmitter emitter,
      List<String> toolsCalled,
      StringBuilder answer)
      throws Exception {
    if (eventName == null) {
      return;
    }
    switch (eventName) {
      case "response.output_text.delta" -> {
        String delta = objectMapper.readTree(data).path("delta").asText("");
        if (!delta.isEmpty()) {
          answer.append(delta);
          sendEvent(emitter, "text_delta", Map.of("text", delta));
        }
      }
      case "response.output_item.added" -> {
        JsonNode item = objectMapper.readTree(data).path("item");
        if ("function_call".equals(item.path("type").asText())) {
          toolsCalled.add(item.path("name").asText());
        }
      }
      case "response.failed", "response.incomplete" -> {
        JsonNode node = objectMapper.readTree(data);
        String message = node.path("response").path("error").path("message").asText("");
        throw new IllegalStateException(
            message.isEmpty() ? "LibreChat reported " + eventName : message);
      }
      default -> {
        // response.created, function_call_arguments.delta, content_part.*, completed: nothing to
        // forward — the browser only needs answer text.
      }
    }
  }

  private String buildRequestBody(List<ChatMessageDto> messages) throws Exception {
    // The Responses API keeps no history of its own here, so the whole conversation is replayed
    // each turn as structured input items — one per message, each keeping its own role. This
    // (unlike a flattened "Speaker: text" transcript) can't be reinterpreted by a message's own
    // text content as a fake turn boundary.
    ArrayNode input = objectMapper.createArrayNode();
    for (ChatMessageDto message : messages) {
      if (message.getContent() == null || message.getContent().isBlank()) {
        continue;
      }
      if (!"user".equals(message.getRole()) && !"assistant".equals(message.getRole())) {
        continue;
      }
      ObjectNode item = objectMapper.createObjectNode();
      item.put("type", "message");
      item.put("role", message.getRole());
      item.put("content", message.getContent());
      input.add(item);
    }
    ObjectNode body = objectMapper.createObjectNode();
    body.put("model", agentId);
    body.put("stream", true);
    body.set("input", input);
    return objectMapper.writeValueAsString(body);
  }

  private String findNavigationTarget(List<String> toolsCalled, String answer) {
    boolean navigated = toolsCalled.stream().anyMatch(t -> t.contains(NAVIGATE_TOOL_MARKER));
    if (!navigated) {
      return null;
    }
    Matcher matcher = FIRST_MARKDOWN_LINK.matcher(answer);
    return matcher.find() ? matcher.group(1) : null;
  }

  private ChatMessageDto assistantMessage(String text) {
    ChatMessageDto dto = new ChatMessageDto();
    dto.setRole("assistant");
    dto.setContent(text);
    return dto;
  }

  private void sendEvent(SseEmitter emitter, String eventName, Object data) throws Exception {
    emitter.send(SseEmitter.event().name(eventName).data(data, MediaType.APPLICATION_JSON));
  }
}
