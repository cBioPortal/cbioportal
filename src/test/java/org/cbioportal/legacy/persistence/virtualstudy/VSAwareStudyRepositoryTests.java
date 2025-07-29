package org.cbioportal.legacy.persistence.virtualstudy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.persistence.CancerTypeRepository;
import org.cbioportal.legacy.persistence.SampleRepository;
import org.cbioportal.legacy.persistence.StudyRepository;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;
import org.junit.Test;
import org.springframework.cache.Cache;

public class VSAwareStudyRepositoryTests {

  final VirtualizationService virtualStudyService =
      spy(
          new VirtualizationService(
              mock(Cache.class),
              mock(VirtualStudyService.class),
              mock(SampleRepository.class),
              mock(VSAwareMolecularProfileRepository.class)));
  final StudyRepository studyRepository = mock(StudyRepository.class);
  final CancerTypeRepository cancerTypeRepository = mock(CancerTypeRepository.class);
  final VSAwareStudyRepository testee =
      new VSAwareStudyRepository(virtualStudyService, studyRepository, cancerTypeRepository);

  @Test
  public void testGetAllStudiesMaterialisedAndVirtualCombined() {
    var cancerStudy1 = new CancerStudy();
    cancerStudy1.setName("Study 1");

    var cancerStudy2 = new CancerStudy();
    cancerStudy2.setName("Study 2");

    doReturn(List.of(cancerStudy1, cancerStudy2))
        .when(studyRepository)
        .getAllStudies(null, Projection.DETAILED.toString(), null, null, null, null);

    var virtualStudy1 = new VirtualStudy();
    var virtualStudyData1 = new VirtualStudyData();
    virtualStudyData1.setName("Virtual Study 1");
    virtualStudyData1.setStudies(Set.of());
    virtualStudy1.setData(virtualStudyData1);

    var virtualStudy2 = new VirtualStudy();
    var virtualStudyData2 = new VirtualStudyData();
    virtualStudyData2.setName("Virtual Study 2");
    virtualStudyData2.setStudies(Set.of());
    virtualStudy2.setData(virtualStudyData2);

    doReturn(List.of(virtualStudy1, virtualStudy2))
        .when(virtualStudyService)
        .getPublishedVirtualStudies();

    List<CancerStudy> result =
        testee.getAllStudies(null, Projection.DETAILED.toString(), null, null, null, null);

    verify(virtualStudyService).getPublishedVirtualStudies();
    verify(studyRepository)
        .getAllStudies(null, Projection.DETAILED.toString(), null, null, null, null);

    assertNotNull(result);
    // note the order of the studies. First the materialised studies, then the virtual studies in
    // the read order
    assertEquals(
        List.of("Study 1", "Study 2", "Virtual Study 1", "Virtual Study 2"),
        result.stream().map(CancerStudy::getName).collect(Collectors.toList()));
  }

  @Test
  public void testGetAllStudiesKeywordFiltering() {
    var cancerStudy1 = new CancerStudy();
    String keyword = "test keyword";
    cancerStudy1.setName("Study 1");

    doReturn(List.of(cancerStudy1))
        .when(studyRepository)
        .getAllStudies(keyword, Projection.DETAILED.toString(), null, null, null, null);

    var virtualStudy1 = new VirtualStudy();
    var virtualStudyData1 = new VirtualStudyData();
    virtualStudyData1.setName("Virtual Study 1");
    virtualStudyData1.setStudies(Set.of());
    virtualStudy1.setData(virtualStudyData1);

    doReturn(List.of(virtualStudy1)).when(virtualStudyService).getPublishedVirtualStudies();

    List<CancerStudy> result =
        testee.getAllStudies(keyword, Projection.DETAILED.toString(), null, null, null, null);

    verify(virtualStudyService).getPublishedVirtualStudies();
    verify(studyRepository)
        .getAllStudies(keyword, Projection.DETAILED.toString(), null, null, null, null);

    assertNotNull(result);
    assertEquals(
        List.of("Study 1", "Virtual Study 1"),
        result.stream().map(CancerStudy::getName).collect(Collectors.toList()));
  }

