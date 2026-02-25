package org.cbioportal.legacy.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.cbioportal.infrastructure.repository.clickhouse.coexpression.ClickhouseCoExpressionRepository;
import org.cbioportal.infrastructure.repository.clickhouse.coexpression.CoExpressionResult;
import org.cbioportal.legacy.model.CoExpression;
import org.cbioportal.legacy.model.EntityType;
import org.cbioportal.legacy.model.Gene;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.persistence.SampleListRepository;
import org.cbioportal.legacy.service.GeneService;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CoExpressionServiceImplTest extends BaseServiceImplTest {

  private static final double THRESHOLD = 0.3;

  @InjectMocks private CoExpressionServiceImpl coExpressionService;

  @Mock private ClickhouseCoExpressionRepository clickhouseCoExpressionRepository;
  @Mock private MolecularProfileService molecularProfileService;
  @Mock private SampleListRepository sampleListRepository;
  @Mock private GeneService geneService;

  @Test
  public void getGeneCoExpressionsWithSampleList() throws Exception {
    setupProfileAndGene(MOLECULAR_PROFILE_ID_A);

    Mockito.when(sampleListRepository.getAllSampleIdsInSampleList(SAMPLE_LIST_ID))
        .thenReturn(Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3));

    String profileTypeA = MOLECULAR_PROFILE_ID_A.substring(STUDY_ID.length() + 1);
    List<CoExpressionResult> clickhouseResults = createClickhouseResults();
    Mockito.when(
            clickhouseCoExpressionRepository.getCoExpressions(
                STUDY_ID,
                profileTypeA,
                STUDY_ID,
                profileTypeA,
                HUGO_GENE_SYMBOL_1,
                Arrays.asList(
                    STUDY_ID + "_" + SAMPLE_ID1,
                    STUDY_ID + "_" + SAMPLE_ID2,
                    STUDY_ID + "_" + SAMPLE_ID3),
                THRESHOLD))
        .thenReturn(clickhouseResults);

    List<CoExpression> result =
        coExpressionService.getCoExpressions(
            MOLECULAR_PROFILE_ID_A,
            SAMPLE_LIST_ID,
            String.valueOf(ENTREZ_GENE_ID_1),
            EntityType.GENE,
            THRESHOLD);

    Assert.assertEquals(2, result.size());
    Assert.assertEquals(String.valueOf(ENTREZ_GENE_ID_2), result.get(0).getGeneticEntityId());
    Assert.assertEquals(EntityType.GENE, result.get(0).getGeneticEntityType());
    Assert.assertNotNull(result.get(0).getSpearmansCorrelation());
    Assert.assertNotNull(result.get(0).getpValue());
  }

  @Test
  public void getGeneCoExpressionsWithDualProfile() throws Exception {
    setupProfileAndGene(MOLECULAR_PROFILE_ID_A);
    MolecularProfile profileB = new MolecularProfile();
    profileB.setCancerStudyIdentifier(STUDY_ID);
    profileB.setStableId(MOLECULAR_PROFILE_ID_B);
    Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID_B))
        .thenReturn(profileB);

    Mockito.when(sampleListRepository.getAllSampleIdsInSampleList(SAMPLE_LIST_ID))
        .thenReturn(Arrays.asList(SAMPLE_ID1, SAMPLE_ID2));

    String profileTypeA = MOLECULAR_PROFILE_ID_A.substring(STUDY_ID.length() + 1);
    String profileTypeB = MOLECULAR_PROFILE_ID_B.substring(STUDY_ID.length() + 1);
    List<CoExpressionResult> clickhouseResults = createClickhouseResults();
    Mockito.when(
            clickhouseCoExpressionRepository.getCoExpressions(
                STUDY_ID,
                profileTypeA,
                STUDY_ID,
                profileTypeB,
                HUGO_GENE_SYMBOL_1,
                Arrays.asList(STUDY_ID + "_" + SAMPLE_ID1, STUDY_ID + "_" + SAMPLE_ID2),
                THRESHOLD))
        .thenReturn(clickhouseResults);

    List<CoExpression> result =
        coExpressionService.getCoExpressions(
            String.valueOf(ENTREZ_GENE_ID_1),
            EntityType.GENE,
            SAMPLE_LIST_ID,
            MOLECULAR_PROFILE_ID_A,
            MOLECULAR_PROFILE_ID_B,
            THRESHOLD);

    Assert.assertEquals(2, result.size());
  }

  @Test
  public void fetchGeneCoExpressions() throws Exception {
    setupProfileAndGene(MOLECULAR_PROFILE_ID_A);
    MolecularProfile profileB = new MolecularProfile();
    profileB.setCancerStudyIdentifier(STUDY_ID);
    profileB.setStableId(MOLECULAR_PROFILE_ID_B);
    Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID_B))
        .thenReturn(profileB);

    String profileTypeA = MOLECULAR_PROFILE_ID_A.substring(STUDY_ID.length() + 1);
    String profileTypeB = MOLECULAR_PROFILE_ID_B.substring(STUDY_ID.length() + 1);
    List<CoExpressionResult> clickhouseResults = createClickhouseResults();
    Mockito.when(
            clickhouseCoExpressionRepository.getCoExpressions(
                STUDY_ID,
                profileTypeA,
                STUDY_ID,
                profileTypeB,
                HUGO_GENE_SYMBOL_1,
                Arrays.asList(STUDY_ID + "_" + SAMPLE_ID1, STUDY_ID + "_" + SAMPLE_ID2),
                THRESHOLD))
        .thenReturn(clickhouseResults);

    List<CoExpression> result =
        coExpressionService.fetchCoExpressions(
            String.valueOf(ENTREZ_GENE_ID_1),
            EntityType.GENE,
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2),
            MOLECULAR_PROFILE_ID_A,
            MOLECULAR_PROFILE_ID_B,
            THRESHOLD);

    Assert.assertEquals(2, result.size());
    Assert.assertEquals(String.valueOf(ENTREZ_GENE_ID_2), result.get(0).getGeneticEntityId());
    Assert.assertEquals(String.valueOf(ENTREZ_GENE_ID_3), result.get(1).getGeneticEntityId());
  }

  @Test
  public void getGenesetCoExpressionsReturnsEmpty() throws Exception {
    List<CoExpression> result =
        coExpressionService.getCoExpressions(
            "GENESET_ID_TEST",
            EntityType.GENESET,
            SAMPLE_LIST_ID,
            "profile_id_gsva_scores_a",
            "profile_id_gsva_scores_b",
            THRESHOLD);

    Assert.assertEquals(0, result.size());
  }

  @Test
  public void fetchGenesetCoExpressionsReturnsEmpty() throws Exception {
    List<CoExpression> result =
        coExpressionService.fetchCoExpressions(
            "GENESET_ID_TEST",
            EntityType.GENESET,
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2),
            "profile_id_gsva_scores_a",
            "profile_id_gsva_scores_b",
            THRESHOLD);

    Assert.assertEquals(0, result.size());
  }

  @Test
  public void getCoExpressionsEmptySampleListReturnsEmpty() throws Exception {
    Mockito.when(sampleListRepository.getAllSampleIdsInSampleList(SAMPLE_LIST_ID))
        .thenReturn(Collections.emptyList());

    List<CoExpression> result =
        coExpressionService.getCoExpressions(
            MOLECULAR_PROFILE_ID,
            SAMPLE_LIST_ID,
            String.valueOf(ENTREZ_GENE_ID_1),
            EntityType.GENE,
            THRESHOLD);

    Assert.assertEquals(0, result.size());
  }

  private void setupProfileAndGene(String profileId) throws Exception {
    MolecularProfile profile = new MolecularProfile();
    profile.setCancerStudyIdentifier(STUDY_ID);
    profile.setStableId(profileId);
    Mockito.when(molecularProfileService.getMolecularProfile(profileId)).thenReturn(profile);

    Gene gene = new Gene();
    gene.setEntrezGeneId(ENTREZ_GENE_ID_1);
    gene.setHugoGeneSymbol(HUGO_GENE_SYMBOL_1);
    Mockito.when(geneService.getGene(String.valueOf(ENTREZ_GENE_ID_1))).thenReturn(gene);
  }

  private List<CoExpressionResult> createClickhouseResults() {
    List<CoExpressionResult> results = new ArrayList<>();
    CoExpressionResult result1 = new CoExpressionResult();
    result1.setEntrezGeneId(ENTREZ_GENE_ID_2);
    result1.setSpearmansCorrelation(0.85);
    result1.setNumSamples(50);
    results.add(result1);
    CoExpressionResult result2 = new CoExpressionResult();
    result2.setEntrezGeneId(ENTREZ_GENE_ID_3);
    result2.setSpearmansCorrelation(-0.72);
    result2.setNumSamples(50);
    results.add(result2);
    return results;
  }
}
