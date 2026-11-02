package org.cbioportal.domain.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The optional JSON contract a curator may put in {@code resource_definition.custom_metadata} to
 * describe the metadata keys a resource's rows carry, e.g.:
 *
 * <pre>{@code
 * {
 *   "version": 1,
 *   "fields": [
 *     { "key": "stain", "type": "string", "label": "Stain", "filterable": true },
 *     { "key": "magnification", "type": "number", "label": "Magnification" }
 *   ]
 * }
 * }</pre>
 *
 * <p>Where a resource declares a contract it is the column list: declared keys become columns, in
 * declaration order, and the importer rejects data carrying keys the contract omits. A resource
 * with no contract behaves exactly as it did before contracts existed, its columns coming from the
 * keys found in the data.
 *
 * <p>Parsing is deliberately lenient: unknown members are ignored and any malformed document is
 * treated as "no contract" so a bad curator edit can never break the resource table.
 */
public record ResourceMetadataSchema(Integer version, List<ResourceMetadataField> fields) {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final ResourceMetadataSchema EMPTY = new ResourceMetadataSchema(null, List.of());

  public static ResourceMetadataSchema empty() {
    return EMPTY;
  }

  public static ResourceMetadataSchema parse(String customMetadataJson) {
    if (customMetadataJson == null || customMetadataJson.isBlank()) {
      return EMPTY;
    }
    try {
      JsonNode root = OBJECT_MAPPER.readTree(customMetadataJson);
      JsonNode fieldsNode = root.get("fields");
      if (fieldsNode == null || !fieldsNode.isArray()) {
        return EMPTY;
      }
      List<ResourceMetadataField> fields = new ArrayList<>();
      for (JsonNode field : fieldsNode) {
        String key = text(field, "key");
        if (key == null || key.isBlank()) {
          // A field declaration with no key cannot be matched to anything; skip it rather than
          // discarding the whole contract.
          continue;
        }
        fields.add(
            new ResourceMetadataField(
                key,
                text(field, "type"),
                text(field, "label"),
                text(field, "description"),
                bool(field, "filterable"),
                bool(field, "visibleByDefault")));
      }
      JsonNode versionNode = root.get("version");
      Integer version = versionNode != null && versionNode.isInt() ? versionNode.asInt() : null;
      return new ResourceMetadataSchema(version, List.copyOf(fields));
    } catch (Exception e) {
      return EMPTY;
    }
  }

  /**
   * Combines the contracts in scope for one request into the single column list the table shows.
   *
   * <p>A cohort can span studies, each declaring the resource for itself, so several contracts can
   * describe one table. Taking any one of them would hide the keys the others declare, so the union
   * is used: each contract's fields in declaration order, earlier contracts first, each key taking
   * the first declaration that mentions it.
   *
   * <p>A key two contracts type differently is left untyped rather than resolved to either, which
   * hands the decision to the values themselves. Guessing wrong in the {@code number} direction
   * puts a range filter on text.
   */
  public static ResourceMetadataSchema merge(List<ResourceMetadataSchema> schemas) {
    if (schemas == null || schemas.isEmpty()) {
      return EMPTY;
    }
    if (schemas.size() == 1) {
      return schemas.get(0);
    }
    Map<String, ResourceMetadataField> merged = new LinkedHashMap<>();
    Integer version = null;
    for (ResourceMetadataSchema schema : schemas) {
      if (version == null) {
        version = schema.version();
      }
      for (ResourceMetadataField field : schema.fields()) {
        ResourceMetadataField existing = merged.get(field.key());
        merged.put(field.key(), existing == null ? field : reconcile(existing, field));
      }
    }
    return new ResourceMetadataSchema(version, List.copyOf(merged.values()));
  }

  /** Keeps the first declaration of a key, except for a type two contracts disagree on. */
  private static ResourceMetadataField reconcile(
      ResourceMetadataField first, ResourceMetadataField later) {
    boolean typesConflict =
        first.type() != null && later.type() != null && !first.type().equals(later.type());
    return typesConflict
        ? new ResourceMetadataField(
            first.key(),
            null,
            first.label(),
            first.description(),
            first.filterable(),
            first.visibleByDefault())
        : first;
  }

  /** Keys that any two of these contracts type differently, in the order they are declared. */
  public static List<String> conflictingTypeKeys(List<ResourceMetadataSchema> schemas) {
    Map<String, String> typeByKey = new LinkedHashMap<>();
    List<String> conflicts = new ArrayList<>();
    for (ResourceMetadataSchema schema : schemas) {
      for (ResourceMetadataField field : schema.fields()) {
        if (field.type() == null) {
          continue;
        }
        String seen = typeByKey.putIfAbsent(field.key(), field.type());
        if (seen != null && !seen.equals(field.type()) && !conflicts.contains(field.key())) {
          conflicts.add(field.key());
        }
      }
    }
    return conflicts;
  }

  /** Declared fields by key, in declaration order. */
  public Map<String, ResourceMetadataField> fieldsByKey() {
    Map<String, ResourceMetadataField> byKey = new LinkedHashMap<>();
    for (ResourceMetadataField field : fields) {
      byKey.putIfAbsent(field.key(), field);
    }
    return byKey;
  }

  private static String text(JsonNode node, String member) {
    JsonNode value = node.get(member);
    return value != null && value.isTextual() ? value.asText() : null;
  }

  private static Boolean bool(JsonNode node, String member) {
    JsonNode value = node.get(member);
    return value != null && value.isBoolean() ? value.asBoolean() : null;
  }
}