  @Test
  public void testGetAllStudiesSortingByNameAscended() {
    var cancerStudy1 = new CancerStudy();
    cancerStudy1.setName("A");

    var cancerStudy2 = new CancerStudy();
    cancerStudy2.setName("C");

    doReturn(List.of(cancerStudy1, cancerStudy2))
        .when(studyRepository)
        .getAllStudies(null, Projection.DETAILED.toString(), null, null, null, null);

    var virtualStudy1 = new VirtualStudy();
    var virtualStudyData1 = new VirtualStudyData();
    virtualStudyData1.setName("B");
    virtualStudyData1.setStudies(Set.of());
    virtualStudy1.setData(virtualStudyData1);

    var virtualStudy2 = new VirtualStudy();
    var virtualStudyData2 = new VirtualStudyData();
    virtualStudyData2.setName("D");
    virtualStudyData2.setStudies(Set.of());
    virtualStudy2.setData(virtualStudyData2);

    doReturn(List.of(virtualStudy2, virtualStudy1))
        .when(virtualStudyService)
        .getPublishedVirtualStudies();

    List<CancerStudy> result =
        testee.getAllStudies(null, Projection.DETAILED.toString(), null, null, "name", "asc");

    verify(virtualStudyService).getPublishedVirtualStudies();
    verify(studyRepository)
        .getAllStudies(null, Projection.DETAILED.toString(), null, null, null, null);

    assertNotNull(result);
    assertEquals(
        List.of("A", "B", "C", "D"),
        result.stream().map(CancerStudy::getName).collect(Collectors.toList()));
  }

  @Test
  public void testGetAllStudiesSortingByNameDescended() {
    var cancerStudy1 = new CancerStudy();
    cancerStudy1.setName("A");

    var cancerStudy2 = new CancerStudy();
    cancerStudy2.setName("C");

    doReturn(List.of(cancerStudy1, cancerStudy2))
        .when(studyRepository)
        .getAllStudies(null, Projection.DETAILED.toString(), null, null, null, null);

    var virtualStudy1 = new VirtualStudy();
    var virtualStudyData1 = new VirtualStudyData();
    virtualStudyData1.setName("B");
    virtualStudyData1.setStudies(Set.of());
    virtualStudy1.setData(virtualStudyData1);

    var virtualStudy2 = new VirtualStudy();
    var virtualStudyData2 = new VirtualStudyData();
    virtualStudyData2.setName("D");
    virtualStudyData2.setStudies(Set.of());
    virtualStudy2.setData(virtualStudyData2);

    doReturn(List.of(virtualStudy2, virtualStudy1))
        .when(virtualStudyService)
        .getPublishedVirtualStudies();

    List<CancerStudy> result =
        testee.getAllStudies(null, Projection.DETAILED.toString(), null, null, "name", "desc");

    verify(virtualStudyService).getPublishedVirtualStudies();
    verify(studyRepository)
        .getAllStudies(null, Projection.DETAILED.toString(), null, null, null, null);

    assertNotNull(result);
    assertEquals(
        List.of("D", "C", "B", "A"),
        result.stream().map(CancerStudy::getName).collect(Collectors.toList()));
  }

  @Test
  public void testGetAllStudiesPagination() {
    var cancerStudy1 = new CancerStudy();
    cancerStudy1.setName("A");

    var cancerStudy2 = new CancerStudy();
    cancerStudy2.setName("C");

    doReturn(List.of(cancerStudy1, cancerStudy2))
        .when(studyRepository)
        .getAllStudies(null, Projection.DETAILED.toString(), null, null, null, null);

    var virtualStudy1 = new VirtualStudy();
    var virtualStudyData1 = new VirtualStudyData();
    virtualStudyData1.setName("B");
    virtualStudyData1.setStudies(Set.of());
    virtualStudy1.setData(virtualStudyData1);

    var virtualStudy2 = new VirtualStudy();
    var virtualStudyData2 = new VirtualStudyData();
    virtualStudyData2.setName("D");
    virtualStudyData2.setStudies(Set.of());
    virtualStudy2.setData(virtualStudyData2);

    doReturn(List.of(virtualStudy1, virtualStudy2))
        .when(virtualStudyService)
        .getPublishedVirtualStudies();

    List<CancerStudy> result =
        testee.getAllStudies(null, Projection.DETAILED.toString(), 2, 1, null, null);

    verify(virtualStudyService).getPublishedVirtualStudies();
    verify(studyRepository)
        .getAllStudies(null, Projection.DETAILED.toString(), null, null, null, null);

    assertNotNull(result);
    assertEquals(
        List.of("B", "D"), result.stream().map(CancerStudy::getName).collect(Collectors.toList()));
  }
}
