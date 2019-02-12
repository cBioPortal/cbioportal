package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.Entity;
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
        // create set1, set2 list of entities
        Entity entity1 = new Entity();
        entity1.setEntityId("sample_id_1");
        entity1.setMolecularProfileId("test1_cna");
        Entity entity2 = new Entity();
        entity2.setEntityId("sample_id_2");
        entity2.setMolecularProfileId("test2_cna");
        List<Entity> set1 = new ArrayList<>();
        set1.add(entity1);
        set1.add(entity2);

        Entity entity3 = new Entity();
        entity3.setEntityId("sample_id_3");
        entity3.setMolecularProfileId("test3_cna");
        Entity entity4 = new Entity();
        entity4.setEntityId("sample_id_4");
        entity4.setMolecularProfileId("test4_cna");
        List<Entity> set2 = new ArrayList<>();
        set2.add(entity3);
        set2.add(entity4);

        List<Entity> allEntities = new ArrayList<>(set1);
        allEntities.addAll(set2);

        Mockito.when(alterationEnrichmentUtil.mapMolecularProfileIdToEntityId(allEntities)).thenReturn(new HashMap<String, List<String>>() {{
            put("test1_cna", Arrays.asList("sample_id_1"));
            put("test2_cna", Arrays.asList("sample_id_2"));
            put("test3_cna", Arrays.asList("sample_id_3"));
            put("test4_cna", Arrays.asList("sample_id_4"));
        }});
        Mockito.when(alterationEnrichmentUtil.mapMolecularProfileIdToEntityId(set1)).thenReturn(new HashMap<String, List<String>>() {{
            put("test1_cna", Arrays.asList("sample_id_1"));
            put("test2_cna", Arrays.asList("sample_id_2"));
        }});
        Map<String, List<String>> allMolecularProfileIdToEntityMap = alterationEnrichmentUtil.mapMolecularProfileIdToEntityId(allEntities);
        Map<String, List<String>> group1MolecularProfileIdToEntityMap = alterationEnrichmentUtil.mapMolecularProfileIdToEntityId(set1);

        // check size of all vs. group 1 entity maps
        Assert.assertEquals(4, allMolecularProfileIdToEntityMap.size());
        Assert.assertEquals(2, group1MolecularProfileIdToEntityMap.size());

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
        for (String molecularProfileId : allMolecularProfileIdToEntityMap.keySet()) {
            Mockito.when(discreteCopyNumberService.getSampleCountByGeneAndAlterationAndSampleIds(molecularProfileId,
            allMolecularProfileIdToEntityMap.get(molecularProfileId), null, null)).thenReturn(copyNumberSampleCountByGenes);
        }

        List<DiscreteCopyNumberData> discreteCopyNumberDataList = new ArrayList<>();
        for (String molecularProfileId : group1MolecularProfileIdToEntityMap.keySet()) {
            Mockito.when(discreteCopyNumberService.fetchDiscreteCopyNumbersInMolecularProfile(molecularProfileId,
                group1MolecularProfileIdToEntityMap.get(molecularProfileId), null, alterationTypes, "ID")).thenReturn(discreteCopyNumberDataList);
        }

        List<AlterationEnrichment> expectedAlterationEnrichments = new ArrayList<>();
        Mockito.when(alterationEnrichmentUtil.createAlterationEnrichments(2, 2, copyNumberSampleCountByGenes,
            discreteCopyNumberDataList, "SAMPLE")).thenReturn(expectedAlterationEnrichments);

        List<AlterationEnrichment> result = copyNumberEnrichmentService.getCopyNumberEnrichments(set1, set2, alterationTypes, "SAMPLE");
        Assert.assertEquals(result, expectedAlterationEnrichments);
    }
}
