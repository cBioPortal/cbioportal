package org.cbioportal.domain.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Expands a resource definition's {@code url_template} into the URL of one resource row.
 *
 * <p>A resource can leave its rows' {@code URL} empty and declare one template instead, e.g. {@code
 * ${base_url}/wsi/patient/${PATIENT_ID}?studyId=${STUDY_ID}&slideKey=${metadata.slide_key}}. The
 * data then names no portal host, and the pattern is stored once rather than on every row.
 *
 * <p>Placeholders:
 *
 * <ul>
 *   <li>{@code ${base_url}}: the portal's own base URL (scheme, host and context path, no trailing
 *       slash), inserted as is.
 *   <li>{@code ${STUDY_ID}}, {@code ${PATIENT_ID}}, {@code ${SAMPLE_ID}}, {@code ${RESOURCE_ID}}:
 *       the row's identifiers.
 *   <li>{@code ${metadata.<key>}}: the row's metadata value for {@code <key>}; a string as is, any
 *       other JSON value as its JSON text.
 * </ul>
 *
 * <p>Every value except {@code ${base_url}} is percent-encoded as a URI component: everything but
 * the RFC 3986 unreserved characters ({@code A-Z a-z 0-9 - . _ ~}) is encoded, as UTF-8. A missing
 * value, such as {@code ${SAMPLE_ID}} on a patient-level row or an absent metadata key, expands to
 * an empty string. An unknown placeholder is left in the output unchanged and logged once; the
 * importer is what validates templates.
 */
public final class ResourceUrlTemplate {

  private static final Logger LOG = LoggerFactory.getLogger(ResourceUrlTemplate.class);

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final TypeReference<Map<String, Object>> METADATA_TYPE = new TypeReference<>() {};

  private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]*)}");
  private static final String METADATA_PREFIX = "metadata.";
  private static final char[] HEX = "0123456789ABCDEF".toCharArray();

  /** Bounds the record of placeholders already reported, so it cannot grow without limit. */
  private static final int MAX_REPORTED_PLACEHOLDERS = 1000;

  private static final Set<String> REPORTED_PLACEHOLDERS = ConcurrentHashMap.newKeySet();

  private ResourceUrlTemplate() {}

  /** The values a template can refer to, for one resource row. Any of them may be null. */
  public record Values(
      String studyId,
      String resourceId,
      String patientId,
      String sampleId,
      Map<String, Object> metadata) {}

  /**
   * The URL a row is served with: its own URL when it has one, otherwise its definition's template
   * expanded for it, otherwise an empty string.
   */
  public static String resolve(String rowUrl, String template, String baseUrl, Values values) {
    if (rowUrl != null && !rowUrl.isBlank()) {
      return rowUrl;
    }
    if (template == null || template.isBlank()) {
      return rowUrl == null ? "" : rowUrl;
    }
    return expand(template, baseUrl, values);
  }

  /** Expands {@code template} for one row. See the class comment for the placeholders. */
  public static String expand(String template, String baseUrl, Values values) {
    if (template == null) {
      return "";
    }
    Matcher matcher = PLACEHOLDER.matcher(template);
    StringBuilder out = new StringBuilder(template.length() + 64);
    while (matcher.find()) {
      String name = matcher.group(1);
      String replacement = replacement(name, baseUrl, values);
      if (replacement == null) {
        reportUnknown(matcher.group());
        replacement = matcher.group();
      }
      matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(out);
    return out.toString();
  }

  /**
   * Parses a row's metadata column. A blank or unreadable value gives an empty map, so the row's
   * metadata placeholders expand to empty strings rather than failing the request.
   */
  public static Map<String, Object> parseMetadata(String json) {
    if (json == null || json.isBlank()) {
      return Map.of();
    }
    try {
      Map<String, Object> metadata = OBJECT_MAPPER.readValue(json, METADATA_TYPE);
      return metadata == null ? Map.of() : metadata;
    } catch (JsonProcessingException e) {
      return Map.of();
    }
  }

  /** The replacement text for a placeholder, or null when the placeholder is not known. */
  private static String replacement(String name, String baseUrl, Values values) {
    switch (name) {
      case "base_url":
        return stripTrailingSlash(baseUrl);
      case "STUDY_ID":
        return encode(values == null ? null : values.studyId());
      case "PATIENT_ID":
        return encode(values == null ? null : values.patientId());
      case "SAMPLE_ID":
        return encode(values == null ? null : values.sampleId());
      case "RESOURCE_ID":
        return encode(values == null ? null : values.resourceId());
      default:
        if (name.startsWith(METADATA_PREFIX) && name.length() > METADATA_PREFIX.length()) {
          String key = name.substring(METADATA_PREFIX.length());
          Map<String, Object> metadata = values == null ? null : values.metadata();
          return encode(metadataText(metadata == null ? null : metadata.get(key)));
        }
        return null;
    }
  }

  private static String metadataText(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof String text) {
      return text;
    }
    try {
      return OBJECT_MAPPER.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      return String.valueOf(value);
    }
  }

  private static String stripTrailingSlash(String baseUrl) {
    if (baseUrl == null) {
      return "";
    }
    int end = baseUrl.length();
    while (end > 0 && baseUrl.charAt(end - 1) == '/') {
      end--;
    }
    return baseUrl.substring(0, end);
  }

  /** Percent-encodes everything but the RFC 3986 unreserved characters, as UTF-8. */
  static String encode(String value) {
    if (value == null || value.isEmpty()) {
      return "";
    }
    StringBuilder out = new StringBuilder(value.length());
    for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
      int c = b & 0xFF;
      if ((c >= 'A' && c <= 'Z')
          || (c >= 'a' && c <= 'z')
          || (c >= '0' && c <= '9')
          || c == '-'
          || c == '.'
          || c == '_'
          || c == '~') {
        out.append((char) c);
      } else {
        out.append('%').append(HEX[c >> 4]).append(HEX[c & 0xF]);
      }
    }
    return out.toString();
  }

  private static void reportUnknown(String placeholder) {
    if (REPORTED_PLACEHOLDERS.size() < MAX_REPORTED_PLACEHOLDERS
        && REPORTED_PLACEHOLDERS.add(placeholder)) {
      LOG.warn(
          "Resource URL template placeholder {} is not recognised; it is left in the URL as is.",
          placeholder);
    }
  }
}
