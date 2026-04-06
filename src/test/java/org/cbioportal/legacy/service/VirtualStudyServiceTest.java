package org.cbioportal.legacy.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.legacy.service.exception.InvalidVirtualStudyDataException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.service.impl.BaseServiceImplTest;
import org.cbioportal.legacy.service.util.SessionServiceRequestHandler;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;
import org.cbioportal.legacy.web.parameter.VirtualStudySamples;
import org.cbioportal.legacy.web.util.StudyViewFilterApplier;
import org.cbioportal.legacy.web.validation.VirtualStudyValidationMessages;
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
  @Mock CancerTypeService cancerTypeService;
  @Mock StudyService studyService;

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

  @Test(expected = InvalidVirtualStudyDataException.class)
  public void publishVirtualStudyShouldRejectFilterErrors() {
    VirtualStudyData virtualStudyData = createPublishableVirtualStudyData();

    Mockito.when(studyViewFilterApplier.apply(virtualStudyData.getStudyViewFilter()))
        .thenThrow(new RuntimeException("boom"));

    try {
      virtualStudyService.publishVirtualStudy("virtual-study", null, null, virtualStudyData);
    } catch (InvalidVirtualStudyDataException e) {
      assertEquals(VirtualStudyValidationMessages.INVALID_FILTERS, e.getMessage());
      verify(sessionServiceRequestHandler, never()).createVirtualStudy(any(), any());
      throw e;
    }
  }

  @Test(expected = InvalidVirtualStudyDataException.class)
  public void publishVirtualStudyShouldRejectEmptyFilterResults() {
    VirtualStudyData virtualStudyData = createPublishableVirtualStudyData();

    Mockito.when(studyViewFilterApplier.apply(virtualStudyData.getStudyViewFilter()))
        .thenReturn(List.of());

    try {
      virtualStudyService.publishVirtualStudy("virtual-study", null, null, virtualStudyData);
    } catch (InvalidVirtualStudyDataException e) {
      assertEquals(VirtualStudyValidationMessages.NO_FILTER_RESULTS, e.getMessage());
      verify(sessionServiceRequestHandler, never()).createVirtualStudy(any(), any());
      throw e;
    }
  }

  @Test
  public void publishVirtualStudyShouldCreateStudyWhenFilterResolvesSamples() throws Exception {
    VirtualStudyData virtualStudyData = createPublishableVirtualStudyData();

    Mockito.when(studyViewFilterApplier.apply(virtualStudyData.getStudyViewFilter()))
        .thenReturn(List.of(sampleIdentifier1));
    Mockito.when(studyService.getStudy("virtual-study"))
        .thenThrow(new StudyNotFoundException("virtual-study"));

    virtualStudyService.publishVirtualStudy("virtual-study", null, null, virtualStudyData);

    verify(sessionServiceRequestHandler)
        .createVirtualStudy(eq("virtual-study"), eq(virtualStudyData));
  }

  private VirtualStudyData createPublishableVirtualStudyData() {
    VirtualStudyData virtualStudyData = new VirtualStudyData();
    virtualStudyData.setName("Test");
    virtualStudyData.setDynamic(true);

    VirtualStudySamples virtualStudySamples = new VirtualStudySamples();
    virtualStudySamples.setId("study_1");
    virtualStudyData.setStudies(Set.of(virtualStudySamples));

    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of("study_1"));
    virtualStudyData.setStudyViewFilter(studyViewFilter);

    return virtualStudyData;
  }
}
