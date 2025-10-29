package org.cbioportal.infrastructure.ai.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Spring AI Tool for retrieving database schema information. Reads and parses SQL schema files to
 * provide table structure on demand.
 */
@Component
public class DatabaseSchemaTools {
  private static final Logger logger = LoggerFactory.getLogger(DatabaseSchemaTools.class);

  private static final String MYSQL_SCHEMA_PATH = "db-scripts/cgds.sql";
  private static final String CLICKHOUSE_SCHEMA_PATH = "db-scripts/clickhouse/clickhouse.sql";

  /**
   * Retrieve the schema definition for a specific table. This helps the AI understand table
   * structure before writing queries.
   */
  @Tool(
      description =
          "Get the schema definition (CREATE TABLE statement) for a specific database table. "
              + "Use this tool when you need to understand the structure, columns, data types, or relationships "
              + "of a table before writing a query. "
              + "Tables are available in the ClickHouse database. Many tables also exist in MySQL with the same structure.")
  public String getTableSchema(
      @ToolParam(
              description =
                  "Name of the table to get schema for (e.g., 'cancer_study', 'mutation', 'patient')")
          String tableName) {
    try {
      // Try ClickHouse schema first (cgds.sql contains both MySQL and ClickHouse compatible tables)
      String schemaPath = MYSQL_SCHEMA_PATH; // cgds.sql contains the main table definitions

      logger.info("Retrieving schema for table '{}' from {}", tableName, schemaPath);

      String schema = extractTableSchema(schemaPath, tableName);

      // If not found in cgds.sql, try ClickHouse-specific schema
      if (schema == null || schema.isEmpty()) {
        schemaPath = CLICKHOUSE_SCHEMA_PATH;
        logger.info("Table not found in cgds.sql, trying ClickHouse schema");
        schema = extractTableSchema(schemaPath, tableName);
      }

      if (schema == null || schema.isEmpty()) {
        return String.format(
            "Table '%s' not found in schema. "
                + "Please check the table name (case-insensitive). "
                + "Use the listTables tool to see all available tables. "
                + "Common tables include: cancer_study, patient, sample, mutation, "
                + "clinical_patient, clinical_sample, genetic_profile, gene.",
            tableName);
      }

      return String.format("Schema for table '%s' (ClickHouse database):\n\n%s", tableName, schema);

    } catch (Exception e) {
      logger.error("Error retrieving schema for table: {}", tableName, e);
      return "Error retrieving schema: " + e.getMessage();
    }
  }

  /** List all available tables in the database. Useful for exploring the database structure. */
  @Tool(
      description =
          "List all available tables in the ClickHouse database. "
              + "Use this to explore what tables are available when you're unsure what data exists.")
  public String listTables() {
    try {
      logger.info("Listing all tables from schema files");

      // Collect tables from both schema files
      List<String> tablesFromCgds = extractAllTableNames(MYSQL_SCHEMA_PATH);
      List<String> tablesFromClickhouse = extractAllTableNames(CLICKHOUSE_SCHEMA_PATH);

      // Combine and deduplicate
      List<String> allTables = new ArrayList<>(tablesFromCgds);
      for (String table : tablesFromClickhouse) {
        if (!allTables.contains(table)) {
          allTables.add(table);
        }
      }

      if (allTables.isEmpty()) {
        return "No tables found in schema files.";
      }

      StringBuilder result = new StringBuilder();
      result.append(
          String.format(
              "Available tables in ClickHouse database (%d total):\n\n", allTables.size()));

      // Separate base tables and derived tables
      List<String> baseTables = new ArrayList<>();
      List<String> derivedTables = new ArrayList<>();

      for (String table : allTables) {
        if (table.toLowerCase().endsWith("_derived")) {
          derivedTables.add(table);
        } else {
          baseTables.add(table);
        }
      }

      // Display base tables grouped by category
      if (!baseTables.isEmpty()) {
        result.append("** Base Tables ** (original data)\n\n");

        // Group base tables by category
        List<String> coreTables = new ArrayList<>();
        List<String> clinicalTables = new ArrayList<>();
        List<String> genomicTables = new ArrayList<>();
        List<String> otherTables = new ArrayList<>();

        for (String table : baseTables) {
          String lower = table.toLowerCase();
          if (lower.contains("clinical")) {
            clinicalTables.add(table);
          } else if (lower.matches(".*(mutation|genetic|gene|cna|structural|alteration).*")) {
            genomicTables.add(table);
          } else if (lower.matches(".*(cancer_study|patient|sample).*")
              && !lower.endsWith("_list")) {
            coreTables.add(table);
          } else {
            otherTables.add(table);
          }
        }

        if (!coreTables.isEmpty()) {
          result.append("Core:\n");
          coreTables.forEach(t -> result.append("  - ").append(t).append("\n"));
          result.append("\n");
        }

        if (!clinicalTables.isEmpty()) {
          result.append("Clinical Data:\n");
          clinicalTables.forEach(t -> result.append("  - ").append(t).append("\n"));
          result.append("\n");
        }

        if (!genomicTables.isEmpty()) {
          result.append("Genomic Data:\n");
          genomicTables.forEach(t -> result.append("  - ").append(t).append("\n"));
          result.append("\n");
        }

        if (!otherTables.isEmpty()) {
          result.append("Other:\n");
          otherTables.forEach(t -> result.append("  - ").append(t).append("\n"));
          result.append("\n");
        }
      }

      // Display derived tables with descriptions
      if (!derivedTables.isEmpty()) {
        result.append("** Derived Tables ** (optimized for queries)\n\n");
        for (String table : derivedTables) {
          result.append("  - ").append(table);
          // Add description based on table name
          String description = getDerivedTableDescription(table);
          if (description != null) {
            result.append(": ").append(description);
          }
          result.append("\n");
        }
      }

      return result.toString();

    } catch (Exception e) {
      logger.error("Error listing tables", e);
      return "Error listing tables: " + e.getMessage();
    }
  }

