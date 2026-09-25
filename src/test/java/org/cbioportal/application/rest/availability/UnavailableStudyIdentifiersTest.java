package org.cbioportal.application.rest.availability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cbioportal.legacy.persistence.mybatis.StudyMapper;
import org.junit.jupiter.api.Test;

class UnavailableStudyIdentifiersTest {

  private final StudyMapper studyMapper = mock(StudyMapper.class);
  private long now;
  private final UnavailableStudyIdentifiers cache =
      new UnavailableStudyIdentifiers(studyMapper, true, 2000, () -> now);

  private static Map<String, String> row(String identifier, String studyId) {
    Map<String, String> row = new HashMap<>();
    row.put("identifier", identifier);
    row.put("studyId", studyId);
    return row;
  }

  @Test
  void disabledTreatsEveryStudyAsAvailableWithoutQuerying() {
    UnavailableStudyIdentifiers disabled =
        new UnavailableStudyIdentifiers(studyMapper, false, 2000, () -> now);
    assertEquals(Map.of(), disabled.get());
    verify(studyMapper, never()).getUnavailableStudyIdentifiers();
  }

  @Test
  void skipsRowsWithNullColumns() {
    when(studyMapper.getUnavailableStudyIdentifiers())
        .thenReturn(List.of(row("study1", "study1"), row(null, "study1"), row("orphan", null)));

    assertEquals(Map.of("study1", "study1"), cache.get());
  }

  @Test
  void refreshesOnlyWhenTtlExpires() {
    when(studyMapper.getUnavailableStudyIdentifiers())
        .thenReturn(List.of(row("study1", "study1")), List.of());

    assertEquals(Map.of("study1", "study1"), cache.get());
    now = 1999;
    assertEquals(Map.of("study1", "study1"), cache.get());
    verify(studyMapper, times(1)).getUnavailableStudyIdentifiers();

    now = 2000;
    assertEquals(Map.of(), cache.get());
    verify(studyMapper, times(2)).getUnavailableStudyIdentifiers();
  }

  @Test
  void firstLoadFailureAllowsReadsAndWaitsForTtlBeforeRetrying() {
    when(studyMapper.getUnavailableStudyIdentifiers())
        .thenThrow(new IllegalStateException("Database unavailable"))
        .thenReturn(List.of(row("study1", "study1")));

    assertEquals(Map.of(), cache.get());
    now = 1999;
    assertEquals(Map.of(), cache.get());
    verify(studyMapper, times(1)).getUnavailableStudyIdentifiers();

    now = 2000;
    assertEquals(Map.of("study1", "study1"), cache.get());
  }

  @Test
  void refreshFailureKeepsPreviousSnapshot() {
    when(studyMapper.getUnavailableStudyIdentifiers())
        .thenReturn(List.of(row("study1", "study1")))
        .thenThrow(new IllegalStateException("Database unavailable"))
        .thenReturn(List.of());

    assertEquals(Map.of("study1", "study1"), cache.get());
    now = 2000;
    assertEquals(Map.of("study1", "study1"), cache.get());
    now = 4000;
    assertEquals(Map.of(), cache.get());
    verify(studyMapper, times(3)).getUnavailableStudyIdentifiers();
  }
}
