package org.cbioportal.legacy.web.util;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.cbioportal.legacy.model.SampleList;
import org.cbioportal.legacy.service.SampleListService;
import org.cbioportal.legacy.service.exception.BulkRequestTooLargeException;
import org.cbioportal.legacy.service.exception.SampleListNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class BulkRequestLimiterTest {

  @Mock private SampleListService sampleListService;

  @InjectMocks private BulkRequestLimiter limiter;

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(limiter, "maxSamplesWithoutGenes", 100);
    ReflectionTestUtils.setField(limiter, "maxValues", 5_000_000L);
    ReflectionTestUtils.setField(limiter, "helpText", "See /llms.txt.");
  }

  private static SampleList sampleListOf(int n) {
    SampleList list = new SampleList();
    list.setSampleIds(IntStream.range(0, n).mapToObj(i -> "S" + i).collect(Collectors.toList()));
    return list;
  }

  private static List<Integer> genes(int n) {
    return IntStream.range(0, n).boxed().collect(Collectors.toList());
  }

  @Test
  public void allowsFewSamplesWithoutGenes() {
    // e.g. the patient view's copy-number table: all genes for one patient's samples
    limiter.checkSampleCount(5, null);
    limiter.checkSampleCount(100, Collections.emptyList());
  }

  @Test
  public void rejectsManySamplesWithoutGenes() {
    BulkRequestTooLargeException e =
        assertThrows(BulkRequestTooLargeException.class, () -> limiter.checkSampleCount(101, null));
    assertTrue(e.getMessage().contains("101 requested"));
    assertTrue(e.getMessage().endsWith("See /llms.txt."));
  }

  @Test
  public void appliesValueLimitWhenGenesGiven() {
    limiter.checkSampleCount(10_000, genes(500));
    assertThrows(
        BulkRequestTooLargeException.class, () -> limiter.checkSampleCount(10_000, genes(501)));
  }

  @Test
  public void resolvesSampleListSize() throws Exception {
    when(sampleListService.getSampleList("big_all")).thenReturn(sampleListOf(1000));
    when(sampleListService.getSampleList("small_all")).thenReturn(sampleListOf(10));

    assertThrows(
        BulkRequestTooLargeException.class, () -> limiter.checkSampleList("big_all", null));
    limiter.checkSampleList("small_all", null);
    limiter.checkSampleList("big_all", genes(10));
  }

  @Test
  public void missingSampleListIsLeftToTheDataQuery() throws Exception {
    when(sampleListService.getSampleList("nope"))
        .thenThrow(new SampleListNotFoundException("nope"));
    limiter.checkSampleList("nope", null);
  }

  @Test
  public void wholeProfilesRequireGenes() {
    assertThrows(BulkRequestTooLargeException.class, () -> limiter.checkWholeProfiles(null));
    limiter.checkWholeProfiles(genes(3));
  }

  @Test
  public void disabledByDefault() {
    ReflectionTestUtils.setField(limiter, "maxSamplesWithoutGenes", -1);
    ReflectionTestUtils.setField(limiter, "maxValues", -1L);

    limiter.checkSampleCount(1_000_000, null);
    limiter.checkSampleCount(1_000_000, genes(20_000));
    limiter.checkSampleList("big_all", null);
    limiter.checkWholeProfiles(null);
    verifyNoInteractions(sampleListService);
  }
}
