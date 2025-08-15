package org.cbioportal.domain.mutation.util;

import org.cbioportal.legacy.web.parameter.MutationMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class MutationUtilTest {

    private List<SampleMolecularIdentifier> sampleMolecularIdentifierList;

    @Before
    public void setUp() throws Exception {
        sampleMolecularIdentifierList=new ArrayList<>();
        var sampleMolecularIdentifier1= new SampleMolecularIdentifier();
        var sampleMolecularIdentifier2= new SampleMolecularIdentifier();
        sampleMolecularIdentifier1.setSampleId("TCGA-A1-A0SH-01");
        sampleMolecularIdentifier1.setMolecularProfileId("study_tcga_pub_mutations");
        sampleMolecularIdentifier2.setSampleId("TCGA-A1-A0SO-01");
        sampleMolecularIdentifier2.setMolecularProfileId("study_tcga_pub_mutations");
        sampleMolecularIdentifierList.add(sampleMolecularIdentifier1);
        sampleMolecularIdentifierList.add(sampleMolecularIdentifier2);
    }

    @Test
    public void extractMolecularProfileIds() {
        List<String> resultMolecularProfileIds = MutationUtil.extractMolecularProfileIds(sampleMolecularIdentifierList);
        assertEquals(2, resultMolecularProfileIds.size());
        assertEquals("study_tcga_pub_mutations", resultMolecularProfileIds.getFirst());
        assertEquals("study_tcga_pub_mutations", resultMolecularProfileIds.get(1));
    }

    @Test
    public void extractSampleIds() {
        List<String> resultSampleIds = MutationUtil.extractSampleIds(sampleMolecularIdentifierList);
        assertEquals(2, resultSampleIds.size());
        assertEquals("TCGA-A1-A0SH-01", resultSampleIds.getFirst());
        assertEquals("TCGA-A1-A0SO-01", resultSampleIds.get(1));
    }
}