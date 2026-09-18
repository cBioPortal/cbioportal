package org.cbioportal.application.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.cbioportal.legacy.persistence.mybatis.StudyMapper;
import org.junit.jupiter.api.Test;

class UnavailableCancerStudyIdsTest {

  private final StudyMapper studyMapper = mock(StudyMapper.class);
  private long now;
  private final UnavailableCancerStudyIds cache =
      new UnavailableCancerStudyIds(studyMapper, 2000, () -> now);

  @Test
  void reportsOnlyIdsReturnedByTheMapperAsUnavailable() {
    when(studyMapper.getUnavailableStudyIds()).thenReturn(List.of("study1"));

    assertTrue(cache.isUnavailable("study1"));
    assertFalse(cache.isUnavailable("study2"));
  }

  @Test
  void refreshesOnlyWhenTtlExpires() {
    when(studyMapper.getUnavailableStudyIds()).thenReturn(List.of("study1"), List.of());

    assertTrue(cache.isUnavailable("study1"));
    now = 1999;
    assertTrue(cache.isUnavailable("study1"));
    verify(studyMapper, times(1)).getUnavailableStudyIds();

    now = 2000;
    assertFalse(cache.isUnavailable("study1"));
    verify(studyMapper, times(2)).getUnavailableStudyIds();
  }

  @Test
  void firstLoadFailureAllowsReadsAndWaitsForTtlBeforeRetrying() {
    when(studyMapper.getUnavailableStudyIds())
        .thenThrow(new IllegalStateException("Database unavailable"))
        .thenReturn(List.of("study1"));

    assertFalse(cache.isUnavailable("study1"));
    now = 1999;
    assertFalse(cache.isUnavailable("study1"));
    verify(studyMapper, times(1)).getUnavailableStudyIds();

    now = 2000;
    assertTrue(cache.isUnavailable("study1"));
    verify(studyMapper, times(2)).getUnavailableStudyIds();
  }

  @Test
  void refreshFailureKeepsPreviousSnapshotAndWaitsForTtlBeforeRetrying() {
    when(studyMapper.getUnavailableStudyIds())
        .thenReturn(List.of("study1"))
        .thenThrow(new IllegalStateException("Database unavailable"))
        .thenReturn(List.of());

    assertTrue(cache.isUnavailable("study1"));
    now = 2000;
    assertTrue(cache.isUnavailable("study1"));
    now = 3999;
    assertTrue(cache.isUnavailable("study1"));
    verify(studyMapper, times(2)).getUnavailableStudyIds();

    now = 4000;
    assertFalse(cache.isUnavailable("study1"));
    verify(studyMapper, times(3)).getUnavailableStudyIds();
  }
}
