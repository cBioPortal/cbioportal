package org.cbioportal.persistence.mybatisclickhouse;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.MutationEventType;
import org.cbioportal.persistence.helper.AlterationFilterHelper;
import org.cbioportal.persistence.mybatisclickhouse.config.MyBatisConfig;
import org.cbioportal.web.parameter.CategorizedClinicalDataCountFilter;
import org.cbioportal.web.parameter.DataFilter;
import org.cbioportal.web.parameter.DataFilterValue;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class StudyViewMapperTest extends AbstractTestcontainers {
    
    private static final String STUDY_TCGA_PUB = "study_tcga_pub";
    private static final String STUDY_ACC_TCGA = "acc_tcga";
    
    @Autowired
    private StudyViewMapper studyViewMapper;
    
    @Test
    public void getFilteredSamples() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(STUDY_TCGA_PUB, STUDY_ACC_TCGA));
        var filteredSamples = studyViewMapper.getFilteredSamples(studyViewFilter,   CategorizedClinicalDataCountFilter.getBuilder().build(), false);
        assertEquals(19, filteredSamples.size());
    }

    @Test
    public void getMutatedGenes() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));
        var alterationCountByGenes = studyViewMapper.getMutatedGenes(studyViewFilter, 
            CategorizedClinicalDataCountFilter.getBuilder().build(), false, 
            AlterationFilterHelper.build(studyViewFilter.getAlterationFilter()));
        assertEquals(3, alterationCountByGenes.size());
        
        var testBrca1AlterationCount = alterationCountByGenes.stream().filter(a -> Objects.equals(a.getHugoGeneSymbol(), "brca1")).findFirst();
        assert(testBrca1AlterationCount.isPresent());
        assertEquals(Integer.valueOf(5), testBrca1AlterationCount.get().getTotalCount());
    } 
    
    @Test
    public void getMutatedGenesWithAlterationFilter() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

        // Create AlterationFilter
        AlterationFilter alterationFilter = new AlterationFilter();
        Map<MutationEventType, Boolean> mutationEventTypeFilterMap = new HashMap<>();
        mutationEventTypeFilterMap.put(MutationEventType.nonsense_mutation, Boolean.TRUE);
        mutationEventTypeFilterMap.put(MutationEventType.other, Boolean.FALSE);
        alterationFilter.setMutationEventTypes(mutationEventTypeFilterMap);
        
        var alterationCountByGenes = studyViewMapper.getMutatedGenes(studyViewFilter,
            CategorizedClinicalDataCountFilter.getBuilder().build(), false,
            AlterationFilterHelper.build(alterationFilter));
        assertEquals(2, alterationCountByGenes.size()); 
        
        AlterationFilter onlyMutationStatusFilter = new AlterationFilter();
        onlyMutationStatusFilter.setMutationEventTypes(new HashMap<>());
        onlyMutationStatusFilter.setIncludeGermline(false);
        onlyMutationStatusFilter.setIncludeSomatic(false);
        onlyMutationStatusFilter.setIncludeUnknownStatus(true);
        
        var alterationCountByGenes1 = studyViewMapper.getMutatedGenes(studyViewFilter,
            CategorizedClinicalDataCountFilter.getBuilder().build(), false,
            AlterationFilterHelper.build(onlyMutationStatusFilter));
        assertEquals(1, alterationCountByGenes1.size());

        AlterationFilter mutationTypeAndStatusFilter = new AlterationFilter();
        mutationTypeAndStatusFilter.setMutationEventTypes(mutationEventTypeFilterMap);
        mutationTypeAndStatusFilter.setMutationEventTypes(new HashMap<>());
        mutationTypeAndStatusFilter.setIncludeGermline(false);
        mutationTypeAndStatusFilter.setIncludeSomatic(false);
        mutationTypeAndStatusFilter.setIncludeUnknownStatus(true);

        var alterationCountByGenes2 = studyViewMapper.getMutatedGenes(studyViewFilter,
            CategorizedClinicalDataCountFilter.getBuilder().build(), false,
            AlterationFilterHelper.build(onlyMutationStatusFilter));
        assertEquals(1, alterationCountByGenes2.size()); 
    }
    
   @Test
   public void getTotalProfiledCountsByGene() {
       StudyViewFilter studyViewFilter = new StudyViewFilter();
       studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));
       
       var totalProfiledCountsMap = studyViewMapper.getTotalProfiledCounts(studyViewFilter,
           CategorizedClinicalDataCountFilter.getBuilder().build(), false,
           "MUTATION_EXTENDED");
       
       assertEquals(3, totalProfiledCountsMap.size());
       
       var akt2TotalProfiledCounts = totalProfiledCountsMap.stream().filter(c -> c.getHugoGeneSymbol().equals("akt2")).findFirst();
       assertTrue(akt2TotalProfiledCounts.isPresent());
       assertEquals(4, akt2TotalProfiledCounts.get().getNumberOfProfiledCases().intValue());
   } 
   
   @Test
    public void getClinicalEventTypeCounts() {
       StudyViewFilter studyViewFilter = new StudyViewFilter();
       studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

       var clinicalEventTypeCounts = studyViewMapper.getClinicalEventTypeCounts(studyViewFilter,
           CategorizedClinicalDataCountFilter.getBuilder().build(), false);

       assertEquals(4, clinicalEventTypeCounts.size());
       
       var clinicalEventTypeCountOptional = clinicalEventTypeCounts.stream().filter(ce -> ce.getEventType().equals("treatment"))
           .findFirst();
       
       assertTrue(clinicalEventTypeCountOptional.isPresent());
       assertEquals(1, clinicalEventTypeCountOptional.get().getCount().intValue());
        
       DataFilter dataFilter = new DataFilter();
       DataFilterValue dataFilterValue = new DataFilterValue();
       dataFilterValue.setValue("treatment");
       dataFilter.setValues(List.of(dataFilterValue));
       studyViewFilter.setClinicalEventFilters(List.of(dataFilter));

       clinicalEventTypeCounts = studyViewMapper.getClinicalEventTypeCounts(studyViewFilter,
           CategorizedClinicalDataCountFilter.getBuilder().build(), true);
       
       assertEquals(2, clinicalEventTypeCounts.size());
       
       clinicalEventTypeCountOptional = clinicalEventTypeCounts.stream().filter(ce -> ce.getEventType().equals("status"))
           .findFirst();

       assertFalse(clinicalEventTypeCountOptional.isPresent());
   }

}