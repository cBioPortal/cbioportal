package org.cbioportal.application.chat;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.TextBlock;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.Tool;
import com.anthropic.models.messages.ToolResultBlockParam;
import com.anthropic.models.messages.ToolUseBlock;
import com.anthropic.models.messages.ToolUseBlockParam;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.application.chat.dto.ChatMessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Multi-step tool-calling loop. Only the final, tool-free step's text is streamed — earlier steps'
 * text is scratch commentary, not the answer.
 */
@Service
public class ChatAgentService {

  private static final Logger LOG = LoggerFactory.getLogger(ChatAgentService.class);

  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

  private static final Set<String> NAVIGATE_TOOL_NAMES =
      Set.of(
          "navigate_to_study_view",
          "navigate_to_patient_view",
          "navigate_to_results_view",
          "navigate_to_group_comparison");

  private final AnthropicClient anthropicClient;
  private final McpToolBridge mcpToolBridge;
  private final SystemPromptService systemPromptService;
  private final ObjectMapper objectMapper;

  @Value("${chat.model:claude-sonnet-5}")
  private String modelName;

  @Value("${chat.max-steps:15}")
  private int maxSteps;

  public ChatAgentService(
      AnthropicClient anthropicClient,
      McpToolBridge mcpToolBridge,
      SystemPromptService systemPromptService,
      ObjectMapper objectMapper) {
    this.anthropicClient = anthropicClient;
    this.mcpToolBridge = mcpToolBridge;
    this.systemPromptService = systemPromptService;
    this.objectMapper = objectMapper;
  }

  @Async
  public void runTurn(List<ChatMessageDto> incomingMessages, SseEmitter emitter) {
    try {
      if (incomingMessages == null || incomingMessages.isEmpty()) {
        throw new IllegalArgumentException("messages must not be empty");
      }
      List<MessageParam> history = toMessageParams(incomingMessages);
      List<ChatMessageDto> newMessages = new ArrayList<>();
      String navigateUrl = null;
      List<String> toolsCalled = new ArrayList<>();
      int stepsUsed = 0;
      boolean answered = false;

      for (int step = 0; step < maxSteps; step++) {
        stepsUsed = step + 1;
        MessageCreateParams.Builder paramsBuilder =
            MessageCreateParams.builder()
                .model(Model.of(modelName))
                .maxTokens(4096L)
                .system(systemPromptService.getSystemPrompt())
                .messages(history);
        for (Tool tool : mcpToolBridge.tools()) {
          paramsBuilder.addTool(tool);
        }

        Message response = anthropicClient.messages().create(paramsBuilder.build());

        // Built once as JSON — both the SDK-bound history entry and the client-facing DTO are
        // derived from this same node tree, instead of walking response.content() twice.
        ArrayNode assistantContentJson = toContentJson(response.content());
        history.add(
            MessageParam.builder()
                .role(MessageParam.Role.ASSISTANT)
                .contentOfBlockParams(toContentBlockParams(assistantContentJson))
                .build());
        newMessages.add(toDto("assistant", assistantContentJson));

        List<ToolUseBlock> toolUses =
            response.content().stream().flatMap(block -> block.toolUse().stream()).toList();

        if (toolUses.isEmpty()) {
          String finalText =
              response.content().stream()
                  .flatMap(block -> block.text().stream())
                  .map(TextBlock::text)
                  .collect(Collectors.joining());
          if (!finalText.isEmpty()) {
            sendEvent(emitter, "text_delta", Map.of("text", finalText));
          }
          LOG.info(
              "chat turn done: steps={} stopReason={} tools={} answerChars={} navigate={}",
              stepsUsed,
              response.stopReason().map(Object::toString).orElse("none"),
              toolsCalled,
              finalText.length(),
              navigateUrl != null ? navigateUrl : "-");
          answered = true;
          break;
        }

        ArrayNode toolResultContentJson = objectMapper.createArrayNode();
        for (ToolUseBlock toolUse : toolUses) {
          Map<String, Object> input = asMap(toolUse._input());
          long startedAt = System.currentTimeMillis();
          String resultText = mcpToolBridge.callTool(toolUse.name(), input);
          toolsCalled.add(toolUse.name());
          LOG.info(
              "  step {} tool {} -> {} chars in {} ms",
              stepsUsed,
              toolUse.name(),
              resultText.length(),
              System.currentTimeMillis() - startedAt);
          if (NAVIGATE_TOOL_NAMES.contains(toolUse.name())) {
            String url = extractUrl(resultText);
            if (url != null) {
              navigateUrl = url;
            }
          }

          ObjectNode toolResultNode = objectMapper.createObjectNode();
          toolResultNode.put("type", "tool_result");
          toolResultNode.put("tool_use_id", toolUse.id());
          toolResultNode.put("content", resultText);
          toolResultContentJson.add(toolResultNode);
        }
        history.add(
            MessageParam.builder()
                .role(MessageParam.Role.USER)
                .contentOfBlockParams(toContentBlockParams(toolResultContentJson))
                .build());
        newMessages.add(toDto("tool", toolResultContentJson));
      }

      if (!answered) {
        LOG.warn(
            "chat turn hit the {}-step cap without a final answer, tools={} — raise chat.max-steps"
                + " if this recurs",
            maxSteps,
            toolsCalled);
        String cappedMessage =
            "I wasn't able to finish answering within the allotted number of steps. Please try"
                + " rephrasing or narrowing your question.";
        sendEvent(emitter, "text_delta", Map.of("text", cappedMessage));
        ArrayNode cappedContentJson = objectMapper.createArrayNode();
        ObjectNode cappedTextNode = objectMapper.createObjectNode();
        cappedTextNode.put("type", "text");
        cappedTextNode.put("text", cappedMessage);
        cappedContentJson.add(cappedTextNode);
        newMessages.add(toDto("assistant", cappedContentJson));
      }

      if (navigateUrl != null) {
        sendEvent(emitter, "navigate", Map.of("url", navigateUrl));
      }
      sendEvent(emitter, "done", Map.of("messages", newMessages));
      emitter.complete();
    } catch (Exception e) {
      LOG.error("chat turn failed", e);
      try {
        sendEvent(
            emitter,
            "error",
            Map.of("message", e.getMessage() != null ? e.getMessage() : "Unknown error"));
      } catch (Exception sendFailure) {
        // emitter is already broken; fall through to completeWithError below
      }
      emitter.completeWithError(e);
    }
  }

