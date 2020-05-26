package org.cbioportal.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.ExpressionEnrichment;
import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.model.GroupStatistics;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.MolecularProfileSamples;
import org.cbioportal.model.ReferenceGenome;
import org.cbioportal.model.Gene;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.SampleService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExpressionEnrichmentServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private ExpressionEnrichmentServiceImpl expressionEnrichmentService;

    @Mock
    private SampleService sampleService;
    @Mock
    private MolecularProfileService molecularProfileService;
    @Mock
    private MolecularDataService molecularDataService;
    @Mock
    private MolecularDataRepository molecularDataRepository;
    @Mock
    private GeneService geneService;

    @Test
    public void getExpressionEnrichments() throws Exception {
        
        CancerStudy cancerStudy = new CancerStudy();
        cancerStudy.setReferenceGenome(ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_NAME);
        cancerStudy.setCancerStudyIdentifier(STUDY_ID);

        MolecularProfile geneMolecularProfile = new MolecularProfile();
        geneMolecularProfile.setCancerStudyIdentifier(STUDY_ID);
        geneMolecularProfile.setStableId(MOLECULAR_PROFILE_ID);
        geneMolecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MRNA_EXPRESSION);
        geneMolecularProfile.setCancerStudy(cancerStudy);

        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID))
                .thenReturn(geneMolecularProfile);
        
        MolecularProfileSamples molecularProfileSamples = new MolecularProfileSamples();
        molecularProfileSamples.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        molecularProfileSamples.setCommaSeparatedSampleIds("1,2,3,4");

        Mockito.when(molecularDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(MOLECULAR_PROFILE_ID))
                .thenReturn(molecularProfileSamples);

        List<Sample> samples = new ArrayList<>();
        Sample sample1 = new Sample();
        sample1.setStableId(SAMPLE_ID1);
        sample1.setInternalId(1);
        sample1.setCancerStudyIdentifier(STUDY_ID);
        samples.add(sample1);
        Sample sample2 = new Sample();
        sample2.setStableId(SAMPLE_ID2);
        sample2.setInternalId(2);
        sample2.setCancerStudyIdentifier(STUDY_ID);
        samples.add(sample2);
        Sample sample3 = new Sample();
        sample3.setStableId(SAMPLE_ID3);
        sample3.setInternalId(3);
        sample3.setCancerStudyIdentifier(STUDY_ID);
        samples.add(sample3);
        Sample sample4 = new Sample();
        sample4.setStableId(SAMPLE_ID4);
        sample4.setInternalId(4);
        sample4.setCancerStudyIdentifier(STUDY_ID);
        samples.add(sample4);

        Mockito.when(sampleService.fetchSamples(Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID),
                Arrays.asList(SAMPLE_ID3, SAMPLE_ID4, SAMPLE_ID1, SAMPLE_ID2), "ID")).thenReturn(samples);

        List<GeneMolecularAlteration> molecularDataList = new ArrayList<GeneMolecularAlteration>();
        GeneMolecularAlteration geneMolecularAlteration1 = new GeneMolecularAlteration();
        geneMolecularAlteration1.setEntrezGeneId(ENTREZ_GENE_ID_2);
        geneMolecularAlteration1.setValues("2,3,2.1,3");
        molecularDataList.add(geneMolecularAlteration1);

        GeneMolecularAlteration geneMolecularAlteration2 = new GeneMolecularAlteration();
        geneMolecularAlteration2.setEntrezGeneId(ENTREZ_GENE_ID_3);
        geneMolecularAlteration2.setValues("1.1,5,2.3,3");
        molecularDataList.add(geneMolecularAlteration2);
        Mockito.when(molecularDataService.getMolecularAlterations(MOLECULAR_PROFILE_ID, null, "SUMMARY"))
                .thenReturn(molecularDataList);
        
        List<Gene> expectedGeneList = new ArrayList<>();
        Gene gene1 = new Gene();
        gene1.setEntrezGeneId(ENTREZ_GENE_ID_2);
        gene1.setHugoGeneSymbol(HUGO_GENE_SYMBOL_2);
        expectedGeneList.add(gene1);
        Gene gene2 = new Gene();
        gene2.setEntrezGeneId(ENTREZ_GENE_ID_3);
        gene2.setHugoGeneSymbol(HUGO_GENE_SYMBOL_3);
        expectedGeneList.add(gene2);

        Mockito.when(geneService.fetchGenes(Arrays.asList("2", "3"),
            "ENTREZ_GENE_ID","SUMMARY")).thenReturn(expectedGeneList);

        Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets = new HashMap<>();
        List<MolecularProfileCaseIdentifier> alteredSampleIdentifieres = new ArrayList<>();
        List<MolecularProfileCaseIdentifier> unalteredSampleIdentifieres = new ArrayList<>();

        MolecularProfileCaseIdentifier caseIdentifier1 = new MolecularProfileCaseIdentifier();
        caseIdentifier1.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        caseIdentifier1.setCaseId(SAMPLE_ID1);
        alteredSampleIdentifieres.add(caseIdentifier1);

        MolecularProfileCaseIdentifier caseIdentifier2 = new MolecularProfileCaseIdentifier();
        caseIdentifier2.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        caseIdentifier2.setCaseId(SAMPLE_ID2);
        alteredSampleIdentifieres.add(caseIdentifier2);

        MolecularProfileCaseIdentifier caseIdentifier3 = new MolecularProfileCaseIdentifier();
        caseIdentifier3.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        caseIdentifier3.setCaseId(SAMPLE_ID3);
        unalteredSampleIdentifieres.add(caseIdentifier3);

        MolecularProfileCaseIdentifier caseIdentifier4 = new MolecularProfileCaseIdentifier();
        caseIdentifier4.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        caseIdentifier4.setCaseId(SAMPLE_ID4);
        unalteredSampleIdentifieres.add(caseIdentifier4);

        molecularProfileCaseSets.put("altered samples", alteredSampleIdentifieres);
        molecularProfileCaseSets.put("unaltered samples", unalteredSampleIdentifieres);

        List<ExpressionEnrichment> result = expressionEnrichmentService
                .getExpressionEnrichments(MOLECULAR_PROFILE_ID, molecularProfileCaseSets, "SAMPLE");

        Assert.assertEquals(2, result.size());
        ExpressionEnrichment expressionEnrichment = result.get(0);
        Assert.assertEquals(ENTREZ_GENE_ID_2, expressionEnrichment.getEntrezGeneId());
        Assert.assertEquals(HUGO_GENE_SYMBOL_2, expressionEnrichment.getHugoGeneSymbol());
        Assert.assertEquals(null, expressionEnrichment.getCytoband());
        Assert.assertEquals(2, expressionEnrichment.getGroupsStatistics().size());

        GroupStatistics unalteredGroupStats = expressionEnrichment.getGroupsStatistics().get(0);
        Assert.assertEquals("unaltered samples", unalteredGroupStats.getName());
        Assert.assertEquals(new BigDecimal("2.55"), unalteredGroupStats.getMeanExpression());
        Assert.assertEquals(new BigDecimal("0.6363961030678927"), unalteredGroupStats.getStandardDeviation());

        GroupStatistics alteredGroupStats = expressionEnrichment.getGroupsStatistics().get(1);
        Assert.assertEquals("altered samples", alteredGroupStats.getName());
        Assert.assertEquals(new BigDecimal("2.5"), alteredGroupStats.getMeanExpression());
        Assert.assertEquals(new BigDecimal("0.7071067811865476"), alteredGroupStats.getStandardDeviation());

        Assert.assertEquals(new BigDecimal("0.9475795430163914"), expressionEnrichment.getpValue());

        expressionEnrichment = result.get(1);
        Assert.assertEquals(ENTREZ_GENE_ID_3, expressionEnrichment.getEntrezGeneId());
        Assert.assertEquals(HUGO_GENE_SYMBOL_3, expressionEnrichment.getHugoGeneSymbol());
        Assert.assertEquals(null, expressionEnrichment.getCytoband());
        Assert.assertEquals(2, expressionEnrichment.getGroupsStatistics().size());

        unalteredGroupStats = expressionEnrichment.getGroupsStatistics().get(0);
        Assert.assertEquals("unaltered samples", unalteredGroupStats.getName());
        Assert.assertEquals(new BigDecimal("2.65"), unalteredGroupStats.getMeanExpression());
        Assert.assertEquals(new BigDecimal("0.4949747468305834"), unalteredGroupStats.getStandardDeviation());

        alteredGroupStats = expressionEnrichment.getGroupsStatistics().get(1);
        Assert.assertEquals("altered samples", alteredGroupStats.getName());
        Assert.assertEquals(new BigDecimal("3.05"), alteredGroupStats.getMeanExpression());
        Assert.assertEquals(new BigDecimal("2.7577164466275352"), alteredGroupStats.getStandardDeviation());

        Assert.assertEquals(new BigDecimal("0.8716148250471419"), expressionEnrichment.getpValue());

    }
}
