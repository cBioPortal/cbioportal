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
  public void requiresASafeMinimumLevelWithinTheZoomRange() {
    String zoom = "\"max_zoom\":0,\"safe_min_level\":0";
    assertTrue(
        isServableRow(
            row(validMetadata().replace(zoom, "\"max_zoom\":4,\"safe_min_level\":2")),
            objectMapper));
    assertFalse(
        isServableRow(
            row(validMetadata().replace(zoom, "\"max_zoom\":4,\"safe_min_level\":5")),
            objectMapper));
    assertFalse(
        isServableRow(row(validMetadata().replace("\"safe_min_level\":0,", "")), objectMapper));
  }

  @Test
  public void requiresTileMetadataSchemaVersion2() {
    String version = "\"tile_metadata_schema_version\":2";
    for (String metadata :
        new String[] {
          validMetadata().replace("," + version, ""),
          validMetadata().replace(version, "\"tile_metadata_schema_version\":99")
        }) {
      assertFalse(metadata, isServableRow(row(metadata), objectMapper));
    }
  }

  @Test
  public void rejectsNonCurrentDecodePolicy() {
    String metadata =
        validMetadata().replace("tile-max=16777216;thumbnail-max=16777216", "tile-max=4194304");
    assertFalse(isServableRow(row(metadata), objectMapper));
  }

  @Test
  public void rejectsIdentifiersAndAbsoluteDatesInTileMetadata() {
    for (String text :
        new String[] {
          "MRN-123456",
          "2024-01-31",
          "01/31/2024",
          "31/01/2024",
          "January 31, 2024",
          "31 January 2024"
        }) {
      Map<String, Object> row =
          row(
              validMetadata()
                  .replace("\"tile_size\":256", "\"tile_size\":256,\"vendor\":\"" + text + "\""));
      assertFalse("should be rejected: " + text, isServableRow(row, objectMapper));
    }
  }

  @Test
  public void acceptsOnlyJpegAndPngThumbnails() {
    for (String contentType : new String[] {"image/jpeg", "image/png", " IMAGE/PNG "}) {
      Map<String, Object> row = row(validMetadata());
      row.put("thumbnail_content_type", contentType);
      assertTrue(
          "content type should be accepted: " + contentType, isServableRow(row, objectMapper));
    }
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
  public void acceptsASourceFingerprintWhoseHexLooksLikeACompactDate() {
    // A real SHA-256 identity digest: "...6ef20939140a7..." contains an eight-digit run that the
    // compact-date rule would read as YYYYMMDD.
    Map<String, Object> row =
        row(withFingerprint("a41afcbce3d16ef20939140a7cfdb3ab5a6d88bf6cb3e586c8cae39cee1bc562"));
    assertTrue(isServableRow(row, objectMapper));
  }

  @Test
  public void rejectsASourceFingerprintThatIsNotALowercaseSha256Digest() {
    for (String fingerprint :
        new String[] {
          "",
          "2024-01-31",
          "MRN 12345678",
          "A41AFCBCE3D16EF20939140A7CFDB3AB5A6D88BF6CB3E586C8CAE39CEE1BC562",
          "a41afcbce3d16ef20939140a7cfdb3ab5a6d88bf6cb3e586c8cae39cee1bc56",
          "a41afcbce3d16ef20939140a7cfdb3ab5a6d88bf6cb3e586c8cae39cee1bc5620",
        }) {
      assertFalse(
          "fingerprint should be rejected: " + fingerprint,
          isServableRow(row(withFingerprint(fingerprint)), objectMapper));
    }
  }

  @Test
  public void rejectsANonTextSourceFingerprint() {
    Map<String, Object> row =
        row(
            validMetadata()
                .replace("\"tile_size\":256", "\"tile_size\":256,\"source_fingerprint\":1"));
    assertFalse(isServableRow(row, objectMapper));
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
  public void rejectsMissingOrMalformedSealedSources() {
    for (String sealedSource :
        new String[] {
          null,
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
    Map<String, Object> row = row(validMetadata());
    row.put("thumbnail_content_type", "image/png");

    WsiSlideSource source = repositoryReturning(row).getSlideSource("study", "patient", SLIDE_KEY);

    assertNotNull(source);
    assertEquals(SLIDE_KEY, source.slideKey());
    assertEquals(SEALED_SOURCE, source.sealedSource());
    assertEquals(128, source.thumbnail().width());
    assertEquals(96, source.thumbnail().height());
    assertEquals("image/png", source.thumbnail().contentType());
  }

  @Test
  public void returnsNullForAnUnservableRow() {
    Map<String, Object> row = row(validMetadata());
    row.put("sealed_source", "not a sealed source");

    assertNull(repositoryReturning(row).getSlideSource("study", "patient", SLIDE_KEY));
  }

  @Test
  public void refusesMalformedSlideKeysBeforeQuerying() {
    ClickhouseWsiSlideAccessRepository repository =
        new ClickhouseWsiSlideAccessRepository(null, null, objectMapper);
    for (String slideKey : new String[] {null, "", "syn-img-0001", "3020726"}) {
      assertNull(repository.getSlideSource("study", "patient", slideKey));
    }
  }

  private ClickhouseWsiSlideAccessRepository repositoryReturning(Map<String, Object> row) {
    ClickhouseWsiContextMapper contextMapper = mock(ClickhouseWsiContextMapper.class);
    ClickhouseWsiSlideAccessMapper mapper = mock(ClickhouseWsiSlideAccessMapper.class);
    when(contextMapper.getStudyContext("study")).thenReturn(Map.of("cancer_study_id", 7L));
    when(mapper.getSlideAccess(7L, "patient", SLIDE_KEY)).thenReturn(row);
    return new ClickhouseWsiSlideAccessRepository(mapper, contextMapper, objectMapper);
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

  private static String withFingerprint(String fingerprint) {
    return validMetadata()
        .replace(
            "\"tile_size\":256",
            "\"tile_size\":256,\"source_fingerprint\":\"" + fingerprint + "\"");
  }

  static boolean isServableRow(Map<String, Object> row, ObjectMapper objectMapper) {
    return ClickhouseWsiSlideAccessRepository.servableTileMetadata(row, objectMapper) != null;
  }
}
