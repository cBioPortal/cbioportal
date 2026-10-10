package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.cbioportal.domain.wsi.WsiSlideSource;
import org.junit.Test;

public class ClickhouseWsiSlideAccessRepositoryTest {

  private static final String SLIDE_KEY = "2351e12d49557627b24fe71e17ec5c64";
  // The contract test vector (nonce || ciphertext || tag, base64url without padding).
  private static final String SEALED_SOURCE =
      "AAECAwQFBgcICQoLNpOTYnY8HVb-KcPzu1F5HPykr7D0YY_UhVbbyjOFRlxC63fxCt09YO1aYC-phb85wDhN5PPPpC0X"
          + "46RSD0K0bRRgSptRb9wDiqMLtftFQ6VpBGfGILaddEV_s-Zsmpp28fG5Z3XTNWnyoBRa9qfr9t209wS7V-LhGl8i";

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void acceptsCompleteSealedPixelRow() {
    assertTrue(isServableRow(row(validMetadata()), objectMapper));
  }

  @Test
  public void rejectsIncompleteTileMetadata() {
    assertFalse(isServableRow(row("{}"), objectMapper));
  }

  @Test
  public void rejectsOversizedThumbnail() {
    Map<String, Object> row = row(validMetadata());
    row.put("thumbnail_width", 8193);
    assertFalse(isServableRow(row, objectMapper));
  }

  @Test
  public void acceptsSafeMinimumLevelWithinZoomRange() {
    assertTrue(
        isServableRow(
            row(
                validMetadata()
                    .replace(
                        "\"max_zoom\":0,\"safe_min_level\":0",
                        "\"max_zoom\":4,\"safe_min_level\":2")),
            objectMapper));
  }

  @Test
  public void rejectsSafeMinimumLevelAboveZoomRange() {
    assertFalse(
        isServableRow(
            row(
                validMetadata()
                    .replace(
                        "\"max_zoom\":0,\"safe_min_level\":0",
                        "\"max_zoom\":4,\"safe_min_level\":5")),
            objectMapper));
  }

  @Test
  public void rejectsTileMetadataWithoutSafeMinimumLevel() {
    String metadata = validMetadata().replace("\"safe_min_level\":0,", "");
    assertFalse(isServableRow(row(metadata), objectMapper));
  }

  @Test
  public void rejectsTileMetadataWithoutASchemaVersion() {
    String metadata = validMetadata().replace(",\"tile_metadata_schema_version\":2", "");
    assertFalse(isServableRow(row(metadata), objectMapper));
  }

  @Test
  public void rejectsUnknownTileMetadataSchema() {
    String metadata =
        validMetadata()
            .replace("\"tile_metadata_schema_version\":2", "\"tile_metadata_schema_version\":99");
    assertFalse(isServableRow(row(metadata), objectMapper));
  }

  @Test
  public void rejectsNonCurrentDecodePolicy() {
    String metadata =
        validMetadata().replace("tile-max=16777216;thumbnail-max=16777216", "tile-max=4194304");
    assertFalse(isServableRow(row(metadata), objectMapper));
  }

  @Test
  public void rejectsIdentifierInTileMetadata() {
    Map<String, Object> row =
        row(
            validMetadata()
                .replace("\"tile_size\":256", "\"tile_size\":256,\"vendor\":\"MRN-123456\""));
    assertFalse(isServableRow(row, objectMapper));
  }

  @Test
  public void rejectsCommonAbsoluteDateFormatsInTileMetadata() {
    for (String date :
        new String[] {
          "2024-01-31", "01/31/2024", "31/01/2024", "January 31, 2024", "31 January 2024"
        }) {
      Map<String, Object> row =
          row(
              validMetadata()
                  .replace("\"tile_size\":256", "\"tile_size\":256,\"vendor\":\"" + date + "\""));
      assertFalse("date should be rejected: " + date, isServableRow(row, objectMapper));
    }
  }

  @Test
  public void acceptsJpegAndPngThumbnails() {
    for (String contentType : new String[] {"image/jpeg", "image/png", " IMAGE/PNG "}) {
      Map<String, Object> row = row(validMetadata());
      row.put("thumbnail_content_type", contentType);
      assertTrue(
          "content type should be accepted: " + contentType, isServableRow(row, objectMapper));
    }
  }

  @Test
  public void rejectsOtherThumbnailContentTypes() {
    for (String contentType :
        new String[] {null, "", "image/gif", "image/svg+xml", "text/html", "image/jpeg; x=1"}) {
      Map<String, Object> row = row(validMetadata());
      row.put("thumbnail_content_type", contentType);
      assertFalse(
          "content type should be rejected: " + contentType, isServableRow(row, objectMapper));
    }
  }

  @Test
  public void acceptsLargeNumericFieldsWithoutDateFalsePositive() {
    Map<String, Object> row = row(validMetadata().replace("\"width\":256", "\"width\":20123456"));
    assertTrue(isServableRow(row, objectMapper));
  }

  @Test
  public void rejectsRowWithoutAValidSlideKey() {
    for (String slideKey : new String[] {null, "", "slide", "2351E12D49557627B24FE71E17EC5C64"}) {
      Map<String, Object> row = row(validMetadata());
      row.put("slide_key", slideKey);
      assertFalse("slide key should be rejected: " + slideKey, isServableRow(row, objectMapper));
    }
  }

