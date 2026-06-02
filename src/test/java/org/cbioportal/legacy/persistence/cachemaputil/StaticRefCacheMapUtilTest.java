package org.cbioportal.legacy.persistence.cachemaputil;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Map;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.SampleList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StaticRefCacheMapUtilTest {

  @Mock private CacheMapBuilder cacheMapBuilder;

  @InjectMocks private StaticRefCacheMapUtil cacheMapUtil;

  @Before
  public void setUp() {
    StaticRefCacheMapUtil.molecularProfileCache = null;
    StaticRefCacheMapUtil.sampleListCache = null;
    StaticRefCacheMapUtil.cancerStudyCache = null;
  }

  @Test
  public void getMolecularProfileMapInitializesOnlyWhenRequested() {
    Map<String, MolecularProfile> expected =
        Collections.singletonMap("mp1", mock(MolecularProfile.class));
    when(cacheMapBuilder.buildMolecularProfileMap()).thenReturn(expected);

    Map<String, MolecularProfile> firstResult = cacheMapUtil.getMolecularProfileMap();
    Map<String, MolecularProfile> secondResult = cacheMapUtil.getMolecularProfileMap();

    assertSame(expected, firstResult);
    assertSame(expected, secondResult);
    verify(cacheMapBuilder, times(1)).buildMolecularProfileMap();
    verify(cacheMapBuilder, never()).buildSampleListMap();
    verify(cacheMapBuilder, never()).buildCancerStudyMap();
  }

  @Test
  public void getSampleListMapInitializesOnlyWhenRequested() {
    Map<String, SampleList> expected = Collections.singletonMap("sl1", mock(SampleList.class));
    when(cacheMapBuilder.buildSampleListMap()).thenReturn(expected);

    Map<String, SampleList> firstResult = cacheMapUtil.getSampleListMap();
    Map<String, SampleList> secondResult = cacheMapUtil.getSampleListMap();

    assertSame(expected, firstResult);
    assertSame(expected, secondResult);
    verify(cacheMapBuilder, times(1)).buildSampleListMap();
    verify(cacheMapBuilder, never()).buildMolecularProfileMap();
    verify(cacheMapBuilder, never()).buildCancerStudyMap();
  }

  @Test
  public void getCancerStudyMapInitializesOnlyWhenRequested() {
    Map<String, CancerStudy> expected = Collections.singletonMap("study1", mock(CancerStudy.class));
    when(cacheMapBuilder.buildCancerStudyMap()).thenReturn(expected);

    Map<String, CancerStudy> firstResult = cacheMapUtil.getCancerStudyMap();
    Map<String, CancerStudy> secondResult = cacheMapUtil.getCancerStudyMap();

    assertSame(expected, firstResult);
    assertSame(expected, secondResult);
    verify(cacheMapBuilder, times(1)).buildCancerStudyMap();
    verify(cacheMapBuilder, never()).buildMolecularProfileMap();
    verify(cacheMapBuilder, never()).buildSampleListMap();
  }

  @Test
  public void initializeCacheMemoryLoadsAllMaps() {
    when(cacheMapBuilder.buildMolecularProfileMap())
        .thenReturn(Collections.singletonMap("mp1", mock(MolecularProfile.class)));
    when(cacheMapBuilder.buildSampleListMap())
        .thenReturn(Collections.singletonMap("sl1", mock(SampleList.class)));
    when(cacheMapBuilder.buildCancerStudyMap())
        .thenReturn(Collections.singletonMap("study1", mock(CancerStudy.class)));

    cacheMapUtil.initializeCacheMemory();

    verify(cacheMapBuilder, times(1)).buildMolecularProfileMap();
    verify(cacheMapBuilder, times(1)).buildSampleListMap();
    verify(cacheMapBuilder, times(1)).buildCancerStudyMap();
  }
}
