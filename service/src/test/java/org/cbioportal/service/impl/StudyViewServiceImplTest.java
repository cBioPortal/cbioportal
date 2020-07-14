package org.cbioportal.service.impl;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.*;
import org.cbioportal.model.util.Select;
import org.cbioportal.service.*;
import org.cbioportal.service.util.MolecularProfileUtil;
import org.junit.Assert;
import org.junit.Before;
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
        molecularProfiles.add(mutationMolecularProfile);

        MolecularProfile discreteCNAMolecularProfile = new MolecularProfile();
        discreteCNAMolecularProfile.setCancerStudyIdentifier(BaseServiceImplTest.STUDY_ID);
        discreteCNAMolecularProfile.setStableId(BaseServiceImplTest.STUDY_ID + "_gistic");
        discreteCNAMolecularProfile.setName("Discrete CNA");
        discreteCNAMolecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.COPY_NUMBER_ALTERATION);
        discreteCNAMolecularProfile.setDatatype("DISCRETE");
        molecularProfiles.add(discreteCNAMolecularProfile);

        List<String> studyIds = Arrays.asList(BaseServiceImplTest.STUDY_ID, BaseServiceImplTest.STUDY_ID);
        List<String> sampleIds = Arrays.asList(BaseServiceImplTest.SAMPLE_ID1, BaseServiceImplTest.SAMPLE_ID2);

        List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers = new ArrayList<>();
        molecularProfileSampleIdentifiers.add(new MolecularProfileCaseIdentifier(BaseServiceImplTest.SAMPLE_ID1, BaseServiceImplTest.STUDY_ID + "_mutations"));
        molecularProfileSampleIdentifiers.add(new MolecularProfileCaseIdentifier(BaseServiceImplTest.SAMPLE_ID2, BaseServiceImplTest.STUDY_ID + "_mutations"));
        molecularProfileSampleIdentifiers.add(new MolecularProfileCaseIdentifier(BaseServiceImplTest.SAMPLE_ID1, BaseServiceImplTest.STUDY_ID + "_gistic"));
        molecularProfileSampleIdentifiers.add(new MolecularProfileCaseIdentifier(BaseServiceImplTest.SAMPLE_ID2, BaseServiceImplTest.STUDY_ID + "_gistic"));

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
        GenomicDataCount expectedGenomicDataCoun2 = new GenomicDataCount();
        expectedGenomicDataCoun2.setCount(2);
        expectedGenomicDataCoun2.setValue("gistic");
        expectedGenomicDataCoun2.setLabel("Discrete CNA");
        expectedGenomicDataCounts.add(expectedGenomicDataCoun2);

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
        Mockito.when(alterationCountService.getSampleMutationCounts(molecularProfileCaseIdentifiers,
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
        Mockito.when(alterationCountService.getSampleStructuralVariantCounts(
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

        Mockito.when(alterationCountService.getSampleCnaCounts(
            anyList(),
            any(Select.class),
            anyBoolean(),
            anyBoolean(),
            any(AlterationFilter.class))).thenReturn(new Pair<>(alterationCountByGenes, 2L));
        List<CopyNumberCountByGene> result = studyViewService.getCNAAlterationCountByGenes(studyIds, sampleIds, alterationFilter);
        Assert.assertEquals(1, result.size());
    }
}
