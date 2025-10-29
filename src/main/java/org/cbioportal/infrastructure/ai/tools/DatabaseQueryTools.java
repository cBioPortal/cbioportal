package org.cbioportal.infrastructure.ai.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Spring AI Tool for querying cBioPortal database. This tool can be called by AI assistants to
 * retrieve cancer genomics data.
 */
@Component
public class DatabaseQueryTools {
  private static final Logger logger = LoggerFactory.getLogger(DatabaseQueryTools.class);
  private final JdbcTemplate jdbcTemplate;

  public DatabaseQueryTools(@Qualifier("clickhouseDataSource") DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    logger.info("DatabaseQueryTools initialized with ClickHouse DataSource");
  }

  /**
   * Execute a SQL query against the cBioPortal ClickHouse database. Only SELECT queries are allowed
   * for security.
   */
  @Tool(
      description =
          "Execute a read-only SQL SELECT query against the cBioPortal ClickHouse database. "
              + "Use this to retrieve cancer genomics data including studies, patients, samples, mutations, and clinical information. "
              + "Only SELECT queries are allowed for security. "
              + "The database contains base tables and optimized derived tables (*_derived). "
              + "Returns formatted query results with clear explanations.")
  public String queryDatabase(
      @ToolParam(
              description =
                  "SQL SELECT query to execute. Example: 'SELECT * FROM cancer_study LIMIT 10'")
          String query,
      @ToolParam(
              description = "Maximum number of rows to return (optional, default: 100, max: 1000)",
              required = false)
          Integer limit) {
    try {
      // Default limit
      if (limit == null) {
        limit = 100;
      }
      if (limit > 1000) {
        limit = 1000;
      }

      query = query.trim();

      // Security check: only allow SELECT queries
      if (!query.toUpperCase().startsWith("SELECT")) {
        logger.warn("Attempted to execute non-SELECT query: {}", query);
        return "Error: Only SELECT queries are allowed for security reasons";
      }

      // Add LIMIT if not present
      if (!query.toUpperCase().contains("LIMIT")) {
        query = query + " LIMIT " + limit;
      }

      logger.info("Executing query: {}", query);

      // Execute query
      List<Map<String, Object>> results = jdbcTemplate.queryForList(query);

      logger.info("Query executed successfully, returned {} rows", results.size());

      // Format results as string
      if (results.isEmpty()) {
        return "Query executed successfully but returned no results. "
            + "Consider checking if:\n"
            + "- The table exists and has data\n"
            + "- The filter conditions are correct\n"
            + "- You're using the correct column names (use getTableSchema to verify)";
      }

      return formatResultsAsMarkdown(results);

    } catch (Exception e) {
      logger.error("Error executing query: {}", query, e);

      // Provide helpful error messages
      String errorMsg = e.getMessage();
      if (errorMsg != null) {
        if (errorMsg.contains("Table") && errorMsg.contains("doesn't exist")) {
          return "Error: The specified table doesn't exist. "
              + "Use the listTables tool to see available tables, "
              + "or check the table name spelling.";
        } else if (errorMsg.contains("Unknown column")) {
          return "Error: Unknown column in the query. "
              + "Use the getTableSchema tool to see available columns for the table.";
        } else if (errorMsg.contains("syntax")) {
          return "Error: SQL syntax error. Please check the query structure. "
              + "Error details: "
              + errorMsg;
        }
      }

      return "Error executing query: "
          + errorMsg
          + "\nTip: Use getTableSchema to verify table structure before querying.";
    }
  }

  /** Format query results as a markdown table for better readability. */
  private String formatResultsAsMarkdown(List<Map<String, Object>> results) {
    if (results.isEmpty()) {
      return "No results found.";
    }

    StringBuilder sb = new StringBuilder();
    sb.append(String.format("**Query Results: %d row(s)**\n\n", results.size()));

    // Get column names from first row
    Map<String, Object> firstRow = results.get(0);
    List<String> columns = new ArrayList<>(firstRow.keySet());

    // Calculate column widths for better formatting
    Map<String, Integer> columnWidths = new HashMap<>();
    for (String col : columns) {
      int maxWidth = col.length();
      for (Map<String, Object> row : results) {
        Object value = row.get(col);
        String strValue = value == null ? "NULL" : value.toString();
        maxWidth = Math.max(maxWidth, strValue.length());
      }
      // Limit column width to 50 characters for very long values
      columnWidths.put(col, Math.min(maxWidth, 50));
    }

    // Build markdown table header
    sb.append("| ");
    for (String col : columns) {
      sb.append(String.format("%-" + columnWidths.get(col) + "s", col)).append(" | ");
    }
    sb.append("\n");

    // Add separator
    sb.append("| ");
    for (String col : columns) {
      sb.append("-".repeat(columnWidths.get(col))).append(" | ");
    }
    sb.append("\n");

    // Add data rows
    for (Map<String, Object> row : results) {
      sb.append("| ");
      for (String col : columns) {
        Object value = row.get(col);
        String strValue = value == null ? "NULL" : value.toString();

        // Truncate very long values
        if (strValue.length() > 50) {
          strValue = strValue.substring(0, 47) + "...";
        }

        sb.append(String.format("%-" + columnWidths.get(col) + "s", strValue)).append(" | ");
      }
      sb.append("\n");
    }

    // Add summary if results were limited
    if (results.size() >= 100) {
      sb.append("\n*Note: Results limited to ")
          .append(results.size())
          .append(
              " rows. Use a more specific query or adjust the limit parameter for different results.*");
    }

    return sb.toString();
  }
}
