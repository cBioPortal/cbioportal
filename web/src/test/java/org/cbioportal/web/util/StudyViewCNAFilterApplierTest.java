package org.cbioportal.web.util;

import org.cbioportal.service.DiscreteCopyNumberService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class StudyViewCNAFilterApplierTest {
    @Mock
    DiscreteCopyNumberService copyNumberService;
    
    @InjectMocks
    StudyViewCNAFilterApplier cnaFilterApplier;
    
    @Test
    public void foo() {
        List<String> sampleIds = Arrays.asList("S1", "S2");
        List<Integer> geneIds = Arrays.asList(1, 2);
        List<Integer> alterations = Arrays.asList(-2, -1, 0, 1, 2);

        Mockito
            .when(copyNumberService.getSampleCountByGeneAndAlterationAndSampleIds(
                Mockito.anyString(),
                sampleIds,
                geneIds,
                alterations,
                
            ))
            .thenReturn(null);
        Assert.assertTrue(true);
    }
}