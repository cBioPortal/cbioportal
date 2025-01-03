package org.cbioportal.service.impl;

import org.cbioportal.model.ExpressionEnrichment;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.model.EnrichmentType;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileSamples;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.ReferenceGenome;
import org.cbioportal.model.Sample;
import org.cbioportal.model.GenericAssayMolecularAlteration;
import org.cbioportal.model.GenericAssayBinaryEnrichment;
import org.cbioportal.model.GenericAssayCategoricalEnrichment;
import org.cbioportal.model.GroupStatistics;

import org.cbioportal.persistence.MolecularDataRepository;
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
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@RunWith(MockitoJUnitRunner.class)
public class GenericAssayEnrichmentServiceImplTest extends BaseServiceImplTest{
    @InjectMocks
    private ExpressionEnrichmentServiceImpl expressionEnrichmentServiceImpl;
    @Mock
    private SampleService sampleService;
    @Mock
    private MolecularProfileService molecularProfileService;
    @Mock
    private MolecularDataRepository molecularDataRepository;

    @Mock
    private GenericAssayService genericAssayService;

    @Mock
    private ExpressionEnrichmentUtil expressionEnrichmentUtil;

    CancerStudy cancerStudy = new CancerStudy();
    MolecularProfile geneMolecularProfile = new MolecularProfile();
    MolecularProfileSamples molecularProfileSamples = new MolecularProfileSamples();
    List<Sample> samples = new ArrayList<>();
    Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets = new HashMap<>();
    Map<String, List<MolecularProfileCaseIdentifier>> molecularProfilePatientLevelCaseSets = new HashMap<>();
    // patient level only data
    public static final String SAMPLE_ID5 = "sample_id5";


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
        sample1.setPatientId(1);
        samples.add(sample1);
        Sample sample2 = new Sample();
        sample2.setStableId(SAMPLE_ID2);
        sample2.setInternalId(2);
        sample2.setCancerStudyIdentifier(STUDY_ID);
        sample2.setPatientId(2);
        samples.add(sample2);
        Sample sample3 = new Sample();
        sample3.setStableId(SAMPLE_ID3);
        sample3.setInternalId(3);
        sample3.setCancerStudyIdentifier(STUDY_ID);
        sample3.setPatientId(3);
        samples.add(sample3);
        Sample sample4 = new Sample();
        sample4.setStableId(SAMPLE_ID4);
        sample4.setInternalId(4);
        sample4.setCancerStudyIdentifier(STUDY_ID);
        sample4.setPatientId(4);
        samples.add(sample4);

        List<MolecularProfileCaseIdentifier> alteredSampleIdentifieres = new ArrayList<>();
        List<MolecularProfileCaseIdentifier> unalteredSampleIdentifieres = new ArrayList<>();
        List<MolecularProfileCaseIdentifier> unalteredPatientLevelSampleIdentifieres = new ArrayList<>();

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
        unalteredPatientLevelSampleIdentifieres.add(caseIdentifier3);

        MolecularProfileCaseIdentifier caseIdentifier4 = new MolecularProfileCaseIdentifier();
        caseIdentifier4.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        caseIdentifier4.setCaseId(SAMPLE_ID4);
        unalteredSampleIdentifieres.add(caseIdentifier4);
        unalteredPatientLevelSampleIdentifieres.add(caseIdentifier4);

        // patient level only data
        MolecularProfileCaseIdentifier caseIdentifier5 = new MolecularProfileCaseIdentifier();
        caseIdentifier5.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        caseIdentifier5.setCaseId(SAMPLE_ID5);
        unalteredPatientLevelSampleIdentifieres.add(caseIdentifier5);

