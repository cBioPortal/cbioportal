
package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cbioportal.model.GenericAssayData;
import org.cbioportal.model.GenericAssayMolecularAlteration;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileSamples;
import org.cbioportal.model.Sample;
import org.cbioportal.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.persistence.GenericAssayRepository;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.GenericAssayNotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GenericAssayServiceImpTest extends BaseServiceImplTest {

    public static final String GENERIC_ASSAY_ID_1 = "generic_assay_id_1";
    public static final String STUDY_ID_1 = "study_id_1";
    private static final int INTERNAL_ID_1 = 1;
    public static final String GENERIC_ASSAY_ID_2 = "generic_assay_id_2";
    public static final String STUDY_ID_2 = "study_id_2";
    private static final int INTERNAL_ID_2 = 2;
    public static final String ENTITY_TYPE = "GENERIC_ASSAY";

    private static final String PROFILE_ID = "test_profile_id";
    private static final List<String> PROFILE_ID_LIST = Arrays.asList(PROFILE_ID);
    private static final List<String> idList = Arrays.asList(GENERIC_ASSAY_ID_1);
    private static final List<GenericAssayMeta> mockGenericAssayMetaList = createGenericAssayMetaList();
    private static final String MOLECULAR_PROFILE_ID_1 = "molecular_profile_id_1";
    private static final String MOLECULAR_PROFILE_ID_2 = "molecular_profile_id_2";
    private static final String STABLE_ID_1 = "stable_id_1";
    private static final String STABLE_ID_2 = "stable_id_2";   

    @InjectMocks
    private GenericAssayServiceImpl genericAssayService;

    @Mock
    private GenericAssayRepository genericAssayRepository;
    @Mock
    private SampleService sampleService;
    @Mock
    private MolecularProfileService geneticProfileService;
    @Mock
    private MolecularDataRepository geneticDataRepository;

    /**
     * This is executed n times, for each of the n test methods below:
     * @throws Exception 
     * @throws DaoException
     */
    @Before 
    public void setUp() throws Exception {

        MolecularProfileSamples molecularProfileSamples1 = new MolecularProfileSamples();
        molecularProfileSamples1.setMolecularProfileId(MOLECULAR_PROFILE_ID_1);
        molecularProfileSamples1.setCommaSeparatedSampleIds("1,2,");

        MolecularProfileSamples molecularProfileSamples2 = new MolecularProfileSamples();
        molecularProfileSamples2.setMolecularProfileId(MOLECULAR_PROFILE_ID_2);
        molecularProfileSamples2.setCommaSeparatedSampleIds("1,2,");

        Map<String, MolecularProfileSamples> commaSeparatedSampleIdsOfMolecularProfilesMap1 = new HashMap<>();
        commaSeparatedSampleIdsOfMolecularProfilesMap1.put(MOLECULAR_PROFILE_ID_1, molecularProfileSamples1);

        Map<String, MolecularProfileSamples> commaSeparatedSampleIdsOfMolecularProfilesMap2 = new HashMap<>();
        commaSeparatedSampleIdsOfMolecularProfilesMap2.put(MOLECULAR_PROFILE_ID_1, molecularProfileSamples1);
        commaSeparatedSampleIdsOfMolecularProfilesMap2.put(MOLECULAR_PROFILE_ID_2, molecularProfileSamples2);

        // stub for samples
        Mockito.when(geneticDataRepository
                .commaSeparatedSampleIdsOfMolecularProfilesMap(Arrays.asList(MOLECULAR_PROFILE_ID_1)))
                .thenReturn(commaSeparatedSampleIdsOfMolecularProfilesMap1);
        Mockito.when(geneticDataRepository.commaSeparatedSampleIdsOfMolecularProfilesMap(
                Arrays.asList(MOLECULAR_PROFILE_ID_1, MOLECULAR_PROFILE_ID_2)))
                .thenReturn(commaSeparatedSampleIdsOfMolecularProfilesMap2);

        List<Sample> sampleList1 = new ArrayList<>();
        Sample sample = new Sample();
        sample.setCancerStudyIdentifier(STUDY_ID);
        sample.setInternalId(INTERNAL_ID_1);
        sample.setStableId(SAMPLE_ID1);
        sampleList1.add(sample);
        Mockito.when(sampleService.fetchSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID1), "ID"))
            .thenReturn(sampleList1);
        List<Sample> sampleListAll = new ArrayList<>(sampleList1);
        sample = new Sample();
        sample.setCancerStudyIdentifier(STUDY_ID);
        sample.setInternalId(INTERNAL_ID_2);
        sample.setStableId(SAMPLE_ID2);
        sampleListAll.add(sample);
        Mockito.when(sampleService.fetchSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), "ID"))
        .thenReturn(sampleListAll);
        Mockito.when(sampleService.fetchSamples(Arrays.asList(STUDY_ID, STUDY_ID), Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), "ID"))
            .thenReturn(sampleListAll);

        //stub for genetic profile
        MolecularProfile geneticProfile1 = new MolecularProfile();
        geneticProfile1.setCancerStudyIdentifier(STUDY_ID);
        geneticProfile1.setStableId(MOLECULAR_PROFILE_ID_1);
        geneticProfile1.setMolecularAlterationType(MolecularAlterationType.GENERIC_ASSAY);
        MolecularProfile geneticProfile2 = new MolecularProfile();
        geneticProfile2.setCancerStudyIdentifier(STUDY_ID);
        geneticProfile2.setStableId(MOLECULAR_PROFILE_ID_2);
        geneticProfile2.setMolecularAlterationType(MolecularAlterationType.GENERIC_ASSAY);
        List<MolecularProfile> geneticProfiles = new ArrayList<MolecularProfile>();
        geneticProfiles.add(geneticProfile1);
        geneticProfiles.add(geneticProfile2);

        Mockito.when(geneticProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID_1)).thenReturn(geneticProfile1);
        Mockito.when(geneticProfileService.getMolecularProfiles(Arrays.asList(MOLECULAR_PROFILE_ID_1), "SUMMARY")).thenReturn(Arrays.asList(geneticProfile1));
        Mockito.when(geneticProfileService.getMolecularProfiles(Arrays.asList(MOLECULAR_PROFILE_ID_1, MOLECULAR_PROFILE_ID_2), "SUMMARY")).thenReturn(geneticProfiles);

        //stub for repository data
        List<GenericAssayMolecularAlteration> genericAssayMolecularAlterationList1 = new ArrayList<>();

        GenericAssayMolecularAlteration genericAssayMolecularAlteration1 = new GenericAssayMolecularAlteration();
        genericAssayMolecularAlteration1.setMolecularProfileId(MOLECULAR_PROFILE_ID_1);
        genericAssayMolecularAlteration1.setGenericAssayStableId(STABLE_ID_1);
        genericAssayMolecularAlteration1.setValues("0.2,0.499");

        GenericAssayMolecularAlteration genericAssayMolecularAlteration2 = new GenericAssayMolecularAlteration();
        genericAssayMolecularAlteration2.setMolecularProfileId(MOLECULAR_PROFILE_ID_1);
        genericAssayMolecularAlteration2.setGenericAssayStableId(STABLE_ID_2);
        genericAssayMolecularAlteration2.setValues("0.89,-0.509");

        genericAssayMolecularAlterationList1.add(genericAssayMolecularAlteration1);
        genericAssayMolecularAlterationList1.add(genericAssayMolecularAlteration2);

        List<GenericAssayMolecularAlteration> genericAssayMolecularAlterationList2 = new ArrayList<>();

        genericAssayMolecularAlteration1 = new GenericAssayMolecularAlteration();
        genericAssayMolecularAlteration1.setMolecularProfileId(MOLECULAR_PROFILE_ID_2);
        genericAssayMolecularAlteration1.setGenericAssayStableId(STABLE_ID_1);
        genericAssayMolecularAlteration1.setValues("0.2,0.499");

        genericAssayMolecularAlteration2 = new GenericAssayMolecularAlteration();
        genericAssayMolecularAlteration2.setMolecularProfileId(MOLECULAR_PROFILE_ID_2);
        genericAssayMolecularAlteration2.setGenericAssayStableId(STABLE_ID_2);
        genericAssayMolecularAlteration2.setValues("0.89,-0.509");

        genericAssayMolecularAlterationList2.add(genericAssayMolecularAlteration1);
        genericAssayMolecularAlterationList2.add(genericAssayMolecularAlteration2);

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(ENTREZ_GENE_ID_1);
        Mockito.when(geneticDataRepository.getGenericAssayMolecularAlterations(MOLECULAR_PROFILE_ID_1, Arrays.asList(STABLE_ID_1, STABLE_ID_2), "SUMMARY"))
            .thenReturn(genericAssayMolecularAlterationList1);
        Mockito.when(geneticDataRepository.getGenericAssayMolecularAlterations(MOLECULAR_PROFILE_ID_2, Arrays.asList(STABLE_ID_1, STABLE_ID_2), "SUMMARY"))
            .thenReturn(genericAssayMolecularAlterationList2);
    }

    @Test
    public void fetchGenericAssayDataInMultipleMolecularProfiles() throws Exception {

        List<GenericAssayData> result = genericAssayService.fetchGenericAssayData(Arrays.asList(MOLECULAR_PROFILE_ID_1, MOLECULAR_PROFILE_ID_2), Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), Arrays.asList(STABLE_ID_1, STABLE_ID_2)
                , PersistenceConstants.SUMMARY_PROJECTION);

        //what we expect: 2 molecular profiles x 2 samples x 2 generic assay items = 8 GenericAssayData items:
        // MOLECULAR_PROFILE_1:
        //     SAMPLE_1:
        //         generic assay1 value: 0.2
        //         generic assay2 value: 0.89
        //     SAMPLE_2:
        //         generic assay1 value: 0.499
        //         generic assay2 value: -0.509
        // MOLECULAR_PROFILE_2:
        //     SAMPLE_1:
        //         generic assay1 value: 0.2
        //         generic assay2 value: 0.89
        //     SAMPLE_2:
        //         generic assay1 value: 0.499
        //         generic assay2 value: -0.509
        Assert.assertEquals(8, result.size());
        GenericAssayData item1 = result.get(0);
        Assert.assertEquals(item1.getSampleId(), SAMPLE_ID1);
        Assert.assertEquals(item1.getStableId(), STABLE_ID_1);
        Assert.assertEquals(item1.getValue(), "0.2");
        Assert.assertEquals(item1.getMolecularProfileId(), MOLECULAR_PROFILE_ID_1);
        GenericAssayData item2 = result.get(1);
        Assert.assertEquals(item2.getSampleId(), SAMPLE_ID1);
        Assert.assertEquals(item2.getStableId(), STABLE_ID_2);
        Assert.assertEquals(item2.getValue(), "0.89");
        Assert.assertEquals(item2.getMolecularProfileId(), MOLECULAR_PROFILE_ID_1);
        GenericAssayData item7 = result.get(6);
        Assert.assertEquals(item7.getSampleId(), SAMPLE_ID2);
        Assert.assertEquals(item7.getStableId(), STABLE_ID_1);
        Assert.assertEquals(item7.getValue(), "0.499");
        Assert.assertEquals(item7.getMolecularProfileId(), MOLECULAR_PROFILE_ID_2);
        GenericAssayData item8 = result.get(7);
        Assert.assertEquals(item8.getSampleId(), SAMPLE_ID2);
        Assert.assertEquals(item8.getStableId(), STABLE_ID_2);
        Assert.assertEquals(item8.getValue(), "-0.509");
        Assert.assertEquals(item8.getMolecularProfileId(), MOLECULAR_PROFILE_ID_2);
    }

    @Test
    public void fetchGenericAssayData() throws Exception {

        List<GenericAssayData> result = genericAssayService.fetchGenericAssayData(MOLECULAR_PROFILE_ID_1, Arrays.asList(SAMPLE_ID1, SAMPLE_ID2),
                Arrays.asList(STABLE_ID_1, STABLE_ID_2), PersistenceConstants.SUMMARY_PROJECTION);

        //what we expect: 2 samples x 2 generic assay items = 4 GenericAssayData items:
        //SAMPLE_1:
        //   generic assay1 value: 0.2
        //   generic assay2 value: 0.89
        //SAMPLE_2:
        //   generic assay1 value: 0.499
        //   generic assay2 value: -0.509
        Assert.assertEquals(4, result.size());
        GenericAssayData item1 = result.get(0);
        Assert.assertEquals(item1.getSampleId(), SAMPLE_ID1);
        Assert.assertEquals(item1.getStableId(), STABLE_ID_1);
        Assert.assertEquals(item1.getValue(), "0.2");
        Assert.assertEquals(item1.getMolecularProfileId(), MOLECULAR_PROFILE_ID_1);
        GenericAssayData item2 = result.get(1);
        Assert.assertEquals(item2.getSampleId(), SAMPLE_ID1);
        Assert.assertEquals(item2.getStableId(), STABLE_ID_2);
        Assert.assertEquals(item2.getValue(), "0.89");
        Assert.assertEquals(item2.getMolecularProfileId(), MOLECULAR_PROFILE_ID_1);
        GenericAssayData item4 = result.get(3);
        Assert.assertEquals(item4.getSampleId(), SAMPLE_ID2);
        Assert.assertEquals(item4.getStableId(), STABLE_ID_2);
        Assert.assertEquals(item4.getValue(), "-0.509");
        Assert.assertEquals(item4.getMolecularProfileId(), MOLECULAR_PROFILE_ID_1);
        
        //check when selecting only 1 sample:
        result = genericAssayService.fetchGenericAssayData(MOLECULAR_PROFILE_ID_1, Arrays.asList(SAMPLE_ID1),
        Arrays.asList(STABLE_ID_1, STABLE_ID_2),PersistenceConstants.SUMMARY_PROJECTION);
        Assert.assertEquals(2, result.size());
        item1 = result.get(0);
        Assert.assertEquals(item1.getSampleId(), SAMPLE_ID1);
        Assert.assertEquals(item1.getStableId(), STABLE_ID_1);
        Assert.assertEquals(item1.getValue(), "0.2");
        Assert.assertEquals(item1.getMolecularProfileId(), MOLECULAR_PROFILE_ID_1);
    }

    @Test
    public void getGenericAssayMetaByStableIdsAndMolecularIds() throws GenericAssayNotFoundException {
        Mockito.when(genericAssayRepository.getGenericAssayMeta(idList))
        .thenReturn(mockGenericAssayMetaList);

        Mockito.when(genericAssayRepository.getGenericAssayStableIdsByMolecularIds(PROFILE_ID_LIST))
        .thenReturn(idList);

        Mockito.when(genericAssayRepository.getGeneticEntityIdByStableId(GENERIC_ASSAY_ID_1))
        .thenReturn(INTERNAL_ID_1);
        Mockito.when(genericAssayRepository.getGeneticEntityIdByStableId(GENERIC_ASSAY_ID_2))
        .thenReturn(INTERNAL_ID_2);

        Mockito.when(genericAssayRepository.getGenericAssayMetaPropertiesMap(INTERNAL_ID_1))
        .thenReturn(new ArrayList<HashMap<String,String>>());
        Mockito.when(genericAssayRepository.getGenericAssayMetaPropertiesMap(INTERNAL_ID_2))
        .thenReturn(new ArrayList<HashMap<String,String>>());

        List<GenericAssayMeta> result = genericAssayService.getGenericAssayMetaByStableIdsAndMolecularIds(idList, PROFILE_ID_LIST, PersistenceConstants.SUMMARY_PROJECTION);
        GenericAssayMeta meta1 = result.get(0);
        GenericAssayMeta meta2 = result.get(1);
        Assert.assertEquals(mockGenericAssayMetaList.get(0).getEntityType(), meta1.getEntityType());
        Assert.assertEquals(mockGenericAssayMetaList.get(0).getStableId(), meta1.getStableId());
        Assert.assertEquals(mockGenericAssayMetaList.get(0).getGenericEntityMetaProperties(), meta1.getGenericEntityMetaProperties());

        Assert.assertEquals(mockGenericAssayMetaList.get(1).getEntityType(), meta2.getEntityType());
        Assert.assertEquals(mockGenericAssayMetaList.get(1).getStableId(), meta2.getStableId());
        Assert.assertEquals(mockGenericAssayMetaList.get(1).getGenericEntityMetaProperties(), meta2.getGenericEntityMetaProperties());
    }

    private static List<GenericAssayMeta> createGenericAssayMetaList() {

        List<GenericAssayMeta> genericAssayMetaList = new ArrayList<>();


        GenericAssayMeta meta1 = new GenericAssayMeta(GENERIC_ASSAY_ID_1,ENTITY_TYPE);
        genericAssayMetaList.add(meta1);

        GenericAssayMeta meta2 = new GenericAssayMeta(GENERIC_ASSAY_ID_2,ENTITY_TYPE);
        genericAssayMetaList.add(meta2);
        return genericAssayMetaList;
    }

}