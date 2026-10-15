package org.cbioportal.infrastructure.repository.clickhouse.mutation;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.domain.mutation.PatientGenePanel;
import org.cbioportal.domain.mutation.PatientMutatedGene;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.model.Mutation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseMutationMapperTest {

  @Autowired private ClickhouseMutationMapper clickhouseMutationMapper;

  @Test
  public void getMutationsInMultipleMolecularProfilesIdProjection() {
    var allMolecularProfileIds = List.of("study_tcga_pub_mutations");
    var allSampleIds = List.of("tcga-a1-a0sh-01");
    var entrezGeneIds = List.of(672);

    var result =
        clickhouseMutationMapper.getMutationsInMultipleMolecularProfilesId(
            allMolecularProfileIds, allSampleIds, entrezGeneIds, false, "ID", "", null, null);

    assertEquals(2, result.size());
    result.forEach(
        mutation -> {
          assertEquals("study_tcga_pub_mutations", mutation.getMolecularProfileId());
          assertEquals("tcga-a1-a0sh-01", mutation.getSampleId());
          assertEquals((Integer) 672, mutation.getEntrezGeneId());
        });
  }

  @Test
  public void getMetaMutationsInMultipleMolecularProfiles() {
    var allMolecularProfileIds = List.of("study_tcga_pub_mutations");
    var allSampleIds = List.of("tcga-a1-a0sh-01");
    var entrezGeneIds = List.of(672);

    var result =
        clickhouseMutationMapper.getMetaMutationsInMultipleMolecularProfiles(
            allMolecularProfileIds, allSampleIds, entrezGeneIds, false);

    assertEquals((Integer) 2, result.getTotalCount());
    assertEquals((Integer) 1, result.getSampleCount());
  }

  @Test
  public void getMetaMutationsInMultipleMolecularProfiles_SampleIdEmpty() {
    var allMolecularProfileIds = List.of("study_tcga_pub_mutations");
    var allSampleIds = new ArrayList<String>();
    var entrezGeneIds = List.of(672);

    var result =
        clickhouseMutationMapper.getMetaMutationsInMultipleMolecularProfiles(
            allMolecularProfileIds, allSampleIds, entrezGeneIds, false);

    assertEquals((Integer) 5, result.getTotalCount());
    assertEquals((Integer) 4, result.getSampleCount());
  }

  @Test
  public void getMetaMutationsInMultipleMolecularProfiles_ProjectionSize() {
    var allMolecularProfileIds = List.of("study_tcga_pub_mutations");
    var allSampleIds = new ArrayList<String>();
    var entrezGeneIds = List.of(672);

    // Calling meta,detailed and summary projection
    List<Mutation> resultID =
        clickhouseMutationMapper.getMutationsInMultipleMolecularProfilesId(
            allMolecularProfileIds, allSampleIds, entrezGeneIds, false, "ID", "", null, null);

    List<Mutation> resultDetailed =
        clickhouseMutationMapper.getDetailedMutationsInMultipleMolecularProfiles(
            allMolecularProfileIds,
            allSampleIds,
            entrezGeneIds,
            false,
            "DETAILED",
            null,
            null,
            null,
            null);

    List<Mutation> resultSummary =
        clickhouseMutationMapper.getSummaryMutationsInMultipleMolecularProfiles(
            allMolecularProfileIds,
            allSampleIds,
            entrezGeneIds,
            false,
            "SUMMARY",
            null,
            null,
            null,
            null);

    // Should produce the same amount of result
    assertEquals(5, resultDetailed.size());
    assertEquals(5, resultSummary.size());
    assertEquals(5, resultID.size());
  }

  @Test
  public void isMutationMolecularProfileOfStudy() {
    assertTrue(
        clickhouseMutationMapper.isMutationMolecularProfileOfStudy(
            "study_tcga_pub", "study_tcga_pub_mutations"));
    // not a mutation profile
    assertFalse(
        clickhouseMutationMapper.isMutationMolecularProfileOfStudy(
            "study_tcga_pub", "study_tcga_pub_gistic"));
    // mutation profile of another study
    assertFalse(
        clickhouseMutationMapper.isMutationMolecularProfileOfStudy(
            "study_tcga_pub", "acc_tcga_mutations"));
    assertFalse(
        clickhouseMutationMapper.isMutationMolecularProfileOfStudy(
            "study_tcga_pub", "no_such_profile"));
  }

  @Test
  public void getMutatedGenesOfPatients() {
    var result =
        clickhouseMutationMapper.getMutatedGenesOfPatients(
            "study_tcga_pub_mutations", List.of("AKT1", "AKT2", "BRCA1"));

    // tcga-a1-a0sh has two BRCA1 mutations, which count once
    assertEquals(7, result.size());
    assertEquals(
        Set.of(
            new PatientMutatedGene("tcga-a1-a0sb", "AKT1"),
            new PatientMutatedGene("tcga-a1-a0sd", "AKT1"),
            new PatientMutatedGene("tcga-a1-a0se", "AKT2"),
            new PatientMutatedGene("tcga-a1-a0sh", "BRCA1"),
            new PatientMutatedGene("tcga-a1-a0si", "BRCA1"),
            new PatientMutatedGene("tcga-a1-a0so", "BRCA1"),
            new PatientMutatedGene("tcga-a1-a0sp", "BRCA1")),
        Set.copyOf(result));
  }

  @Test
  public void getGenePanelsOfPatients() {
    // tcga-a1-a0se is the only patient with an AKT2 mutation; the reference patient has none
    var result =
        clickhouseMutationMapper.getGenePanelsOfPatients(
            "study_tcga_pub", "study_tcga_pub_mutations", List.of("AKT2"), "tcga-a1-a0sj");

    assertEquals(
        Set.of(
            new PatientGenePanel("tcga-a1-a0se", "testpanel2"),
            new PatientGenePanel("tcga-a1-a0sj", "WES")),
        Set.copyOf(result));
  }

  @Test
  public void getGenePanelGenes() {
    var result =
        clickhouseMutationMapper.getGenePanelGenes(
            List.of("testpanel2", "WES"), List.of("AKT1", "AKT2", "BRCA1"));

    // testpanel2 does not cover BRCA1, the whole exome covers every gene
    assertEquals(
        Set.of("testpanel2:AKT1", "testpanel2:AKT2", "WES:AKT1", "WES:AKT2", "WES:BRCA1"),
        result.stream()
            .map(gene -> gene.getGenePanelId() + ":" + gene.getHugoGeneSymbol())
            .collect(Collectors.toSet()));
  }
}
