package org.cbioportal.domain.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;

public class ResourceMetadataSchemaTest {

  @Test
  public void parse_readsEveryPresentationField() {
    ResourceMetadataSchema schema =
        ResourceMetadataSchema.parse(
            """
            {"version":1,"fields":[
              {"key":"stain","type":"string","label":"Stain",
               "description":"Histology stain","filterable":true}
            ]}""");

    assertThat(schema.version()).isEqualTo(1);
    assertThat(schema.fields()).hasSize(1);
    ResourceMetadataField field = schema.fields().get(0);
    assertThat(field.key()).isEqualTo("stain");
    assertThat(field.type()).isEqualTo("string");
    assertThat(field.label()).isEqualTo("Stain");
    assertThat(field.description()).isEqualTo("Histology stain");
    assertThat(field.filterable()).isTrue();
  }

  @Test
  public void parse_preservesDeclarationOrder() {
    ResourceMetadataSchema schema =
        ResourceMetadataSchema.parse(
            "{\"fields\":[{\"key\":\"zebra\"},{\"key\":\"alpha\"},{\"key\":\"mid\"}]}");

    assertThat(schema.fieldsByKey().keySet()).containsExactly("zebra", "alpha", "mid");
  }

  @Test
  public void parse_leavesUndeclaredAttributesNull() {
    // null means "not declared", which must stay distinguishable from an explicit false so
    // auto-detection is not overridden by a default.
    ResourceMetadataSchema schema = ResourceMetadataSchema.parse("{\"fields\":[{\"key\":\"a\"}]}");

    ResourceMetadataField field = schema.fields().get(0);
    assertThat(field.type()).isNull();
    assertThat(field.label()).isNull();
    assertThat(field.description()).isNull();
    assertThat(field.filterable()).isNull();
  }

  @Test
  public void parse_ignoresUnknownMembersAndKeylessFields() {
    ResourceMetadataSchema schema =
        ResourceMetadataSchema.parse(
            "{\"fields\":[{\"key\":\"a\",\"renderAs\":\"badge\",\"required\":true},{\"label\":\"no key\"}]}");

    assertThat(schema.fieldsByKey().keySet()).containsExactly("a");
  }

  @Test
  public void parse_treatsMalformedOrMissingContractAsAbsent() {
    // A bad curator edit must never break the resource table.
    for (String input : List.of("", "   ", "not json", "{}", "{\"fields\":\"nope\"}", "[]")) {
      assertThat(ResourceMetadataSchema.parse(input).fields()).isEmpty();
    }
    assertThat(ResourceMetadataSchema.parse(null).fields()).isEmpty();
  }

  @Test
  public void parse_ignoresNonTextualAndNonBooleanValues() {
    ResourceMetadataSchema schema =
        ResourceMetadataSchema.parse(
            "{\"fields\":[{\"key\":\"a\",\"label\":42,\"filterable\":\"yes\"}]}");

    assertThat(schema.fields().get(0).label()).isNull();
    assertThat(schema.fields().get(0).filterable()).isNull();
  }
}
