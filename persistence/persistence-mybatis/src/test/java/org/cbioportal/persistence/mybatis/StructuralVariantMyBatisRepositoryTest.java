/*
 * Copyright (c) 2018 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.StructuralVariant;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.ArrayList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class StructuralVariantMyBatisRepositoryTest {

    @Autowired
    StructuralVariantMyBatisRepository structuralVariantMyBatisRepository;
    
    @Test
    public void fetchStructuralVariantsNoSampleIdentifiers() throws Exception {

        List<String> molecularProfileIds = new ArrayList<String>();
        List<Integer> entrezGeneIds = new ArrayList<Integer>();
        List<String> sampleIds = new ArrayList<String>();

        molecularProfileIds.add("study_tcga_pub_sv");
        entrezGeneIds.add(57670);

        List<StructuralVariant> result = 
                structuralVariantMyBatisRepository.fetchStructuralVariants(molecularProfileIds, 
                        entrezGeneIds, sampleIds);

        Assert.assertEquals(2,  result.size());
        StructuralVariant structuralVariantFirstResult = result.get(0);
        Assert.assertEquals("study_tcga_pub_sv", structuralVariantFirstResult.getMolecularProfileId());
        Assert.assertEquals(1, structuralVariantFirstResult.getStructuralVariantId());
        Assert.assertEquals((int) 1, structuralVariantFirstResult.getSampleIdInternal());
        Assert.assertEquals((String) "TCGA-A1-A0SB-01", structuralVariantFirstResult.getSampleId());
        Assert.assertEquals((String) "TCGA-A1-A0SB", structuralVariantFirstResult.getPatientId());
        Assert.assertEquals((String) "study_tcga_pub", structuralVariantFirstResult.getStudyId());
        Assert.assertEquals((Integer) 57670, structuralVariantFirstResult.getSite1EntrezGeneId());
        Assert.assertEquals("KIAA1549", structuralVariantFirstResult.getSite1HugoSymbol());
        Assert.assertEquals("ENST00000242365", structuralVariantFirstResult.getSite1EnsemblTranscriptId());
        Assert.assertEquals((Integer) 15, structuralVariantFirstResult.getSite1Exon());
        Assert.assertEquals("7", structuralVariantFirstResult.getSite1Chromosome());
        Assert.assertEquals((Integer) 138536968, structuralVariantFirstResult.getSite1Position());
        Assert.assertEquals("KIAA1549-BRAF.K16B10.COSF509_1", structuralVariantFirstResult.getSite1Description());
        Assert.assertEquals((Integer) 673, structuralVariantFirstResult.getSite2EntrezGeneId());
        Assert.assertEquals("BRAF", structuralVariantFirstResult.getSite2HugoSymbol());
        Assert.assertEquals("ENST00000288602", structuralVariantFirstResult.getSite2EnsemblTranscriptId());
        Assert.assertEquals((Integer) 10, structuralVariantFirstResult.getSite2Exon());
        Assert.assertEquals("7", structuralVariantFirstResult.getSite2Chromosome());
        Assert.assertEquals((Integer) 140482957, structuralVariantFirstResult.getSite2Position());
        Assert.assertEquals("KIAA1549-BRAF.K16B10.COSF509_2", structuralVariantFirstResult.getSite2Description());
        Assert.assertEquals(null, structuralVariantFirstResult.getSite2EffectOnFrame());
        Assert.assertEquals("GRCh37", structuralVariantFirstResult.getNcbiBuild());
        Assert.assertEquals("no", structuralVariantFirstResult.getDnaSupport());
        Assert.assertEquals("yes", structuralVariantFirstResult.getRnaSupport());
        Assert.assertEquals(null, structuralVariantFirstResult.getNormalReadCount());
        Assert.assertEquals((Integer) 100000, structuralVariantFirstResult.getTumorReadCount());
        Assert.assertEquals(null, structuralVariantFirstResult.getNormalVariantCount());
        Assert.assertEquals((Integer) 90000, structuralVariantFirstResult.getTumorVariantCount());
        Assert.assertEquals(null, structuralVariantFirstResult.getNormalPairedEndReadCount());
        Assert.assertEquals(null, structuralVariantFirstResult.getTumorPairedEndReadCount());
        Assert.assertEquals(null, structuralVariantFirstResult.getNormalSplitReadCount());
        Assert.assertEquals(null, structuralVariantFirstResult.getTumorSplitReadCount());
        Assert.assertEquals("KIAA1549-BRAF.K16B10.COSF509", structuralVariantFirstResult.getAnnotation());
        Assert.assertEquals(null, structuralVariantFirstResult.getBreakpointType());
        Assert.assertEquals(null, structuralVariantFirstResult.getCenter());
        Assert.assertEquals(null, structuralVariantFirstResult.getConnectionType());
        Assert.assertEquals("Fusion", structuralVariantFirstResult.getEventInfo());
        Assert.assertEquals(null, structuralVariantFirstResult.getVariantClass());
        Assert.assertEquals(null, structuralVariantFirstResult.getLength());
        Assert.assertEquals("Gain-of-Function", structuralVariantFirstResult.getComments());
        Assert.assertEquals("COSMIC:COSF509", structuralVariantFirstResult.getExternalAnnotation());
        Assert.assertEquals("Putative_Passenger", structuralVariantFirstResult.getDriverFilter());
        Assert.assertEquals("Pathogenic", structuralVariantFirstResult.getDriverFilterAnn());
        Assert.assertEquals("Tier 1", structuralVariantFirstResult.getDriverTiersFilter());
        Assert.assertEquals("Potentially Actionable", structuralVariantFirstResult.getDriverTiersFilterAnn());
        StructuralVariant structuralVariantSecondResult = result.get(1);
        Assert.assertEquals("study_tcga_pub_sv", structuralVariantSecondResult.getMolecularProfileId());
        Assert.assertEquals(5, structuralVariantSecondResult.getStructuralVariantId());
        Assert.assertEquals((int) 2, structuralVariantSecondResult.getSampleIdInternal());
        Assert.assertEquals((String) "TCGA-A1-A0SD-01", structuralVariantSecondResult.getSampleId());
        Assert.assertEquals((String) "TCGA-A1-A0SD", structuralVariantSecondResult.getPatientId());
        Assert.assertEquals((String) "study_tcga_pub", structuralVariantSecondResult.getStudyId());
        Assert.assertEquals((Integer) 57670, structuralVariantSecondResult.getSite1EntrezGeneId());
        Assert.assertEquals("KIAA1549", structuralVariantSecondResult.getSite1HugoSymbol());
        Assert.assertEquals("ENST00000242365", structuralVariantSecondResult.getSite1EnsemblTranscriptId());
        Assert.assertEquals((Integer) 15, structuralVariantSecondResult.getSite1Exon());
        Assert.assertEquals("7", structuralVariantSecondResult.getSite1Chromosome());
        Assert.assertEquals((Integer) 138536968, structuralVariantSecondResult.getSite1Position());
        Assert.assertEquals("KIAA1549-BRAF.K16B10.COSF509_1", structuralVariantSecondResult.getSite1Description());
        Assert.assertEquals((Integer)673, structuralVariantSecondResult.getSite2EntrezGeneId());
        Assert.assertEquals("BRAF", structuralVariantSecondResult.getSite2HugoSymbol());
        Assert.assertEquals("ENST00000288602", structuralVariantSecondResult.getSite2EnsemblTranscriptId());
        Assert.assertEquals((Integer) 10, structuralVariantSecondResult.getSite2Exon());
        Assert.assertEquals("7", structuralVariantSecondResult.getSite2Chromosome());
        Assert.assertEquals((Integer) 140482957, structuralVariantSecondResult.getSite2Position());
        Assert.assertEquals("KIAA1549-BRAF.K16B10.COSF509_2", structuralVariantSecondResult.getSite2Description());
        Assert.assertEquals(null, structuralVariantSecondResult.getSite2EffectOnFrame());
        Assert.assertEquals("GRCh37", structuralVariantSecondResult.getNcbiBuild());
        Assert.assertEquals("no", structuralVariantSecondResult.getDnaSupport());
        Assert.assertEquals("yes", structuralVariantSecondResult.getRnaSupport());
        Assert.assertEquals(null, structuralVariantSecondResult.getNormalReadCount());
        Assert.assertEquals((Integer) 100000, structuralVariantSecondResult.getTumorReadCount());
        Assert.assertEquals(null, structuralVariantSecondResult.getNormalVariantCount());
        Assert.assertEquals((Integer) 90000, structuralVariantSecondResult.getTumorVariantCount());
        Assert.assertEquals(null, structuralVariantSecondResult.getNormalPairedEndReadCount());
        Assert.assertEquals(null, structuralVariantSecondResult.getTumorPairedEndReadCount());
        Assert.assertEquals(null, structuralVariantSecondResult.getNormalSplitReadCount());
        Assert.assertEquals(null, structuralVariantSecondResult.getTumorSplitReadCount());
        Assert.assertEquals("KIAA1549-BRAF.K16B10.COSF509", structuralVariantSecondResult.getAnnotation());
        Assert.assertEquals(null, structuralVariantSecondResult.getBreakpointType());
        Assert.assertEquals(null, structuralVariantSecondResult.getCenter());
        Assert.assertEquals(null, structuralVariantSecondResult.getConnectionType());
        Assert.assertEquals("Fusion", structuralVariantSecondResult.getEventInfo());
        Assert.assertEquals(null, structuralVariantSecondResult.getVariantClass());
        Assert.assertEquals(null, structuralVariantSecondResult.getLength());
        Assert.assertEquals("Gain-of-Function", structuralVariantSecondResult.getComments());
        Assert.assertEquals("COSMIC:COSF509", structuralVariantSecondResult.getExternalAnnotation());
        Assert.assertEquals(null, structuralVariantSecondResult.getDriverFilter());
        Assert.assertEquals(null, structuralVariantSecondResult.getDriverFilterAnn());
        Assert.assertEquals(null, structuralVariantSecondResult.getDriverTiersFilter());
        Assert.assertEquals(null, structuralVariantSecondResult.getDriverTiersFilterAnn());

    }

    @Test
    public void fetchStructuralVariantsWithSampleIdentifier() throws Exception {

        List<String> molecularProfileIds = new ArrayList<String>();
        List<Integer> entrezGeneIds = new ArrayList<Integer>();
        List<String> sampleIds = new ArrayList<String>();

        molecularProfileIds.add("study_tcga_pub_sv");
        entrezGeneIds.add(57670);
        sampleIds.add("TCGA-A1-A0SB-01");

        List<StructuralVariant> result = 
                structuralVariantMyBatisRepository.fetchStructuralVariants(molecularProfileIds, 
                        entrezGeneIds, sampleIds);

        Assert.assertEquals(1,  result.size());
        StructuralVariant structuralVariantResult = result.get(0);
        Assert.assertEquals("study_tcga_pub_sv", structuralVariantResult.getMolecularProfileId());
        Assert.assertEquals(1, structuralVariantResult.getStructuralVariantId());
        Assert.assertEquals((int) 1, structuralVariantResult.getSampleIdInternal());
        Assert.assertEquals((String) "TCGA-A1-A0SB-01", structuralVariantResult.getSampleId());
        Assert.assertEquals((String) "TCGA-A1-A0SB", structuralVariantResult.getPatientId());
        Assert.assertEquals((String) "study_tcga_pub", structuralVariantResult.getStudyId());

    }

    @Test
    public void fetchStructuralVariantsMultiStudyWithSampleIdentifiers() throws Exception {

        List<Integer> entrezGeneIds = new ArrayList<Integer>();
        List<String> molecularProfileIds = new ArrayList<String>();
        List<String> sampleIds = new ArrayList<String>();

        entrezGeneIds.add(57670);
        molecularProfileIds.add("study_tcga_pub_sv");
        sampleIds.add("TCGA-A1-A0SB-01");
        molecularProfileIds.add("acc_tcga_mutations");
        sampleIds.add("TCGA-A1-B0SO-01");
        
        List<StructuralVariant> result = 
                structuralVariantMyBatisRepository.fetchStructuralVariants(molecularProfileIds, 
                        entrezGeneIds, sampleIds);

        Assert.assertEquals(2,  result.size());
        StructuralVariant structuralVariantFirstResult = result.get(0);
        Assert.assertEquals("acc_tcga_mutations", structuralVariantFirstResult.getMolecularProfileId());
        Assert.assertEquals(6, structuralVariantFirstResult.getStructuralVariantId());
        Assert.assertEquals((int) 15, structuralVariantFirstResult.getSampleIdInternal());
        Assert.assertEquals((String) "TCGA-A1-B0SO-01", structuralVariantFirstResult.getSampleId());
        Assert.assertEquals((String) "TCGA-A1-B0SO", structuralVariantFirstResult.getPatientId());
        Assert.assertEquals((String) "acc_tcga", structuralVariantFirstResult.getStudyId());
        StructuralVariant structuralVariantSecondResult = result.get(1);
        Assert.assertEquals("study_tcga_pub_sv", structuralVariantSecondResult.getMolecularProfileId());
        Assert.assertEquals(1, structuralVariantSecondResult.getStructuralVariantId());
        Assert.assertEquals((int) 1, structuralVariantSecondResult.getSampleIdInternal());
        Assert.assertEquals((String) "TCGA-A1-A0SB-01", structuralVariantSecondResult.getSampleId());
        Assert.assertEquals((String) "TCGA-A1-A0SB", structuralVariantSecondResult.getPatientId());
        Assert.assertEquals((String) "study_tcga_pub", structuralVariantSecondResult.getStudyId());
    }
}
