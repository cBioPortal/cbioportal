package org.cbioportal.persistence.mybatisclickhouse;

import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.persistence.mybatisclickhouse.config.MyBatisConfig;

import org.cbioportal.web.parameter.CategorizedClinicalDataCountFilter;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class CaseListSamplesCountsTest extends AbstractTestcontainers {
    
    private static final String STUDY_TCGA_PUB = "study_tcga_pub";
    private static final String STUDY_ACC_TCGA = "acc_tcga";
    
    @Autowired
    private StudyViewMapper studyViewMapper;
    
    @Autowired
    private StudyViewMyBatisRepository studyViewMyBatisRepository;

    @Test
    public void getMolecularProfileCounts() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

        var caseList = new ArrayList<String>(Arrays.asList("pub_cna"));
        var caseListGroups = new ArrayList(Arrays.asList(caseList));

        studyViewFilter.setCaseLists(caseListGroups);
       
        var sampleListCounts = studyViewMapper.getCaseListDataCounts(studyViewFilter,
            CategorizedClinicalDataCountFilter.getBuilder().build(), false );

        var size = sampleListCounts.stream().filter(gc->gc.getValue().equals("mrna"))
            .findFirst().get().getCount().intValue();
        assertEquals(7, size);
        
    }

    @Test
    public void getMolecularProfileCountsMultipleListsOr() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

        var caseList = new ArrayList<String>(Arrays.asList("mrna","pub_cna"));
        var caseListGroups = new ArrayList(Arrays.asList(caseList));

        studyViewFilter.setCaseLists(caseListGroups);

        var sampleListCounts = studyViewMapper.getCaseListDataCounts(studyViewFilter,
            CategorizedClinicalDataCountFilter.getBuilder().build(), false );

        var size = sampleListCounts.stream().filter(gc->gc.getValue().equals("mrna"))
            .findFirst().get().getCount().intValue();
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

        var sampleListCounts = studyViewMapper.getCaseListDataCounts(studyViewFilter,
            CategorizedClinicalDataCountFilter.getBuilder().build(), false );

        var size = sampleListCounts.stream().filter(gc->gc.getValue().equals("mrna"))
            .findFirst().get().getCount().intValue();
        assertEquals(7, size);

    }

    @Test
    public void getMolecularProfileCountsAcrossStudies() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB, STUDY_ACC_TCGA));

        var caseList1 = new ArrayList<String>(Arrays.asList("all"));
        var caseListGroups = new ArrayList(Arrays.asList(caseList1));

        studyViewFilter.setCaseLists(caseListGroups);

        var sampleListCounts = studyViewMyBatisRepository.getCaseListDataCounts(studyViewFilter);

        var size = sampleListCounts.stream().filter(gc->gc.getValue().equals("all"))
            .findFirst().get().getCount().intValue();
        assertEquals(15, size);

    }
    
    

//    @Test
//    public void getMolecularProfileCountsMultipleStudies() {
//        StudyViewFilter studyViewFilter = new StudyViewFilter();
//        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB, STUDY_ACC_TCGA));
//
//        var profiles = new ArrayList<String>(Arrays.asList("mutations"));
//        var profileGroups = new ArrayList<List<String>>(Arrays.asList(profiles));
//
//        studyViewFilter.setGenomicProfiles(profileGroups);
//
//        var molecularProfileCounts = studyViewMapper.getMolecularProfileSampleCounts(studyViewFilter,
//            CategorizedClinicalDataCountFilter.getBuilder().build(), false );
//
//        var size = molecularProfileCounts.stream().filter(gc->gc.getValue().equals("mutations"))
//            .findFirst().get().getCount().intValue();
//        assertEquals(10, size);
//
//    }
//
//    @Test
//    public void getMolecularProfileCountsMultipleProfilesUnion() {
//        StudyViewFilter studyViewFilter = new StudyViewFilter();
//        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));
//
//        var profiles = new ArrayList<String>(Arrays.asList("mutations","mrna"));
//        var profileGroups = new ArrayList<List<String>>(Arrays.asList(profiles));
//
//        studyViewFilter.setGenomicProfiles(profileGroups);
//
//        var molecularProfileCounts = studyViewMapper.getMolecularProfileSampleCounts(studyViewFilter,
//            CategorizedClinicalDataCountFilter.getBuilder().build(), false );
//
//        var sizeMutations = molecularProfileCounts.stream().filter(gc->gc.getValue().equals("mutations"))
//            .findFirst().get().getCount().intValue();
//        assertEquals(10, sizeMutations);
//
//        var sizeMrna = molecularProfileCounts.stream().filter(gc->gc.getValue().equals("mrna"))
//            .findFirst().get().getCount().intValue();
//        assertEquals(9, sizeMrna);
//
//    }
//
//    @Test
//    public void getMolecularProfileCountsMultipleProfilesIntersect() {
//        StudyViewFilter studyViewFilter = new StudyViewFilter();
//        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));
//
//        var profile1 = new ArrayList<String>(Arrays.asList("mutations"));
//        var profile2 = new ArrayList<String>(Arrays.asList("mrna"));
//        var profileGroups = new ArrayList<List<String>>(Arrays.asList(profile1, profile2));
//
//        studyViewFilter.setGenomicProfiles(profileGroups);
//
//        var molecularProfileCounts = studyViewMapper.getMolecularProfileSampleCounts(studyViewFilter,
//            CategorizedClinicalDataCountFilter.getBuilder().build(), false );
//
//        var sizeMutations = molecularProfileCounts.stream().filter(gc->gc.getValue().equals("mutations"))
//            .findFirst().get().getCount().intValue();
//        assertEquals(9, sizeMutations);
//
//
//
//    }
//   
//   
   

}