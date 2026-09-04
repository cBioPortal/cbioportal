package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.cbioportal.domain.wsi.WsiSlideAccess;
import org.cbioportal.domain.wsi.WsiThumbnail;
import org.cbioportal.domain.wsi.WsiTileMetadata;
import org.cbioportal.domain.wsi.repository.WsiSlideAccessRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseWsiSlideAccessRepository implements WsiSlideAccessRepository {

  private static final int TILE_METADATA_SCHEMA_VERSION = 2;
  private static final int MAX_DECODE_PIXELS = 16_777_216;
  private static final String DECODE_POLICY_VERSION =
      "geometry-v2;tile-max=16777216;thumbnail-max=16777216";
  private static final Set<String> SOURCE_EXTENSIONS =
      Set.of("svs", "tif", "tiff", "ndpi", "mrxs", "scn");
  private static final Set<String> THUMBNAIL_EXTENSIONS = Set.of("jpg", "jpeg", "png");
  private static final Pattern ABSOLUTE_DATE = Pattern.compile(
      "(?<!\\d)(?:19|20)\\d{2}[-_/](?:0?[1-9]|1[0-2])[-_/](?:0?[1-9]|[12]\\d|3[01])(?!\\d)");
  private static final Pattern MONTH_FIRST_DATE = Pattern.compile(
      "(?<!\\d)(?:0?[1-9]|1[0-2])[-_/](?:0?[1-9]|[12]\\d|3[01])[-_/](?:19|20)\\d{2}(?!\\d)");
  private static final Pattern DAY_FIRST_DATE = Pattern.compile(
      "(?<!\\d)(?:0?[1-9]|[12]\\d|3[01])[-_/](?:0?[1-9]|1[0-2])[-_/](?:19|20)\\d{2}(?!\\d)");
  private static final Pattern NAMED_MONTH_DATE = Pattern.compile(
      "(?i)(?<![a-z0-9])(?:(?:jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|"
          + "may|jun(?:e)?|jul(?:y)?|aug(?:ust)?|sep(?:t(?:ember)?)?|oct(?:ober)?|"
          + "nov(?:ember)?|dec(?:ember)?)\\s+(?:0?[1-9]|[12]\\d|3[01])(?:st|nd|rd|th)?"
          + "(?:,)?\\s+(?:19|20)\\d{2}|(?:0?[1-9]|[12]\\d|3[01])[-/\\s]+"
          + "(?:jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|jun(?:e)?|"
          + "jul(?:y)?|aug(?:ust)?|sep(?:t(?:ember)?)?|oct(?:ober)?|nov(?:ember)?|"
          + "dec(?:ember)?)[-/\\s]+(?:19|20)\\d{2})(?![a-z0-9])");
  private static final Pattern COMPACT_DATE = Pattern.compile(
      "(?<!\\d)(?:19|20)\\d{6}(?!\\d)");
  private static final Pattern LABELLED_MRN = Pattern.compile(
      "(?i)\\b(?:mrn|medical[ _-]?record(?:[ _-]?number)?)\\b\\s*[:=#-]?\\s*\\d{4,}");
  private static final Set<String> ALLOWED_METADATA_KEYS = Set.of(
      "dimensions", "levels", "level_dimensions", "level_downsamples", "max_zoom",
      "tile_size", "mpp", "objective_power", "vendor", "identity_version", "safe_min_level",
      "tile_metadata_schema_version", "decode_policy_version", "max_decode_pixels",
      "thumbnail_max_decode_pixels", "source_fingerprint");

  private final ClickhouseWsiSlideAccessMapper mapper;
  private final ClickhouseWsiContextMapper contextMapper;
  private final ObjectMapper objectMapper;

  public ClickhouseWsiSlideAccessRepository(
      ClickhouseWsiSlideAccessMapper mapper,
      ClickhouseWsiContextMapper contextMapper,
      ObjectMapper objectMapper) {
    this.mapper = mapper;
    this.contextMapper = contextMapper;
    this.objectMapper = objectMapper;
  }

  @Override
  public WsiSlideAccess getSlideAccess(String studyId, String imageId) {
    // A portal response contains the exact object URLs. Refuse to issue a
    // capability unless both production allowlists are configured; structural
    // checks in isServableRow() remain independently unit-testable.
    if (!artifactPolicyConfigured()) {
      return null;
    }
    Map<String, Object> context = contextMapper.getStudyContext(studyId);
    if (context == null) {
      return null;
    }
    Map<String, Object> row =
        mapper.getSlideAccess(
            longValue(context.get("cancer_study_id")),
            imageId);
    if (!isServableRow(row, objectMapper)) {
      return null;
    }
    String sourceUrl = stringValue(row.get("source_url"));
    String metadataJson = stringValue(row.get("tile_metadata_json"));
    String thumbnailUrl = stringValue(row.get("thumbnail_url"));
    if (!safeArtifactUrl(sourceUrl, SOURCE_EXTENSIONS, "WSI_ALLOWED_SOURCE_PREFIXES")
        || !safeArtifactUrl(
            thumbnailUrl, THUMBNAIL_EXTENSIONS, "WSI_ALLOWED_THUMBNAIL_PREFIXES")) {
      return null;
    }
    try {
      WsiTileMetadata metadata = objectMapper.readValue(metadataJson, WsiTileMetadata.class);
      int width = numberValue(row.get("thumbnail_width"));
      int height = numberValue(row.get("thumbnail_height"));
      String contentType = stringValue(row.get("thumbnail_content_type"));
      return new WsiSlideAccess(
          imageId,
          sourceUrl,
          metadata,
          new WsiThumbnail(thumbnailUrl, width, height, contentType),
          null,
          null,
          0);
    } catch (JsonProcessingException | RuntimeException exception) {
      return null;
    }
  }

  static boolean isServableRow(Map<String, Object> row, ObjectMapper objectMapper) {
    if (row == null || !boolValue(row.get("can_serve_tiles"))) {
      return false;
    }
    String sourceUrl = stringValue(row.get("source_url"));
    String imageId = stringValue(row.get("image_id"));
    String metadataJson = stringValue(row.get("tile_metadata_json"));
    String thumbnailUrl = stringValue(row.get("thumbnail_url"));
    String contentType = stringValue(row.get("thumbnail_content_type"));
    int width = numberValue(row.get("thumbnail_width"));
    int height = numberValue(row.get("thumbnail_height"));
    if (sourceUrl == null || imageId == null || metadataJson == null || thumbnailUrl == null
        || contentType == null || width <= 0 || height <= 0
        || width > 8192 || height > 8192) {
      return false;
    }
    if (!safeArtifactUrl(sourceUrl, SOURCE_EXTENSIONS, null)
        || !safeArtifactUrl(thumbnailUrl, THUMBNAIL_EXTENSIONS, null)) {
      return false;
    }
    if (!thumbnailContentTypeMatches(thumbnailUrl, contentType)) {
      return false;
    }
    try {
      JsonNode metadataNode = objectMapper.readTree(metadataJson);
      if (!metadataNode.isObject()) {
        return false;
      }
      // Metadata is serialized into the portal response. Apply the same
      // fail-closed identifier/date checks as the importer and hierarchy
      // endpoint to string values while ignoring numeric geometry.
      if (containsForbiddenMetadataText(metadataNode)) {
        return false;
      }
      var fieldNames = metadataNode.fieldNames();
      while (fieldNames.hasNext()) {
        if (!ALLOWED_METADATA_KEYS.contains(fieldNames.next())) {
          return false;
        }
      }
      return validMetadata(objectMapper.treeToValue(metadataNode, WsiTileMetadata.class));
    } catch (JsonProcessingException | RuntimeException exception) {
      return false;
    }
  }

  private static boolean safeArtifactUrl(String value, Set<String> extensions, String prefixEnv) {
    try {
      URI uri = URI.create(value);
      String scheme = uri.getScheme();
      if (scheme == null || !("s3".equalsIgnoreCase(scheme) || "file".equalsIgnoreCase(scheme))
          || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null) {
        return false;
      }
      if ("s3".equalsIgnoreCase(scheme) && (uri.getHost() == null || uri.getHost().isBlank())) {
        return false;
      }
      if ("file".equalsIgnoreCase(scheme)
          && uri.getHost() != null
          && !uri.getHost().isBlank()
          && !"localhost".equalsIgnoreCase(uri.getHost())) {
        return false;
      }
      String path = uri.getPath();
      if (path == null || path.endsWith("/")
          || java.util.Arrays.stream(path.split("/", -1))
              .anyMatch(segment -> ".".equals(segment) || "..".equals(segment))) {
        return false;
      }
      if (prefixEnv != null && !approvedPrefix(value, prefixEnv)) {
        return false;
      }
      if (containsAbsoluteDate(value)
          || containsAbsoluteDate(path)
          || COMPACT_DATE.matcher(value).find()
          || COMPACT_DATE.matcher(path).find()
          || LABELLED_MRN.matcher(value).find()
          || LABELLED_MRN.matcher(path).find()) {
        return false;
      }
      String filename = path.substring(path.lastIndexOf('/') + 1);
      int dot = filename.lastIndexOf('.');
      if (dot <= 0) {
        return false;
      }
      String extension = filename.substring(dot + 1).toLowerCase(java.util.Locale.ROOT);
      return extensions.contains(extension);
    } catch (IllegalArgumentException exception) {
      return false;
    }
  }

  private static boolean approvedPrefix(String value, String environmentVariable) {
    String configured = System.getenv(environmentVariable);
    if (configured == null || configured.isBlank()) {
      return false;
    }
    for (String rawPrefix : configured.split(",")) {
      String prefix = rawPrefix.trim().replaceAll("/+$", "");
      if (!prefix.isBlank() && value.startsWith(prefix + "/")) {
        return true;
      }
    }
    return false;
  }

  private static boolean containsAbsoluteDate(String value) {
    return ABSOLUTE_DATE.matcher(value).find()
        || MONTH_FIRST_DATE.matcher(value).find()
        || DAY_FIRST_DATE.matcher(value).find()
        || NAMED_MONTH_DATE.matcher(value).find();
  }

  private static boolean artifactPolicyConfigured() {
    return !System.getenv().getOrDefault("WSI_ALLOWED_SOURCE_PREFIXES", "").isBlank()
        && !System.getenv().getOrDefault("WSI_ALLOWED_THUMBNAIL_PREFIXES", "").isBlank();
  }

  private static boolean validMetadata(WsiTileMetadata metadata) {
    boolean isCurrentSchema =
        metadata != null
            && metadata.tileMetadataSchemaVersion() != null
            && metadata.tileMetadataSchemaVersion() == TILE_METADATA_SCHEMA_VERSION;
    if (metadata == null
        || metadata.dimensions() == null
        || metadata.dimensions().width() <= 0
        || metadata.dimensions().height() <= 0
        || metadata.levels() <= 0
        || metadata.levelDimensions() == null
        || metadata.levelDimensions().size() != metadata.levels()
        || metadata.maxZoom() < 0
        || (metadata.safeMinLevel() != null
            && (metadata.safeMinLevel() < 0 || metadata.safeMinLevel() > metadata.maxZoom()))
        || (metadata.tileMetadataSchemaVersion() != null
            && metadata.tileMetadataSchemaVersion() != TILE_METADATA_SCHEMA_VERSION)
        || (isCurrentSchema
            && (metadata.safeMinLevel() == null
                || metadata.levelDownsamples() == null
                || metadata.levelDownsamples().size() != metadata.levels()
                || metadata.levelDownsamples().stream()
                    .anyMatch(value -> value == null || !Double.isFinite(value) || value <= 0)))
        || (isCurrentSchema
            && (metadata.maxDecodePixels() == null
                || metadata.maxDecodePixels() != MAX_DECODE_PIXELS
                || metadata.thumbnailMaxDecodePixels() == null
                || metadata.thumbnailMaxDecodePixels() != MAX_DECODE_PIXELS
                || !DECODE_POLICY_VERSION.equals(metadata.decodePolicyVersion())))
        || metadata.tileSize() <= 0) {
      return false;
    }
    return metadata.levelDimensions().stream()
        .allMatch(level -> level != null && level.width() > 0 && level.height() > 0);
  }

  private static boolean containsForbiddenMetadataText(JsonNode node) {
    if (node == null) {
      return false;
    }
    if (node.isTextual()) {
      String value = node.asText();
      return LABELLED_MRN.matcher(value).find()
          || containsAbsoluteDate(value)
          || COMPACT_DATE.matcher(value).find();
    }
    if (node.isObject() || node.isArray()) {
      var children = node.elements();
      while (children.hasNext()) {
        if (containsForbiddenMetadataText(children.next())) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean thumbnailContentTypeMatches(String value, String contentType) {
    try {
      URI uri = URI.create(value);
      String path = uri.getPath();
      if (path == null) {
        return false;
      }
      int dot = path.lastIndexOf('.');
      if (dot <= path.lastIndexOf('/')) {
        return false;
      }
      String extension = path.substring(dot + 1).toLowerCase(java.util.Locale.ROOT);
      String expected = switch (extension) {
        case "jpg", "jpeg" -> "image/jpeg";
        case "png" -> "image/png";
        default -> null;
      };
      return expected != null && expected.equalsIgnoreCase(contentType.trim());
    } catch (IllegalArgumentException exception) {
      return false;
    }
  }

  private static String stringValue(Object value) {
    return value == null || value.toString().isBlank() ? null : value.toString();
  }

  private static int numberValue(Object value) {
    return value instanceof Number ? ((Number) value).intValue() : 0;
  }

  private static long longValue(Object value) {
    return value == null ? 0L : ((Number) value).longValue();
  }

  private static boolean boolValue(Object value) {
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    if (value == null) {
      return false;
    }
    try {
      return Integer.parseInt(value.toString()) != 0;
    } catch (NumberFormatException exception) {
      return false;
    }
  }
}
