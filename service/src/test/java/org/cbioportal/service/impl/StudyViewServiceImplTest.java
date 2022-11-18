package org.cbioportal.service.impl;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.*;
import org.cbioportal.model.util.Select;
import org.cbioportal.service.*;
import org.cbioportal.service.util.MolecularProfileUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class StudyViewServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private StudyViewServiceImpl studyViewService;
    @Mock
    private MolecularProfileService molecularProfileService;
    @Mock
    private GenePanelService genePanelService;
    @Spy
    @InjectMocks
    private MolecularProfileUtil molecularProfileUtil;
    @Mock
    private AlterationCountService alterationCountService;
    @Mock
    private SignificantlyMutatedGeneService significantlyMutatedGeneService;
    @Mock
    private SignificantCopyNumberRegionService significantCopyNumberRegionService;
    @Mock
    private GenericAssayService genericAssayService;
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
