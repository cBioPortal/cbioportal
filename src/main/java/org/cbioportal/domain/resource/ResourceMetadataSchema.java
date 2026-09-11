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
 * <p>The contract decorates columns; it does not create them. Column existence always comes from
 * the keys actually discovered in the data, so a contract that declares a key nobody imported adds
 * nothing, and data with no contract behaves exactly as it did before contracts existed.
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
