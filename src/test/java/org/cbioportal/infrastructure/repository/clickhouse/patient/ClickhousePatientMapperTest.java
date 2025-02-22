package org.cbioportal.infrastructure.repository.clickhouse.patient;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cbioportal.domain.studyview.StudyViewFilterFactory;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.service.util.StudyViewColumnarServiceUtil;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhousePatientMapperTest {

  private static final String STUDY_TCGA_PUB = "study_tcga_pub";
  private static final String STUDY_ACC_TCGA = "acc_tcga";

  @Autowired private ClickhousePatientMapper mapper;

  @Test
  public void getMolecularProfileCounts() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

    var caseList = new ArrayList<String>(Arrays.asList("pub_cna"));
    var caseListGroups = new ArrayList(Arrays.asList(caseList));

    studyViewFilter.setCaseLists(caseListGroups);

    var sampleListCounts =
        mapper.getCaseListDataCounts(
            StudyViewFilterFactory.make(
                studyViewFilter, null, studyViewFilter.getStudyIds(), null));

    var size =
        sampleListCounts.stream()
            .filter(gc -> gc.getValue().equals("mrna"))
            .findFirst()
            .get()
            .getCount()
            .intValue();
    assertEquals(7, size);
  }

  @Test
  public void getMolecularProfileCountsMultipleListsOr() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

    var caseList = new ArrayList<String>(Arrays.asList("mrna", "pub_cna"));
    var caseListGroups = new ArrayList(Arrays.asList(caseList));

    studyViewFilter.setCaseLists(caseListGroups);

    var sampleListCounts =
        mapper.getCaseListDataCounts(
            StudyViewFilterFactory.make(
                studyViewFilter, null, studyViewFilter.getStudyIds(), null));

    var size =
        sampleListCounts.stream()
            .filter(gc -> gc.getValue().equals("mrna"))
            .findFirst()
            .get()
            .getCount()
            .intValue();
    assertEquals(8, size);
  }

  @Test
  public void getMolecularProfileCountsMultipleListsAnd() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

    var caseList1 = new ArrayList<String>(Arrays.asList("mrna"));
    var caseList2 = new ArrayList<String>(Arrays.asList("pub_cna"));
    var caseListGroups = new ArrayList(Arrays.asList(caseList1, caseList2));

    studyViewFilter.setCaseLists(caseListGroups);

    var sampleListCounts =
        mapper.getCaseListDataCounts(
            StudyViewFilterFactory.make(
                studyViewFilter, null, studyViewFilter.getStudyIds(), null));

    var size =
        sampleListCounts.stream()
            .filter(gc -> gc.getValue().equals("mrna"))
            .findFirst()
            .get()
            .getCount()
            .intValue();
    assertEquals(7, size);
  }

  @Test
  public void getMolecularProfileCountsAcrossStudies() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB, STUDY_ACC_TCGA));

    var caseList1 = new ArrayList<String>(Arrays.asList("all"));
    var caseListGroups = new ArrayList(Arrays.asList(caseList1));

    studyViewFilter.setCaseLists(caseListGroups);

    var unMergedCounts =
        mapper.getCaseListDataCounts(
            StudyViewFilterFactory.make(
                studyViewFilter, null, studyViewFilter.getStudyIds(), null));

    var caseListCountsMerged = StudyViewColumnarServiceUtil.mergeCaseListCounts(unMergedCounts);

    var sizeUnmerged =
        unMergedCounts.stream()
            .filter(gc -> gc.getValue().equals("all"))
            .findFirst()
            .get()
            .getCount()
            .intValue();
    assertEquals(14, sizeUnmerged);

    // now we've combined the "all" from the two studies
    var sizeMerged =
        caseListCountsMerged.stream()
            .filter(gc -> gc.getValue().equals("all"))
            .findFirst()
            .get()
            .getCount()
            .intValue();
    assertEquals(15, sizeMerged);
  }
}
