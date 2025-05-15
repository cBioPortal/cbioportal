package org.cbioportal.legacy.service.impl;

import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.math3.util.Pair;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.AlterationCountByStructuralVariant;
import org.cbioportal.legacy.model.AlterationFilter;
import org.cbioportal.legacy.model.CNA;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.model.MutationEventType;
import org.cbioportal.legacy.model.util.Select;
import org.cbioportal.legacy.persistence.AlterationRepository;
import org.cbioportal.legacy.persistence.MolecularProfileRepository;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.service.util.AlterationEnrichmentUtil;
import org.cbioportal.legacy.service.util.MolecularProfileUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AlterationCountServiceImplTest extends BaseServiceImplTest {

  private AlterationCountServiceImpl alterationCountService;
  @Mock private AlterationRepository alterationRepository;
  @Mock private AlterationEnrichmentUtil<AlterationCountByGene> alterationEnrichmentUtil;
  @Mock private AlterationEnrichmentUtil<CopyNumberCountByGene> alterationEnrichmentUtilCna;

  @Mock
  private AlterationEnrichmentUtil<AlterationCountByStructuralVariant>
      alterationEnrichmentUtilStructVar;

  @Spy @InjectMocks private MolecularProfileUtil molecularProfileUtil;
  @Mock private MolecularProfileRepository molecularProfileRepository;

  List<MolecularProfileCaseIdentifier> caseIdentifiers =
      Arrays.asList(new MolecularProfileCaseIdentifier("A", MOLECULAR_PROFILE_ID));
  Select<MutationEventType> mutationEventTypes =
      Select.byValues(Arrays.asList(MutationEventType.missense_mutation));
  Select<CNA> cnaEventTypes = Select.byValues(Arrays.asList(CNA.AMP));
  Select<Integer> entrezGeneIds = Select.all();
  boolean includeFrequency = true;
  boolean includeMissingAlterationsFromGenePanel = false;
  List<AlterationCountByGene> expectedCountByGeneList;
  List<CopyNumberCountByGene> expectedCnaCountByGeneList;
  List<AlterationCountByStructuralVariant> expectedStructuralVariantList;
  AlterationFilter alterationFilter =
      new AlterationFilter(
          mutationEventTypes,
          cnaEventTypes,
          false,
          false,
          false,
          false,
          false,
          false,
          Select.none(),
          false);

  @Before
  public void setup() {
    MockitoAnnotations.openMocks(this);

    alterationCountService =
        new AlterationCountServiceImpl(
            alterationRepository,
            alterationEnrichmentUtil,
            alterationEnrichmentUtilCna,
            alterationEnrichmentUtilStructVar,
            molecularProfileRepository);

    MolecularProfile molecularProfile = new MolecularProfile();
    molecularProfile.setStableId(MOLECULAR_PROFILE_ID);
    molecularProfile.setCancerStudyIdentifier(STUDY_ID);

    when(molecularProfileRepository.getMolecularProfiles(
            Collections.singleton(MOLECULAR_PROFILE_ID), "SUMMARY"))
        .thenReturn(Arrays.asList(molecularProfile));

    AlterationCountByGene alterationCountByGene = new AlterationCountByGene();
    alterationCountByGene.setEntrezGeneId(ENTREZ_GENE_ID_1);
    expectedCountByGeneList = Arrays.asList(alterationCountByGene);

    CopyNumberCountByGene copyNumberCountByGene = new CopyNumberCountByGene();
    copyNumberCountByGene.setEntrezGeneId(ENTREZ_GENE_ID_1);
    copyNumberCountByGene.setAlteration(2);
    expectedCnaCountByGeneList = Arrays.asList(copyNumberCountByGene);

    final AlterationCountByStructuralVariant alterationCountByStructuralVariant =
        new AlterationCountByStructuralVariant();
    alterationCountByStructuralVariant.setGene1EntrezGeneId(ENTREZ_GENE_ID_1);
    alterationCountByStructuralVariant.setGene2EntrezGeneId(ENTREZ_GENE_ID_2);
    alterationCountByStructuralVariant.setGene1HugoGeneSymbol(HUGO_GENE_SYMBOL_1);
    alterationCountByStructuralVariant.setGene2HugoGeneSymbol(HUGO_GENE_SYMBOL_2);
    expectedStructuralVariantList = Arrays.asList(alterationCountByStructuralVariant);
  }

  @Test
  public void getSampleAlterationGeneCounts() {

    // this mock tests correct argument types
    when(alterationRepository.getSampleAlterationGeneCounts(
            new HashSet<>(caseIdentifiers), entrezGeneIds, alterationFilter))
        .thenReturn(expectedCountByGeneList);

    Pair<List<AlterationCountByGene>, Long> result =
        alterationCountService.getSampleAlterationGeneCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);

    verify(alterationEnrichmentUtil, times(1))
        .includeFrequencyForSamples(anyList(), anyList(), anyBoolean());
  }

  @Test
  public void getPatientAlterationGeneCounts() {

    // this mock tests correct argument types
    when(alterationRepository.getPatientAlterationGeneCounts(
            new HashSet<>(caseIdentifiers), entrezGeneIds, alterationFilter))
        .thenReturn(expectedCountByGeneList);

    alterationCountService.getPatientAlterationGeneCounts(
        caseIdentifiers,
        entrezGeneIds,
        includeFrequency,
        includeMissingAlterationsFromGenePanel,
        alterationFilter);

    verify(alterationEnrichmentUtil, times(1))
        .includeFrequencyForPatients(anyList(), anyList(), anyBoolean());
  }

  @Test
  public void getSampleMutationGeneCounts() {
    // this mock tests correct argument types
    when(alterationRepository.getSampleAlterationGeneCounts(
            new HashSet<>(caseIdentifiers), entrezGeneIds, alterationFilter))
        .thenReturn(expectedCountByGeneList);

    Pair<List<AlterationCountByGene>, Long> result =
        alterationCountService.getSampleMutationGeneCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);

    Assert.assertEquals(expectedCountByGeneList, result.getFirst());
  }

  @Test
  public void getPatientMutationGeneCounts() throws MolecularProfileNotFoundException {

    // this mock tests correct argument types
    when(alterationRepository.getPatientAlterationGeneCounts(
            new HashSet<>(caseIdentifiers), entrezGeneIds, alterationFilter))
        .thenReturn(expectedCountByGeneList);

    Pair<List<AlterationCountByGene>, Long> result =
        alterationCountService.getPatientMutationGeneCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);

    Assert.assertEquals(expectedCountByGeneList, result.getFirst());
  }

  @Test
  public void getSampleCnaGeneCounts() {

    // this mock tests correct argument types
    when(alterationRepository.getSampleCnaGeneCounts(
            new TreeSet<>(caseIdentifiers), entrezGeneIds, alterationFilter))
        .thenReturn(expectedCnaCountByGeneList);

    Pair<List<CopyNumberCountByGene>, Long> result =
        alterationCountService.getSampleCnaGeneCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);

    verify(alterationEnrichmentUtilCna, times(1))
        .includeFrequencyForSamples(anyList(), anyList(), anyBoolean());
    Assert.assertEquals(expectedCnaCountByGeneList, result.getFirst());
  }

  @Test
  public void getPatientCnaGeneCounts() {

    // this mock tests correct argument types
    when(alterationRepository.getPatientCnaGeneCounts(
            new HashSet<>(caseIdentifiers), entrezGeneIds, alterationFilter))
        .thenReturn(expectedCnaCountByGeneList);

    Pair<List<CopyNumberCountByGene>, Long> result =
        alterationCountService.getPatientCnaGeneCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);

    verify(alterationEnrichmentUtilCna, times(1))
        .includeFrequencyForPatients(anyList(), anyList(), anyBoolean());
    Assert.assertEquals(expectedCnaCountByGeneList, result.getFirst());
  }

  @Test
  public void getSampleStructuralVariantCounts() {

    when(alterationRepository.getSampleStructuralVariantCounts(
            new TreeSet<>(caseIdentifiers), alterationFilter))
        .thenReturn(expectedStructuralVariantList);

    Pair<List<AlterationCountByStructuralVariant>, Long> result =
        alterationCountService.getSampleStructuralVariantCounts(
            caseIdentifiers,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);

    verify(alterationEnrichmentUtilStructVar, times(1))
        .includeFrequencyForSamples(anyList(), anyList(), anyBoolean());
    Assert.assertEquals(expectedStructuralVariantList, result.getFirst());
  }

  @Test
  public void testMergeAlterationCountsAcrossStudies() {
    // Setup case identifiers from multiple studies
    MolecularProfileCaseIdentifier case1 =
        new MolecularProfileCaseIdentifier("Sample1", MOLECULAR_PROFILE_ID);
    MolecularProfileCaseIdentifier case2 =
        new MolecularProfileCaseIdentifier("Sample2", "another_profile_id");
    List<MolecularProfileCaseIdentifier> multiStudyCaseIdentifiers = Arrays.asList(case1, case2);

    // Create a second molecular profile for a different study
    MolecularProfile molecularProfile1 = new MolecularProfile();
    molecularProfile1.setStableId(MOLECULAR_PROFILE_ID);
    molecularProfile1.setCancerStudyIdentifier(STUDY_ID);

    MolecularProfile molecularProfile2 = new MolecularProfile();
    molecularProfile2.setStableId("another_profile_id");
    molecularProfile2.setCancerStudyIdentifier("another_study_id");

    // Mock the repository to return profiles for both studies
    when(molecularProfileRepository.getMolecularProfiles(
            new HashSet<>(Arrays.asList(MOLECULAR_PROFILE_ID, "another_profile_id")), "SUMMARY"))
        .thenReturn(Arrays.asList(molecularProfile1, molecularProfile2));

    // Create two alteration counts for the same gene from different studies
    AlterationCountByGene geneCount1 = new AlterationCountByGene();
    geneCount1.setEntrezGeneId(ENTREZ_GENE_ID_1);
    geneCount1.setHugoGeneSymbol(HUGO_GENE_SYMBOL_1);
    geneCount1.setNumberOfAlteredCases(5);
    geneCount1.setTotalCount(10);

    AlterationCountByGene geneCount2 = new AlterationCountByGene();
    geneCount2.setEntrezGeneId(ENTREZ_GENE_ID_1);
    geneCount2.setHugoGeneSymbol(HUGO_GENE_SYMBOL_1);
    geneCount2.setNumberOfAlteredCases(3);
    geneCount2.setTotalCount(7);

    // Define expected values explicitly
    final int expectedTotalCount = 17; // 10 + 7
    final int expectedAlteredCases = 8; // 5 + 3

    // Mock the repository to return different counts for different study groups
    when(alterationRepository.getSampleAlterationGeneCounts(
            anySet(), eq(entrezGeneIds), eq(alterationFilter)))
        .thenAnswer(
            invocation -> {
              Set<MolecularProfileCaseIdentifier> caseSet = invocation.getArgument(0);
              String profileId = caseSet.iterator().next().getMolecularProfileId();
              if (profileId.equals(MOLECULAR_PROFILE_ID)) {
                return List.of(geneCount1);
              } else {
                return List.of(geneCount2);
              }
            });

    // Mock frequency calculation to return a static value
    when(alterationEnrichmentUtil.includeFrequencyForSamples(anyList(), anyList(), anyBoolean()))
        .thenReturn(20L);

    // Call the method under test
    Pair<List<AlterationCountByGene>, Long> result =
        alterationCountService.getSampleAlterationGeneCounts(
            multiStudyCaseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);

    // Verify the repository was called for each study group
    verify(alterationRepository, times(2))
        .getSampleAlterationGeneCounts(anySet(), eq(entrezGeneIds), eq(alterationFilter));

    // Verify frequency calculation was called
    verify(alterationEnrichmentUtil, times(1))
        .includeFrequencyForSamples(
            anyList(), anyList(), eq(includeMissingAlterationsFromGenePanel));

    // Check result integrity
    Assert.assertNotNull("Result should not be null", result);
    Assert.assertNotNull("Gene count list should not be null", result.getFirst());
    Assert.assertEquals("Should return one merged gene count", 1, result.getFirst().size());

    // Verify the counts were properly merged
    AlterationCountByGene mergedCount = result.getFirst().getFirst();
    Assert.assertEquals(
        "EntrezGeneId should match", ENTREZ_GENE_ID_1, mergedCount.getEntrezGeneId());
    Assert.assertEquals(
        "HugoGeneSymbol should match", HUGO_GENE_SYMBOL_1, mergedCount.getHugoGeneSymbol());
    Assert.assertEquals(
        "Total count should be summed", expectedTotalCount, mergedCount.getTotalCount().intValue());
    Assert.assertEquals(
        "Number of altered cases should be summed",
        expectedAlteredCases,
        mergedCount.getNumberOfAlteredCases().intValue());

    // Verify total profiled count
    Assert.assertEquals(
        "Total profiled count should match mocked value", 20L, result.getSecond().longValue());
  }

  @Test
  public void testMergeAlterationCountsWithEmptyInput() {
    // Test with empty input
    Pair<List<AlterationCountByGene>, Long> result =
        alterationCountService.getSampleAlterationGeneCounts(
            Collections.emptyList(), // empty list
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);

    // Verify results
    Assert.assertNotNull("Result should not be null", result);
    Assert.assertTrue("Gene count list should be empty", result.getFirst().isEmpty());
    Assert.assertEquals("Profiled count should be zero", 0L, result.getSecond().longValue());
  }
}
