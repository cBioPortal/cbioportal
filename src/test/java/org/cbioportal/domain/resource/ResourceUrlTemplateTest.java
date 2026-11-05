package org.cbioportal.domain.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class ResourceUrlTemplateTest {

  private static final String BASE = "https://portal.example.org";

  private static ResourceUrlTemplate.Values sampleRow(Map<String, Object> metadata) {
    return new ResourceUrlTemplate.Values(
        "mskimpact", "WSI_SAMPLE", "P-0000001", "P-0000001-T01-IM3", metadata);
  }

  @Test
  public void expandsEveryPlaceholder() {
    String url =
        ResourceUrlTemplate.expand(
            "${base_url}/r/${RESOURCE_ID}/${STUDY_ID}/${PATIENT_ID}/${SAMPLE_ID}?k=${metadata.slide_key}",
            BASE,
            sampleRow(Map.of("slide_key", "2351e12d4955")));

    assertThat(url)
        .isEqualTo(
            "https://portal.example.org/r/WSI_SAMPLE/mskimpact/P-0000001/P-0000001-T01-IM3"
                + "?k=2351e12d4955");
  }

  @Test
  public void expandsTheSlideViewerTemplate() {
    String url =
        ResourceUrlTemplate.expand(
            "${base_url}/wsi/patient/${PATIENT_ID}?studyId=${STUDY_ID}&slideKey=${metadata.slide_key}",
            BASE,
            sampleRow(Map.of("slide_key", "2351e12d4955")));

    assertThat(url)
        .isEqualTo(
            "https://portal.example.org/wsi/patient/P-0000001?studyId=mskimpact"
                + "&slideKey=2351e12d4955");
  }

  @Test
  public void percentEncodesValuesAsUriComponents() {
    ResourceUrlTemplate.Values values =
        new ResourceUrlTemplate.Values(
            "a b", "x/y", "p&q=1", "s?#%+", Map.of("note", "café ~-._!*'()"));

    String url =
        ResourceUrlTemplate.expand(
            "${STUDY_ID}|${RESOURCE_ID}|${PATIENT_ID}|${SAMPLE_ID}|${metadata.note}", BASE, values);

    assertThat(url)
        .isEqualTo("a%20b|x%2Fy|p%26q%3D1|s%3F%23%25%2B|caf%C3%A9%20~-._%21%2A%27%28%29");
  }

  @Test
  public void doesNotEncodeTheBaseUrl() {
    assertThat(
            ResourceUrlTemplate.expand(
                "${base_url}/x", "https://host:8443/a b", sampleRow(Map.of())))
        .isEqualTo("https://host:8443/a b/x");
  }

  @Test
  public void baseUrlWithAContextPath() {
    assertThat(
            ResourceUrlTemplate.expand(
                "${base_url}/wsi", "https://host.example.org/cbioportal", sampleRow(Map.of())))
        .isEqualTo("https://host.example.org/cbioportal/wsi");
  }

  @Test
  public void baseUrlWithoutAContextPath() {
    assertThat(
            ResourceUrlTemplate.expand(
                "${base_url}/wsi", "http://localhost:8080", sampleRow(Map.of())))
        .isEqualTo("http://localhost:8080/wsi");
  }

  @Test
  public void baseUrlTrailingSlashIsDropped() {
    assertThat(
            ResourceUrlTemplate.expand(
                "${base_url}/wsi", "https://host.example.org/cbioportal/", sampleRow(Map.of())))
        .isEqualTo("https://host.example.org/cbioportal/wsi");
  }

  @Test
  public void missingBaseUrlExpandsToEmpty() {
    assertThat(ResourceUrlTemplate.expand("${base_url}/wsi", null, sampleRow(Map.of())))
        .isEqualTo("/wsi");
  }

  @Test
  public void missingValuesExpandToEmptyStrings() {
    ResourceUrlTemplate.Values patientRow =
        new ResourceUrlTemplate.Values("mskimpact", "WSI_PATIENT", "P-1", null, null);

    assertThat(
            ResourceUrlTemplate.expand(
                "s=${SAMPLE_ID}&k=${metadata.slide_key}&p=${PATIENT_ID}", BASE, patientRow))
        .isEqualTo("s=&k=&p=P-1");
  }

  @Test
  public void absentMetadataKeyAndJsonNullExpandToEmpty() {
    Map<String, Object> metadata = new LinkedHashMap<>();
    metadata.put("present", "yes");
    metadata.put("explicitNull", null);

    assertThat(
            ResourceUrlTemplate.expand(
                "${metadata.absent}|${metadata.explicitNull}|${metadata.present}",
                BASE,
                sampleRow(metadata)))
        .isEqualTo("||yes");
  }

  @Test
  public void nonStringMetadataExpandsToItsJsonText() {
    Map<String, Object> metadata = new LinkedHashMap<>();
    metadata.put("n", 42);
    metadata.put("d", 0.25);
    metadata.put("b", true);
    metadata.put("list", List.of(1, "a"));
    metadata.put("obj", Map.of("k", "v"));

    assertThat(
            ResourceUrlTemplate.expand(
                "${metadata.n}|${metadata.d}|${metadata.b}|${metadata.list}|${metadata.obj}",
                BASE,
                sampleRow(metadata)))
        .isEqualTo("42|0.25|true|%5B1%2C%22a%22%5D|%7B%22k%22%3A%22v%22%7D");
  }

  @Test
  public void metadataParsedFromJsonKeepsItsTypes() {
    Map<String, Object> metadata =
        ResourceUrlTemplate.parseMetadata("{\"slide_key\":\"abc\",\"mpp\":0.5,\"z\":[1,2]}");

    assertThat(
            ResourceUrlTemplate.expand(
                "${metadata.slide_key}/${metadata.mpp}/${metadata.z}", BASE, sampleRow(metadata)))
        .isEqualTo("abc/0.5/%5B1%2C2%5D");
  }

  @Test
  public void unreadableMetadataIsTreatedAsEmpty() {
    assertThat(ResourceUrlTemplate.parseMetadata("not json")).isEmpty();
    assertThat(ResourceUrlTemplate.parseMetadata("[1,2]")).isEmpty();
    assertThat(ResourceUrlTemplate.parseMetadata(null)).isEmpty();
    assertThat(ResourceUrlTemplate.parseMetadata("  ")).isEmpty();
  }

  @Test
  public void unknownPlaceholdersAreLeftAsIs() {
    assertThat(
            ResourceUrlTemplate.expand(
                "${base_url}/${UNKNOWN}/${patient_id}/${metadata.}/${}/${PATIENT_ID}",
                BASE,
                sampleRow(Map.of())))
        .isEqualTo(
            "https://portal.example.org/${UNKNOWN}/${patient_id}/${metadata.}/${}/P-0000001");
  }

  @Test
  public void textOutsidePlaceholdersIsCopiedVerbatim() {
    assertThat(
            ResourceUrlTemplate.expand(
                "https://other.example.org/a b?x=$1&y=\\${PATIENT_ID", BASE, sampleRow(Map.of())))
        .isEqualTo("https://other.example.org/a b?x=$1&y=\\${PATIENT_ID");
  }

  @Test
  public void replacementTextIsNotReinterpreted() {
    // A value that itself looks like a placeholder, or carries regex replacement syntax, is
    // inserted literally (and encoded), not expanded again.
    assertThat(
            ResourceUrlTemplate.expand(
                "${metadata.k}|${base_url}",
                "https://h/$1\\x",
                sampleRow(Map.of("k", "${STUDY_ID}"))))
        .isEqualTo("%24%7BSTUDY_ID%7D|https://h/$1\\x");
  }

  @Test
  public void resolve_rowUrlWins() {
    assertThat(
            ResourceUrlTemplate.resolve(
                "https://example.com/own.svs", "${base_url}/x", BASE, sampleRow(Map.of())))
        .isEqualTo("https://example.com/own.svs");
  }

  @Test
  public void resolve_emptyOrBlankRowUrlUsesTheTemplate() {
    assertThat(ResourceUrlTemplate.resolve("", "${base_url}/x", BASE, sampleRow(Map.of())))
        .isEqualTo(BASE + "/x");
    assertThat(ResourceUrlTemplate.resolve(null, "${base_url}/x", BASE, sampleRow(Map.of())))
        .isEqualTo(BASE + "/x");
    assertThat(ResourceUrlTemplate.resolve("  ", "${base_url}/x", BASE, sampleRow(Map.of())))
        .isEqualTo(BASE + "/x");
  }

  @Test
  public void resolve_noUrlAndNoTemplateGivesEmpty() {
    assertThat(ResourceUrlTemplate.resolve("", null, BASE, sampleRow(Map.of()))).isEmpty();
    assertThat(ResourceUrlTemplate.resolve(null, "", BASE, sampleRow(Map.of()))).isEmpty();
  }
}
