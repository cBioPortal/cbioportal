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

  @Test
  public void theEmptyKey_isReadFromTheRawDocument() {
    assertThat(ResourceMetadataValueSql.of(""))
        .isEqualTo("JSONExtractString(ifNull(rdata.metadata, '{}'), '')");
  }
}
