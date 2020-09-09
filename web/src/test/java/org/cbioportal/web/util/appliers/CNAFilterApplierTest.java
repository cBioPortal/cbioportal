package org.cbioportal.web.util.appliers;

import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CNAFilterApplierTest {
    @Mock
    DiscreteCopyNumberService copyNumberService;
    
    @InjectMocks
    CNAFilterApplier cnaFilterApplier;
    
    @Test
    public void shouldNotFilterIfNoFilters() throws MolecularProfileNotFoundException {
        List<SampleIdentifier> unfilteredIds = Arrays.asList(
            sampleIdentifier("S1", "ST1"),
            sampleIdentifier("S2", "ST2")
        );

        Mockito
            .when(copyNumberService.getSampleCountByGeneAndAlterationAndSampleIds(
                Mockito.anyString(),
                Mockito.anyList(),
                Mockito.anyList(),
                Mockito.anyList()
            ))
            .thenReturn(null);

        List<SampleIdentifier> actual = cnaFilterApplier.applyFilters(unfilteredIds, new ArrayList<>());
        List<SampleIdentifier> expected = unfilteredIds;
        
        Assert.assertEquals(expected, actual);
    }
    
    private SampleIdentifier sampleIdentifier(String studyId, String sampleId) {
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setStudyId(studyId);
        sampleIdentifier.setSampleId(sampleId);
        return sampleIdentifier;
    }
}