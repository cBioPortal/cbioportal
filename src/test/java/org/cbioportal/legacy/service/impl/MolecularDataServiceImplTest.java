package org.cbioportal.legacy.service.impl;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cbioportal.legacy.model.DiscreteCopyNumberData;
import org.cbioportal.legacy.model.GeneMolecularAlteration;
import org.cbioportal.legacy.model.GeneMolecularData;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfileSamples;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.DiscreteCopyNumberRepository;
import org.cbioportal.legacy.persistence.MolecularDataRepository;
import org.cbioportal.legacy.persistence.SampleListRepository;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.SampleService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MolecularDataServiceImplTest extends BaseServiceImplTest {

  @InjectMocks private MolecularDataServiceImpl molecularDataService;

  @Mock private MolecularDataRepository molecularDataRepository;
  @Mock private DiscreteCopyNumberRepository discreteCopyNumberRepository;
  @Mock private SampleService sampleService;
  @Mock private MolecularProfileService molecularProfileService;
  @Mock private SampleListRepository sampleListRepository;

  @Test
  public void getMolecularData() throws Exception {

    when(sampleListRepository.getAllSampleIdsInSampleList(SAMPLE_LIST_ID))
        .thenReturn(Arrays.asList(SAMPLE_ID1));

    MolecularProfileSamples molecularProfileSamples = new MolecularProfileSamples();
    molecularProfileSamples.setMolecularProfileId(MOLECULAR_PROFILE_ID);
    molecularProfileSamples.setCommaSeparatedSampleIds("1,2,");

    when(molecularDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(MOLECULAR_PROFILE_ID))
        .thenReturn(molecularProfileSamples);

    MolecularProfile molecularProfile = new MolecularProfile();
    molecularProfile.setCancerStudyIdentifier(STUDY_ID);
    molecularProfile.setMolecularAlterationType(
        MolecularProfile.MolecularAlterationType.MRNA_EXPRESSION);
    when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID))
        .thenReturn(molecularProfile);

    List<Sample> sampleList = new ArrayList<>();
    Sample sample = new Sample();
    sample.setInternalId(1);
    sample.setStableId(SAMPLE_ID1);
    sampleList.add(sample);
    when(sampleService.fetchSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID1), "ID"))
        .thenReturn(sampleList);

    List<GeneMolecularAlteration> molecularAlterationList = new ArrayList<>();
    GeneMolecularAlteration molecularAlteration = new GeneMolecularAlteration();
    molecularAlteration.setEntrezGeneId(ENTREZ_GENE_ID_1);
    molecularAlteration.setValues("0.4674,-0.3456");
    molecularAlterationList.add(molecularAlteration);

    List<Integer> entrezGeneIds = new ArrayList<>();
    entrezGeneIds.add(ENTREZ_GENE_ID_1);
    when(molecularDataRepository.getGeneMolecularAlterations(
            MOLECULAR_PROFILE_ID, entrezGeneIds, PROJECTION))
        .thenReturn(molecularAlterationList);

    List<GeneMolecularData> result =
        molecularDataService.getMolecularData(
            MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID, entrezGeneIds, PROJECTION);

    Assert.assertEquals(1, result.size());
    GeneMolecularData molecularData = result.get(0);
    Assert.assertEquals(ENTREZ_GENE_ID_1, molecularData.getEntrezGeneId());
    Assert.assertEquals(MOLECULAR_PROFILE_ID, molecularData.getMolecularProfileId());
    Assert.assertEquals(SAMPLE_ID1, molecularData.getSampleId());
    Assert.assertEquals("0.4674", molecularData.getValue());
  }

  @Test
  public void getMetaMolecularData() throws Exception {

    when(sampleListRepository.getAllSampleIdsInSampleList(SAMPLE_LIST_ID))
        .thenReturn(Arrays.asList(SAMPLE_ID1));

    MolecularProfileSamples molecularProfileSamples = new MolecularProfileSamples();
    molecularProfileSamples.setMolecularProfileId(MOLECULAR_PROFILE_ID);
    molecularProfileSamples.setCommaSeparatedSampleIds("1,2,");

    when(molecularDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(MOLECULAR_PROFILE_ID))
        .thenReturn(molecularProfileSamples);

    MolecularProfile molecularProfile = new MolecularProfile();
    molecularProfile.setCancerStudyIdentifier(STUDY_ID);
    molecularProfile.setMolecularAlterationType(
        MolecularProfile.MolecularAlterationType.MRNA_EXPRESSION);
    when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID))
        .thenReturn(molecularProfile);

    List<Sample> sampleList = new ArrayList<>();
    Sample sample = new Sample();
    sample.setInternalId(1);
    sample.setStableId(SAMPLE_ID1);
    sampleList.add(sample);
    when(sampleService.fetchSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID1), "ID"))
        .thenReturn(sampleList);

    List<GeneMolecularAlteration> molecularAlterationList = new ArrayList<>();
    GeneMolecularAlteration molecularAlteration = new GeneMolecularAlteration();
    molecularAlteration.setEntrezGeneId(ENTREZ_GENE_ID_1);
    molecularAlteration.setValues("0.4674,-0.3456");
    molecularAlterationList.add(molecularAlteration);

    List<Integer> entrezGeneIds = new ArrayList<>();
    entrezGeneIds.add(ENTREZ_GENE_ID_1);
    when(molecularDataRepository.getGeneMolecularAlterations(
            MOLECULAR_PROFILE_ID, entrezGeneIds, "ID"))
        .thenReturn(molecularAlterationList);

    BaseMeta result =
        molecularDataService.getMetaMolecularData(
            MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID, entrezGeneIds);

    Assert.assertEquals((Integer) 1, result.getTotalCount());
  }

  @Test
  public void getMolecularDataOfAllSamplesOfMolecularProfile() throws Exception {

    MolecularProfile molecularProfile = new MolecularProfile();
    molecularProfile.setMolecularAlterationType(
        MolecularProfile.MolecularAlterationType.MRNA_EXPRESSION);
    when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID))
        .thenReturn(molecularProfile);

    MolecularProfileSamples molecularProfileSamples = new MolecularProfileSamples();
    molecularProfileSamples.setMolecularProfileId(MOLECULAR_PROFILE_ID);
    molecularProfileSamples.setCommaSeparatedSampleIds("1,2,");

    when(molecularDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(MOLECULAR_PROFILE_ID))
        .thenReturn(molecularProfileSamples);

    List<GeneMolecularAlteration> molecularAlterationList = new ArrayList<>();
    GeneMolecularAlteration molecularAlteration = new GeneMolecularAlteration();
    molecularAlteration.setEntrezGeneId(ENTREZ_GENE_ID_1);
    molecularAlteration.setValues("0.4674,-0.3456");
    molecularAlterationList.add(molecularAlteration);

    List<Integer> entrezGeneIds = new ArrayList<>();
    entrezGeneIds.add(ENTREZ_GENE_ID_1);
    when(molecularDataRepository.getGeneMolecularAlterations(
            MOLECULAR_PROFILE_ID, entrezGeneIds, PROJECTION))
        .thenReturn(molecularAlterationList);

    List<Integer> internalIds = new ArrayList<>();
    internalIds.add(1);
    internalIds.add(2);

    List<Sample> samples = new ArrayList<>();
    Sample sample1 = new Sample();
    sample1.setInternalId(1);
    sample1.setStableId(SAMPLE_ID1);
    samples.add(sample1);
    Sample sample2 = new Sample();
    sample2.setInternalId(2);
    sample2.setStableId("sample_id_2");
    samples.add(sample2);
    when(sampleService.getSamplesByInternalIds(internalIds)).thenReturn(samples);

    List<GeneMolecularData> result =
        molecularDataService.fetchMolecularData(
            MOLECULAR_PROFILE_ID, null, entrezGeneIds, PROJECTION);

    Assert.assertEquals(2, result.size());
    GeneMolecularData molecularData1 = result.get(0);
    Assert.assertEquals(ENTREZ_GENE_ID_1, molecularData1.getEntrezGeneId());
    Assert.assertEquals(MOLECULAR_PROFILE_ID, molecularData1.getMolecularProfileId());
    Assert.assertEquals(SAMPLE_ID1, molecularData1.getSampleId());
    Assert.assertEquals("0.4674", molecularData1.getValue());
    GeneMolecularData molecularData2 = result.get(1);
    Assert.assertEquals(ENTREZ_GENE_ID_1, molecularData2.getEntrezGeneId());
    Assert.assertEquals(MOLECULAR_PROFILE_ID, molecularData2.getMolecularProfileId());
    Assert.assertEquals("sample_id_2", molecularData2.getSampleId());
    Assert.assertEquals("-0.3456", molecularData2.getValue());
  }

  @Test
  public void fetchMetaMolecularData() throws Exception {

    MolecularProfile molecularProfile = new MolecularProfile();
    molecularProfile.setMolecularAlterationType(
        MolecularProfile.MolecularAlterationType.MRNA_EXPRESSION);
    when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID))
        .thenReturn(molecularProfile);

    MolecularProfileSamples molecularProfileSamples = new MolecularProfileSamples();
    molecularProfileSamples.setMolecularProfileId(MOLECULAR_PROFILE_ID);
    molecularProfileSamples.setCommaSeparatedSampleIds("1,2,");

    when(molecularDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(MOLECULAR_PROFILE_ID))
        .thenReturn(molecularProfileSamples);

    List<GeneMolecularAlteration> molecularAlterationList = new ArrayList<>();
    GeneMolecularAlteration molecularAlteration = new GeneMolecularAlteration();
    molecularAlteration.setEntrezGeneId(ENTREZ_GENE_ID_1);
    molecularAlteration.setValues("0.4674,-0.3456");
    molecularAlterationList.add(molecularAlteration);

    List<Integer> entrezGeneIds = new ArrayList<>();
    entrezGeneIds.add(ENTREZ_GENE_ID_1);
    when(molecularDataRepository.getGeneMolecularAlterations(
            MOLECULAR_PROFILE_ID, entrezGeneIds, "ID"))
        .thenReturn(molecularAlterationList);

    List<Integer> internalIds = new ArrayList<>();
    internalIds.add(1);
    internalIds.add(2);

    List<Sample> samples = new ArrayList<>();
    Sample sample1 = new Sample();
    sample1.setInternalId(1);
    sample1.setStableId(SAMPLE_ID1);
    samples.add(sample1);
    Sample sample2 = new Sample();
    sample2.setInternalId(2);
    sample2.setStableId("sample_id_2");
    samples.add(sample2);
    when(sampleService.getSamplesByInternalIds(internalIds)).thenReturn(samples);

    BaseMeta result =
        molecularDataService.fetchMetaMolecularData(MOLECULAR_PROFILE_ID, null, entrezGeneIds);

    Assert.assertEquals((Integer) 2, result.getTotalCount());
  }

  @Test
  public void getNumberOfSamplesInMolecularProfile() throws Exception {

    MolecularProfileSamples molecularProfileSamples = new MolecularProfileSamples();
    molecularProfileSamples.setMolecularProfileId(MOLECULAR_PROFILE_ID);
    molecularProfileSamples.setCommaSeparatedSampleIds("1,2,");

    when(molecularDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(MOLECULAR_PROFILE_ID))
        .thenReturn(molecularProfileSamples);

    Integer result =
        molecularDataService.getNumberOfSamplesInMolecularProfile(MOLECULAR_PROFILE_ID);

    Assert.assertEquals((Integer) 2, result);
  }

  @Test
  public void getMolecularDataInMultipleMolecularProfilesByGeneQueries() {

    // two record come in ..
    List<GeneMolecularData> unfilteredData = new ArrayList<>();
    GeneMolecularData geneMolecularData1 = new GeneMolecularData();
    geneMolecularData1.setEntrezGeneId(1);
    geneMolecularData1.setMolecularProfileId("profile1");
    geneMolecularData1.setValue("-2");
    geneMolecularData1.setSampleId("sample1");
    GeneMolecularData geneMolecularData2 = new GeneMolecularData();
    geneMolecularData2.setEntrezGeneId(1);
    geneMolecularData2.setMolecularProfileId("profile1");
    geneMolecularData2.setValue("-1");
    geneMolecularData2.setSampleId("sample2");
    unfilteredData.add(geneMolecularData1);
    unfilteredData.add(geneMolecularData2);

    MolecularDataServiceImpl spy = spy(molecularDataService);
    doReturn(unfilteredData)
        .when(spy)
        .getMolecularDataInMultipleMolecularProfiles(anyList(), anyList(), anyList(), anyString());

    List<DiscreteCopyNumberData> selectedCnaEvents = new ArrayList<>();
    DiscreteCopyNumberData discreteCopyNumberData1 = new DiscreteCopyNumberData();
    discreteCopyNumberData1.setEntrezGeneId(1);
    discreteCopyNumberData1.setMolecularProfileId("profile1");
    discreteCopyNumberData1.setAlteration(-2);
    discreteCopyNumberData1.setSampleId("sample1");
    selectedCnaEvents.add(discreteCopyNumberData1);

    when(discreteCopyNumberRepository
            .getDiscreteCopyNumbersInMultipleMolecularProfilesByGeneQueries(
                anyList(), anyList(), anyList(), anyString()))
        .thenReturn(selectedCnaEvents);

    List<GeneMolecularData> filteredData =
        spy.getMolecularDataInMultipleMolecularProfilesByGeneQueries(
            Arrays.asList(), Arrays.asList(), Arrays.asList(), "projection");

    // one record comes out ...
    // so, test whether record correctly removed from result set
    Assert.assertEquals(1, filteredData.size());
    Assert.assertEquals("sample1", filteredData.get(0).getSampleId());
  }

  @Test
  public void getMolecularDataInMultipleMolecularProfiles() throws Exception {

    // Setup sample lists for two molecular profiles
    MolecularProfileSamples mpsA = new MolecularProfileSamples();
    mpsA.setMolecularProfileId(MOLECULAR_PROFILE_ID_A);
    mpsA.setCommaSeparatedSampleIds("1,2,");

    MolecularProfileSamples mpsB = new MolecularProfileSamples();
    mpsB.setMolecularProfileId(MOLECULAR_PROFILE_ID_B);
    mpsB.setCommaSeparatedSampleIds("3,");

    when(molecularDataRepository.commaSeparatedSampleIdsOfMolecularProfilesMap(
            new java.util.TreeSet<>(Arrays.asList(MOLECULAR_PROFILE_ID_A, MOLECULAR_PROFILE_ID_B))))
        .thenReturn(java.util.Map.of(MOLECULAR_PROFILE_ID_A, mpsA, MOLECULAR_PROFILE_ID_B, mpsB));

    // Configure molecular profiles
    MolecularProfile mpA = new MolecularProfile();
    mpA.setStableId(MOLECULAR_PROFILE_ID_A);
    mpA.setCancerStudyIdentifier(STUDY_ID);
    MolecularProfile mpB = new MolecularProfile();
    mpB.setStableId(MOLECULAR_PROFILE_ID_B);
    mpB.setCancerStudyIdentifier(STUDY_ID);
    when(molecularProfileService.getMolecularProfiles(
            new java.util.TreeSet<>(Arrays.asList(MOLECULAR_PROFILE_ID_A, MOLECULAR_PROFILE_ID_B)), "SUMMARY"))
        .thenReturn(Arrays.asList(mpA, mpB));

    // Prepare sample list across profiles
    List<Sample> samples = new ArrayList<>();
    Sample s1 = new Sample();
    s1.setInternalId(1);
    s1.setStableId(SAMPLE_ID1);
    s1.setPatientStableId(PATIENT_ID_1);
    s1.setCancerStudyIdentifier(STUDY_ID);
    samples.add(s1);

    Sample s2 = new Sample();
    s2.setInternalId(2);
    s2.setStableId(SAMPLE_ID2);
    s2.setPatientStableId(PATIENT_ID_2);
    s2.setCancerStudyIdentifier(STUDY_ID);
    samples.add(s2);

    Sample s3 = new Sample();
    s3.setInternalId(3);
    s3.setStableId(SAMPLE_ID3);
    s3.setPatientStableId(PATIENT_ID_3);
    s3.setCancerStudyIdentifier(STUDY_ID);
    samples.add(s3);

    when(sampleService.getSamplesByInternalIds(Arrays.asList(1, 2, 3))).thenReturn(samples);

    // Mock molecular alterations for both genes and both profiles
    GeneMolecularAlteration alterationA1 = new GeneMolecularAlteration();
    alterationA1.setMolecularProfileId(MOLECULAR_PROFILE_ID_A);
    alterationA1.setEntrezGeneId(ENTREZ_GENE_ID_1);
    alterationA1.setValues("1,0");

    GeneMolecularAlteration alterationA2 = new GeneMolecularAlteration();
    alterationA2.setMolecularProfileId(MOLECULAR_PROFILE_ID_A);
    alterationA2.setEntrezGeneId(ENTREZ_GENE_ID_2);
    alterationA2.setValues("0,1");

    GeneMolecularAlteration alterationB1 = new GeneMolecularAlteration();
    alterationB1.setMolecularProfileId(MOLECULAR_PROFILE_ID_B);
    alterationB1.setEntrezGeneId(ENTREZ_GENE_ID_1);
    alterationB1.setValues("1");

    List<GeneMolecularAlteration> molecularAlterationList =
        Arrays.asList(alterationA1, alterationA2, alterationB1);

    when(molecularDataRepository
            .getGeneMolecularAlterationsInMultipleMolecularProfiles(
                new java.util.TreeSet<>(Arrays.asList(MOLECULAR_PROFILE_ID_A, MOLECULAR_PROFILE_ID_B)),
                Arrays.asList(ENTREZ_GENE_ID_1, ENTREZ_GENE_ID_2),
                PROJECTION))
        .thenReturn(molecularAlterationList);

    // Call the method under test
    List<GeneMolecularData> result =
        molecularDataService.getMolecularDataInMultipleMolecularProfiles(
            Arrays.asList(MOLECULAR_PROFILE_ID_A, MOLECULAR_PROFILE_ID_B),
            null,
            Arrays.asList(ENTREZ_GENE_ID_1, ENTREZ_GENE_ID_2),
            PROJECTION);

    // Verify repository was called once
    verify(molecularDataRepository, times(1))
        .getGeneMolecularAlterationsInMultipleMolecularProfiles(
            new java.util.TreeSet<>(Arrays.asList(MOLECULAR_PROFILE_ID_A, MOLECULAR_PROFILE_ID_B)),
            Arrays.asList(ENTREZ_GENE_ID_1, ENTREZ_GENE_ID_2),
            PROJECTION);

    // There are two samples for profile A and one sample for profile B, and three molecular alteration entries:
    // - profile A has two genes -> 2 samples * 2 genes = 4
    // - profile B has 1 gene -> 1 sample * 1 gene = 1
    // Total expected results = 5
    Assert.assertEquals(5, result.size());
  }

  @Test
  public void fetchMolecularDataCountsInMultipleMolecularProfiles() {
    // Setup test data - similar to getMolecularDataInMultipleMolecularProfiles test
    List<GeneMolecularAlteration> molecularAlterations = new ArrayList<>();
    GeneMolecularAlteration alteration1 = new GeneMolecularAlteration();
    alteration1.setMolecularProfileId(MOLECULAR_PROFILE_ID_A);
    alteration1.setEntrezGeneId(ENTREZ_GENE_ID_1);
    alteration1.setValues("0.4181,0.4181");
    molecularAlterations.add(alteration1);

    GeneMolecularAlteration alteration2 = new GeneMolecularAlteration();
    alteration2.setMolecularProfileId(MOLECULAR_PROFILE_ID_A);
    alteration2.setEntrezGeneId(ENTREZ_GENE_ID_2);
    alteration2.setValues("0.5332,0.5332");
    molecularAlterations.add(alteration2);

    GeneMolecularAlteration alteration3 = new GeneMolecularAlteration();
    alteration3.setMolecularProfileId(MOLECULAR_PROFILE_ID_B);
    alteration3.setEntrezGeneId(ENTREZ_GENE_ID_1);
    alteration3.setValues("-0.4737");
    molecularAlterations.add(alteration3);

    when(molecularDataRepository.getGeneMolecularAlterationsInMultipleMolecularProfiles(
            any(), any(), any()))
        .thenReturn(molecularAlterations);

    when(molecularDataRepository.commaSeparatedSampleIdsOfMolecularProfilesMap(any()))
        .thenReturn(createSampleIdMap());

    List<Sample> samples = createSamples();
    when(sampleService.getSamplesByInternalIds(any())).thenReturn(samples);

    // Execute
    List<org.cbioportal.legacy.model.MolecularDataCountItem> result =
        molecularDataService.fetchMolecularDataCountsInMultipleMolecularProfiles(
            Arrays.asList(MOLECULAR_PROFILE_ID_A, MOLECULAR_PROFILE_ID_B),
            null,
            Arrays.asList(ENTREZ_GENE_ID_1, ENTREZ_GENE_ID_2));

    // Verify
    Assert.assertEquals(2, result.size());
    
    // Profile A should have 4 data points (2 samples * 2 genes)
    org.cbioportal.legacy.model.MolecularDataCountItem profileACount =
        result.stream()
            .filter(item -> item.getMolecularProfileId().equals(MOLECULAR_PROFILE_ID_A))
            .findFirst()
            .orElse(null);
    Assert.assertNotNull(profileACount);
    Assert.assertEquals(Integer.valueOf(4), profileACount.getCount());

    // Profile B should have 1 data point (1 sample * 1 gene)
    org.cbioportal.legacy.model.MolecularDataCountItem profileBCount =
        result.stream()
            .filter(item -> item.getMolecularProfileId().equals(MOLECULAR_PROFILE_ID_B))
            .findFirst()
            .orElse(null);
    Assert.assertNotNull(profileBCount);
    Assert.assertEquals(Integer.valueOf(1), profileBCount.getCount());
  }
}
