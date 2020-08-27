package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.StructuralVariantCountByGene;
import org.cbioportal.model.web.parameter.EnrichmentType;
import org.cbioportal.service.StructuralVariantService;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class StructuralVariantEnrichmentServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private StructuralVariantEnrichmentServiceImpl structuralVariantEnrichmentService;

    @Mock
    private StructuralVariantService structuralVariantService;
    @Mock
    private AlterationEnrichmentUtil<StructuralVariantCountByGene> alterationEnrichmentUtil;

    @Test
    public void getStructuralVariantEnrichments() throws Exception {
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

        Map<String, List<MolecularProfileCaseIdentifier>> groupMolecularProfileCaseSets = new HashMap<>();
        groupMolecularProfileCaseSets.put("altered group", molecularProfileCaseSet1);
        groupMolecularProfileCaseSets.put("unaltered group", molecularProfileCaseSet2);

        for (String molecularProfileId : groupMolecularProfileCaseSets.keySet()) {

            List<String> molecularProfileIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();

            groupMolecularProfileCaseSets.get(molecularProfileId)
                    .forEach(molecularProfileCase -> {
                        molecularProfileIds.add(molecularProfileCase.getMolecularProfileId());
                        sampleIds.add(molecularProfileCase.getCaseId());
                    });

            Mockito.when(structuralVariantService.getSampleCountInMultipleMolecularProfiles(new ArrayList<>(molecularProfileIds),
                    new ArrayList<>(sampleIds), null, false, true)).thenReturn(new ArrayList<>());
        }

        Mockito.when(alterationEnrichmentUtil.createAlterationEnrichments(new HashMap<>(),
                groupMolecularProfileCaseSets)).thenReturn(new ArrayList<>());

        List<AlterationEnrichment> result = structuralVariantEnrichmentService
                .getStructuralVariantEnrichments(groupMolecularProfileCaseSets, EnrichmentType.SAMPLE);
        Assert.assertEquals(result, new ArrayList<>());
    }
}
