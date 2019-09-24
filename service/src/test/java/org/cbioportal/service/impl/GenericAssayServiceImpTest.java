
package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.cbioportal.model.GenericAssayData;
import org.cbioportal.model.GenericAssayMolecularAlteration;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;
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
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GenericAssayServiceImpTest extends BaseServiceImplTest {

    public static final String GENERIC_ASSAY_ID_1 = "generic_assay_id_1";
    public static final String STUDY_ID_1 = "study_id_1";
    private static final int INTERNAL_ID_1 = 1;
    public static final String GENERIC_ASSAY_ID_2 = "generic_assay_id_2";
    public static final String STUDY_ID_2 = "study_id_2";
    private static final int INTERNAL_ID_2 = 2;
    public static final String ENTITY_TYPE = "GENERIC_ASSAY";

    private static final List<String> idList = Arrays.asList(GENERIC_ASSAY_ID_1, GENERIC_ASSAY_ID_2);
    private static final  List<GenericAssayMeta> mockGenericAssayMetaList = createGenericAssayMetaList();

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
        //stub for samples
        Mockito.when(geneticDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(
                "1,2,");

        List<Sample> sampleList1 = new ArrayList<>();
        Sample sample = new Sample();
        sample.setInternalId(1);
        sample.setStableId(SAMPLE_ID1);
        sampleList1.add(sample);
        Mockito.when(sampleService.fetchSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID1), "ID"))
            .thenReturn(sampleList1);
        List<Sample> sampleListAll = new ArrayList<>(sampleList1);
        sample = new Sample();
        sample.setInternalId(2);
        sample.setStableId(SAMPLE_ID2);
        sampleListAll.add(sample);
        Mockito.when(sampleService.fetchSamples(Arrays.asList(STUDY_ID, STUDY_ID), Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), "ID"))
            .thenReturn(sampleListAll);

        //stub for genetic profile
        MolecularProfile geneticProfile = new MolecularProfile();
        geneticProfile.setCancerStudyIdentifier(STUDY_ID);
        Mockito.when(geneticProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(geneticProfile);

        //stub for repository data
        List<GenericAssayMolecularAlteration> genericAssayMolecularAlterationList = new ArrayList<>();

        GenericAssayMolecularAlteration genericAssayMolecularAlteration1 = new GenericAssayMolecularAlteration();
        genericAssayMolecularAlteration1.setGenericAssayStableId(GENESET_ID1);
        genericAssayMolecularAlteration1.setValues("0.2,0.499");

        GenericAssayMolecularAlteration genericAssayMolecularAlteration2 = new GenericAssayMolecularAlteration();
        genericAssayMolecularAlteration2.setGenericAssayStableId(GENESET_ID2);
        genericAssayMolecularAlteration2.setValues("0.89,-0.509");

        genericAssayMolecularAlterationList.add(genericAssayMolecularAlteration1);
        genericAssayMolecularAlterationList.add(genericAssayMolecularAlteration2);

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(ENTREZ_GENE_ID_1);
        Mockito.when(geneticDataRepository.getGenericAssayMolecularAlterations(MOLECULAR_PROFILE_ID, Arrays.asList(GENESET_ID1, GENESET_ID2), "SUMMARY"))
            .thenReturn(genericAssayMolecularAlterationList);
    }

    @Test
    public void fetchGenericAssayData() throws Exception {

        List<GenericAssayData> result = genericAssayService.fetchGenericAssayData(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1, SAMPLE_ID2),
                Arrays.asList(GENESET_ID1, GENESET_ID2), PersistenceConstants.SUMMARY_PROJECTION);

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
        Assert.assertEquals(item1.getStableId(), GENESET_ID1);
        Assert.assertEquals(item1.getValue(), "0.2");
        Assert.assertEquals(item1.getMolecularProfileId(), MOLECULAR_PROFILE_ID);
        GenericAssayData item2 = result.get(1);
        Assert.assertEquals(item2.getSampleId(), SAMPLE_ID1);
        Assert.assertEquals(item2.getStableId(), GENESET_ID2);
        Assert.assertEquals(item2.getValue(), "0.89");
        Assert.assertEquals(item2.getMolecularProfileId(), MOLECULAR_PROFILE_ID);
        GenericAssayData item4 = result.get(3);
        Assert.assertEquals(item4.getSampleId(), SAMPLE_ID2);
        Assert.assertEquals(item4.getStableId(), GENESET_ID2);
        Assert.assertEquals(item4.getValue(), "-0.509");
        Assert.assertEquals(item4.getMolecularProfileId(), MOLECULAR_PROFILE_ID);
        
        //check when selecting only 1 sample:
        result = genericAssayService.fetchGenericAssayData(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1),
        Arrays.asList(GENESET_ID1, GENESET_ID2),PersistenceConstants.SUMMARY_PROJECTION);
        Assert.assertEquals(2, result.size());
        item1 = result.get(0);
        Assert.assertEquals(item1.getSampleId(), SAMPLE_ID1);
        Assert.assertEquals(item1.getStableId(), GENESET_ID1);
        Assert.assertEquals(item1.getValue(), "0.2");
        Assert.assertEquals(item1.getMolecularProfileId(), MOLECULAR_PROFILE_ID);
    }

    @Test
    public void getGenericAssayMetaByStableId() throws GenericAssayNotFoundException {
        Mockito.when(genericAssayRepository.getGenericAssayMeta(Arrays.asList(GENERIC_ASSAY_ID_1)))
        .thenReturn(Arrays.asList(mockGenericAssayMetaList.get(0)));

        Mockito.when(genericAssayRepository.getGeneticEntityIdByStableId(GENERIC_ASSAY_ID_1))
        .thenReturn(INTERNAL_ID_1);

        Mockito.when(genericAssayRepository.getGenericAssayMetaPropertiesMap(INTERNAL_ID_1))
        .thenReturn(new ArrayList<HashMap<String,String>>());

        GenericAssayMeta result = genericAssayService.getGenericAssayMetaByStableId(GENERIC_ASSAY_ID_1);
        Assert.assertEquals(mockGenericAssayMetaList.get(0).getEntityType(), result.getEntityType());
        Assert.assertEquals(mockGenericAssayMetaList.get(0).getStableId(), result.getStableId());
        Assert.assertEquals(mockGenericAssayMetaList.get(0).getGenericEntityMetaProperties(), result.getGenericEntityMetaProperties());
    }

    @Test
    public void getGenericAssayMetaByStableIds() throws GenericAssayNotFoundException {
        Mockito.when(genericAssayRepository.getGenericAssayMeta(idList))
        .thenReturn(mockGenericAssayMetaList);

        Mockito.when(genericAssayRepository.getGeneticEntityIdByStableId(GENERIC_ASSAY_ID_1))
        .thenReturn(INTERNAL_ID_1);
        Mockito.when(genericAssayRepository.getGeneticEntityIdByStableId(GENERIC_ASSAY_ID_2))
        .thenReturn(INTERNAL_ID_2);

        Mockito.when(genericAssayRepository.getGenericAssayMetaPropertiesMap(INTERNAL_ID_1))
        .thenReturn(new ArrayList<HashMap<String,String>>());
        Mockito.when(genericAssayRepository.getGenericAssayMetaPropertiesMap(INTERNAL_ID_2))
        .thenReturn(new ArrayList<HashMap<String,String>>());

        List<GenericAssayMeta> result = genericAssayService.getGenericAssayMetaByStableIds(idList);
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