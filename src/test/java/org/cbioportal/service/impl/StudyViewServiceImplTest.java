package org.cbioportal.service.impl;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.CNA;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenericAssayData;
import org.cbioportal.model.GenericAssayDataCount;
import org.cbioportal.model.GenericAssayDataCountItem;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.MutationEventType;
import org.cbioportal.model.util.Select;
import org.cbioportal.service.AlterationCountService;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.GenericAssayService;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.SignificantCopyNumberRegionService;
import org.cbioportal.service.SignificantlyMutatedGeneService;
import org.cbioportal.service.util.MolecularProfileUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(MockitoJUnitRunner.class)
public class StudyViewServiceImplTest extends BaseServiceImplTest {
    
    @Spy
    @InjectMocks
    private StudyViewServiceImpl studyViewService;
    @Mock
    private MolecularProfileService molecularProfileService;
    @Mock
    private GenePanelService genePanelService;
    @Spy
    @MockBean
    private MolecularProfileUtil molecularProfileUtil;
    @Mock
    private AlterationCountService alterationCountService;
    @Mock
    private SignificantlyMutatedGeneService significantlyMutatedGeneService;
    @Mock
    private SignificantCopyNumberRegionService significantCopyNumberRegionService;
    @Mock
    private GenericAssayService genericAssayService;
    @Mock
    private MolecularDataService molecularDataService;
    @Mock
    private GeneService geneService;
    @Mock
    private MutationService mutationService;
    private AlterationFilter alterationFilter = new AlterationFilter();

    @Test
    public void getGenomicDataCounts() throws Exception {

        List<MolecularProfile> molecularProfiles = new ArrayList<>();
        MolecularProfile mutationMolecularProfile = new MolecularProfile();
        mutationMolecularProfile.setCancerStudyIdentifier(BaseServiceImplTest.STUDY_ID);
        mutationMolecularProfile.setStableId(BaseServiceImplTest.STUDY_ID + "_mutations");
        mutationMolecularProfile.setName("Mutations");
        mutationMolecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED);
        mutationMolecularProfile.setDatatype("MAF");
        mutationMolecularProfile.setPatientLevel(false);
        molecularProfiles.add(mutationMolecularProfile);

