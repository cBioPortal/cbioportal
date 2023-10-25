package org.cbioportal.persistence.mybatis.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MolecularProfileCaseIdentifierUtilTest {
    @InjectMocks
    private MolecularProfileCaseIdentifierUtil molecularProfileCaseIdentifierUtil;

    @Test
    public void getGroupedCasesByMolecularProfileId() {

        final String MOLECULAR_PROFILE_ID_1 = "molecular_profile_id_1";
        final String MOLECULAR_PROFILE_ID_2 = "molecular_profile_id_2";
        final String SAMPLE_ID_1 = "sample_id_1";
        final String SAMPLE_ID_2 = "sample_id_2";
        final String SAMPLE_ID_3 = "sample_id_3";
        
        Map<String, Set<String>> result = molecularProfileCaseIdentifierUtil.getGroupedCasesByMolecularProfileId(new ArrayList<>(), new ArrayList<>());
        Assert.assertEquals("empty request", 0, result.size());

        result = molecularProfileCaseIdentifierUtil.getGroupedCasesByMolecularProfileId(Arrays.asList(MOLECULAR_PROFILE_ID_1), new ArrayList<>());
        Assert.assertEquals("empty sample ids", 1, result.size());
        Assert.assertEquals(0, result.get(MOLECULAR_PROFILE_ID_1).size());


        result = molecularProfileCaseIdentifierUtil.getGroupedCasesByMolecularProfileId(
            Arrays.asList(MOLECULAR_PROFILE_ID_1, MOLECULAR_PROFILE_ID_1, MOLECULAR_PROFILE_ID_1),
            Arrays.asList(SAMPLE_ID_1, SAMPLE_ID_2, SAMPLE_ID_3));

        Assert.assertEquals("valid - single profile", 1, result.size());

        result = molecularProfileCaseIdentifierUtil.getGroupedCasesByMolecularProfileId(
            Arrays.asList(MOLECULAR_PROFILE_ID_1, MOLECULAR_PROFILE_ID_1, MOLECULAR_PROFILE_ID_2),
            Arrays.asList(SAMPLE_ID_1, SAMPLE_ID_2, SAMPLE_ID_3));

        Assert.assertEquals("valid - multiple profiles", 2, result.size());
        Assert.assertEquals(2, result.get(MOLECULAR_PROFILE_ID_1).size());
        Assert.assertEquals(1, result.get(MOLECULAR_PROFILE_ID_2).size());
    }
}
