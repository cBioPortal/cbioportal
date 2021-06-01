package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationDriverAnnotation;
import org.cbioportal.model.CustomDriverAnnotationReport;
import org.cbioportal.persistence.AlterationDriverAnnotationRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AlterationDriverAnnotationServiceImplTest {

    @InjectMocks
    private AlterationDriverAnnotationServiceImpl alterationDriverAnnotationService;
    @Mock
    private AlterationDriverAnnotationRepository alterationDriverAnnotationRepository;
    private AlterationDriverAnnotation alterationDriverAnnotation1;
    private AlterationDriverAnnotation alterationDriverAnnotation2;

    @Before
    public void setUp() throws Exception {
        alterationDriverAnnotation1 = new AlterationDriverAnnotation();
        alterationDriverAnnotation2 = new AlterationDriverAnnotation();
    }

    @Test
    public void getCustomDriverAnnotationProps() {

        alterationDriverAnnotation1.setDriverFilter("Putative_Driver");
        alterationDriverAnnotation1.setDriverTiersFilter("Class1");
        alterationDriverAnnotation2.setDriverFilter("Putative_Passenger");
        alterationDriverAnnotation2.setDriverTiersFilter("Class2");
        List<AlterationDriverAnnotation> annotationList = Arrays.asList(alterationDriverAnnotation1, alterationDriverAnnotation2);
        when(alterationDriverAnnotationRepository.getAlterationDriverAnnotations(isNull())).thenReturn(annotationList);

        CustomDriverAnnotationReport props = alterationDriverAnnotationService.getCustomDriverAnnotationProps(null);

        Assert.assertTrue(props.getHasBinary());
        Assert.assertTrue(props.getTiers().containsAll(Arrays.asList("Class1", "Class2")));
        
    }
    
    @Test
    public void getCustomDriverAnnotationPropsNoFilter() {
        alterationDriverAnnotation1.setDriverFilter("Filter1");
        alterationDriverAnnotation1.setDriverTiersFilter("Class1");
        alterationDriverAnnotation2.setDriverFilter("Filter2");
        alterationDriverAnnotation2.setDriverTiersFilter("Class2");
        List<AlterationDriverAnnotation> annotationList = Arrays.asList(alterationDriverAnnotation1, alterationDriverAnnotation2);
        when(alterationDriverAnnotationRepository.getAlterationDriverAnnotations(isNull())).thenReturn(annotationList);

        CustomDriverAnnotationReport props = alterationDriverAnnotationService.getCustomDriverAnnotationProps(null);

        Assert.assertFalse(props.getHasBinary());
        Assert.assertTrue(props.getTiers().containsAll(Arrays.asList("Class1", "Class2")));
        
    }
    
    @Test
    public void getCustomDriverAnnotationPropsNoTiersFilter() {

        alterationDriverAnnotation1.setDriverFilter("Putative_Driver");
        alterationDriverAnnotation2.setDriverFilter("Putative_Passenger");
        List<AlterationDriverAnnotation> annotationList = Arrays.asList(alterationDriverAnnotation1, alterationDriverAnnotation2);
        when(alterationDriverAnnotationRepository.getAlterationDriverAnnotations(isNull())).thenReturn(annotationList);

        CustomDriverAnnotationReport props = alterationDriverAnnotationService.getCustomDriverAnnotationProps(null);

        Assert.assertTrue(props.getHasBinary());
        Assert.assertEquals(0, props.getTiers().size());
        
    }
    
    @Test
    public void getCustomDriverAnnotationPropsOneFilter() {

        alterationDriverAnnotation1.setDriverFilter("Putative_Driver");
        List<AlterationDriverAnnotation> annotationList = Arrays.asList(alterationDriverAnnotation1, alterationDriverAnnotation2);
        when(alterationDriverAnnotationRepository.getAlterationDriverAnnotations(isNull())).thenReturn(annotationList);

        CustomDriverAnnotationReport props = alterationDriverAnnotationService.getCustomDriverAnnotationProps(null);

        Assert.assertTrue(props.getHasBinary());
        Assert.assertEquals(0, props.getTiers().size());
        
    }
    
    @Test
    public void getCustomDriverAnnotationPropsNoFiltersAtAll() {

        List<AlterationDriverAnnotation> annotationList = Arrays.asList(alterationDriverAnnotation1, alterationDriverAnnotation2);
        when(alterationDriverAnnotationRepository.getAlterationDriverAnnotations(isNull())).thenReturn(annotationList);

        CustomDriverAnnotationReport props = alterationDriverAnnotationService.getCustomDriverAnnotationProps(null);

        Assert.assertFalse(props.getHasBinary());
        Assert.assertEquals(0, props.getTiers().size());
        
    }

    @Test
    public void getCustomDriverAnnotationPropsWithoutNATiers() {
        AlterationDriverAnnotation naTierAnnotation = new AlterationDriverAnnotation();
        naTierAnnotation.setDriverTiersFilter("NA");
        List<String> molecularProfileIds = Arrays.asList("test_1", "test_2");
        List<AlterationDriverAnnotation> annotationList = Arrays.asList(alterationDriverAnnotation1, naTierAnnotation);
        when(alterationDriverAnnotationRepository.getAlterationDriverAnnotations(molecularProfileIds))
            .thenReturn(annotationList);

        CustomDriverAnnotationReport props = alterationDriverAnnotationService
            .getCustomDriverAnnotationProps(molecularProfileIds);

        Assert.assertFalse("NA has to be removed from the set of tiers", props.getTiers().contains("NA"));
    }

    @Test
    public void getCustomDriverAnnotationPropsWithoutEmptyTiers() {
        AlterationDriverAnnotation emptyTierAnnotation = new AlterationDriverAnnotation();
        emptyTierAnnotation.setDriverTiersFilter("");
        List<String> molecularProfileIds = Arrays.asList("test_1", "test_2");
        List<AlterationDriverAnnotation> annotationList = Arrays.asList(alterationDriverAnnotation1, emptyTierAnnotation);
        when(alterationDriverAnnotationRepository.getAlterationDriverAnnotations(molecularProfileIds))
            .thenReturn(annotationList);

        CustomDriverAnnotationReport props = alterationDriverAnnotationService
            .getCustomDriverAnnotationProps(molecularProfileIds);

        Assert.assertFalse("Empty string tier has to be removed from the set", props.getTiers().contains(""));
    }
}