  /** Extract CREATE TABLE statement for a specific table from the SQL file. */
  private String extractTableSchema(String schemaPath, String tableName) throws IOException {
    ClassPathResource resource = new ClassPathResource(schemaPath);

    try (InputStream inputStream = resource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

      StringBuilder currentTable = new StringBuilder();
      boolean inTargetTable = false;
      int parenthesesDepth = 0;

      String line;
      while ((line = reader.readLine()) != null) {
        String trimmed = line.trim();

        // Check if this is the start of our target table (case-insensitive)
        if (trimmed
            .toUpperCase()
            .matches("CREATE\\s+TABLE\\s+`?" + tableName.toUpperCase() + "`?.*")) {
          inTargetTable = true;
          currentTable.append(line).append("\n");
          parenthesesDepth = countChar(line, '(') - countChar(line, ')');
          continue;
        }

        if (inTargetTable) {
          currentTable.append(line).append("\n");
          parenthesesDepth += countChar(line, '(') - countChar(line, ')');

          // Table definition ends when we close all parentheses and find semicolon
          if (parenthesesDepth <= 0 && trimmed.endsWith(";")) {
            return currentTable.toString();
          }
        }
      }
    }

    return null; // Table not found
  }

  /** Extract all table names from the SQL file. */
  private List<String> extractAllTableNames(String schemaPath) throws IOException {
    ClassPathResource resource = new ClassPathResource(schemaPath);
    List<String> tables = new ArrayList<>();

    try (InputStream inputStream = resource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

      String line;
      while ((line = reader.readLine()) != null) {
        String trimmed = line.trim();

        // Match CREATE TABLE statements
        if (trimmed.toUpperCase().startsWith("CREATE TABLE")) {
          // Extract table name between backticks or after TABLE keyword
          String tableName = extractTableName(trimmed);
          if (tableName != null) {
            tables.add(tableName);
          }
        }
      }
    }

    return tables;
  }

  /** Extract table name from CREATE TABLE statement. */
  private String extractTableName(String createTableLine) {
    // Match pattern: CREATE TABLE `table_name` or CREATE TABLE table_name
    String pattern = "CREATE\\s+TABLE\\s+`?([a-zA-Z0-9_]+)`?.*";
    if (createTableLine.toUpperCase().matches(pattern)) {
      return createTableLine.replaceAll("(?i)CREATE\\s+TABLE\\s+`?([a-zA-Z0-9_]+)`?.*", "$1");
    }
    return null;
  }

  /** Count occurrences of a character in a string. */
  private int countChar(String str, char c) {
    return (int) str.chars().filter(ch -> ch == c).count();
  }

  /** Get description for derived tables. */
  private String getDerivedTableDescription(String tableName) {
    return switch (tableName.toLowerCase()) {
      case "sample_derived" -> "Integrated sample, patient, and study information";
      case "genomic_event_derived" -> "Consolidated genomic events (mutations, CNAs, etc.)";
      case "clinical_data_derived" -> "Optimized clinical data";
      case "clinical_event_derived" -> "Clinical events timeline data";
      case "genetic_alteration_derived" -> "Genetic alterations optimized for queries";
      case "generic_assay_data_derived" -> "Generic assay data";
      case "sample_to_gene_panel_derived" -> "Sample to gene panel mapping";
      case "gene_panel_to_gene_derived" -> "Gene panel composition";
      default -> null;
    };
  }
}
