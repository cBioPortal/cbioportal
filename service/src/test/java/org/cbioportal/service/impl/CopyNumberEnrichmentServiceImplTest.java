package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class CopyNumberEnrichmentServiceImplTest extends BaseServiceImplTest {
    
    @InjectMocks
    private CopyNumberEnrichmentServiceImpl copyNumberEnrichmentService;

    @Mock
    private DiscreteCopyNumberService discreteCopyNumberService;
    @Mock
    private AlterationEnrichmentUtil alterationEnrichmentUtil;
    
    @Test
    public void getCopyNumberEnrichments() throws Exception {

        List<String> alteredSampleIds = new ArrayList<>();
        alteredSampleIds.add("sample_id_1");
        alteredSampleIds.add("sample_id_2");
        List<String> unalteredSampleIds = new ArrayList<>();
        unalteredSampleIds.add("sample_id_3");
        unalteredSampleIds.add("sample_id_4");
        List<String> allSampleIds = new ArrayList<>(alteredSampleIds);
        allSampleIds.addAll(unalteredSampleIds);

        List<Integer> alterationTypes = new ArrayList<>();
        alterationTypes.add(-2);

        List<CopyNumberCountByGene> copyNumberSampleCountByGenes = new ArrayList<>();
        Mockito.when(discreteCopyNumberService.getSampleCountByGeneAndAlterationAndSampleIds(MOLECULAR_PROFILE_ID, 
            allSampleIds, null, null)).thenReturn(copyNumberSampleCountByGenes);

        List<DiscreteCopyNumberData> discreteCopyNumberDataList = new ArrayList<>();
        Mockito.when(discreteCopyNumberService.fetchDiscreteCopyNumbersInMolecularProfile(MOLECULAR_PROFILE_ID, 
            alteredSampleIds, null, alterationTypes, "ID")).thenReturn(discreteCopyNumberDataList);

        List<AlterationEnrichment> expectedAlterationEnrichments = new ArrayList<>();
        Mockito.when(alterationEnrichmentUtil.createAlterationEnrichments(2, 2, copyNumberSampleCountByGenes,
            discreteCopyNumberDataList, "SAMPLE")).thenReturn(expectedAlterationEnrichments);

        List<AlterationEnrichment> result = copyNumberEnrichmentService.getCopyNumberEnrichments(MOLECULAR_PROFILE_ID,
            alteredSampleIds, unalteredSampleIds, alterationTypes, "SAMPLE");

        Assert.assertEquals(result, expectedAlterationEnrichments);
    }
}