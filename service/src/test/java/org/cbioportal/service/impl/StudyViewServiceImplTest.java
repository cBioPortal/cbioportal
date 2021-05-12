package org.cbioportal.service.impl;

import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.MolecularProfileService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

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
}
