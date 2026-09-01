package org.cbioportal.domain.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses the optional JSON-schema-like contract that curators may put in {@code
 * resource_definition.custom_metadata} to explicitly declare a metadata key's type, e.g.:
 *
 * <pre>{@code
 * {
 *   "version": 1,
 *   "fields": [
 *     { "key": "magnification", "type": "number", ... },
 *     { "key": "dose_id", "type": "string", ... }
 *   ]
 * }
 * }</pre>
 *
 * This is purely an optional override on top of auto-detection (see {@link
 * ResourceMetadataKeyStats}) — a curator can force a numeric-looking key to stay categorical (or
 * vice versa). Any parse error or missing schema is treated as "no override" so auto-detection
 * behavior is unaffected.
 */
public final class ResourceMetadataSchema {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private ResourceMetadataSchema() {}

  /** Returns a map of metadata key -> declared "type" (e.g. "number", "string"), or empty. */
  public static Map<String, String> parseDeclaredTypes(String customMetadataJson) {
    Map<String, String> declaredTypes = new HashMap<>();
    if (customMetadataJson == null || customMetadataJson.isBlank()) {
      return declaredTypes;
    }
    try {
      JsonNode root = OBJECT_MAPPER.readTree(customMetadataJson);
      JsonNode fields = root.get("fields");
      if (fields != null && fields.isArray()) {
        for (JsonNode field : fields) {
          JsonNode keyNode = field.get("key");
          JsonNode typeNode = field.get("type");
          if (keyNode != null && typeNode != null) {
            declaredTypes.put(keyNode.asText(), typeNode.asText());
          }
        }
      }
    } catch (Exception e) {
      // Malformed custom_metadata should never break the resource table; just fall back to
      // auto-detection for every key.
      return new HashMap<>();
    }
    return declaredTypes;
  }
}
