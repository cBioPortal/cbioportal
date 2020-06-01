package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
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
        MolecularProfileCaseIdentifier molecularProfileCase1 = new MolecularProfileCaseIdentifier();
        molecularProfileCase1.setCaseId("sample_id_1");
        molecularProfileCase1.setMolecularProfileId("test1_cna");
        MolecularProfileCaseIdentifier molecularProfileCase2 = new MolecularProfileCaseIdentifier();
        molecularProfileCase2.setCaseId("sample_id_2");
        molecularProfileCase2.setMolecularProfileId("test2_cna");
        List<MolecularProfileCaseIdentifier> molecularProfileCaseSet1 = new ArrayList<>();
        molecularProfileCaseSet1.add(molecularProfileCase1);
        molecularProfileCaseSet1.add(molecularProfileCase2);

        MolecularProfileCaseIdentifier molecularProfileCase3 = new MolecularProfileCaseIdentifier();
        molecularProfileCase3.setCaseId("sample_id_3");
        molecularProfileCase3.setMolecularProfileId("test3_cna");
        MolecularProfileCaseIdentifier molecularProfileCase4 = new MolecularProfileCaseIdentifier();
        molecularProfileCase4.setCaseId("sample_id_4");
        molecularProfileCase4.setMolecularProfileId("test4_cna");
        List<MolecularProfileCaseIdentifier> molecularProfileCaseSet2 = new ArrayList<>();
        molecularProfileCaseSet2.add(molecularProfileCase3);
        molecularProfileCaseSet2.add(molecularProfileCase4);

        Map<String, List<MolecularProfileCaseIdentifier>> groupMolecularProfileCaseSets = new HashMap<String, List<MolecularProfileCaseIdentifier>>();
        groupMolecularProfileCaseSets.put("altered group", molecularProfileCaseSet1);
        groupMolecularProfileCaseSets.put("unaltered group", molecularProfileCaseSet2);

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

        for (String molecularProfileId : groupMolecularProfileCaseSets.keySet()) {

            List<String> molecularProfileIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();

            groupMolecularProfileCaseSets.getOrDefault(molecularProfileId, new ArrayList<>())
                    .forEach(molecularProfileCase -> {
                        molecularProfileIds.add(molecularProfileCase.getMolecularProfileId());
                        sampleIds.add(molecularProfileCase.getCaseId());
                    });

            Mockito.when(discreteCopyNumberService.getSampleCountInMultipleMolecularProfiles(molecularProfileIds,
                    sampleIds, null, null, false, true)).thenReturn(new ArrayList<CopyNumberCountByGene>());
        }

        List<AlterationEnrichment> expectedAlterationEnrichments = new ArrayList<>();
        Mockito.when(alterationEnrichmentUtil.createAlterationEnrichments(new HashMap<>(),
                groupMolecularProfileCaseSets, "SAMPLE")).thenReturn(expectedAlterationEnrichments);

        List<AlterationEnrichment> result = copyNumberEnrichmentService
                .getCopyNumberEnrichments(groupMolecularProfileCaseSets, alterationTypes, "SAMPLE");
        Assert.assertEquals(result, expectedAlterationEnrichments);
    }
}
