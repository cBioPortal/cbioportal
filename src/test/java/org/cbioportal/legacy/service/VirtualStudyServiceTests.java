package org.cbioportal.legacy.service;

import static org.cbioportal.legacy.service.VirtualStudyService.ALL_USERS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.legacy.service.impl.VirtualStudyServiceImpl;
import org.cbioportal.legacy.service.util.SessionServiceRequestHandler;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;
import org.cbioportal.legacy.web.parameter.VirtualStudySamples;
import org.cbioportal.legacy.web.util.StudyViewFilterApplier;
import org.junit.Test;

public class VirtualStudyServiceTests {
  final StudyViewFilterApplier studyViewFilterApplier = mock(StudyViewFilterApplier.class);
  final SessionServiceRequestHandler sessionServiceRequestHandler =
      mock(SessionServiceRequestHandler.class);
  final CancerTypeService cancerTypeService = mock(CancerTypeService.class);
  final SampleService sampleService = mock(SampleService.class);
  final VirtualStudyService testee =
      new VirtualStudyServiceImpl(
          sampleService, cancerTypeService, sessionServiceRequestHandler, studyViewFilterApplier);

  @Test
  public void testGetPublishedVirtualStudies() {
    VirtualStudy staticVirtualStudy = new VirtualStudy();
    VirtualStudyData staticVirtualStudyData = new VirtualStudyData();
    staticVirtualStudyData.setName("Static Virtual Study");
    VirtualStudySamples staticVirtualStudySamples = new VirtualStudySamples();
    staticVirtualStudySamples.setId("static-virtual-study");
    staticVirtualStudySamples.setSamples(Set.of("sample1", "sample2"));
    staticVirtualStudyData.setStudies(Set.of(staticVirtualStudySamples));
    staticVirtualStudyData.setDynamic(false);
    staticVirtualStudy.setData(staticVirtualStudyData);

    VirtualStudy dynamicVirtualStudy = new VirtualStudy();
    VirtualStudyData dynamicVirtualStudyData = new VirtualStudyData();
    dynamicVirtualStudyData.setName("Dynamic Virtual Study");
    VirtualStudySamples dynamicVirtualStudySamples = new VirtualStudySamples();
    dynamicVirtualStudySamples.setId("dynamic-virtual-study");
    dynamicVirtualStudySamples.setSamples(Set.of("sample3", "sample4"));
    dynamicVirtualStudyData.setStudies(Set.of(dynamicVirtualStudySamples));
    dynamicVirtualStudyData.setDynamic(true);
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of("dynamic-virtual-study"));
    dynamicVirtualStudyData.setStudyViewFilter(studyViewFilter);
    dynamicVirtualStudy.setData(dynamicVirtualStudyData);

    doReturn(List.of(staticVirtualStudy, dynamicVirtualStudy))
        .when(sessionServiceRequestHandler)
        .getVirtualStudiesAccessibleToUser(ALL_USERS);
    SampleIdentifier existingSampleIdentifier = new SampleIdentifier();
    existingSampleIdentifier.setSampleId("sample4");
    existingSampleIdentifier.setStudyId("dynamic-virtual-study");
    SampleIdentifier newSampleIdentifier = new SampleIdentifier();
    newSampleIdentifier.setSampleId("sample5");
    newSampleIdentifier.setStudyId("dynamic-virtual-study");
    doReturn(List.of(existingSampleIdentifier, newSampleIdentifier))
        .when(studyViewFilterApplier)
        .apply(any(StudyViewFilter.class));

    List<VirtualStudy> virtualStudies = testee.getPublishedVirtualStudies();

    verify(sessionServiceRequestHandler).getVirtualStudiesAccessibleToUser(ALL_USERS);
    verify(studyViewFilterApplier).apply(any(StudyViewFilter.class));
    assertNotNull(virtualStudies);
    assertEquals(
        Set.of("Static Virtual Study", "Dynamic Virtual Study"),
        virtualStudies.stream().map(vs -> vs.getData().getName()).collect(Collectors.toSet()));
    assertEquals(
        Set.of("sample1", "sample2"),
        virtualStudies.get(0).getData().getStudies().iterator().next().getSamples());
    assertEquals(
        Set.of("sample4", "sample5"),
        virtualStudies.get(1).getData().getStudies().iterator().next().getSamples());
  }

  @Test
  public void testGetPublishedVirtualStudiesKeywords() {
    // will be filtered out since it does not contain the keyword neither in name nor in description
    VirtualStudy staticVirtualStudy1 = new VirtualStudy();
    VirtualStudyData staticVirtualStudyData1 = new VirtualStudyData();
    staticVirtualStudyData1.setName("Static Virtual Study");
    staticVirtualStudyData1.setStudies(Set.of());
    staticVirtualStudyData1.setDynamic(false);
    staticVirtualStudy1.setData(staticVirtualStudyData1);

    VirtualStudy dynamicVirtualStudy1 = new VirtualStudy();
    VirtualStudyData dynamicVirtualStudyData1 = new VirtualStudyData();
    dynamicVirtualStudyData1.setName("Dynamic Virtual KeYwOrD Study");
    dynamicVirtualStudyData1.setStudies(Set.of());
    dynamicVirtualStudyData1.setDynamic(true);
    dynamicVirtualStudy1.setData(dynamicVirtualStudyData1);

    VirtualStudy staticVirtualStudy2 = new VirtualStudy();
    VirtualStudyData staticVirtualStudyData2 = new VirtualStudyData();
    staticVirtualStudyData2.setName("Static Virtual Study 2");
    staticVirtualStudyData2.setDescription("* KEYWORD *");
    staticVirtualStudyData2.setStudies(Set.of());
    staticVirtualStudyData2.setDynamic(false);
    staticVirtualStudy2.setData(staticVirtualStudyData2);

    // will be filtered out since it does not contain the keyword neither in name nor in description
    VirtualStudy dynamicVirtualStudy2 = new VirtualStudy();
    VirtualStudyData dynamicVirtualStudyData2 = new VirtualStudyData();
    dynamicVirtualStudyData2.setName("Dynamic Virtual Study 2");
    dynamicVirtualStudyData2.setStudies(Set.of());
    dynamicVirtualStudyData2.setDynamic(true);
    dynamicVirtualStudy2.setData(dynamicVirtualStudyData2);

    doReturn(
            List.of(
                staticVirtualStudy1,
                dynamicVirtualStudy1,
                staticVirtualStudy2,
                dynamicVirtualStudy2))
        .when(sessionServiceRequestHandler)
        .getVirtualStudiesAccessibleToUser(ALL_USERS);

    List<VirtualStudy> virtualStudies = testee.getPublishedVirtualStudies("kEyWoRd");

    assertNotNull(virtualStudies);
    assertEquals(
        Set.of("Dynamic Virtual KeYwOrD Study", "Static Virtual Study 2"),
        virtualStudies.stream().map(vs -> vs.getData().getName()).collect(Collectors.toSet()));
  }
}