        molecularProfileCaseSets.put("altered samples", alteredSampleIdentifieres);
        molecularProfileCaseSets.put("unaltered samples", unalteredSampleIdentifieres);
        molecularProfilePatientLevelCaseSets.put("altered samples", alteredSampleIdentifieres);
        molecularProfilePatientLevelCaseSets.put("unaltered samples", unalteredPatientLevelSampleIdentifieres);

        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID))
            .thenReturn(geneMolecularProfile);
    }

    @Test
    public void getGenericAssayBinaryEnrichments() throws Exception {
        geneMolecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.GENERIC_ASSAY);
        geneMolecularProfile.setDatatype("BINARY");
        List<GenericAssayMolecularAlteration> molecularDataList = new ArrayList<GenericAssayMolecularAlteration>();
        GenericAssayMolecularAlteration genericAssayMolecularAlteration1 = new GenericAssayMolecularAlteration();
        genericAssayMolecularAlteration1.setGenericAssayStableId(HUGO_GENE_SYMBOL_1);

        // here are 2 groups
        genericAssayMolecularAlteration1.setValues("true,true,true,false");
        molecularDataList.add(genericAssayMolecularAlteration1);

        GenericAssayMolecularAlteration genericAssayMolecularAlteration2 = new GenericAssayMolecularAlteration();
        genericAssayMolecularAlteration2.setGenericAssayStableId(HUGO_GENE_SYMBOL_2);
        genericAssayMolecularAlteration2.setValues("true,false,false,true");
        molecularDataList.add(genericAssayMolecularAlteration2);
        Mockito.when(molecularDataRepository.getGenericAssayMolecularAlterationsIterable(MOLECULAR_PROFILE_ID, null,
            "SUMMARY")).thenReturn(molecularDataList);

        List<ExpressionEnrichment> expectedEnrichments = new ArrayList<>();
        GenericAssayBinaryEnrichment enrichment1 = new GenericAssayBinaryEnrichment();
        enrichment1.setStableId(HUGO_GENE_SYMBOL_1);
        GroupStatistics unalteredGroupStats1 = new GroupStatistics();
        unalteredGroupStats1.setName("unaltered samples");
        unalteredGroupStats1.setMeanExpression(new BigDecimal("0.5"));
        unalteredGroupStats1.setStandardDeviation(new BigDecimal("0.7071067811865476"));
        GroupStatistics alteredGroupStats1 = new GroupStatistics();
        alteredGroupStats1.setName("altered samples");
        alteredGroupStats1.setMeanExpression(new BigDecimal("1.0"));
        alteredGroupStats1.setStandardDeviation(new BigDecimal("0.0"));
        enrichment1.setGroupsStatistics(List.of(unalteredGroupStats1, alteredGroupStats1));
        enrichment1.setpValue(new BigDecimal("0.49999999999999983"));
        enrichment1.setqValue(new BigDecimal("0.99999999999999966"));
        expectedEnrichments.add(enrichment1);

        GenericAssayBinaryEnrichment enrichment2 = new GenericAssayBinaryEnrichment();
        enrichment2.setStableId(HUGO_GENE_SYMBOL_2);
        GroupStatistics unalteredGroupStats2 = new GroupStatistics();
        unalteredGroupStats2.setName("unaltered samples");
        unalteredGroupStats2.setMeanExpression(new BigDecimal("0.5"));
        unalteredGroupStats2.setStandardDeviation(new BigDecimal("0.7071067811865476"));
        GroupStatistics alteredGroupStats2 = new GroupStatistics();
        alteredGroupStats2.setName("altered samples");
        alteredGroupStats2.setMeanExpression(new BigDecimal("0.5"));
        alteredGroupStats2.setStandardDeviation(new BigDecimal("0.7071067811865476"));
        enrichment2.setGroupsStatistics(List.of(unalteredGroupStats2, alteredGroupStats2));
        enrichment2.setpValue(new BigDecimal("1.0"));
        enrichment2.setqValue(new BigDecimal("1.0"));
        expectedEnrichments.add(enrichment2);

        Mockito.when(expressionEnrichmentUtil.getGenericAssayBinaryEnrichments(geneMolecularProfile, molecularProfileCaseSets, EnrichmentType.SAMPLE, molecularDataList))
            .thenReturn(expectedEnrichments);

        Mockito.when(genericAssayService.getGenericAssayMetaByStableIdsAndMolecularIds(
                Arrays.asList(HUGO_GENE_SYMBOL_1, HUGO_GENE_SYMBOL_2),
                Arrays.asList(MOLECULAR_PROFILE_ID, MOLECULAR_PROFILE_ID), "SUMMARY"))
            .thenReturn(Arrays.asList(new GenericAssayMeta(HUGO_GENE_SYMBOL_1),
                new GenericAssayMeta(HUGO_GENE_SYMBOL_2)));

        List<GenericAssayBinaryEnrichment> result = expressionEnrichmentServiceImpl.getGenericAssayBinaryEnrichments(MOLECULAR_PROFILE_ID,
            molecularProfileCaseSets, EnrichmentType.SAMPLE);

        Assert.assertEquals(2, result.size());
        GenericAssayBinaryEnrichment genericAssayBinaryEnrichment = result.get(0);
        Assert.assertEquals(HUGO_GENE_SYMBOL_1, genericAssayBinaryEnrichment.getStableId());
        Assert.assertEquals(2, genericAssayBinaryEnrichment.getGroupsStatistics().size());

        GroupStatistics unalteredGroupStats = genericAssayBinaryEnrichment.getGroupsStatistics().get(0);
        Assert.assertEquals("unaltered samples", unalteredGroupStats.getName());
        Assert.assertEquals(new BigDecimal("0.5"), unalteredGroupStats.getMeanExpression());
        Assert.assertEquals(new BigDecimal("0.7071067811865476"), unalteredGroupStats.getStandardDeviation());

        GroupStatistics alteredGroupStats = genericAssayBinaryEnrichment.getGroupsStatistics().get(1);
        Assert.assertEquals("altered samples", alteredGroupStats.getName());
        Assert.assertEquals(new BigDecimal("1.0"), alteredGroupStats.getMeanExpression());
        Assert.assertEquals(new BigDecimal("0.0"), alteredGroupStats.getStandardDeviation());

        Assert.assertEquals(new BigDecimal("0.49999999999999983"), genericAssayBinaryEnrichment.getpValue());
        Assert.assertEquals(new BigDecimal("0.99999999999999966"), genericAssayBinaryEnrichment.getqValue());

        genericAssayBinaryEnrichment = result.get(1);
        Assert.assertEquals(HUGO_GENE_SYMBOL_2, genericAssayBinaryEnrichment.getStableId());
        Assert.assertEquals(2, genericAssayBinaryEnrichment.getGroupsStatistics().size());

        unalteredGroupStats = genericAssayBinaryEnrichment.getGroupsStatistics().get(0);
        Assert.assertEquals("unaltered samples", unalteredGroupStats.getName());
        Assert.assertEquals(new BigDecimal("0.5"), unalteredGroupStats.getMeanExpression());
        Assert.assertEquals(new BigDecimal("0.7071067811865476"), unalteredGroupStats.getStandardDeviation());

        alteredGroupStats = genericAssayBinaryEnrichment.getGroupsStatistics().get(1);
        Assert.assertEquals("altered samples", alteredGroupStats.getName());
        Assert.assertEquals(new BigDecimal("0.5"), alteredGroupStats.getMeanExpression());
        Assert.assertEquals(new BigDecimal("0.7071067811865476"), alteredGroupStats.getStandardDeviation());

        Assert.assertEquals(new BigDecimal("1.0"), genericAssayBinaryEnrichment.getpValue());
        Assert.assertEquals(new BigDecimal("1.0"), genericAssayBinaryEnrichment.getqValue());
    }


    @Test
    public void getGenericAssayCategoricalEnrichments() throws MolecularProfileNotFoundException {
        geneMolecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.GENERIC_ASSAY);
        geneMolecularProfile.setDatatype("CATEGORICAL");
        List<GenericAssayMolecularAlteration> molecularDataList = new ArrayList<GenericAssayMolecularAlteration>();
        GenericAssayMolecularAlteration genericAssayMolecularAlteration1 = new GenericAssayMolecularAlteration();
        genericAssayMolecularAlteration1.setGenericAssayStableId(HUGO_GENE_SYMBOL_1);
        genericAssayMolecularAlteration1.setValues("category1,category1,category2,category2");
        molecularDataList.add(genericAssayMolecularAlteration1);

        GenericAssayMolecularAlteration genericAssayMolecularAlteration2 = new GenericAssayMolecularAlteration();
        genericAssayMolecularAlteration2.setGenericAssayStableId(HUGO_GENE_SYMBOL_2);
        genericAssayMolecularAlteration2.setValues("category2,category2,category1,category1");
        molecularDataList.add(genericAssayMolecularAlteration2);
        Mockito.when(molecularDataRepository.getGenericAssayMolecularAlterationsIterable(MOLECULAR_PROFILE_ID, null,
            "SUMMARY")).thenReturn(molecularDataList);

        List<ExpressionEnrichment> expectedEnrichments = new ArrayList<>();
        GenericAssayCategoricalEnrichment enrichment1 = new GenericAssayCategoricalEnrichment();
        enrichment1.setStableId(HUGO_GENE_SYMBOL_1);
        GroupStatistics unalteredGroupStats1 = new GroupStatistics();
        unalteredGroupStats1.setName("unaltered samples");
        GroupStatistics alteredGroupStats1 = new GroupStatistics();
        alteredGroupStats1.setName("altered samples");
        enrichment1.setGroupsStatistics(List.of(unalteredGroupStats1, alteredGroupStats1));
        enrichment1.setpValue(new BigDecimal("0.04550026389635764"));
        enrichment1.setqValue(new BigDecimal("0.04550026389635764"));
        expectedEnrichments.add(enrichment1);

        GenericAssayCategoricalEnrichment enrichment2 = new GenericAssayCategoricalEnrichment();
        enrichment2.setStableId(HUGO_GENE_SYMBOL_2);
        GroupStatistics unalteredGroupStats2 = new GroupStatistics();
        unalteredGroupStats2.setName("unaltered samples");
        GroupStatistics alteredGroupStats2 = new GroupStatistics();
        alteredGroupStats2.setName("altered samples");
        enrichment2.setGroupsStatistics(List.of(unalteredGroupStats2, alteredGroupStats2));
        enrichment2.setpValue(new BigDecimal("0.04550026389635764"));
        enrichment2.setqValue(new BigDecimal("0.04550026389635764"));
        expectedEnrichments.add(enrichment2);

        Mockito.when(expressionEnrichmentUtil.getGenericAssayCategoricalEnrichments(geneMolecularProfile, molecularProfileCaseSets, EnrichmentType.SAMPLE, molecularDataList))
            .thenReturn(expectedEnrichments);

        Mockito.when(genericAssayService.getGenericAssayMetaByStableIdsAndMolecularIds(
                Arrays.asList(HUGO_GENE_SYMBOL_1, HUGO_GENE_SYMBOL_2),
                Arrays.asList(MOLECULAR_PROFILE_ID, MOLECULAR_PROFILE_ID), "SUMMARY"))
            .thenReturn(Arrays.asList(new GenericAssayMeta(HUGO_GENE_SYMBOL_1),
                new GenericAssayMeta(HUGO_GENE_SYMBOL_2)));

        List<GenericAssayCategoricalEnrichment> result = expressionEnrichmentServiceImpl.getGenericAssayCategoricalEnrichments(MOLECULAR_PROFILE_ID,
            molecularProfileCaseSets, EnrichmentType.SAMPLE);

        Assert.assertEquals(2, result.size());
        GenericAssayCategoricalEnrichment genericAssayCategoricalEnrichment = result.get(0);
        Assert.assertEquals(HUGO_GENE_SYMBOL_1, genericAssayCategoricalEnrichment.getStableId());
        Assert.assertEquals(2, genericAssayCategoricalEnrichment.getGroupsStatistics().size());

        GroupStatistics unalteredGroupStats = genericAssayCategoricalEnrichment.getGroupsStatistics().get(0);
        Assert.assertEquals("unaltered samples", unalteredGroupStats.getName());

        GroupStatistics alteredGroupStats = genericAssayCategoricalEnrichment.getGroupsStatistics().get(1);
        Assert.assertEquals("altered samples", alteredGroupStats.getName());

        Assert.assertEquals(new BigDecimal("0.04550026389635764"), genericAssayCategoricalEnrichment.getpValue());
        Assert.assertEquals(new BigDecimal("0.04550026389635764"), genericAssayCategoricalEnrichment.getqValue());

        genericAssayCategoricalEnrichment = result.get(1);
        Assert.assertEquals(HUGO_GENE_SYMBOL_2, genericAssayCategoricalEnrichment.getStableId());
        Assert.assertEquals(2, genericAssayCategoricalEnrichment.getGroupsStatistics().size());

        unalteredGroupStats = genericAssayCategoricalEnrichment.getGroupsStatistics().get(0);
        Assert.assertEquals("unaltered samples", unalteredGroupStats.getName());

        alteredGroupStats = genericAssayCategoricalEnrichment.getGroupsStatistics().get(1);
        Assert.assertEquals("altered samples", alteredGroupStats.getName());

        Assert.assertEquals(new BigDecimal("0.04550026389635764"), genericAssayCategoricalEnrichment.getpValue());
        Assert.assertEquals(new BigDecimal("0.04550026389635764"), genericAssayCategoricalEnrichment.getqValue());
    }

}