package org.cbioportal.service.impl;

import java.math.BigDecimal;
import java.util.*;

import org.cbioportal.model.*;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.GenericAssayService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.util.ExpressionEnrichmentUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExpressionEnrichmentServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private ExpressionEnrichmentServiceImpl enrichmentServiceImpl;
    @Mock
    private SampleService sampleService;
    @Mock
    private MolecularProfileService molecularProfileService;
    @Mock
    private MolecularDataRepository molecularDataRepository;
    @Mock
    private GeneService geneService;
    @Spy
    @InjectMocks
    private ExpressionEnrichmentUtil expressionEnrichmentUtil;
    @Mock
    private GenericAssayService genericAssayService;

    CancerStudy cancerStudy = new CancerStudy();
    MolecularProfile geneMolecularProfile = new MolecularProfile();
    MolecularProfileSamples molecularProfileSamples = new MolecularProfileSamples();
    List<Sample> samples = new ArrayList<>();
    Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets = new HashMap<>();

    @Before
    public void setup() throws MolecularProfileNotFoundException {
        cancerStudy.setReferenceGenome(ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_NAME);
        cancerStudy.setCancerStudyIdentifier(STUDY_ID);

        geneMolecularProfile.setCancerStudyIdentifier(STUDY_ID);
        geneMolecularProfile.setStableId(MOLECULAR_PROFILE_ID);

        geneMolecularProfile.setCancerStudy(cancerStudy);

        molecularProfileSamples.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        molecularProfileSamples.setCommaSeparatedSampleIds("1,2,3,4");

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

        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID))
                .thenReturn(geneMolecularProfile);

        Mockito.when(molecularDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(MOLECULAR_PROFILE_ID))
                .thenReturn(molecularProfileSamples);

        Mockito.when(sampleService.fetchSamples(Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID),
                Arrays.asList(SAMPLE_ID3, SAMPLE_ID4, SAMPLE_ID1, SAMPLE_ID2), "ID")).thenReturn(samples);
    }

    @Test
    public void getGenomicEnrichments() throws Exception {
        geneMolecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MRNA_EXPRESSION);

        List<GeneMolecularAlteration> molecularDataList = new ArrayList<GeneMolecularAlteration>();
        GeneMolecularAlteration geneMolecularAlteration1 = new GeneMolecularAlteration();
        geneMolecularAlteration1.setEntrezGeneId(ENTREZ_GENE_ID_2);
        geneMolecularAlteration1.setValues("2,3,2.1,3");
        molecularDataList.add(geneMolecularAlteration1);

        GeneMolecularAlteration geneMolecularAlteration2 = new GeneMolecularAlteration();
        geneMolecularAlteration2.setEntrezGeneId(ENTREZ_GENE_ID_3);
        geneMolecularAlteration2.setValues("1.1,5,2.3,3");
        molecularDataList.add(geneMolecularAlteration2);
        Mockito.when(molecularDataRepository.getGeneMolecularAlterationsIterable(MOLECULAR_PROFILE_ID, null, "SUMMARY"))
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

        Mockito.when(geneService.fetchGenes(Arrays.asList("2", "3"), "ENTREZ_GENE_ID", "SUMMARY"))
                .thenReturn(expectedGeneList);

        List<GenomicEnrichment> result = enrichmentServiceImpl.getGenomicEnrichments(MOLECULAR_PROFILE_ID,
                molecularProfileCaseSets, "SAMPLE");

        Assert.assertEquals(2, result.size());
        GenomicEnrichment expressionEnrichment = result.get(0);
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

    @Test
    public void getGenericAssayEnrichments() throws Exception {
        geneMolecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.GENERIC_ASSAY);

        List<GenericAssayMolecularAlteration> molecularDataList = new ArrayList<GenericAssayMolecularAlteration>();
        GenericAssayMolecularAlteration genericAssayMolecularAlteration1 = new GenericAssayMolecularAlteration();
        genericAssayMolecularAlteration1.setGenericAssayStableId(HUGO_GENE_SYMBOL_1);
        genericAssayMolecularAlteration1.setValues("2,3,2.1,3");
        molecularDataList.add(genericAssayMolecularAlteration1);

        GenericAssayMolecularAlteration geneMolecularAlteration2 = new GenericAssayMolecularAlteration();
        geneMolecularAlteration2.setGenericAssayStableId(HUGO_GENE_SYMBOL_2);
        geneMolecularAlteration2.setValues("1.1,5,2.3,3");
        molecularDataList.add(geneMolecularAlteration2);
        Mockito.when(molecularDataRepository.getGenericAssayMolecularAlterationsIterable(MOLECULAR_PROFILE_ID, null,
                "SUMMARY")).thenReturn(molecularDataList);

        Mockito.when(genericAssayService.getGenericAssayMetaByStableIdsAndMolecularIds(
                Arrays.asList(HUGO_GENE_SYMBOL_1, HUGO_GENE_SYMBOL_2),
                Arrays.asList(MOLECULAR_PROFILE_ID, MOLECULAR_PROFILE_ID), "SUMMARY"))
                .thenReturn(Arrays.asList(new GenericAssayMeta(HUGO_GENE_SYMBOL_1),
                        new GenericAssayMeta(HUGO_GENE_SYMBOL_2)));

        List<GenericAssayEnrichment> result = enrichmentServiceImpl.getGenericAssayEnrichments(MOLECULAR_PROFILE_ID,
                molecularProfileCaseSets, "SAMPLE");

        Assert.assertEquals(2, result.size());
        GenericAssayEnrichment genericAssayEnrichment = result.get(0);
        Assert.assertEquals(HUGO_GENE_SYMBOL_1, genericAssayEnrichment.getStableId());
        Assert.assertEquals(2, genericAssayEnrichment.getGroupsStatistics().size());

        GroupStatistics unalteredGroupStats = genericAssayEnrichment.getGroupsStatistics().get(0);
        Assert.assertEquals("unaltered samples", unalteredGroupStats.getName());
        Assert.assertEquals(new BigDecimal("2.55"), unalteredGroupStats.getMeanExpression());
        Assert.assertEquals(new BigDecimal("0.6363961030678927"), unalteredGroupStats.getStandardDeviation());

        GroupStatistics alteredGroupStats = genericAssayEnrichment.getGroupsStatistics().get(1);
        Assert.assertEquals("altered samples", alteredGroupStats.getName());
        Assert.assertEquals(new BigDecimal("2.5"), alteredGroupStats.getMeanExpression());
        Assert.assertEquals(new BigDecimal("0.7071067811865476"), alteredGroupStats.getStandardDeviation());

        Assert.assertEquals(new BigDecimal("0.9475795430163914"), genericAssayEnrichment.getpValue());

        genericAssayEnrichment = result.get(1);
        Assert.assertEquals(HUGO_GENE_SYMBOL_2, genericAssayEnrichment.getStableId());
        Assert.assertEquals(2, genericAssayEnrichment.getGroupsStatistics().size());

        unalteredGroupStats = genericAssayEnrichment.getGroupsStatistics().get(0);
        Assert.assertEquals("unaltered samples", unalteredGroupStats.getName());
        Assert.assertEquals(new BigDecimal("2.65"), unalteredGroupStats.getMeanExpression());
        Assert.assertEquals(new BigDecimal("0.4949747468305834"), unalteredGroupStats.getStandardDeviation());

        alteredGroupStats = genericAssayEnrichment.getGroupsStatistics().get(1);
        Assert.assertEquals("altered samples", alteredGroupStats.getName());
        Assert.assertEquals(new BigDecimal("3.05"), alteredGroupStats.getMeanExpression());
        Assert.assertEquals(new BigDecimal("2.7577164466275352"), alteredGroupStats.getStandardDeviation());

        Assert.assertEquals(new BigDecimal("0.8716148250471419"), genericAssayEnrichment.getpValue());

    }
}
