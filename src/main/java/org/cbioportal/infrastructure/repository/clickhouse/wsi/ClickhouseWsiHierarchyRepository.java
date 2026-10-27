package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import java.util.List;
import java.util.Map;
import org.cbioportal.domain.wsi.WsiBlock;
import org.cbioportal.domain.wsi.WsiContract;
import org.cbioportal.domain.wsi.WsiHierarchy;
import org.cbioportal.domain.wsi.WsiPart;
import org.cbioportal.domain.wsi.WsiSampleGroup;
import org.cbioportal.domain.wsi.WsiSlide;
import org.cbioportal.domain.wsi.repository.WsiHierarchyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseWsiHierarchyRepository implements WsiHierarchyRepository {

  private static final Logger LOG = LoggerFactory.getLogger(ClickhouseWsiHierarchyRepository.class);

  private final ClickhouseWsiHierarchyMapper mapper;
  private final ClickhouseWsiContextMapper contextMapper;

  public ClickhouseWsiHierarchyRepository(
      ClickhouseWsiHierarchyMapper mapper, ClickhouseWsiContextMapper contextMapper) {
    this.mapper = mapper;
    this.contextMapper = contextMapper;
  }

  @Override
  public WsiHierarchy getPatientHierarchy(String studyId, String patientId) {
    Map<String, Object> context = contextMapper.getPatientContext(studyId, patientId);
    if (context == null) {
      return null;
    }
    List<Map<String, Object>> rows =
        mapper.getPatientHierarchy(contextLongValue(context, "cancer_study_id"), patientId);
    if (rows.isEmpty()) {
      // The patient exists but has no WSI resource rows: an empty hierarchy, not a missing one.
      return new WsiHierarchy(null, List.of());
    }

    Map<String, WsiSampleGroupBuilder> samples = new java.util.LinkedHashMap<>();
    int unkeyedSlides = 0;
    for (Map<String, Object> row : rows) {
      String slideKey = stringValue(row, "slide_key");
      if (!WsiContract.isSlideKey(slideKey)) {
        // A slide without an opaque key cannot be addressed.
        unkeyedSlides++;
        continue;
      }
      String sampleKey = stringValue(row, "sample_id");
      String sampleMapKey = sampleKey == null ? "" : sampleKey;
      WsiSampleGroupBuilder sample =
          samples.computeIfAbsent(sampleMapKey, ignored -> new WsiSampleGroupBuilder(sampleKey));
      String partKey = stringValue(row, "part_key");
      WsiPartBuilder part =
          sample.parts.computeIfAbsent(
              partKey,
              ignored ->
                  new WsiPartBuilder(
                      stringValue(row, "part_number"),
                      stringValue(row, "part_type"),
                      stringValue(row, "part_description"),
                      stringValue(row, "subspecialty")));
      String blockKey = stringValue(row, "block_key");
      WsiBlockBuilder block =
          part.blocks.computeIfAbsent(
              blockKey,
              ignored ->
                  new WsiBlockBuilder(
                      stringValue(row, "block_number"), stringValue(row, "block_label")));
      block.slides.add(
          new WsiSlide(
              slideKey,
              stringValue(row, "stain_name"),
              stringValue(row, "stain_group"),
              boolValue(row, "is_hne"),
              boolValue(row, "is_ihc"),
              stringValue(row, "magnification"),
              longValue(row, "file_size_bytes"),
              boolValue(row, "can_serve_tiles"),
              resolveSlideType(row),
              sampleKey,
              stringValue(row, "match_level"),
              stringValue(row, "specimen_key")));
    }

    if (unkeyedSlides > 0) {
      LOG.warn("Dropped {} WSI hierarchy slide(s) without a valid slide_key", unkeyedSlides);
    }

    List<WsiSampleGroup> sampleGroups =
        samples.values().stream().map(WsiSampleGroupBuilder::build).toList();
    return new WsiHierarchy(referenceSampleId(rows, patientId), sampleGroups);
  }

  /**
   * The patient's reference sample, carried on every WSI row. Unmatched rows sort first and may
   * omit it, so this takes the first non-empty value; rows that disagree are logged, since the
   * importer validates one reference sample per patient.
   */
  static String referenceSampleId(List<Map<String, Object>> rows, String patientId) {
    String reference = null;
    for (Map<String, Object> row : rows) {
      String candidate = stringValue(row, "reference_sample_id");
      if (candidate == null) {
        continue;
      }
      if (reference == null) {
        reference = candidate;
      } else if (!reference.equals(candidate)) {
        LOG.warn(
            "WSI rows for patient {} disagree on reference_sample_id ({} vs {}); using {}",
            patientId,
            reference,
            candidate,
            reference);
        break;
      }
    }
    return reference;
  }

  private static String stringValue(Map<String, Object> row, String key) {
    Object value = row.get(key);
    // ClickHouse's JSONExtractString yields '' for a missing key. Normalize it back to null so
    // absent fields do not become phantom samples, parts or blocks in the domain model.
    return value == null || value.toString().isEmpty() ? null : value.toString();
  }

  private static Long longValue(Map<String, Object> row, String key) {
    Object value = row.get(key);
    return value == null ? null : ((Number) value).longValue();
  }

  private static long contextLongValue(Map<String, Object> row, String key) {
    Object value = row.get(key);
    return value == null ? 0L : ((Number) value).longValue();
  }

  private static boolean boolValue(Map<String, Object> row, String key) {
    Object value = row.get(key);
    return value instanceof Boolean
        ? (Boolean) value
        : value != null && ((Number) value).intValue() != 0;
  }

  /**
   * Older WSI snapshots did not populate slide_type, although the resolved boolean stain flags were
   * present. Keep the API contract stable by deriving the type from those authoritative flags
   * before falling back to the stored value. This prevents nullable legacy rows from being
   * interpreted as H&E by clients.
   */
  static String resolveSlideType(Map<String, Object> row) {
    if (boolValue(row, "is_ihc")) {
      return "IHC";
    }
    if (boolValue(row, "is_hne")) {
      return "H&E";
    }
    String slideType = stringValue(row, "slide_type");
    if ("H&E".equals(slideType)
        || "IHC".equals(slideType)
        || "Other".equals(slideType)
        || "Unknown".equals(slideType)) {
      return slideType;
    }
    // A legacy NULL or uncontrolled value is ambiguous, not a confirmed Other.
    return "Unknown";
  }

  private static final class WsiSampleGroupBuilder {
    private final String sampleId;
    private final Map<String, WsiPartBuilder> parts = new java.util.LinkedHashMap<>();

    private WsiSampleGroupBuilder(String sampleId) {
      this.sampleId = sampleId;
    }

    private WsiSampleGroup build() {
      return new WsiSampleGroup(
          sampleId, parts.values().stream().map(WsiPartBuilder::build).toList());
    }
  }

  private static final class WsiPartBuilder {
    private final String partNumber;
    private final String partType;
    private final String partDescription;
    private final String subspecialty;
    private final Map<String, WsiBlockBuilder> blocks = new java.util.LinkedHashMap<>();

    private WsiPartBuilder(
        String partNumber, String partType, String partDescription, String subspecialty) {
      this.partNumber = partNumber;
      this.partType = partType;
      this.partDescription = partDescription;
      this.subspecialty = subspecialty;
    }

    private WsiPart build() {
      return new WsiPart(
          partNumber,
          partType,
          partDescription,
          subspecialty,
          blocks.values().stream().map(WsiBlockBuilder::build).toList());
    }
  }

  private static final class WsiBlockBuilder {
    private final String blockNumber;
    private final String blockLabel;
    private final List<WsiSlide> slides = new java.util.ArrayList<>();

    private WsiBlockBuilder(String blockNumber, String blockLabel) {
      this.blockNumber = blockNumber;
      this.blockLabel = blockLabel;
    }

    private WsiBlock build() {
      return new WsiBlock(blockNumber, blockLabel, slides);
    }
  }
}