  private void sendEvent(SseEmitter emitter, String eventName, Object data) throws Exception {
    emitter.send(SseEmitter.event().name(eventName).data(data, MediaType.APPLICATION_JSON));
  }

  /**
   * The single source of truth for a step's assistant content: the SDK history entry and the
   * client-facing DTO are both derived from this JSON, instead of walking response.content()
   * separately for each.
   */
  private ArrayNode toContentJson(List<ContentBlock> blocks) {
    ArrayNode contentArray = objectMapper.createArrayNode();
    for (ContentBlock block : blocks) {
      block
          .text()
          .ifPresent(
              t -> {
                ObjectNode node = objectMapper.createObjectNode();
                node.put("type", "text");
                node.put("text", t.text());
                contentArray.add(node);
              });
      block
          .toolUse()
          .ifPresent(
              t -> {
                ObjectNode node = objectMapper.createObjectNode();
                node.put("type", "tool_use");
                node.put("id", t.id());
                node.put("name", t.name());
                node.set("input", objectMapper.valueToTree(asMap(t._input())));
                contentArray.add(node);
              });
    }
    return contentArray;
  }

  private ChatMessageDto toDto(String role, ArrayNode content) {
    ChatMessageDto dto = new ChatMessageDto();
    dto.setRole(role);
    dto.setContent(content);
    return dto;
  }

  private List<MessageParam> toMessageParams(List<ChatMessageDto> incoming) {
    List<MessageParam> result = new ArrayList<>();
    for (ChatMessageDto dto : incoming) {
      if ("system".equals(dto.getRole())) {
        continue;
      }
      MessageParam.Role role =
          "tool".equals(dto.getRole())
              ? MessageParam.Role.USER
              : MessageParam.Role.of(dto.getRole());
      JsonNode content = dto.getContent();
      MessageParam.Builder builder = MessageParam.builder().role(role);
      if (content.isTextual()) {
        builder.content(content.asText());
      } else if (content.isArray()) {
        builder.contentOfBlockParams(toContentBlockParams(content));
      }
      result.add(builder.build());
    }
    return result;
  }

  private List<ContentBlockParam> toContentBlockParams(JsonNode blocksArray) {
    List<ContentBlockParam> result = new ArrayList<>();
    for (JsonNode block : blocksArray) {
      String type = block.path("type").asText();
      switch (type) {
        case "text":
          result.add(
              ContentBlockParam.ofText(
                  TextBlockParam.builder().text(block.path("text").asText()).build()));
          break;
        case "tool_use":
          result.add(
              ContentBlockParam.ofToolUse(
                  ToolUseBlockParam.builder()
                      .id(block.path("id").asText())
                      .name(block.path("name").asText())
                      .input(toToolInput(block.path("input")))
                      .build()));
          break;
        case "tool_result":
          result.add(
              ContentBlockParam.ofToolResult(
                  ToolResultBlockParam.builder()
                      .toolUseId(block.path("tool_use_id").asText())
                      .content(block.path("content").asText())
                      .build()));
          break;
        default:
          break;
      }
    }
    return result;
  }

  private ToolUseBlockParam.Input toToolInput(JsonNode inputNode) {
    ToolUseBlockParam.Input.Builder builder = ToolUseBlockParam.Input.builder();
    Map<String, Object> rawMap = objectMapper.convertValue(inputNode, MAP_TYPE);
    rawMap.forEach((key, value) -> builder.putAdditionalProperty(key, JsonValue.from(value)));
    return builder.build();
  }

  private String extractUrl(String toolResultJson) {
    try {
      JsonNode node = objectMapper.readTree(toolResultJson);
      JsonNode urlNode = node.path("url");
      return urlNode.isTextual() ? urlNode.asText() : null;
    } catch (Exception e) {
      return null;
    }
  }

  private static Map<String, Object> asMap(JsonValue value) {
    return value.convert(MAP_TYPE);
  }
}
