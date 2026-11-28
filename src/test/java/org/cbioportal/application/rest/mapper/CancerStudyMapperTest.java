package org.cbioportal.application.rest.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;
import org.cbioportal.legacy.model.CancerStudy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CancerStudyMapperTest {

  private static final Date IMPORT_DATE = Date.from(Instant.parse("2011-12-18T13:17:17Z"));

  private TimeZone originalTimeZone;

  @Before
  public void setNonUtcDefaultTimeZone() {
    originalTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("America/Denver"));
  }

  @After
  public void restoreDefaultTimeZone() {
    TimeZone.setDefault(originalTimeZone);
  }

  @Test
  public void formatsImportDateInUtcRegardlessOfDefaultTimeZone() {
    CancerStudy cancerStudy = new CancerStudy();
    cancerStudy.setImportDate(IMPORT_DATE);

    var dto = CancerStudyMapper.INSTANCE.toDto(cancerStudy);

    assertEquals("2011-12-18 13:17:17", dto.getImportDate());
  }

  @Test
  public void mapsNullImportDateToNull() {
    assertNull(new DateMapper().toUtcDateTime(null));
  }
}
