package org.cbioportal.infrastructure.repository.clickhouse.resource;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ResourceMetadataValueSqlTest {

  @Test
  public void namesTheKeysSubcolumn_andStripsThePrefix() {
    assertThat(ResourceMetadataValueSql.of("stain_group"))
        .isEqualTo(
            "substring(ifNull(CAST(rdata.metadata_json.`stain_group`, 'Nullable(String)'), ''), 2)");
  }

  @Test
  public void escapesBackquotesAndBackslashes_soAnyKeyStaysOneIdentifier() {
    assertThat(ResourceMetadataValueSql.of("a`b\\c")).contains("rdata.metadata_json.`a\\`b\\\\c`,");
    assertThat(ResourceMetadataValueSql.of("x`, 'String'), ''), 2) OR 1 = 1 --"))
        .contains("rdata.metadata_json.`x\\`, 'String'), ''), 2) OR 1 = 1 --`,");
  }

  /**
   * MyBatis parses #{...} placeholders in the SQL after ${...} is substituted, so no '#' may reach
   * it from a key. ${...} is not parsed again and passes through unchanged.
   */
  @Test
  public void writesHashAsAnEscape_soMyBatisFindsNoPlaceholderInAKey() {
    assertThat(ResourceMetadataValueSql.of("#{search}"))
        .isEqualTo(
            "substring(ifNull(CAST(rdata.metadata_json.`\\x23{search}`, 'Nullable(String)'), ''), 2)");
    assertThat(ResourceMetadataValueSql.of("\\#{x}")).contains("metadata_json.`\\\\\\x23{x}`,");
    assertThat(ResourceMetadataValueSql.of("${x}")).contains("metadata_json.`${x}`,");
    assertThat(ResourceMetadataValueSql.of("a.:#{x}")).doesNotContain("#");
  }

  @Test
  public void theEmptyKey_isReadFromTheRawDocument() {
    assertThat(ResourceMetadataValueSql.of(""))
        .isEqualTo("JSONExtractString(ifNull(rdata.metadata, '{}'), '')");
  }

  /** The subcolumn syntax gives .: (a type hint) and .^ (a sub-object) a meaning of their own. */
  @Test
  public void keysTheSubcolumnSyntaxCannotName_areReadFromTheRawDocument() {
    assertThat(ResourceMetadataValueSql.of("a.:`b`"))
        .isEqualTo("JSONExtractString(ifNull(rdata.metadata, '{}'), 'a.:`b`')");
    assertThat(ResourceMetadataValueSql.of("a.^b"))
        .isEqualTo("JSONExtractString(ifNull(rdata.metadata, '{}'), 'a.^b')");
    assertThat(ResourceMetadataValueSql.of("x.:', 'y') OR 1 = 1 --\\"))
        .isEqualTo(
            "JSONExtractString(ifNull(rdata.metadata, '{}'), 'x.:\\', \\'y\\') OR 1 = 1 --\\\\')");
  }
}
