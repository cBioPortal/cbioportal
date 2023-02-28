package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cbioportal.model.Geneset;
import org.cbioportal.model.GenesetMolecularData;
import org.cbioportal.model.GenesetHierarchyInfo;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.persistence.GenesetHierarchyRepository;
import org.cbioportal.service.GenesetDataService;
import org.cbioportal.service.GenesetService;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GenesetHierarchyServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private GenesetHierarchyServiceImpl genesetHierarchyService;

    @Mock
    private GenesetDataService genesetDataService;
    @Mock
    private MolecularDataService geneticDataService;
    @Mock
    private GenesetService genesetService;
    @Mock
    private SampleService sampleService;
    @Mock
    private MolecularProfileService geneticProfileService;
    @Mock
    private GenesetHierarchyRepository genesetHierarchyRepository;

    
    public static final String PVALUE_GENETIC_PROFILE_ID = "p_value_genetic_profile_id";

    /**
     * This is executed n times, for each of the n test methods below:
     * @throws Exception 
     * @throws DaoException
     */
    @Before 
    public void setUp() throws Exception {

        MolecularProfile geneticProfile = new MolecularProfile();
        geneticProfile.setCancerStudyIdentifier(STUDY_ID);
        geneticProfile.setDatatype("GSVA-SCORE");
        Mockito.when(geneticProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(geneticProfile);
        
        //stub for geneset scores:
        List<GenesetMolecularData> genesetScoresDataList1 = new ArrayList<GenesetMolecularData>();
        genesetScoresDataList1.add(getSimpleFlatGenesetDataItem(SAMPLE_ID1, GENESET_ID1, "0.2"));
        genesetScoresDataList1.add(getSimpleFlatGenesetDataItem(SAMPLE_ID2, GENESET_ID1, "0.499"));
        genesetScoresDataList1.add(getSimpleFlatGenesetDataItem(SAMPLE_ID3, GENESET_ID1, "0.470"));
        genesetScoresDataList1.add(getSimpleFlatGenesetDataItem(SAMPLE_ID1, GENESET_ID2, "-0.35"));
        genesetScoresDataList1.add(getSimpleFlatGenesetDataItem(SAMPLE_ID2, GENESET_ID2, "0.12"));
        genesetScoresDataList1.add(getSimpleFlatGenesetDataItem(SAMPLE_ID3, GENESET_ID2, "-0.11"));
        Mockito.when(genesetDataService.fetchGenesetData(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3),
                null))
            .thenReturn(genesetScoresDataList1);
        
        //stubs for related p-values:
        MolecularProfile pvalueGeneticProfile = new MolecularProfile();
        pvalueGeneticProfile.setStableId(PVALUE_GENETIC_PROFILE_ID);
        pvalueGeneticProfile.setDatatype("P-VALUE");
        Mockito.when(geneticProfileService.getMolecularProfilesReferringTo(MOLECULAR_PROFILE_ID))
            .thenReturn(Arrays.asList(pvalueGeneticProfile));

        List<GenesetMolecularData> genesetPvaluesDataList1 = new ArrayList<GenesetMolecularData>();
        genesetPvaluesDataList1.add(getSimpleFlatGenesetDataItem(SAMPLE_ID1, GENESET_ID1, "0.016"));
        genesetPvaluesDataList1.add(getSimpleFlatGenesetDataItem(SAMPLE_ID2, GENESET_ID1, "0.0359"));
        genesetPvaluesDataList1.add(getSimpleFlatGenesetDataItem(SAMPLE_ID3, GENESET_ID1, "0.0219"));
        genesetPvaluesDataList1.add(getSimpleFlatGenesetDataItem(SAMPLE_ID1, GENESET_ID2, "0.046"));
        genesetPvaluesDataList1.add(getSimpleFlatGenesetDataItem(SAMPLE_ID2, GENESET_ID2, "0.0019"));
        genesetPvaluesDataList1.add(getSimpleFlatGenesetDataItem(SAMPLE_ID3, GENESET_ID2, "0.001"));
        Mockito.when(genesetDataService.fetchGenesetData(PVALUE_GENETIC_PROFILE_ID, Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3),
                null))
            .thenReturn(genesetPvaluesDataList1);
        
        //stubs for hierarchy nodes, parents and genesets:
        
        List<GenesetHierarchyInfo> hierarchySuperNodes = new ArrayList<GenesetHierarchyInfo>();
        GenesetHierarchyInfo node1 = new GenesetHierarchyInfo();
        node1.setNodeId(1);
        node1.setNodeName("Root node");
        hierarchySuperNodes.add(node1);
        GenesetHierarchyInfo node2 = new GenesetHierarchyInfo();
        node2.setNodeId(2);
        node2.setNodeName("sub node A");
        node2.setParentId(1);
        node2.setParentNodeName(node1.getNodeName());
        hierarchySuperNodes.add(node2);
        GenesetHierarchyInfo node3 = new GenesetHierarchyInfo();
        node3.setNodeId(3);
        node3.setNodeName("sub node B");
        node3.setParentId(1);
        node3.setParentNodeName(node1.getNodeName());
        hierarchySuperNodes.add(node3);
        Mockito.when(genesetHierarchyRepository.getGenesetHierarchySuperNodes(Arrays.asList(GENESET_ID1, GENESET_ID2)))
            .thenReturn(hierarchySuperNodes);
        Mockito.when(genesetHierarchyRepository.getGenesetHierarchySuperNodes(Arrays.asList(GENESET_ID2, GENESET_ID1)))
        .thenReturn(hierarchySuperNodes);
        
        List<GenesetHierarchyInfo> hierarchyParents = new ArrayList<GenesetHierarchyInfo>();
        GenesetHierarchyInfo parentNode1 = new GenesetHierarchyInfo();
        parentNode1.setNodeId(4);
        parentNode1.setNodeName("parent node 1");
        parentNode1.setParentId(2);
        parentNode1.setParentNodeName(node2.getNodeName());
        hierarchyParents.add(parentNode1);
        GenesetHierarchyInfo parentNode2 = new GenesetHierarchyInfo();
        parentNode2.setNodeId(5);
        parentNode2.setNodeName("parent node 2");
        parentNode2.setParentId(2);
        parentNode2.setParentNodeName(node2.getNodeName());
        hierarchyParents.add(parentNode2);
        Mockito.when(genesetHierarchyRepository.getGenesetHierarchyParents(Arrays.asList(GENESET_ID1, GENESET_ID2)))
            .thenReturn(hierarchyParents);
        Mockito.when(genesetHierarchyRepository.getGenesetHierarchyParents(Arrays.asList(GENESET_ID2, GENESET_ID1)))
        .thenReturn(hierarchyParents);
        
        Geneset geneset1 = new Geneset();
        geneset1.setGenesetId(GENESET_ID1);
        geneset1.setDescription(GENESET_ID1);
        geneset1.setName(GENESET_ID1);
        Geneset geneset2 = new Geneset();
        geneset2.setGenesetId(GENESET_ID2);
        geneset2.setDescription(GENESET_ID2);
        geneset2.setName(GENESET_ID2);
        Mockito.when(genesetHierarchyRepository.getGenesetHierarchyGenesets(parentNode1.getNodeId()))
            .thenReturn(Arrays.asList(geneset1, geneset2)); //genesets 1 and 2 as children
        Mockito.when(genesetHierarchyRepository.getGenesetHierarchyGenesets(parentNode2.getNodeId()))
            .thenReturn(Arrays.asList(geneset2)); //only geneset 2 as child
    }

    private GenesetMolecularData getSimpleFlatGenesetDataItem(String sampleStableId, String genesetId, String value){
    
        GenesetMolecularData item = new GenesetMolecularData();
        item.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        item.setGenesetId(genesetId);
        item.setSampleId(sampleStableId);
        item.setValue(value);
        return item;
    }
    
    @Test
    public void fetchCorrelatedGenes() throws Exception {

        //50th percentile (median), with thresholds abs_score=0.4 and p-value=0.05:
        List<GenesetHierarchyInfo> result = genesetHierarchyService.fetchGenesetHierarchyInfo(MOLECULAR_PROFILE_ID, 50, 0.4, 0.05,
                Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3));

        //what we expect: at threshold 0.4 only GENESET_ID1 will qualify, so only the hierarchy related to this 
        //geneset should return. That is:
        //   Root node ->  sub node A -> parent node 1 -> GENESET_ID1, with representative (median) score=0.470 and p-value=0.0219

        Assert.assertEquals(3, result.size());
        //3 nodes, last one with 1 leaf (geneset):
        Assert.assertEquals(null, result.get(0).getGenesets());
        Assert.assertEquals(1, result.get(2).getGenesets().size());
        Geneset geneset = result.get(2).getGenesets().get(0);
        Assert.assertEquals(GENESET_ID1, geneset.getGenesetId());
        Assert.assertEquals((Double) 0.470, geneset.getRepresentativeScore());
        Assert.assertEquals((Double) 0.0219, geneset.getRepresentativePvalue());

        //90th percentile, with thresholds abs_score=0.3 and p-value=0.05:
        result = genesetHierarchyService.fetchGenesetHierarchyInfo(MOLECULAR_PROFILE_ID, 90, 0.3, 0.05,
                Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3));

        //what we expect: at threshold 0.3 both GENESET_ID1 & 2 will qualify, so the hierarchy related to these
        //genesets should return. That is:
        //   Root node ->  sub node A -> parent node 1 -> GENESET_ID1, with representative score=0.499 and p-value=0.0359
        //                                             -> GENESET_ID2, with representative score=-0.35 and p-value=0.046
        //   Root node ->  sub node A -> parent node 2 -> GENESET_ID2, with representative score=-0.35 and p-value=0.046
        Assert.assertEquals(4, result.size());
        //4 nodes, last 2 with leaf(s):
        Assert.assertEquals(null, result.get(0).getGenesets());
        Assert.assertEquals(null, result.get(1).getGenesets());
        Assert.assertEquals(2, result.get(2).getGenesets().size());
        Assert.assertEquals(1, result.get(3).getGenesets().size());
        geneset = result.get(2).getGenesets().get(0);
        Assert.assertEquals(GENESET_ID1, geneset.getGenesetId());
        Assert.assertEquals((Double) 0.499, geneset.getRepresentativeScore());
        Assert.assertEquals((Double) 0.0359, geneset.getRepresentativePvalue());
        geneset = result.get(2).getGenesets().get(1);
        Assert.assertEquals(GENESET_ID2, geneset.getGenesetId());
        Assert.assertEquals((Double) (-0.35), geneset.getRepresentativeScore());
        Assert.assertEquals((Double) 0.046, geneset.getRepresentativePvalue());
        //last one is also GENESET_ID2:
        Assert.assertEquals(geneset, result.get(3).getGenesets().get(0));

        //40th percentile, with thresholds abs_score=0.1 and (stricter) p-value=0.01:
        result = genesetHierarchyService.fetchGenesetHierarchyInfo(MOLECULAR_PROFILE_ID, 40, 0.1, 0.01,
                Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3));

        //what we expect: at threshold 0.1 both GENESET_ID1 & 2 will initially qualify, but GENESET_ID1 will finally not
        //make it because of its p-values. So result should be:
        //   Root node ->  sub node A -> parent node 1 -> GENESET_ID2, with representative score=0.12 and p-value=0.0019
        //   Root node ->  sub node A -> parent node 2 -> GENESET_ID2, with representative score=0.12 and p-value=0.0019
        Assert.assertEquals(4, result.size());
        //4 nodes, last 2 with one leaf each:
        Assert.assertEquals(null, result.get(0).getGenesets());
        Assert.assertEquals(null, result.get(1).getGenesets());
        Assert.assertEquals(1, result.get(2).getGenesets().size());
        Assert.assertEquals(1, result.get(3).getGenesets().size());
        geneset = result.get(2).getGenesets().get(0);
        Assert.assertEquals(GENESET_ID2, geneset.getGenesetId());
        Assert.assertEquals((Double) 0.12, geneset.getRepresentativeScore());
        Assert.assertEquals((Double) 0.0019, geneset.getRepresentativePvalue());
        //last one is also GENESET_ID2:
        Assert.assertEquals(geneset, result.get(3).getGenesets().get(0));
    }
}
