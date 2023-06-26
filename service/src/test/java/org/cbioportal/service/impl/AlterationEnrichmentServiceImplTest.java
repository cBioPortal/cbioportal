package org.cbioportal.service.impl;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.CNA;
import org.cbioportal.model.EnrichmentType;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.model.MutationEventType;
import org.cbioportal.model.util.Select;
import org.cbioportal.service.AlterationCountService;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AlterationEnrichmentServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private AlterationEnrichmentServiceImpl alterationEnrichmentService;
    @Mock
    private AlterationCountService alterationCountService;
    @Mock
    private AlterationEnrichmentUtil<MutationCountByGene> alterationEnrichmentUtil;

    @Test
    public void getAlterationEnrichments() throws Exception {
        // create set1, set2 list of entities
        MolecularProfileCaseIdentifier molecularProfileCase1 = new MolecularProfileCaseIdentifier();
        molecularProfileCase1.setCaseId("sample_id_1");
        molecularProfileCase1.setMolecularProfileId("test1_mutations");
        MolecularProfileCaseIdentifier molecularProfileCase2 = new MolecularProfileCaseIdentifier();
        molecularProfileCase2.setCaseId("sample_id_2");
        molecularProfileCase2.setMolecularProfileId("test2_mutations");
        List<MolecularProfileCaseIdentifier> molecularProfileCaseSet1 = new ArrayList<>();
        molecularProfileCaseSet1.add(molecularProfileCase1);
        molecularProfileCaseSet1.add(molecularProfileCase2);

        MolecularProfileCaseIdentifier molecularProfileCase3 = new MolecularProfileCaseIdentifier();
        molecularProfileCase3.setCaseId("sample_id_3");
        molecularProfileCase3.setMolecularProfileId("test3_mutations");
        MolecularProfileCaseIdentifier molecularProfileCase4 = new MolecularProfileCaseIdentifier();
        molecularProfileCase4.setCaseId("sample_id_4");
        molecularProfileCase4.setMolecularProfileId("test4_mutations");
        List<MolecularProfileCaseIdentifier> molecularProfileCaseSet2 = new ArrayList<>();
        molecularProfileCaseSet2.add(molecularProfileCase3);
        molecularProfileCaseSet2.add(molecularProfileCase4);

        Map<String, List<MolecularProfileCaseIdentifier>> groupMolecularProfileCaseSets = new HashMap<String, List<MolecularProfileCaseIdentifier>>();
        groupMolecularProfileCaseSets.put("altered group", molecularProfileCaseSet1);
        groupMolecularProfileCaseSets.put("unaltered group", molecularProfileCaseSet2);

        Pair<List<AlterationCountByGene>, Long> alterationSampleCountByGeneList = new Pair<>(new ArrayList<>(), 0L);
        Select<MutationEventType> mutationTypes = Select.none();
        Select<CNA> cnaTypes = Select.none();
        
        AlterationFilter alterationFilter = new AlterationFilter();

        //  return counts for each of the two groups 
        for (String molecularProfileId : groupMolecularProfileCaseSets.keySet()) {
            Mockito.when(alterationCountService.getSampleAlterationGeneCounts(
                groupMolecularProfileCaseSets.get(molecularProfileId),
                Select.all(), true, true, alterationFilter)
            ).thenReturn(alterationSampleCountByGeneList);
        }

        List<AlterationEnrichment> expectedAlterationEnrichments = new ArrayList<>();
        Mockito.when(alterationEnrichmentUtil.createAlterationEnrichments(new HashMap<>()))
            .thenReturn(expectedAlterationEnrichments);

        List<AlterationEnrichment> result = alterationEnrichmentService
            .getAlterationEnrichments(
                groupMolecularProfileCaseSets,
                EnrichmentType.SAMPLE,
                alterationFilter);
        Assert.assertEquals(result, expectedAlterationEnrichments);
    }
}
