/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
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

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.StructuralVariant;
import org.cbioportal.persistence.StructuralVariantRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

/**
 *
 * @author jake
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class})
@Configurable
public class StructuralVariantMyBatisRepositoryTest {
    @Autowired
    StructuralVariantRepository structuralVariantRepository;

    @Test
    public void getWithoutStudy() {
        List<String> geneticProfileStableIds = new ArrayList<>();
        List<String> sampleStableIds = new ArrayList<>();
        sampleStableIds.add("TCGA-A1-A0SB-01");
        sampleStableIds.add("TCGA-A1-A0SD-01");
        sampleStableIds.add("TCGA-A1-A0SE-01");
        sampleStableIds.add("TCGA-A1-A0SF-01");
        List<String> hugoGeneSymbols = new ArrayList<>();
        hugoGeneSymbols.add("ERBB2");
        List<StructuralVariant> result = structuralVariantRepository.getStructuralVariant(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getWithStudyOnly() {
        List<String> geneticProfileStableIds = new ArrayList<>();
        geneticProfileStableIds.add("study_tcga_pub_sv");
        List<String> sampleStableIds = new ArrayList<>();
        List<String> hugoGeneSymbols = new ArrayList<>();
        List<StructuralVariant> result = structuralVariantRepository.getStructuralVariant(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.size());
    }

    @Test
    public void getWithStudyAllSamples() {
        List<String> geneticProfileStableIds = new ArrayList<>();
        geneticProfileStableIds.add("study_tcga_pub_sv");
        List<String> sampleStableIds = new ArrayList<>();
        sampleStableIds.add("TCGA-A1-A0SB-01");
        sampleStableIds.add("TCGA-A1-A0SD-01");
        sampleStableIds.add("TCGA-A1-A0SE-01");
        sampleStableIds.add("TCGA-A1-A0SF-01");
        List<String> hugoGeneSymbols = new ArrayList<>();
        List<StructuralVariant> result = structuralVariantRepository.getStructuralVariant(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.size());
    }

    @Test
    public void getWithStudyTwoSamples() {
        List<String> geneticProfileStableIds = new ArrayList<>();
        geneticProfileStableIds.add("study_tcga_pub_sv");
        List<String> sampleStableIds = new ArrayList<>();
        sampleStableIds.add("TCGA-A1-A0SB-01");
        sampleStableIds.add("TCGA-A1-A0SE-01");
        List<String> hugoGeneSymbols = new ArrayList<>();
        List<StructuralVariant> result = structuralVariantRepository.getStructuralVariant(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getWithStudyOneSample() {
        List<String> geneticProfileStableIds = new ArrayList<>();
        geneticProfileStableIds.add("study_tcga_pub_sv");
        List<String> sampleStableIds = new ArrayList<>();
        sampleStableIds.add("TCGA-A1-A0SF-01");
        List<String> hugoGeneSymbols = new ArrayList<>();
        List<StructuralVariant> result = structuralVariantRepository.getStructuralVariant(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getWithStudyAllSamplesTwoGenes() {
        List<String> geneticProfileStableIds = new ArrayList<>();
        geneticProfileStableIds.add("study_tcga_pub_sv");
        List<String> sampleStableIds = new ArrayList<>();
        sampleStableIds.add("TCGA-A1-A0SB-01");
        sampleStableIds.add("TCGA-A1-A0SD-01");
        sampleStableIds.add("TCGA-A1-A0SE-01");
        sampleStableIds.add("TCGA-A1-A0SF-01");
        List<String> hugoGeneSymbols = new ArrayList<>();
        hugoGeneSymbols.add("GRB7");
        hugoGeneSymbols.add("ERBB2");
        List<StructuralVariant> result = structuralVariantRepository.getStructuralVariant(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.size());
    }

    @Test
    public void getWithStudyTwoSamplesTwoGenes() {
        List<String> geneticProfileStableIds = new ArrayList<>();
        geneticProfileStableIds.add("study_tcga_pub_sv");
        List<String> sampleStableIds = new ArrayList<>();
        sampleStableIds.add("TCGA-A1-A0SB-01");
        sampleStableIds.add("TCGA-A1-A0SE-01");
        List<String> hugoGeneSymbols = new ArrayList<>();
        hugoGeneSymbols.add("GRB7");
        hugoGeneSymbols.add("ERBB2");
        List<StructuralVariant> result = structuralVariantRepository.getStructuralVariant(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getWithStudyOneSampleTwoGenes() {
        List<String> geneticProfileStableIds = new ArrayList<>();
        geneticProfileStableIds.add("study_tcga_pub_sv");
        List<String> sampleStableIds = new ArrayList<>();
        sampleStableIds.add("TCGA-A1-A0SF-01");
        List<String> hugoGeneSymbols = new ArrayList<>();
        hugoGeneSymbols.add("GRB7");
        hugoGeneSymbols.add("ERBB2");
        List<StructuralVariant> result = structuralVariantRepository.getStructuralVariant(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getWithStudyAllSamplesOneGene_1() {
        List<String> geneticProfileStableIds = new ArrayList<>();
        geneticProfileStableIds.add("study_tcga_pub_sv");
        List<String> sampleStableIds = new ArrayList<>();
        sampleStableIds.add("TCGA-A1-A0SB-01");
        sampleStableIds.add("TCGA-A1-A0SD-01");
        sampleStableIds.add("TCGA-A1-A0SE-01");
        sampleStableIds.add("TCGA-A1-A0SF-01");
        List<String> hugoGeneSymbols = new ArrayList<>();
        hugoGeneSymbols.add("ERBB2");
        List<StructuralVariant> result = structuralVariantRepository.getStructuralVariant(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.size());
    }

    @Test
    public void getWithStudyAllSamplesOneGene_2() {
        List<String> geneticProfileStableIds = new ArrayList<>();
        geneticProfileStableIds.add("study_tcga_pub_sv");
        List<String> sampleStableIds = new ArrayList<>();
        sampleStableIds.add("TCGA-A1-A0SB-01");
        sampleStableIds.add("TCGA-A1-A0SD-01");
        sampleStableIds.add("TCGA-A1-A0SE-01");
        sampleStableIds.add("TCGA-A1-A0SF-01");
        List<String> hugoGeneSymbols = new ArrayList<>();
        hugoGeneSymbols.add("GRB7");
        List<StructuralVariant> result = structuralVariantRepository.getStructuralVariant(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getWithStudyTwoSamplesOneGene() {
        List<String> geneticProfileStableIds = new ArrayList<>();
        geneticProfileStableIds.add("study_tcga_pub_sv");
        List<String> sampleStableIds = new ArrayList<>();
        sampleStableIds.add("TCGA-A1-A0SB-01");
        sampleStableIds.add("TCGA-A1-A0SE-01");
        List<String> hugoGeneSymbols = new ArrayList<>();
        hugoGeneSymbols.add("ERBB2");
        List<StructuralVariant> result = structuralVariantRepository.getStructuralVariant(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getWithStudyOneSampleOneGene() {
        List<String> geneticProfileStableIds = new ArrayList<>();
        geneticProfileStableIds.add("study_tcga_pub_sv");
        List<String> sampleStableIds = new ArrayList<>();
        sampleStableIds.add("TCGA-A1-A0SF-01");
        List<String> hugoGeneSymbols = new ArrayList<>();
        hugoGeneSymbols.add("ERBB2");
        List<StructuralVariant> result = structuralVariantRepository.getStructuralVariant(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getWithStudyTwoGenes() {
        List<String> geneticProfileStableIds = new ArrayList<>();
        geneticProfileStableIds.add("study_tcga_pub_sv");
        List<String> sampleStableIds = new ArrayList<>();
        List<String> hugoGeneSymbols = new ArrayList<>();
        hugoGeneSymbols.add("GRB7");
        hugoGeneSymbols.add("ERBB2");
        List<StructuralVariant> result = structuralVariantRepository.getStructuralVariant(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.size());
    }

    @Test
    public void getWithStudyOneGene_1() {
        List<String> geneticProfileStableIds = new ArrayList<>();
        geneticProfileStableIds.add("study_tcga_pub_sv");
        List<String> sampleStableIds = new ArrayList<>();
        List<String> hugoGeneSymbols = new ArrayList<>();
        hugoGeneSymbols.add("GRB7");
        List<StructuralVariant> result = structuralVariantRepository.getStructuralVariant(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getWithStudyOneGene_2() {
        List<String> geneticProfileStableIds = new ArrayList<>();
        geneticProfileStableIds.add("study_tcga_pub_sv");
        List<String> sampleStableIds = new ArrayList<>();
        List<String> hugoGeneSymbols = new ArrayList<>();
        hugoGeneSymbols.add("ERBB2");
        List<StructuralVariant> result = structuralVariantRepository.getStructuralVariant(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.size());
    }

    @Test
    public void getWithIncorrectStudy() {
        List<String> geneticProfileStableIds = new ArrayList<>();
        geneticProfileStableIds.add("incorrect_study_id");
        List<String> sampleStableIds = new ArrayList<>();
        sampleStableIds.add("TCGA-A1-A0SF-01");
        List<String> hugoGeneSymbols = new ArrayList<>();
        hugoGeneSymbols.add("ERBB2");
        List<StructuralVariant> result = structuralVariantRepository.getStructuralVariant(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getWithStudyIncorrectSample() {
        List<String> geneticProfileStableIds = new ArrayList<>();
        geneticProfileStableIds.add("study_tcga_pub_sv");
        List<String> sampleStableIds = new ArrayList<>();
        sampleStableIds.add("incorrect_sample_id");
        List<String> hugoGeneSymbols = new ArrayList<>();
        hugoGeneSymbols.add("ERBB2");
        List<StructuralVariant> result = structuralVariantRepository.getStructuralVariant(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getWithStudyIncorrectGene() {
        List<String> geneticProfileStableIds = new ArrayList<>();
        geneticProfileStableIds.add("study_tcga_pub_sv");
        List<String> sampleStableIds = new ArrayList<>();
        sampleStableIds.add("TCGA-A1-A0SF-01");
        List<String> hugoGeneSymbols = new ArrayList<>();
        hugoGeneSymbols.add("KRAS");
        List<StructuralVariant> result = structuralVariantRepository.getStructuralVariant(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }
}