  @Test
  public void rejectsRowWithoutASealedSource() {
    Map<String, Object> row = row(validMetadata());
    row.remove("sealed_source");
    assertFalse(isServableRow(row, objectMapper));
  }

  @Test
  public void rejectsMalformedSealedSources() {
    for (String sealedSource :
        new String[] {
          "",
          " ",
          // Standard-alphabet base64 and padding are not base64url without padding.
          SEALED_SOURCE.substring(0, 40) + "+/" + SEALED_SOURCE.substring(42),
          SEALED_SOURCE + "==",
          SEALED_SOURCE + " ",
          SEALED_SOURCE + ".",
          // An object URI is not a sealed source.
          "s3://bucket/slide.svs",
          // Decodes to 28 bytes: shorter than nonce + tag + one byte of ciphertext.
          "A".repeat(38),
          // A length that is not valid base64.
          "A".repeat(41),
          // Over the 4096-character limit.
          "A".repeat(4097)
        }) {
      Map<String, Object> row = row(validMetadata());
      row.put("sealed_source", sealedSource);
      assertFalse(
          "sealed source should be rejected: " + sealedSource, isServableRow(row, objectMapper));
    }
  }

  @Test
  public void acceptsSealedSourcesAtTheLengthBounds() {
    // 39 characters decode to 29 bytes: nonce + tag + one byte of ciphertext.
    for (String sealedSource : new String[] {"A".repeat(39), "A".repeat(4096)}) {
      Map<String, Object> row = row(validMetadata());
      row.put("sealed_source", sealedSource);
      assertTrue(
          "sealed source should be accepted (length " + sealedSource.length() + ")",
          isServableRow(row, objectMapper));
    }
  }

  @Test
  public void returnsTheStoredSealedSourceVerbatim() {
    ClickhouseWsiContextMapper contextMapper = mock(ClickhouseWsiContextMapper.class);
    ClickhouseWsiSlideAccessMapper mapper = mock(ClickhouseWsiSlideAccessMapper.class);
    when(contextMapper.getStudyContext("study")).thenReturn(Map.of("cancer_study_id", 7L));
    Map<String, Object> row = row(validMetadata());
    row.put("thumbnail_content_type", "image/png");
    when(mapper.getSlideAccess(7L, "patient", SLIDE_KEY)).thenReturn(row);
    ClickhouseWsiSlideAccessRepository repository =
        new ClickhouseWsiSlideAccessRepository(mapper, contextMapper, objectMapper);

    WsiSlideSource source = repository.getSlideSource("study", "patient", SLIDE_KEY);

    assertNotNull(source);
    assertEquals(SLIDE_KEY, source.slideKey());
    assertEquals(SEALED_SOURCE, source.sealedSource());
    assertEquals(128, source.thumbnail().width());
    assertEquals(96, source.thumbnail().height());
    assertEquals("image/png", source.thumbnail().contentType());
  }

  @Test
  public void returnsNullForAnUnservableRow() {
    ClickhouseWsiContextMapper contextMapper = mock(ClickhouseWsiContextMapper.class);
    ClickhouseWsiSlideAccessMapper mapper = mock(ClickhouseWsiSlideAccessMapper.class);
    when(contextMapper.getStudyContext("study")).thenReturn(Map.of("cancer_study_id", 7L));
    Map<String, Object> row = row(validMetadata());
    row.put("sealed_source", "not a sealed source");
    when(mapper.getSlideAccess(7L, "patient", SLIDE_KEY)).thenReturn(row);
    ClickhouseWsiSlideAccessRepository repository =
        new ClickhouseWsiSlideAccessRepository(mapper, contextMapper, objectMapper);

    assertNull(repository.getSlideSource("study", "patient", SLIDE_KEY));
  }

  @Test
  public void refusesMalformedSlideKeysBeforeQuerying() {
    ClickhouseWsiSlideAccessRepository repository =
        new ClickhouseWsiSlideAccessRepository(null, null, objectMapper);
    for (String slideKey : new String[] {null, "", "syn-img-0001", "3020726"}) {
      assertNull(repository.getSlideSource("study", "patient", slideKey));
    }
  }

  private static Map<String, Object> row(String metadata) {
    Map<String, Object> row = new HashMap<>();
    row.put("can_serve_tiles", true);
    row.put("slide_key", SLIDE_KEY);
    row.put("sealed_source", SEALED_SOURCE);
    row.put("tile_metadata_json", metadata);
    row.put("thumbnail_width", 128);
    row.put("thumbnail_height", 96);
    row.put("thumbnail_content_type", "image/jpeg");
    return row;
  }

  private static String validMetadata() {
    return "{\"dimensions\":{\"width\":256,\"height\":256},"
        + "\"levels\":1,\"level_dimensions\":[{\"width\":256,\"height\":256}],"
        + "\"level_downsamples\":[1.0],\"max_zoom\":0,"
        + "\"safe_min_level\":0,\"tile_size\":256,\"tile_metadata_schema_version\":2,"
        + "\"decode_policy_version\":\"geometry-v2;tile-max=16777216;thumbnail-max=16777216\","
        + "\"max_decode_pixels\":16777216,\"thumbnail_max_decode_pixels\":16777216}";
  }

  static boolean isServableRow(Map<String, Object> row, ObjectMapper objectMapper) {
    return ClickhouseWsiSlideAccessRepository.servableTileMetadata(row, objectMapper) != null;
  }
}
