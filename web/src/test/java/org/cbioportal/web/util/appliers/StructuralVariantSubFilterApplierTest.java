package org.cbioportal.web.util.appliers;

import com.google.common.collect.Sets;
import org.cbioportal.model.GeneFilter;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.StructVarFilterQuery;
import org.cbioportal.model.StructuralVariant;
import org.cbioportal.model.util.Select;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.impl.StructuralVariantServiceImpl;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.util.StudyViewFilterUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class StructuralVariantSubFilterApplierTest {

    static final String ST_1_ID = "ST_1_ID";
    static final String MP_1_ID = "MP_1_ID";
    static final String G1 = "G1";
    static final String G2 = "G2";
    
    @InjectMocks
    StructuralVariantSubFilterApplier subject;

    @Mock
    MolecularProfileService molecularProfileService;

    @Mock
    StructuralVariantServiceImpl structuralVariantService;

    @Mock
    StudyViewFilterUtil studyViewFilterUtil;

    private StudyViewFilter emptyStudyViewFilter = new StudyViewFilter();
    private StudyViewFilter structVarStudyViewFilter = new StudyViewFilter();

    @Before
    public void setUp() throws Exception {

        // For the test this arrangement does not matter much. What is more important
        // is the SVs returned by the structuralVariantService service mock in response to
        // subsequent query filter calls.
        List<StructVarFilterQuery> svA = Arrays.asList(createQuery(G1, G2));
        List<StructVarFilterQuery> svB = Arrays.asList(createQuery(G1, G2));
        List<List<StructVarFilterQuery>> svA_and_svB_filterQuery = Arrays.asList(svA, svB);
        
        final GeneFilter geneFilter = new GeneFilter();
        geneFilter.setStructVarQueries(svA_and_svB_filterQuery);
        geneFilter.setMolecularProfileIds(Sets.newHashSet(MP_1_ID));
        structVarStudyViewFilter.setGeneFilters(Arrays.asList(geneFilter));

        MolecularProfile molecularProfile1 = new MolecularProfile();
        molecularProfile1.setStableId(MP_1_ID);
        molecularProfile1.setCancerStudyIdentifier(ST_1_ID);
        molecularProfile1.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.STRUCTURAL_VARIANT);

        when(molecularProfileService.getMolecularProfilesInStudies(anyList(), eq("SUMMARY")))
            .thenReturn(Arrays.asList(molecularProfile1));

        final List<StructuralVariant> structuralVariantsQuerySvA = Arrays.asList(
            createStructVar("1", ST_1_ID),
            createStructVar("2", ST_1_ID),
            createStructVar("3", ST_1_ID),
            createStructVar("4", ST_1_ID),
            createStructVar("5", ST_1_ID)
        );
        // This result determines the final result.
        final List<StructuralVariant> structuralVariantsQuerySvB = Arrays.asList(
            createStructVar("4", ST_1_ID),
            createStructVar("5", ST_1_ID)
        );
        // Subsequently, return results for svA and svB queries.
        when(studyViewFilterUtil.cleanSvQueryGeneIds(anyList()))
            .thenReturn(svA, svB);
        when(structuralVariantService.fetchStructuralVariantsByStructVarQueries(anyList(), anyList(), anyList())
        ).thenReturn(structuralVariantsQuerySvA, structuralVariantsQuerySvB);
    }

    // Does the function return OR relations between genes in the sv query filter?
    @Test
    public void filter() {
        List<SampleIdentifier> sampleIdentifiers = Arrays.asList(
            createSampleId("1", ST_1_ID),
            createSampleId("2", ST_1_ID),
            createSampleId("3", ST_1_ID),
            createSampleId("4", ST_1_ID),
            createSampleId("5", ST_1_ID),
            createSampleId("6", ST_1_ID),
            createSampleId("7", ST_1_ID),
            createSampleId("8", ST_1_ID),
            createSampleId("9", ST_1_ID),
            createSampleId("10", ST_1_ID)
        );
        List<SampleIdentifier> expected = Arrays.asList(
            createSampleId("4", ST_1_ID),
            createSampleId("5", ST_1_ID)
        );
        Assert.assertEquals(expected, subject.filter(sampleIdentifiers, structVarStudyViewFilter));
    }

    @Test
    public void shouldApplyFilter() {
        Assert.assertFalse(subject.shouldApplyFilter(emptyStudyViewFilter));
        Assert.assertTrue(subject.shouldApplyFilter(structVarStudyViewFilter)); 
    }
    
    private StructVarFilterQuery createQuery(String gene1, String gene2) {
        return new StructVarFilterQuery(gene1, gene2,
            true, true, true, Select.all(),
            true, true, true, true);
    }
    
    private SampleIdentifier createSampleId(String samplId, String studyId) {
        final SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setStudyId(studyId);
        sampleIdentifier.setSampleId(samplId);
        return sampleIdentifier;
    }
    
    private StructuralVariant createStructVar(String samplId, String studyId) {
        final StructuralVariant structuralVariant = new StructuralVariant();
        structuralVariant.setStudyId(studyId);
        structuralVariant.setSampleId(samplId);
        return structuralVariant;
    }

}