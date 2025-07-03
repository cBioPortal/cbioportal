package org.cbioportal.legacy.persistence.virtualstudy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.persistence.SampleRepository;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;
import org.cbioportal.legacy.web.parameter.VirtualStudySamples;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
// FIXME: changed implementation of SampleRepository, so this test needs to be updated
public class VSAwareSampleRepositoryTests {
  final VirtualStudyService virtualStudyService = mock(VirtualStudyService.class);
  final SampleRepository sampleRepository = mock(SampleRepository.class);
  final VSAwareSampleRepository testee =
      new VSAwareSampleRepository(virtualStudyService, sampleRepository);

  @Test
  public void testFetchSamples_materialised_studies_only() {
    Sample sample = new Sample();
    sample.setStableId("SAMPLE1");
    List<Sample> returnSamples = List.of(sample);
    when(sampleRepository.fetchSamples(List.of("STUDY1"), List.of("SAMPLE1"), "ID"))
        .thenReturn(returnSamples);
    VirtualStudy vs = mock(VirtualStudy.class);
    doReturn("VS1").when(vs).getId();
    doReturn(List.of(vs)).when(virtualStudyService).getPublishedVirtualStudies();

    List<Sample> returnedSamples = testee.fetchSamples(List.of("STUDY1"), List.of("SAMPLE1"), "ID");

    assertEquals(returnSamples, returnedSamples);
  }

  @Test
  public void testFetchSamples_virtual_studies_only() {
    Sample sample = new Sample();
    sample.setInternalId(1);
    sample.setStableId("SAMPLE1");
    sample.setSampleType(Sample.SampleType.METASTATIC);
    sample.setPatientId(2);
    sample.setPatientStableId("PATIENT1");
    sample.setCancerStudyIdentifier("STUDY1");
    List<Sample> returnSamples = List.of(sample);
    when(sampleRepository.fetchSamples(List.of("STUDY1"), List.of("SAMPLE1"), "ID"))
        .thenReturn(returnSamples);
    VirtualStudy vs = spy(new VirtualStudy());
    VirtualStudyData vsd = new VirtualStudyData();
    VirtualStudySamples vss1 = new VirtualStudySamples();
    vss1.setId("STUDY1");
    vss1.setSamples(Set.of("SAMPLE1"));
    vsd.setStudies(Set.of(vss1));
    vs.setData(vsd);
    doReturn("VS1").when(vs).getId();
    doReturn(List.of(vs)).when(virtualStudyService).getPublishedVirtualStudies();

    List<Sample> returnedSamples =
        testee.fetchSamples(List.of("VS1"), List.of("STUDY1_SAMPLE1"), "ID");
    assertNotNull(returnedSamples);
    assertEquals(1, returnedSamples.size());
    Sample returnedSample = returnedSamples.getFirst();
    assertNotNull(returnedSample);
    assertNull(
        returnedSample
            .getInternalId()); // We don't preserve the internal Ids in the virtual study to fail if
    // they are used for virtual studies
    assertEquals("STUDY1_SAMPLE1", returnedSample.getStableId());
    assertEquals(Sample.SampleType.METASTATIC, returnedSample.getSampleType());
    assertNull(
        returnedSample
            .getPatientId()); // We don't preserve the internal Ids in the virtual study to fail if
    // they are used for virtual studies
    assertEquals("STUDY1_PATIENT1", returnedSample.getPatientStableId());
  }
}
