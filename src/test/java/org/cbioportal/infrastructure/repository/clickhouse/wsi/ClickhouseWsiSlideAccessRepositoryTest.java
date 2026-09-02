package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class ClickhouseWsiSlideAccessRepositoryTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void acceptsCompleteSourceBoundPixelRow() {
    assertTrue(
        ClickhouseWsiSlideAccessRepository.isServableRow(row(validMetadata()), objectMapper));
  }

  @Test
  public void rejectsIncompleteTileMetadata() {
    assertFalse(ClickhouseWsiSlideAccessRepository.isServableRow(row("{}"), objectMapper));
  }

  @Test
  public void rejectsOversizedThumbnail() {
    Map<String, Object> row = row(validMetadata());
    row.put("thumbnail_width", 8193);
    assertFalse(ClickhouseWsiSlideAccessRepository.isServableRow(row, objectMapper));
  }

  @Test
  public void acceptsSafeMinimumLevelWithinZoomRange() {
    assertTrue(
        ClickhouseWsiSlideAccessRepository.isServableRow(
            row(
                validMetadata().replace(
                    "\"max_zoom\":0,\"safe_min_level\":0",
                    "\"max_zoom\":4,\"safe_min_level\":2")),
            objectMapper));
  }

  @Test
  public void rejectsSafeMinimumLevelAboveZoomRange() {
    assertFalse(
        ClickhouseWsiSlideAccessRepository.isServableRow(
            row(
                validMetadata().replace(
                    "\"max_zoom\":0,\"safe_min_level\":0",
                    "\"max_zoom\":4,\"safe_min_level\":5")),
            objectMapper));
  }

  @Test
  public void acceptsCurrentTileMetadataSchema() {
    String metadata = validMetadata().replace(
        "\"tile_size\":256",
        "\"tile_size\":256,\"tile_metadata_schema_version\":2"
            + ",\"decode_policy_version\":\"geometry-v2;tile-max=16777216;thumbnail-max=16777216\""
            + ",\"max_decode_pixels\":16777216,\"thumbnail_max_decode_pixels\":16777216");
    assertTrue(
        ClickhouseWsiSlideAccessRepository.isServableRow(
            row(metadata),
            objectMapper));
  }

  @Test
  public void rejectsCurrentTileMetadataWithoutSafeMinimumLevel() {
    String metadata = validMetadata()
        .replace("\"safe_min_level\":0,", "")
        .replace(
            "\"tile_size\":256",
            "\"tile_size\":256,\"tile_metadata_schema_version\":2"
                + ",\"decode_policy_version\":\"geometry-v2;tile-max=16777216;thumbnail-max=16777216\""
                + ",\"max_decode_pixels\":16777216,\"thumbnail_max_decode_pixels\":16777216");
    assertFalse(
        ClickhouseWsiSlideAccessRepository.isServableRow(
            row(metadata), objectMapper));
  }

  @Test
  public void rejectsUnknownTileMetadataSchema() {
    assertFalse(
        ClickhouseWsiSlideAccessRepository.isServableRow(
            row(
                validMetadata().replace(
                    "\"tile_size\":256",
                    "\"tile_size\":256,\"tile_metadata_schema_version\":99")),
            objectMapper));
  }

  @Test
  public void acceptsCurrentDecodePolicy() {
    String metadata = validMetadata().replace(
        "\"tile_size\":256",
        "\"tile_size\":256,\"tile_metadata_schema_version\":2"
            + ",\"decode_policy_version\":\"geometry-v2;tile-max=16777216;thumbnail-max=16777216\""
            + ",\"max_decode_pixels\":16777216,\"thumbnail_max_decode_pixels\":16777216");
    assertTrue(
        ClickhouseWsiSlideAccessRepository.isServableRow(
            row(metadata), objectMapper));
  }

  @Test
  public void rejectsNonCurrentDecodePolicy() {
    String metadata = validMetadata().replace(
        "\"tile_size\":256",
        "\"tile_size\":256,\"tile_metadata_schema_version\":2"
            + ",\"decode_policy_version\":\"geometry-v2;tile-max=4194304;thumbnail-max=4194304\""
            + ",\"max_decode_pixels\":16777216,\"thumbnail_max_decode_pixels\":16777216");
    assertFalse(
        ClickhouseWsiSlideAccessRepository.isServableRow(
            row(metadata), objectMapper));
  }

  @Test
  public void rejectsUnsafeArtifactUri() {
    Map<String, Object> row = row(validMetadata());
    row.put("source_url", "s3://bucket/slide.svs?mrn=123456");
    assertFalse(ClickhouseWsiSlideAccessRepository.isServableRow(row, objectMapper));
  }

  @Test
  public void rejectsIdentifierInArtifactPath() {
    Map<String, Object> row = row(validMetadata());
    row.put("source_url", "s3://bucket/MRN-123456.svs");
    assertFalse(ClickhouseWsiSlideAccessRepository.isServableRow(row, objectMapper));
  }

  @Test
  public void rejectsIdentifierInTileMetadata() {
    Map<String, Object> row = row(validMetadata().replace(
        "\"tile_size\":256", "\"tile_size\":256,\"vendor\":\"MRN-123456\""));
    assertFalse(ClickhouseWsiSlideAccessRepository.isServableRow(row, objectMapper));
  }

  @Test
  public void rejectsDateInTileMetadata() {
    Map<String, Object> row = row(validMetadata().replace(
        "\"tile_size\":256", "\"tile_size\":256,\"vendor\":\"2024-01-31\""));
    assertFalse(ClickhouseWsiSlideAccessRepository.isServableRow(row, objectMapper));
  }

  @Test
  public void rejectsThumbnailMimeTypeThatDoesNotMatchExtension() {
    Map<String, Object> row = row(validMetadata());
    row.put("thumbnail_content_type", "image/png");
    assertFalse(ClickhouseWsiSlideAccessRepository.isServableRow(row, objectMapper));
  }

  @Test
  public void acceptsLargeNumericFieldsWithoutDateFalsePositive() {
    Map<String, Object> row = row(validMetadata().replace(
        "\"width\":256", "\"width\":20123456"));
    row.put("thumbnail_content_type", "image/jpeg");
    assertTrue(ClickhouseWsiSlideAccessRepository.isServableRow(row, objectMapper));
  }

  private static Map<String, Object> row(String metadata) {
    Map<String, Object> row = new HashMap<>();
    row.put("can_serve_tiles", true);
    row.put("image_id", "slide");
    row.put("source_url", "s3://bucket/slide.svs");
    row.put("tile_metadata_json", metadata);
    row.put("thumbnail_url", "s3://bucket/slide.jpg");
    row.put("thumbnail_width", 128);
    row.put("thumbnail_height", 96);
    row.put("thumbnail_content_type", "image/jpeg");
    return row;
  }

  private static String validMetadata() {
    return "{\"dimensions\":{\"width\":256,\"height\":256},"
        + "\"levels\":1,\"level_dimensions\":[{\"width\":256,\"height\":256}],"
        + "\"level_downsamples\":[1.0],\"max_zoom\":0,"
        + "\"safe_min_level\":0,\"tile_size\":256}";
  }
}
