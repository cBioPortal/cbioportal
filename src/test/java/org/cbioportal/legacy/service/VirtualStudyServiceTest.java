package org.cbioportal.legacy.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.legacy.service.impl.BaseServiceImplTest;
import org.cbioportal.legacy.service.util.SessionServiceRequestHandler;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;
import org.cbioportal.legacy.web.parameter.VirtualStudySamples;
import org.cbioportal.legacy.web.util.StudyViewFilterApplier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VirtualStudyServiceTest extends BaseServiceImplTest {

  @InjectMocks private VirtualStudyService virtualStudyService;

  @Mock SessionServiceRequestHandler sessionServiceRequestHandler;
  @Mock StudyViewFilterApplier studyViewFilterApplier;

  SampleIdentifier sampleIdentifier1 = new SampleIdentifier();

  {
    sampleIdentifier1.setStudyId("STUDY_1");
  }

  SampleIdentifier sampleIdentifier2 = new SampleIdentifier();

  {
    sampleIdentifier2.setStudyId("STUDY_2");
  }

  @Test
  public void testStaticVirtualStudy() throws Exception {
    VirtualStudy response = new VirtualStudy();
    VirtualStudyData virtualStudyData = new VirtualStudyData();
    virtualStudyData.setDynamic(false);
    virtualStudyData.setName("Test");
    VirtualStudySamples vss = new VirtualStudySamples();
    vss.setId("STUDY_N");
    vss.setSamples(Set.of("S1", "S2"));
    virtualStudyData.setStudies(Set.of(vss));
    response.setData(virtualStudyData);
    Mockito.when(sessionServiceRequestHandler.getVirtualStudyById("123")).thenReturn(response);

    VirtualStudy virtualStudy = virtualStudyService.getVirtualStudy("123");
    assertNotNull(virtualStudy.getData());
    assertNotNull(virtualStudy.getData().getStudies());
    assertEquals(1, virtualStudy.getData().getStudies().size());
    assertEquals("STUDY_N", virtualStudy.getData().getStudies().iterator().next().getId());
  }

  @Test
  public void testDynamicVirtualStudy() throws Exception {
    VirtualStudy response = new VirtualStudy();
    VirtualStudyData virtualStudyData = new VirtualStudyData();
    virtualStudyData.setDynamic(true);
    virtualStudyData.setName("Test");
    virtualStudyData.setStudies(Set.of());
    response.setData(virtualStudyData);
    Mockito.when(sessionServiceRequestHandler.getVirtualStudyById("123")).thenReturn(response);

    Mockito.when(studyViewFilterApplier.apply(Mockito.any()))
        .thenReturn(List.of(sampleIdentifier1, sampleIdentifier2));

    VirtualStudy virtualStudy = virtualStudyService.getVirtualStudy("123");
    assertNotNull(virtualStudy.getData());
    assertNotNull(virtualStudy.getData().getStudies());
    assertEquals(2, virtualStudy.getData().getStudies().size());
    assertEquals(
        Set.of("STUDY_1", "STUDY_2"),
        virtualStudy.getData().getStudies().stream()
            .map(VirtualStudySamples::getId)
            .collect(Collectors.toSet()));
  }
}
