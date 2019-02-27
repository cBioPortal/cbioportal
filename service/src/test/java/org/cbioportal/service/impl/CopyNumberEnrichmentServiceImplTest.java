package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.MolecularProfileCase;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

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
        // create molecularProfileCaseSet1, molecularProfileCaseSet2 list of entities
        MolecularProfileCase molecularProfileCase1 = new MolecularProfileCase();
        molecularProfileCase1.setCaseId("sample_id_1");
        molecularProfileCase1.setMolecularProfileId("test1_cna");
        MolecularProfileCase molecularProfileCase2 = new MolecularProfileCase();
        molecularProfileCase2.setCaseId("sample_id_2");
        molecularProfileCase2.setMolecularProfileId("test2_cna");
        List<MolecularProfileCase> molecularProfileCaseSet1 = new ArrayList<>();
        molecularProfileCaseSet1.add(molecularProfileCase1);
        molecularProfileCaseSet1.add(molecularProfileCase2);

        MolecularProfileCase molecularProfileCase3 = new MolecularProfileCase();
        molecularProfileCase3.setCaseId("sample_id_3");
        molecularProfileCase3.setMolecularProfileId("test3_cna");
        MolecularProfileCase molecularProfileCase4 = new MolecularProfileCase();
        molecularProfileCase4.setCaseId("sample_id_4");
        molecularProfileCase4.setMolecularProfileId("test4_cna");
        List<MolecularProfileCase> molecularProfileCaseSet2 = new ArrayList<>();
        molecularProfileCaseSet2.add(molecularProfileCase3);
        molecularProfileCaseSet2.add(molecularProfileCase4);

        List<MolecularProfileCase> allEntities = new ArrayList<>(molecularProfileCaseSet1);
        allEntities.addAll(molecularProfileCaseSet2);

        Mockito.when(alterationEnrichmentUtil.mapMolecularProfileIdToCaseId(allEntities)).thenReturn(new HashMap<String, List<String>>() {{
            put("test1_cna", Arrays.asList("sample_id_1"));
            put("test2_cna", Arrays.asList("sample_id_2"));
            put("test3_cna", Arrays.asList("sample_id_3"));
            put("test4_cna", Arrays.asList("sample_id_4"));
        }});
        Mockito.when(alterationEnrichmentUtil.mapMolecularProfileIdToCaseId(molecularProfileCaseSet1)).thenReturn(new HashMap<String, List<String>>() {{
            put("test1_cna", Arrays.asList("sample_id_1"));
            put("test2_cna", Arrays.asList("sample_id_2"));
        }});
        Map<String, List<String>> allMolecularProfileIdToCaseMap = alterationEnrichmentUtil.mapMolecularProfileIdToCaseId(allEntities);
        Map<String, List<String>> group1MolecularProfileIdToCaseMap = alterationEnrichmentUtil.mapMolecularProfileIdToCaseId(molecularProfileCaseSet1);

        // check size of all vs. group 1 molecular profile case maps
        Assert.assertEquals(4, allMolecularProfileIdToCaseMap.size());
        Assert.assertEquals(2, group1MolecularProfileIdToCaseMap.size());

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
        for (String molecularProfileId : allMolecularProfileIdToCaseMap.keySet()) {
            Mockito.when(discreteCopyNumberService.getSampleCountByGeneAndAlterationAndSampleIds(molecularProfileId,
            allMolecularProfileIdToCaseMap.get(molecularProfileId), null, null)).thenReturn(copyNumberSampleCountByGenes);
        }

        List<DiscreteCopyNumberData> discreteCopyNumberDataList = new ArrayList<>();
        for (String molecularProfileId : group1MolecularProfileIdToCaseMap.keySet()) {
            Mockito.when(discreteCopyNumberService.fetchDiscreteCopyNumbersInMolecularProfile(molecularProfileId,
                group1MolecularProfileIdToCaseMap.get(molecularProfileId), null, alterationTypes, "ID")).thenReturn(discreteCopyNumberDataList);
        }

        List<AlterationEnrichment> expectedAlterationEnrichments = new ArrayList<>();
        Mockito.when(alterationEnrichmentUtil.createAlterationEnrichments(2, 2, copyNumberSampleCountByGenes,
            discreteCopyNumberDataList, "SAMPLE")).thenReturn(expectedAlterationEnrichments);

        List<AlterationEnrichment> result = copyNumberEnrichmentService.getCopyNumberEnrichments(molecularProfileCaseSet1, molecularProfileCaseSet2, alterationTypes, "SAMPLE");
        Assert.assertEquals(result, expectedAlterationEnrichments);
    }
}
