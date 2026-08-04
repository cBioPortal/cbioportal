package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import java.util.List;
import java.util.Map;
import org.cbioportal.domain.wsi.WsiBlock;
import org.cbioportal.domain.wsi.WsiHierarchy;
import org.cbioportal.domain.wsi.WsiPart;
import org.cbioportal.domain.wsi.WsiSampleGroup;
import org.cbioportal.domain.wsi.WsiSlide;
import org.cbioportal.domain.wsi.repository.WsiHierarchyRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseWsiHierarchyRepository implements WsiHierarchyRepository {

  private final ClickhouseWsiHierarchyMapper mapper;

  public ClickhouseWsiHierarchyRepository(ClickhouseWsiHierarchyMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public WsiHierarchy getPatientHierarchy(String studyId, String patientId) {
    List<Map<String, Object>> rows = mapper.getPatientHierarchy(studyId, patientId);
    if (rows.isEmpty()) {
      return null;
    }

    Map<String, Object> first = rows.get(0);
    Map<String, WsiSampleGroupBuilder> samples = new java.util.LinkedHashMap<>();
    for (Map<String, Object> row : rows) {
      if (value(row, "image_id", String.class) == null) {
        continue;
      }
      String sampleKey = value(row, "sample_id", String.class);
      String sampleMapKey = sampleKey == null ? "" : sampleKey;
      WsiSampleGroupBuilder sample =
          samples.computeIfAbsent(sampleMapKey, ignored -> new WsiSampleGroupBuilder(sampleKey));
      String partKey = value(row, "part_key", String.class);
      WsiPartBuilder part =
          sample.parts.computeIfAbsent(
              partKey,
              ignored ->
                  new WsiPartBuilder(
                      value(row, "part_number", String.class),
                      value(row, "part_designator", String.class),
                      value(row, "part_type", String.class),
                      value(row, "part_description", String.class),
                      value(row, "subspecialty", String.class),
                      value(row, "path_dx_title", String.class)));
      String blockKey = value(row, "block_key", String.class);
      WsiBlockBuilder block =
          part.blocks.computeIfAbsent(
              blockKey,
              ignored ->
                  new WsiBlockBuilder(
                      value(row, "block_number", String.class),
                      value(row, "block_label", String.class)));
      block.slides.add(
          new WsiSlide(
              value(row, "image_id", String.class),
              value(row, "stain_name", String.class),
              value(row, "stain_group", String.class),
              boolValue(row, "is_hne"),
              boolValue(row, "is_ihc"),
              value(row, "magnification", String.class),
              longValue(row, "file_size_bytes"),
              boolValue(row, "can_serve_tiles"),
              value(row, "barcode", String.class),
              value(row, "slide_type", String.class),
              sampleKey,
              value(row, "match_level", String.class),
              value(row, "specimen_key", String.class),
              intValue(row, "procedure_date_days"),
              value(row, "timepoint_source", String.class)));
    }

    List<WsiSampleGroup> sampleGroups =
        samples.values().stream().map(WsiSampleGroupBuilder::build).toList();
    return new WsiHierarchy(
        value(first, "reference_sample_id", String.class),
        value(first, "reference_sequencing_date", String.class),
        sampleGroups);
  }

  private static <T> T value(Map<String, Object> row, String key, Class<T> type) {
    Object value = row.get(key);
    if (value == null) {
      return null;
    }
    if (type == String.class) {
      return type.cast(value.toString());
    }
    return type.cast(value);
  }

  private static Integer intValue(Map<String, Object> row, String key) {
    Object value = row.get(key);
    return value == null ? null : ((Number) value).intValue();
  }

  private static Long longValue(Map<String, Object> row, String key) {
    Object value = row.get(key);
    return value == null ? null : ((Number) value).longValue();
  }

  private static boolean boolValue(Map<String, Object> row, String key) {
    Object value = row.get(key);
    return value instanceof Boolean ? (Boolean) value : value != null && ((Number) value).intValue() != 0;
  }

  private static final class WsiSampleGroupBuilder {
    private final String sampleId;
    private final Map<String, WsiPartBuilder> parts = new java.util.LinkedHashMap<>();

    private WsiSampleGroupBuilder(String sampleId) {
      this.sampleId = sampleId;
    }

    private WsiSampleGroup build() {
      return new WsiSampleGroup(sampleId, parts.values().stream().map(WsiPartBuilder::build).toList());
    }
  }

  private static final class WsiPartBuilder {
    private final WsiPart part;
    private final Map<String, WsiBlockBuilder> blocks = new java.util.LinkedHashMap<>();

    private WsiPartBuilder(
        String partNumber,
        String partDesignator,
        String partType,
        String partDescription,
        String subspecialty,
        String pathDxTitle) {
      this.part = new WsiPart(partNumber, partDesignator, partType, partDescription, subspecialty, pathDxTitle, null);
    }

    private WsiPart build() {
      return new WsiPart(
          part.partNumber(),
          part.partDesignator(),
          part.partType(),
          part.partDescription(),
          part.subspecialty(),
          part.pathDxTitle(),
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
