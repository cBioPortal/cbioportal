package org.cbioportal.service.impl;

import org.cbioportal.model.CNA;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.EnrichmentType;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.util.Select;
import org.cbioportal.service.AlterationCountService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CopyNumberEnrichmentServiceImplTest {

    @InjectMocks
    private CopyNumberEnrichmentServiceImpl cnaCountService;
    @Mock
    private AlterationCountService alterationCountService;
    
    // FIXME use SelectMockitoArgumentMatcher in util module when implemented
    // see issue https://github.com/cBioPortal/cbioportal/issues/8297
    private static class SelectMockitoArgumentMatcher implements ArgumentMatcher<Select> {
        private String checkWhat;

        public SelectMockitoArgumentMatcher(String checkWhat) {
            this.checkWhat = checkWhat;
        }

        @Override
        public boolean matches(Select select) {
            switch (checkWhat) {
                case "ALL":
                    return select.hasAll();
                case "EMPTY":
                    return select.hasNone();
                case "SOME":
                    return select.hasValues();
                default:
                    return false;
            }
        }
    }

    @Before
    public void setUp() throws Exception {

        List<MolecularProfileCaseIdentifier> molecularProfileCaseSet = new ArrayList<>();
        molecularProfileCaseSet.add(new MolecularProfileCaseIdentifier("caseA", "profileB"));
        
        groupMolecularProfileCaseSets = new HashMap<>();
        groupMolecularProfileCaseSets.put("altered group", molecularProfileCaseSet);
        groupMolecularProfileCaseSets.put("unaltered group", molecularProfileCaseSet);

        List<CopyNumberCountByGene> counts = new ArrayList<>();
        
        when(alterationCountService.getSampleCnaCounts(
            eq(molecularProfileCaseSet),
            argThat(new SelectMockitoArgumentMatcher("ALL")),
            eq(true),
            eq(true),
            argThat(new SelectMockitoArgumentMatcher("SOME")))
        ).thenReturn(counts);
    }

    Map<String, List<MolecularProfileCaseIdentifier>> groupMolecularProfileCaseSets;

    @Test
    public void testGetCopyNumberCountByGeneAndGroup() throws MolecularProfileNotFoundException {
        Map<String, List<CopyNumberCountByGene>> copyNumberCountByGeneAndGroup = cnaCountService.getCopyNumberCountByGeneAndGroup(groupMolecularProfileCaseSets, CNA.AMP, EnrichmentType.SAMPLE);
        Assert.assertEquals(2, copyNumberCountByGeneAndGroup.keySet().size());
    }
}
