package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.MolecularProfileCase;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.service.MutationService;
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
public class MutationEnrichmentServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private MutationEnrichmentServiceImpl mutationEnrichmentService;

    @Mock
    private MutationService mutationService;
    @Mock
    private AlterationEnrichmentUtil alterationEnrichmentUtil;

    @Test
    public void getMutationEnrichments() throws Exception {
        // create set1, set2 list of entities
        MolecularProfileCase molecularProfileCase1 = new MolecularProfileCase();
        molecularProfileCase1.setCaseId("sample_id_1");
        molecularProfileCase1.setMolecularProfileId("test1_mutations");
        MolecularProfileCase molecularProfileCase2 = new MolecularProfileCase();
        molecularProfileCase2.setCaseId("sample_id_2");
        molecularProfileCase2.setMolecularProfileId("test2_mutations");
        List<MolecularProfileCase> molecularProfileCaseSet1 = new ArrayList<>();
        molecularProfileCaseSet1.add(molecularProfileCase1);
        molecularProfileCaseSet1.add(molecularProfileCase2);

        MolecularProfileCase molecularProfileCase3 = new MolecularProfileCase();
        molecularProfileCase3.setCaseId("sample_id_3");
        molecularProfileCase3.setMolecularProfileId("test3_mutations");
        MolecularProfileCase molecularProfileCase4 = new MolecularProfileCase();
        molecularProfileCase4.setCaseId("sample_id_4");
        molecularProfileCase4.setMolecularProfileId("test4_mutations");
        List<MolecularProfileCase> molecularProfileCaseSet2 = new ArrayList<>();
        molecularProfileCaseSet2.add(molecularProfileCase3);
        molecularProfileCaseSet2.add(molecularProfileCase4);

        List<MolecularProfileCase> allEntities = new ArrayList<>(molecularProfileCaseSet1);
        allEntities.addAll(molecularProfileCaseSet2);

        Mockito.when(alterationEnrichmentUtil.mapMolecularProfileIdToCaseId(allEntities)).thenReturn(new HashMap<String, List<String>>() {{
            put("test1_mutations", Arrays.asList("sample_id_1"));
            put("test2_mutations", Arrays.asList("sample_id_2"));
            put("test3_mutations", Arrays.asList("sample_id_3"));
            put("test4_mutations", Arrays.asList("sample_id_4"));
        }});
        Mockito.when(alterationEnrichmentUtil.mapMolecularProfileIdToCaseId(molecularProfileCaseSet1)).thenReturn(new HashMap<String, List<String>>() {{
            put("test1_mutations", Arrays.asList("sample_id_1"));
            put("test2_mutations", Arrays.asList("sample_id_2"));
        }});
        Map<String, List<String>> allMolecularProfileIdToCaseMap = alterationEnrichmentUtil.mapMolecularProfileIdToCaseId(allEntities);
        Map<String, List<String>> group1MolecularProfileIdToCaseMap = alterationEnrichmentUtil.mapMolecularProfileIdToCaseId(molecularProfileCaseSet1);

        // check size of all vs. group 1 molecular profile case maps
        Assert.assertEquals(4, allMolecularProfileIdToCaseMap.size());
        Assert.assertEquals(2, group1MolecularProfileIdToCaseMap.size());

        List<MutationCountByGene> mutationSampleCountByGeneList = new ArrayList<>();
        for (String molecularProfileId : allMolecularProfileIdToCaseMap.keySet()) {
            Mockito.when(mutationService.getSampleCountByEntrezGeneIdsAndSampleIds(molecularProfileId, allMolecularProfileIdToCaseMap.get(molecularProfileId), null))
                .thenReturn(mutationSampleCountByGeneList);
        }

        List<Mutation> mutations = new ArrayList<>();
        for (String molecularProfileId : group1MolecularProfileIdToCaseMap.keySet()) {
            Mockito.when(mutationService.fetchMutationsInMolecularProfile(molecularProfileId, group1MolecularProfileIdToCaseMap.get(molecularProfileId), null, null,
                "ID", null, null, null, null)).thenReturn(mutations);
        }

        List<AlterationEnrichment> expectedAlterationEnrichments = new ArrayList<>();
        Mockito.when(alterationEnrichmentUtil.createAlterationEnrichments(2, 2, mutationSampleCountByGeneList,
            mutations, "SAMPLE")).thenReturn(expectedAlterationEnrichments);

        List<AlterationEnrichment> result = mutationEnrichmentService.getMutationEnrichments(molecularProfileCaseSet1, molecularProfileCaseSet2, "SAMPLE");
        Assert.assertEquals(result, expectedAlterationEnrichments);
    }
}
