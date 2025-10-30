package org.cbioportal.infrastructure.ai;

import org.cbioportal.infrastructure.ai.tools.DatabaseQueryTools;
import org.cbioportal.infrastructure.ai.tools.DatabaseSchemaTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * MCP (Model Context Protocol) Server Application for cBioPortal database queries.
 *
 * <p>This is a lightweight, standalone application that exposes database query tools via the MCP
 * protocol. It communicates through stdin/stdout and is designed for fast startup and low resource
 * usage.
 *
 * <p>Usage: java -jar cbioportal-mcp-server-exec.jar --spring.profiles.active=mcp
 *
 * <p>The server exposes two main tools: - DatabaseQueryTools: Execute SQL queries against
 * ClickHouse - DatabaseSchemaTools: Retrieve table schemas and list available tables
 */
@SpringBootApplication
@ComponentScan(
    basePackages = {
      "org.cbioportal.infrastructure.ai.tools", // Tool implementations
      "org.cbioportal.legacy.properties" // DataSource configuration
    })
public class McpServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(McpServerApplication.class, args);
  }

  /**
   * Register database tools with the MCP server. The MethodToolCallbackProvider automatically
   * discovers methods annotated with @Tool in the provided tool objects.
   *
   * @param queryTools Tool for executing database queries
   * @param schemaTools Tool for retrieving schema information
   * @return ToolCallbackProvider configured with cBioPortal tools
   */
  @Bean
  public ToolCallbackProvider cbioportalTools(
      DatabaseQueryTools queryTools, DatabaseSchemaTools schemaTools) {
    return MethodToolCallbackProvider.builder().toolObjects(queryTools, schemaTools).build();
  }
}
