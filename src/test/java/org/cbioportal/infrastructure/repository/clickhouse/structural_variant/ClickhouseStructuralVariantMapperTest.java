package org.cbioportal.infrastructure.repository.clickhouse.structural_variant;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.model.StructuralVariant;
import org.cbioportal.legacy.model.StructuralVariantGeneSubQuery;
import org.cbioportal.legacy.model.StructuralVariantQuery;
import org.cbioportal.legacy.model.StructuralVariantSpecialValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@JdbcTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseStructuralVariantMapperTest {

  // study_tcga_pub_sv (7): tcga-a1-a0sb-01 has 57670-673, 8031-5979, 27436-238, 7113-2078;
  // tcga-a1-a0sd-01 has 57670-673 (twice) and 27436-238.
  // acc_tcga_sv (13): tcga-a1-b0so-01 has 57670-673, 8031-5979, 7113-2078 and 8031-null.

  private static final String PUB_SV = "study_tcga_pub_sv";
  private static final String ACC_SV = "acc_tcga_sv";
  private static final String SAMPLE_B = "tcga-a1-a0sb-01";
  private static final String SAMPLE_D = "tcga-a1-a0sd-01";
  private static final String ACC_SAMPLE = "tcga-a1-b0so-01";

  @Autowired private ClickhouseStructuralVariantMapper mapper;

  @Test
  public void profileIdsWithoutGenesReturnEverything() {
    assertEquals(7, fetch(List.of(PUB_SV), null, null, null).size());
    assertEquals(11, fetch(List.of(PUB_SV, ACC_SV), null, null, null).size());
  }

  @Test
  public void genesMatchOnEitherSideOfTheVariant() {
    assertEquals("site 2 gene BRAF", 3, fetch(List.of(PUB_SV), null, List.of(673), null).size());
    assertEquals(
        "site 1 gene KIAA1549", 3, fetch(List.of(PUB_SV), null, List.of(57670), null).size());
    assertEquals(1, fetch(List.of(PUB_SV), null, List.of(8031), null).size());
    assertEquals(
        "either gene of the list",
        5,
        fetch(List.of(PUB_SV), null, List.of(673, 27436), null).size());
  }

  @Test
  public void entriesCarryIdentifiersAndGeneSymbols() {
    List<StructuralVariant> result = fetch(List.of(PUB_SV), null, List.of(7113), null);

    assertEquals(1, result.size());
    StructuralVariant variant = result.get(0);
    assertEquals(PUB_SV, variant.getMolecularProfileId());
    assertEquals(SAMPLE_B, variant.getSampleId());
    assertEquals("tcga-a1-a0sb", variant.getPatientId());
    assertEquals("study_tcga_pub", variant.getStudyId());
    assertEquals("TMPRSS2", variant.getSite1HugoSymbol().toUpperCase());
    assertEquals("ERG", variant.getSite2HugoSymbol().toUpperCase());
    assertEquals(Integer.valueOf(7113), variant.getSite1EntrezGeneId());
    assertEquals(Integer.valueOf(2078), variant.getSite2EntrezGeneId());
  }

  @Test
  public void singleProfileWithSamplesRestrictsToThoseSamples() {
    List<StructuralVariant> result =
        fetch(List.of(PUB_SV, PUB_SV), List.of(SAMPLE_D, SAMPLE_D), List.of(57670, 27436), null);

    assertEquals(3, result.size());
    assertTrue(result.stream().allMatch(v -> v.getSampleId().equals(SAMPLE_D)));
  }

  @Test
  public void severalProfilesMatchExactSampleProfilePairs() {
    // tcga-a1-a0sd-01 is requested for the first profile only, tcga-a1-b0so-01 for the second
    List<StructuralVariant> result =
        fetch(List.of(PUB_SV, ACC_SV), List.of(SAMPLE_B, ACC_SAMPLE), null, null);

    Set<String> keys =
        result.stream()
            .map(v -> v.getMolecularProfileId() + "/" + v.getSampleId())
            .collect(Collectors.toSet());
    assertEquals(Set.of(PUB_SV + "/" + SAMPLE_B, ACC_SV + "/" + ACC_SAMPLE), keys);
    assertEquals(4 + 4, result.size());
  }

  @Test
  public void structuralVariantQueriesAddToTheGeneMatch() {
    StructuralVariantQuery noSecondGene =
        query(
            new StructuralVariantGeneSubQuery(8031),
            special(StructuralVariantSpecialValue.NO_GENE));
    List<StructuralVariant> onlyUnpaired =
        fetch(List.of(ACC_SV), null, null, List.of(noSecondGene));
    assertEquals(1, onlyUnpaired.size());
    assertNull(onlyUnpaired.get(0).getSite2EntrezGeneId());

    StructuralVariantQuery anyWithBraf =
        query(
            special(StructuralVariantSpecialValue.ANY_GENE),
            new StructuralVariantGeneSubQuery(673));
    assertEquals(3, fetch(List.of(PUB_SV), null, null, List.of(anyWithBraf)).size());

    assertEquals(
        "gene ids and queries are OR-ed",
        4,
        fetch(List.of(PUB_SV), null, List.of(8031), List.of(anyWithBraf)).size());
  }

  @Test
  public void unknownProfilesAndSamplesReturnNothing() {
    assertTrue(fetch(List.of("no_such_profile"), null, null, null).isEmpty());
    assertTrue(fetch(List.of(PUB_SV), List.of("no_such_sample"), null, null).isEmpty());
    assertTrue(fetch(List.of(PUB_SV), null, List.of(999999999), null).isEmpty());
  }

  private List<StructuralVariant> fetch(
      List<String> profileIds,
      List<String> sampleIds,
      List<Integer> genes,
      List<StructuralVariantQuery> queries) {
    String singleProfile = null;
    String[] samples = null;
    String[] pairs = null;
    if (sampleIds != null) {
      if (profileIds.stream().distinct().count() == 1) {
        singleProfile = profileIds.get(0);
        samples = sampleIds.toArray(new String[0]);
      } else {
        pairs = new String[sampleIds.size()];
        for (int i = 0; i < pairs.length; i++) {
          pairs[i] = sampleIds.get(i) + ":" + profileIds.get(i);
        }
      }
    }
    return mapper.fetchStructuralVariants(
        profileIds, singleProfile, samples, pairs, genes, queries);
  }

  private StructuralVariantGeneSubQuery special(StructuralVariantSpecialValue value) {
    return new StructuralVariantGeneSubQuery(value);
  }

  private StructuralVariantQuery query(
      StructuralVariantGeneSubQuery gene1, StructuralVariantGeneSubQuery gene2) {
    StructuralVariantQuery query = new StructuralVariantQuery();
    query.setGene1(gene1);
    query.setGene2(gene2);
    return query;
  }
}
