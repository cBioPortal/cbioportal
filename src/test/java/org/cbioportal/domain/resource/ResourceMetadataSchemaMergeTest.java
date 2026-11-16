package org.cbioportal.domain.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;

/**
 * A cohort can span studies and the contract is declared per (resource, study), so one table can be
 * described by several contracts that need combining into one column list.
 */
public class ResourceMetadataSchemaMergeTest {

  private static final ResourceMetadataSchema STUDY_1 =
      ResourceMetadataSchema.parse(
          """
          {"version":1,"fields":[
            {"key":"stain","type":"string","label":"Stain","filterable":false},
            {"key":"grade","type":"number","label":"Grade","visibleByDefault":true}]}
          """);

  private static final ResourceMetadataSchema STUDY_2 =
      ResourceMetadataSchema.parse(
          """
          {"version":1,"fields":[
            {"key":"grade","type":"string","label":"Grade (other)"},
            {"key":"reviewer","type":"string","label":"Reviewer"}]}
          """);

  @Test
  public void mergeIsTheUnionOfFields_earlierContractsFirst() {
    ResourceMetadataSchema merged = ResourceMetadataSchema.merge(List.of(STUDY_1, STUDY_2));

    assertThat(merged.fieldsByKey().keySet()).containsExactly("stain", "grade", "reviewer");
  }

  @Test
  public void aKeyTakesTheFirstDeclarationThatMentionsIt() {
    ResourceMetadataSchema merged = ResourceMetadataSchema.merge(List.of(STUDY_1, STUDY_2));

    ResourceMetadataField grade = merged.fieldsByKey().get("grade");
    assertThat(grade.label()).isEqualTo("Grade");
    assertThat(grade.visibleByDefault()).isTrue();
  }

  @Test
  public void aKeyTwoContractsTypeDifferentlyIsLeftUntyped() {
    // Neither type can be trusted for the whole cohort, and declaring "number" over text would put
    // a range filter on it. Null restores auto-detection from the values.
    ResourceMetadataSchema merged = ResourceMetadataSchema.merge(List.of(STUDY_1, STUDY_2));

    assertThat(merged.fieldsByKey().get("grade").type()).isNull();
    assertThat(merged.fieldsByKey().get("stain").type()).isEqualTo("string");
  }

  @Test
  public void aKeyOnlyOneContractTypesKeepsThatType() {
    ResourceMetadataSchema untyped =
        ResourceMetadataSchema.parse("{\"fields\":[{\"key\":\"grade\",\"label\":\"G\"}]}");

    ResourceMetadataSchema merged = ResourceMetadataSchema.merge(List.of(STUDY_1, untyped));

    assertThat(merged.fieldsByKey().get("grade").type()).isEqualTo("number");
  }

  @Test
  public void mergingOneContractReturnsItUnchanged() {
    assertThat(ResourceMetadataSchema.merge(List.of(STUDY_1))).isSameAs(STUDY_1);
  }

  @Test
  public void mergingNothingIsTheEmptyContract() {
    assertThat(ResourceMetadataSchema.merge(List.of()).fields()).isEmpty();
    assertThat(ResourceMetadataSchema.merge(null).fields()).isEmpty();
  }

  @Test
  public void conflictingTypeKeysNamesOnlyTheDisagreements() {
    assertThat(ResourceMetadataSchema.conflictingTypeKeys(List.of(STUDY_1, STUDY_2)))
        .containsExactly("grade");
    assertThat(ResourceMetadataSchema.conflictingTypeKeys(List.of(STUDY_1, STUDY_1))).isEmpty();
  }
}
