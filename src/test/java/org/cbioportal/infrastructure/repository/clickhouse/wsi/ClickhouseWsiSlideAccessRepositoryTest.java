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

  private static Map<String, Object> row(String metadata) {
    Map<String, Object> row = new HashMap<>();
    row.put("can_serve_tiles", true);
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
        + "\"max_zoom\":0,\"tile_size\":256}";
  }
}
