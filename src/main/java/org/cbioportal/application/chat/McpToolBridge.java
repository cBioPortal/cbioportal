package org.cbioportal.application.chat;

import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.Tool;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.PreDestroy;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Exposes both MCP servers' tools as Anthropic Tool definitions and dispatches calls to them. */
@Component
public class McpToolBridge {

  private final McpSyncClient navigatorClient;
  private final McpSyncClient dataQueryClient;
  private final Map<String, McpSyncClient> toolOwners = new HashMap<>();
  private final List<Tool> tools = new ArrayList<>();

  public McpToolBridge(
      @Value("${chat.mcp.navigator-url}") String navigatorUrl,
      @Value("${chat.mcp.data-query-url}") String dataQueryUrl) {
    this.navigatorClient = connect(navigatorUrl);
    this.dataQueryClient = connect(dataQueryUrl);
    registerTools(navigatorClient);
    registerTools(dataQueryClient);
  }

  private McpSyncClient connect(String url) {
    // Transport takes origin and path separately. Keep the path exact: FastMCP serves "/mcp/" and
    // 307-redirects "/mcp", which the JDK HTTP client won't follow.
    URI uri = URI.create(url);
    String baseUri = uri.getScheme() + "://" + uri.getAuthority();
    String endpoint = uri.getRawPath();
    HttpClientStreamableHttpTransport transport =
        HttpClientStreamableHttpTransport.builder(baseUri).endpoint(endpoint).build();
    McpSyncClient client =
        McpClient.sync(transport)
            .requestTimeout(Duration.ofSeconds(120))
            .clientInfo(McpSchema.Implementation.builder("cbioportal-chat-agent", "0.1.0").build())
            .build();
    client.initialize();
    return client;
  }

  private void registerTools(McpSyncClient client) {
    for (McpSchema.Tool mcpTool : client.listTools().tools()) {
      toolOwners.put(mcpTool.name(), client);
      tools.add(toAnthropicTool(mcpTool));
    }
  }

  private Tool toAnthropicTool(McpSchema.Tool mcpTool) {
    Map<String, Object> schema = mcpTool.inputSchema();
    Tool.InputSchema.Builder inputSchema = Tool.InputSchema.builder();

    Object type = schema.get("type");
    inputSchema.type(JsonValue.from(type != null ? type : "object"));

    Object propertiesValue = schema.get("properties");
    if (propertiesValue instanceof Map<?, ?> properties) {
      Tool.InputSchema.Properties.Builder propertiesBuilder = Tool.InputSchema.Properties.builder();
      properties.forEach(
          (key, value) ->
              propertiesBuilder.putAdditionalProperty(String.valueOf(key), JsonValue.from(value)));
      inputSchema.properties(propertiesBuilder.build());
    }

    Object requiredValue = schema.get("required");
    if (requiredValue instanceof List<?> required) {
      List<String> requiredNames = new ArrayList<>();
      required.forEach(name -> requiredNames.add(String.valueOf(name)));
      inputSchema.required(requiredNames);
    }

    return Tool.builder()
        .name(mcpTool.name())
        .description(mcpTool.description() != null ? mcpTool.description() : "")
        .inputSchema(inputSchema.build())
        .build();
  }

  public List<Tool> tools() {
    return tools;
  }

  public String callTool(String name, Map<String, Object> input) {
    McpSyncClient client = toolOwners.get(name);
    if (client == null) {
      return "Error: no MCP server registered the tool \"" + name + "\"";
    }
    McpSchema.CallToolResult result = client.callTool(new McpSchema.CallToolRequest(name, input));
    StringBuilder text = new StringBuilder();
    for (McpSchema.Content content : result.content()) {
      if (content instanceof McpSchema.TextContent textContent) {
        text.append(textContent.text());
      }
    }
    return text.toString();
  }

  @PreDestroy
  public void shutdown() {
    navigatorClient.closeGracefully();
    dataQueryClient.closeGracefully();
  }
}
