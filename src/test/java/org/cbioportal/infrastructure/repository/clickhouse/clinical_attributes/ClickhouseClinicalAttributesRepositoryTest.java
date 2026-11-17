package org.cbioportal.infrastructure.repository.clickhouse.clinical_attributes;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.cbioportal.domain.clinical_attributes.ClinicalAttribute;
import org.cbioportal.legacy.web.parameter.ClinicalDataType;
import org.junit.Test;

public class ClickhouseClinicalAttributesRepositoryTest {

  @Test
  public void getClinicalAttributeDatatypeMapHandlesSampleOnlyMetadata() {
    ClickhouseClinicalAttributesMapper mapper = mock(ClickhouseClinicalAttributesMapper.class);
    when(mapper.getClinicalAttributes())
        .thenReturn(
            List.of(new ClinicalAttribute("SAMPLE_ONLY_ATTRIBUTE", "NUMBER", false, "study")));

    ClickhouseClinicalAttributesRepository repository =
        new ClickhouseClinicalAttributesRepository(mapper);

    assertEquals(
        ClinicalDataType.SAMPLE,
        repository.getClinicalAttributeDatatypeMap().get("SAMPLE_ONLY_ATTRIBUTE"));
  }
}