        MolecularProfile discreteCNAMolecularProfile = new MolecularProfile();
        discreteCNAMolecularProfile.setCancerStudyIdentifier(BaseServiceImplTest.STUDY_ID);
        discreteCNAMolecularProfile.setStableId(BaseServiceImplTest.STUDY_ID + "_gistic");
        discreteCNAMolecularProfile.setName("Discrete CNA");
        discreteCNAMolecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.COPY_NUMBER_ALTERATION);
        discreteCNAMolecularProfile.setDatatype("DISCRETE");
        discreteCNAMolecularProfile.setPatientLevel(false);
        molecularProfiles.add(discreteCNAMolecularProfile);

        MolecularProfile patientLevelMolecularProfile = new MolecularProfile();
        patientLevelMolecularProfile.setCancerStudyIdentifier(BaseServiceImplTest.STUDY_ID);
        patientLevelMolecularProfile.setStableId(BaseServiceImplTest.STUDY_ID + "_patient");
        patientLevelMolecularProfile.setName("Patient Profile");
        patientLevelMolecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.GENERIC_ASSAY);
        patientLevelMolecularProfile.setDatatype("LIMIT-VALUE");
        patientLevelMolecularProfile.setPatientLevel(true);
        molecularProfiles.add(patientLevelMolecularProfile);        

        List<String> studyIds = Arrays.asList(BaseServiceImplTest.STUDY_ID, BaseServiceImplTest.STUDY_ID);
        List<String> sampleIds = Arrays.asList(BaseServiceImplTest.SAMPLE_ID1, BaseServiceImplTest.SAMPLE_ID2);

        List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers = new ArrayList<>();
        molecularProfileSampleIdentifiers.add(new MolecularProfileCaseIdentifier(BaseServiceImplTest.SAMPLE_ID1, BaseServiceImplTest.STUDY_ID + "_mutations"));
        molecularProfileSampleIdentifiers.add(new MolecularProfileCaseIdentifier(BaseServiceImplTest.SAMPLE_ID2, BaseServiceImplTest.STUDY_ID + "_mutations"));
        molecularProfileSampleIdentifiers.add(new MolecularProfileCaseIdentifier(BaseServiceImplTest.SAMPLE_ID1, BaseServiceImplTest.STUDY_ID + "_gistic"));
        molecularProfileSampleIdentifiers.add(new MolecularProfileCaseIdentifier(BaseServiceImplTest.SAMPLE_ID2, BaseServiceImplTest.STUDY_ID + "_gistic"));
        molecularProfileSampleIdentifiers.add(new MolecularProfileCaseIdentifier(BaseServiceImplTest.SAMPLE_ID1, BaseServiceImplTest.STUDY_ID + "_patient"));
        molecularProfileSampleIdentifiers.add(new MolecularProfileCaseIdentifier(BaseServiceImplTest.SAMPLE_ID2, BaseServiceImplTest.STUDY_ID + "_patient"));

        List<GenePanelData> genePanelDataList = new ArrayList<>();
        GenePanelData panelData1 = new GenePanelData();
        panelData1.setMolecularProfileId(BaseServiceImplTest.STUDY_ID + "_mutations");
        panelData1.setSampleId(BaseServiceImplTest.SAMPLE_ID1);
        panelData1.setProfiled(true);
        genePanelDataList.add(panelData1);
        GenePanelData panelData2 = new GenePanelData();
        panelData2.setMolecularProfileId(BaseServiceImplTest.STUDY_ID + "_mutations");
        panelData2.setSampleId(BaseServiceImplTest.SAMPLE_ID2);
        panelData2.setProfiled(true);
        genePanelDataList.add(panelData2);
        GenePanelData panelData3 = new GenePanelData();
        panelData3.setMolecularProfileId(BaseServiceImplTest.STUDY_ID + "_gistic");
        panelData3.setSampleId(BaseServiceImplTest.SAMPLE_ID1);
        panelData3.setProfiled(true);
        genePanelDataList.add(panelData3);
        GenePanelData panelData4 = new GenePanelData();
        panelData4.setMolecularProfileId(BaseServiceImplTest.STUDY_ID + "_gistic");
        panelData4.setSampleId(BaseServiceImplTest.SAMPLE_ID2);
        panelData4.setProfiled(true);
        genePanelDataList.add(panelData4);
        GenePanelData panelData5 = new GenePanelData();
        panelData5.setMolecularProfileId(BaseServiceImplTest.STUDY_ID + "_patient");
        panelData5.setSampleId(BaseServiceImplTest.SAMPLE_ID1);
        panelData5.setPatientId(BaseServiceImplTest.PATIENT_ID_1);
        panelData5.setProfiled(true);
        genePanelDataList.add(panelData5);
        GenePanelData panelData6 = new GenePanelData();
        panelData6.setMolecularProfileId(BaseServiceImplTest.STUDY_ID + "_patient");
        panelData6.setSampleId(BaseServiceImplTest.SAMPLE_ID2);
        panelData6.setPatientId(BaseServiceImplTest.PATIENT_ID_1);
        panelData6.setProfiled(true);
        genePanelDataList.add(panelData6);

        Mockito.when(molecularProfileService.getMolecularProfilesInStudies(anyList(), anyString()))
            .thenReturn(molecularProfiles);

        Mockito.when(molecularProfileService.getMolecularProfileCaseIdentifiers(studyIds, sampleIds))
            .thenReturn(molecularProfileSampleIdentifiers);
        Mockito.when(genePanelService.fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileSampleIdentifiers))
            .thenReturn(genePanelDataList);

        List<GenomicDataCount> expectedGenomicDataCounts = new ArrayList<>();
        GenomicDataCount expectedGenomicDataCount1 = new GenomicDataCount();
        expectedGenomicDataCount1.setCount(2);
        expectedGenomicDataCount1.setValue("mutations");
        expectedGenomicDataCount1.setLabel("Mutations");
        expectedGenomicDataCounts.add(expectedGenomicDataCount1);
        GenomicDataCount expectedGenomicDataCount2 = new GenomicDataCount();
        expectedGenomicDataCount2.setCount(1);
        expectedGenomicDataCount2.setValue("patient");
        expectedGenomicDataCount2.setLabel("Patient Profile");
        expectedGenomicDataCounts.add(expectedGenomicDataCount2);
        GenomicDataCount expectedGenomicDataCount3 = new GenomicDataCount();
        expectedGenomicDataCount3.setCount(2);
        expectedGenomicDataCount3.setValue("gistic");
        expectedGenomicDataCount3.setLabel("Discrete CNA");
        expectedGenomicDataCounts.add(expectedGenomicDataCount3);

        List<GenomicDataCount> result = studyViewService.getGenomicDataCounts(studyIds, sampleIds);

        Assert.assertEquals(expectedGenomicDataCounts, result);

    }

    @Test
    public void getMutationAlterationCountByGenes() throws Exception {

        List<String> studyIds = Arrays.asList(BaseServiceImplTest.STUDY_ID, BaseServiceImplTest.STUDY_ID);
        List<String> sampleIds = Arrays.asList(BaseServiceImplTest.SAMPLE_ID1, BaseServiceImplTest.SAMPLE_ID2);


        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setCancerStudyIdentifier(BaseServiceImplTest.STUDY_ID);
        List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers = new ArrayList<>();
        MolecularProfileCaseIdentifier profileCaseIdentifier1 = new MolecularProfileCaseIdentifier(BaseServiceImplTest.SAMPLE_ID1, BaseServiceImplTest.MOLECULAR_PROFILE_ID);
        molecularProfileCaseIdentifiers.add(profileCaseIdentifier1);
        MolecularProfileCaseIdentifier profileCaseIdentifier2 = new MolecularProfileCaseIdentifier(BaseServiceImplTest.SAMPLE_ID2, BaseServiceImplTest.MOLECULAR_PROFILE_ID);
        molecularProfileCaseIdentifiers.add(profileCaseIdentifier2);
        Mockito.when(molecularProfileService.getFirstMutationProfileCaseIdentifiers(studyIds, sampleIds))
            .thenReturn(molecularProfileCaseIdentifiers);

        List<AlterationCountByGene> alterationCountByGenes = new ArrayList<>();
        AlterationCountByGene alterationCountByGene1 = new AlterationCountByGene();
        alterationCountByGene1.setEntrezGeneId(BaseServiceImplTest.ENTREZ_GENE_ID_1);
        alterationCountByGene1.setHugoGeneSymbol(BaseServiceImplTest.HUGO_GENE_SYMBOL_1);
        alterationCountByGene1.setTotalCount(2);
        alterationCountByGene1.setNumberOfProfiledCases(2);
        alterationCountByGenes.add(alterationCountByGene1);
        Mockito.when(alterationCountService.getSampleMutationGeneCounts(molecularProfileCaseIdentifiers,
            Select.all(),
            true,
            false,
            alterationFilter))
            .thenReturn(new Pair<>(alterationCountByGenes, 2L));
        List<AlterationCountByGene> result = studyViewService.getMutationAlterationCountByGenes(studyIds, sampleIds, alterationFilter);
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getMutationCountsByGeneSpecific() {
        List<String> studyIds = Arrays.asList(BaseServiceImplTest.STUDY_ID, BaseServiceImplTest.STUDY_ID);
        List<String> sampleIds = Arrays.asList(BaseServiceImplTest.SAMPLE_ID1, BaseServiceImplTest.SAMPLE_ID2);

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setCancerStudyIdentifier(BaseServiceImplTest.STUDY_ID);
        List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers = new ArrayList<>();
        MolecularProfileCaseIdentifier profileCaseIdentifier1 = new MolecularProfileCaseIdentifier(BaseServiceImplTest.SAMPLE_ID1, BaseServiceImplTest.MOLECULAR_PROFILE_ID);
        molecularProfileCaseIdentifiers.add(profileCaseIdentifier1);
        MolecularProfileCaseIdentifier profileCaseIdentifier2 = new MolecularProfileCaseIdentifier(BaseServiceImplTest.SAMPLE_ID2, BaseServiceImplTest.MOLECULAR_PROFILE_ID);
        molecularProfileCaseIdentifiers.add(profileCaseIdentifier2);
        Mockito.when(molecularProfileService.getMutationProfileCaseIdentifiers(studyIds, sampleIds))
            .thenReturn(molecularProfileCaseIdentifiers);
        
        List<String> hugoGeneSymbols = new ArrayList<>();
        hugoGeneSymbols.add(BaseServiceImplTest.HUGO_GENE_SYMBOL_1);
        List<Gene> genes = new ArrayList<>();
        Gene gene = new Gene();
        gene.setEntrezGeneId(BaseServiceImplTest.ENTREZ_GENE_ID_1);
        gene.setHugoGeneSymbol(BaseServiceImplTest.HUGO_GENE_SYMBOL_1);
        gene.setGeneticEntityId(BaseServiceImplTest.GENETIC_ENTITY_ID_1);
        genes.add(gene);
        Mockito.when(geneService.fetchGenes(hugoGeneSymbols, "HUGO_GENE_SYMBOL", "SUMMARY"))
            .thenReturn(genes);

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(ENTREZ_GENE_ID_1);
        List<AlterationCountByGene> alterationCountByGenes = new ArrayList<>();
        AlterationCountByGene alterationCountByGene1 = new AlterationCountByGene();
        alterationCountByGene1.setEntrezGeneId(BaseServiceImplTest.ENTREZ_GENE_ID_1);
        alterationCountByGene1.setHugoGeneSymbol(BaseServiceImplTest.HUGO_GENE_SYMBOL_1);
        alterationCountByGene1.setNumberOfAlteredCases(2);
        alterationCountByGene1.setTotalCount(2);
        alterationCountByGene1.setNumberOfProfiledCases(2);
        alterationCountByGenes.add(alterationCountByGene1);
        
        Mockito.when(alterationCountService.getSampleMutationGeneCounts(anyList(),
                any(),
                anyBoolean(),
                anyBoolean(),
                any()))
            .thenReturn(new Pair<>(alterationCountByGenes, 2L));
        
        List<Pair<String, String>> genomicDataFilters = new ArrayList<>();
        Pair<String, String> genomicDataFilter = new Pair<>(BaseServiceImplTest.HUGO_GENE_SYMBOL_1, BaseServiceImplTest.PROFILE_TYPE_1);
        genomicDataFilters.add(genomicDataFilter);
        
        List<GenomicDataCountItem> result = studyViewService.getMutationCountsByGeneSpecific(
            studyIds, sampleIds, genomicDataFilters, alterationFilter);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(1, result.get(0).getCounts().size());
    }

    @Test
        public void getMutationTypeCountsByGeneSpecific() {
        List<String> studyIds = Arrays.asList(BaseServiceImplTest.STUDY_ID, BaseServiceImplTest.STUDY_ID);
        List<String> sampleIds = Arrays.asList(BaseServiceImplTest.SAMPLE_ID1, BaseServiceImplTest.SAMPLE_ID2);

        List<String> hugoGeneSymbols = new ArrayList<>();
        hugoGeneSymbols.add(BaseServiceImplTest.HUGO_GENE_SYMBOL_1);
        List<Gene> genes = new ArrayList<>();
        Gene gene = new Gene();
        gene.setEntrezGeneId(BaseServiceImplTest.ENTREZ_GENE_ID_1);
        gene.setHugoGeneSymbol(BaseServiceImplTest.HUGO_GENE_SYMBOL_1);
        gene.setGeneticEntityId(BaseServiceImplTest.GENETIC_ENTITY_ID_1);
        genes.add(gene);
        Mockito.when(geneService.fetchGenes(hugoGeneSymbols, "HUGO_GENE_SYMBOL", "SUMMARY"))
            .thenReturn(genes);

        List<MolecularProfile> molecularProfiles = new ArrayList<>();
        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setCancerStudyIdentifier(BaseServiceImplTest.STUDY_ID);
        molecularProfile.setStableId(BaseServiceImplTest.STABLE_ID_1);
        molecularProfiles.add(molecularProfile);
        Mockito.when(molecularProfileService.getMolecularProfilesInStudies(studyIds, "SUMMARY"))
            .thenReturn(molecularProfiles);
        
        Map<String, List<MolecularProfile>> molecularProfileMap = new HashMap<>();
        molecularProfileMap.put(BaseServiceImplTest.PROFILE_TYPE_1, molecularProfiles);
        Mockito.when(molecularProfileUtil.categorizeMolecularProfilesByStableIdSuffixes(molecularProfiles))
            .thenReturn(molecularProfileMap);
        
        GenomicDataCountItem genomicDataCountItem = new GenomicDataCountItem();
        genomicDataCountItem.setHugoGeneSymbol(BaseServiceImplTest.HUGO_GENE_SYMBOL_1);
        genomicDataCountItem.setProfileType(BaseServiceImplTest.PROFILE_TYPE_1);
        List<GenomicDataCount> genomicDataCounts = new ArrayList<>();
        GenomicDataCount genomicDataCount1 = new GenomicDataCount();
        genomicDataCount1.setLabel(MutationEventType.missense_mutation.getMutationType());
        genomicDataCount1.setValue(MutationEventType.missense_mutation.getMutationType());
        genomicDataCount1.setCount(2);
        genomicDataCounts.add(genomicDataCount1);
        GenomicDataCount genomicDataCount2 = new GenomicDataCount();
        genomicDataCount2.setLabel(MutationEventType.splice_site_indel.getMutationType());
        genomicDataCount2.setValue(MutationEventType.splice_site_indel.getMutationType());
        genomicDataCount2.setCount(2);
        genomicDataCounts.add(genomicDataCount2);
        genomicDataCountItem.setCounts(genomicDataCounts);
        
        Mockito.when(mutationService.getMutationCountsByType(
            anyList(), anyList(), anyList(), anyString()
        )).thenReturn(genomicDataCountItem);

        List<Pair<String, String>> genomicDataFilters = new ArrayList<>();
        Pair<String, String> genomicDataFilter = new Pair<>(BaseServiceImplTest.HUGO_GENE_SYMBOL_1, BaseServiceImplTest.PROFILE_TYPE_1);
        genomicDataFilters.add(genomicDataFilter);

        List<GenomicDataCountItem> result = studyViewService.getMutationTypeCountsByGeneSpecific(
            studyIds, sampleIds, genomicDataFilters);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(BaseServiceImplTest.HUGO_GENE_SYMBOL_1, result.get(0).getHugoGeneSymbol());
        Assert.assertEquals(BaseServiceImplTest.PROFILE_TYPE_1, result.get(0).getProfileType());
        Assert.assertEquals(2, result.get(0).getCounts().get(0).getCount().intValue());
        Assert.assertEquals(2, result.get(0).getCounts().get(1).getCount().intValue());
    }
    
    @Test
    public void getStructuralVariantAlterationCountByGenes() throws Exception {

        List<String> studyIds = Arrays.asList(BaseServiceImplTest.STUDY_ID, BaseServiceImplTest.STUDY_ID);
        List<String> sampleIds = Arrays.asList(BaseServiceImplTest.SAMPLE_ID1, BaseServiceImplTest.SAMPLE_ID2);


        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setCancerStudyIdentifier(BaseServiceImplTest.STUDY_ID);
        List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers = new ArrayList<>();
        MolecularProfileCaseIdentifier profileCaseIdentifier1 = new MolecularProfileCaseIdentifier(BaseServiceImplTest.SAMPLE_ID1, BaseServiceImplTest.MOLECULAR_PROFILE_ID);
        molecularProfileCaseIdentifiers.add(profileCaseIdentifier1);
        MolecularProfileCaseIdentifier profileCaseIdentifier2 = new MolecularProfileCaseIdentifier(BaseServiceImplTest.SAMPLE_ID2, BaseServiceImplTest.MOLECULAR_PROFILE_ID);
        molecularProfileCaseIdentifiers.add(profileCaseIdentifier2);
        Mockito.when(molecularProfileService.getFirstStructuralVariantProfileCaseIdentifiers(studyIds, sampleIds))
            .thenReturn(molecularProfileCaseIdentifiers);

        List<AlterationCountByGene> alterationCountByGenes = new ArrayList<>();
        AlterationCountByGene alterationCountByGene1 = new AlterationCountByGene();
        alterationCountByGene1.setEntrezGeneId(BaseServiceImplTest.ENTREZ_GENE_ID_1);
        alterationCountByGene1.setHugoGeneSymbol(BaseServiceImplTest.HUGO_GENE_SYMBOL_1);
        alterationCountByGene1.setTotalCount(2);
        alterationCountByGene1.setNumberOfProfiledCases(2);
        alterationCountByGenes.add(alterationCountByGene1);
        Mockito.when(alterationCountService.getSampleStructuralVariantGeneCounts(
            molecularProfileCaseIdentifiers,
            Select.all(),
            true,
            false,
            alterationFilter))
            .thenReturn(new Pair<>(alterationCountByGenes, 2L));
        List<AlterationCountByGene> result = studyViewService.getStructuralVariantAlterationCountByGenes(studyIds, sampleIds, alterationFilter);
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getCNAAlterationCountByGenes() throws Exception {

        List<String> studyIds = Arrays.asList(BaseServiceImplTest.STUDY_ID, BaseServiceImplTest.STUDY_ID);
        List<String> sampleIds = Arrays.asList(BaseServiceImplTest.SAMPLE_ID1, BaseServiceImplTest.SAMPLE_ID2);
        List<CNA> CNA_TYPES_AMP_AND_HOMDEL = Collections.unmodifiableList(Arrays.asList(CNA.AMP, CNA.HOMDEL));

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setCancerStudyIdentifier(BaseServiceImplTest.STUDY_ID);
        List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers = new ArrayList<>();
        MolecularProfileCaseIdentifier profileCaseIdentifier1 = new MolecularProfileCaseIdentifier(BaseServiceImplTest.SAMPLE_ID1, BaseServiceImplTest.MOLECULAR_PROFILE_ID);
        molecularProfileCaseIdentifiers.add(profileCaseIdentifier1);
        MolecularProfileCaseIdentifier profileCaseIdentifier2 = new MolecularProfileCaseIdentifier(BaseServiceImplTest.SAMPLE_ID2, BaseServiceImplTest.MOLECULAR_PROFILE_ID);
        molecularProfileCaseIdentifiers.add(profileCaseIdentifier2);
        Mockito.when(molecularProfileService.getFirstDiscreteCNAProfileCaseIdentifiers(studyIds, sampleIds))
            .thenReturn(molecularProfileCaseIdentifiers);

        List<CopyNumberCountByGene> alterationCountByGenes = new ArrayList<>();
        CopyNumberCountByGene alterationCountByGene1 = new CopyNumberCountByGene();
        alterationCountByGene1.setEntrezGeneId(BaseServiceImplTest.ENTREZ_GENE_ID_1);
        alterationCountByGene1.setHugoGeneSymbol(BaseServiceImplTest.HUGO_GENE_SYMBOL_1);
        alterationCountByGene1.setTotalCount(2);
        alterationCountByGene1.setNumberOfProfiledCases(2);
        alterationCountByGene1.setAlteration(2);
        alterationCountByGenes.add(alterationCountByGene1);

        Mockito.when(alterationCountService.getSampleCnaGeneCounts(
            anyList(),
            any(Select.class),
            anyBoolean(),
            anyBoolean(),
            any(AlterationFilter.class))).thenReturn(new Pair<>(alterationCountByGenes, 2L));
        List<CopyNumberCountByGene> result = studyViewService.getCNAAlterationCountByGenes(studyIds, sampleIds, alterationFilter);
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getCNAAlterationCountByGeneSpecific() throws Exception {

        List<String> sampleIds = Arrays.asList(BaseServiceImplTest.SAMPLE_ID1, BaseServiceImplTest.SAMPLE_ID2, BaseServiceImplTest.SAMPLE_ID3);
        List<String> studyIds = Collections.nCopies(3, BaseServiceImplTest.STUDY_ID);
        List<Pair<String, String>> genomicDataFilters = new ArrayList<>();
        Pair<String, String> genomicDataFilter1 = new Pair<>(BaseServiceImplTest.HUGO_GENE_SYMBOL_1, BaseServiceImplTest.PROFILE_TYPE_1);
        Pair<String, String> genomicDataFilter2 = new Pair<>(BaseServiceImplTest.HUGO_GENE_SYMBOL_2, BaseServiceImplTest.PROFILE_TYPE_2);
        genomicDataFilters.add(genomicDataFilter1);
        genomicDataFilters.add(genomicDataFilter2);

        List<MolecularProfile> molecularProfiles = new ArrayList<>();
        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setCancerStudyIdentifier(BaseServiceImplTest.STUDY_ID);
        molecularProfile.setStableId(BaseServiceImplTest.STUDY_ID + "_" + BaseServiceImplTest.MOLECULAR_PROFILE_ID_A);
        molecularProfiles.add(molecularProfile);
        
        Mockito.when(molecularProfileService.getMolecularProfilesInStudies(studyIds, "SUMMARY"))
            .thenReturn(molecularProfiles);
        
        Map<String, List<MolecularProfile>> molecularProfileMap = new HashMap<>();
        molecularProfileMap.put(BaseServiceImplTest.PROFILE_TYPE_1, molecularProfiles);
        molecularProfileMap.put(BaseServiceImplTest.PROFILE_TYPE_2, molecularProfiles);
   
        Mockito.when(molecularProfileUtil.categorizeMolecularProfilesByStableIdSuffixes(molecularProfiles))
            .thenReturn(molecularProfileMap);
        
        List<Gene> genes = new ArrayList<>();
        Gene gene1 = new Gene();
        gene1.setEntrezGeneId(BaseServiceImplTest.ENTREZ_GENE_ID_1);
        gene1.setHugoGeneSymbol(BaseServiceImplTest.HUGO_GENE_SYMBOL_1);
        Gene gene2 = new Gene();
        gene2.setEntrezGeneId(BaseServiceImplTest.ENTREZ_GENE_ID_2);
        gene2.setHugoGeneSymbol(BaseServiceImplTest.HUGO_GENE_SYMBOL_2);
        genes.add(gene1);
        genes.add(gene2);

        Mockito.when(geneService.fetchGenes(anyList(), anyString(), anyString()))
                .thenReturn(genes);

        List<GeneMolecularData> geneMolecularData = new ArrayList<>();
        GeneMolecularData geneMolecularData1 = new GeneMolecularData();
        geneMolecularData1.setValue("-2");
        GeneMolecularData geneMolecularData2 = new GeneMolecularData();
        geneMolecularData2.setValue("-2");
        GeneMolecularData geneMolecularData3 = new GeneMolecularData();
        geneMolecularData3.setValue("2");
        GeneMolecularData geneMolecularData4 = new GeneMolecularData();
        geneMolecularData4.setValue("2");
        geneMolecularData.add(geneMolecularData1);
        geneMolecularData.add(geneMolecularData2);
        geneMolecularData.add(geneMolecularData3);
        geneMolecularData.add(geneMolecularData4);
        
        Mockito.when(molecularDataService.getMolecularDataInMultipleMolecularProfiles(
            anyList(),
            anyList(),
            anyList(),
            anyString()))
            .thenReturn(geneMolecularData);
        
        List<GenomicDataCountItem> result = studyViewService.getCNAAlterationCountsByGeneSpecific(studyIds, sampleIds, genomicDataFilters);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(BaseServiceImplTest.HUGO_GENE_SYMBOL_1, result.get(0).getHugoGeneSymbol());
        Assert.assertEquals(BaseServiceImplTest.PROFILE_TYPE_1, result.get(0).getProfileType());
        Assert.assertEquals(2, result.get(0).getCounts().get(0).getCount().intValue());
        Assert.assertEquals(2, result.get(0).getCounts().get(1).getCount().intValue());
        Assert.assertEquals(BaseServiceImplTest.HUGO_GENE_SYMBOL_2, result.get(1).getHugoGeneSymbol());
        Assert.assertEquals(BaseServiceImplTest.PROFILE_TYPE_2, result.get(1).getProfileType());
        Assert.assertEquals(2, result.get(1).getCounts().get(0).getCount().intValue());
        Assert.assertEquals(2, result.get(1).getCounts().get(1).getCount().intValue());
    }

    @Test
    public void fetchGenericAssayDataCounts() throws Exception {

        List<String> stableIds = Arrays.asList(BaseServiceImplTest.STABLE_ID_1, BaseServiceImplTest.STABLE_ID_2);
        List<String> molecularProfileIds = Collections.nCopies(3, BaseServiceImplTest.STUDY_ID + "_" + BaseServiceImplTest.MOLECULAR_PROFILE_ID_A);
        List<String> sampleIds = Arrays.asList(BaseServiceImplTest.SAMPLE_ID1, BaseServiceImplTest.SAMPLE_ID2, BaseServiceImplTest.SAMPLE_ID3);
        List<String> studyIds = Collections.nCopies(3, BaseServiceImplTest.STUDY_ID);
        List<String> profileTypes = Arrays.asList(BaseServiceImplTest.MOLECULAR_PROFILE_ID_A);

        List<GenericAssayData> gaDataList = new ArrayList<>();
        GenericAssayData gaData1 = new GenericAssayData();
        gaData1.setGenericAssayStableId(BaseServiceImplTest.STABLE_ID_1);
        gaData1.setSampleId(BaseServiceImplTest.SAMPLE_ID1);
        gaData1.setValue(BaseServiceImplTest.CATEGORY_VALUE_1);
        gaDataList.add(gaData1);

        GenericAssayData gaData2 = new GenericAssayData();
        gaData2.setGenericAssayStableId(BaseServiceImplTest.STABLE_ID_1);
        gaData2.setSampleId(BaseServiceImplTest.SAMPLE_ID2);
        gaData2.setValue(BaseServiceImplTest.CATEGORY_VALUE_1);
        gaDataList.add(gaData2);

        GenericAssayData gaData3 = new GenericAssayData();
        gaData3.setGenericAssayStableId(BaseServiceImplTest.STABLE_ID_1);
        gaData3.setSampleId(BaseServiceImplTest.SAMPLE_ID3);
        gaData3.setValue(BaseServiceImplTest.CATEGORY_VALUE_2);
        gaDataList.add(gaData3);

        GenericAssayData gaData4 = new GenericAssayData();
        gaData4.setGenericAssayStableId(BaseServiceImplTest.STABLE_ID_2);
        gaData4.setSampleId(BaseServiceImplTest.SAMPLE_ID1);
        gaData4.setValue(BaseServiceImplTest.CATEGORY_VALUE_1);
        gaDataList.add(gaData4);

        GenericAssayData gaData5 = new GenericAssayData();
        gaData5.setGenericAssayStableId(BaseServiceImplTest.STABLE_ID_2);
        gaData5.setSampleId(BaseServiceImplTest.SAMPLE_ID2);
        gaData5.setValue(BaseServiceImplTest.EMPTY_VALUE_1);
        gaDataList.add(gaData5);

        GenericAssayData gaData6 = new GenericAssayData();
        gaData6.setGenericAssayStableId(BaseServiceImplTest.STABLE_ID_2);
        gaData6.setSampleId(BaseServiceImplTest.SAMPLE_ID3);
        gaData6.setValue(BaseServiceImplTest.EMPTY_VALUE_2);
        gaDataList.add(gaData6);

        Mockito.when(genericAssayService.fetchGenericAssayData(molecularProfileIds, sampleIds, stableIds, "SUMMARY"))
            .thenReturn(gaDataList);

        List<MolecularProfile> molecularProfiles = new ArrayList<>();
        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setCancerStudyIdentifier(BaseServiceImplTest.STUDY_ID);
        molecularProfile.setStableId(BaseServiceImplTest.STUDY_ID + "_" + BaseServiceImplTest.MOLECULAR_PROFILE_ID_A);
        molecularProfiles.add(molecularProfile);
        
        Mockito.when(molecularProfileService.getMolecularProfilesInStudies(studyIds, "SUMMARY"))
            .thenReturn(molecularProfiles);

        List<GenericAssayDataCountItem> expectedCountItems = new ArrayList<>();
        GenericAssayDataCountItem countItem1 = new GenericAssayDataCountItem();
        countItem1.setStableId(BaseServiceImplTest.STABLE_ID_1);
        GenericAssayDataCount count1 = new GenericAssayDataCount();
        count1.setValue(BaseServiceImplTest.CATEGORY_VALUE_1);
        count1.setCount(2);
        GenericAssayDataCount count2 = new GenericAssayDataCount();
        count2.setValue(BaseServiceImplTest.CATEGORY_VALUE_2);
        count2.setCount(1);       
        countItem1.setCounts(Arrays.asList(count1, count2));
        expectedCountItems.add(countItem1);

        GenericAssayDataCountItem countItem2 = new GenericAssayDataCountItem();
        countItem2.setStableId(BaseServiceImplTest.STABLE_ID_2);
        GenericAssayDataCount count3 = new GenericAssayDataCount();
        count3.setValue(BaseServiceImplTest.CATEGORY_VALUE_1);
        count3.setCount(1);
        GenericAssayDataCount count4 = new GenericAssayDataCount();
        count4.setValue("NA");
        count4.setCount(2);
        countItem2.setCounts(Arrays.asList(count3, count4));
        expectedCountItems.add(countItem2);
        
        List<GenericAssayDataCountItem> result = studyViewService.fetchGenericAssayDataCounts(sampleIds, studyIds, stableIds, profileTypes);

        Assert.assertEquals(2, result.size());

        GenericAssayDataCountItem item1 = result.get(0);
        Assert.assertEquals(BaseServiceImplTest.STABLE_ID_1, item1.getStableId());
        GenericAssayDataCount countInItem1 = item1.getCounts().get(0);
        Assert.assertEquals(BaseServiceImplTest.CATEGORY_VALUE_1, countInItem1.getValue());
        Assert.assertEquals((Integer) 2, countInItem1.getCount());
        GenericAssayDataCount countInItem2 = item1.getCounts().get(1);
        Assert.assertEquals(BaseServiceImplTest.CATEGORY_VALUE_2, countInItem2.getValue());
        Assert.assertEquals((Integer) 1, countInItem2.getCount());

        GenericAssayDataCountItem item2 = result.get(1);
        Assert.assertEquals(BaseServiceImplTest.STABLE_ID_2, item2.getStableId());
        GenericAssayDataCount countInItem3 = item2.getCounts().get(0);
        Assert.assertEquals(BaseServiceImplTest.CATEGORY_VALUE_1, countInItem3.getValue());
        Assert.assertEquals((Integer) 1, countInItem3.getCount());
        GenericAssayDataCount countInItem4 = item2.getCounts().get(1);
        Assert.assertEquals(BaseServiceImplTest.EMPTY_VALUE_2, countInItem4.getValue());
        Assert.assertEquals((Integer) 2, countInItem4.getCount());
    }
}
