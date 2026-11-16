package org.cbioportal.infrastructure.repository.clickhouse.mutation;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.model.Mutation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@JdbcTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseMutationMapperTest {

  @Autowired private ClickhouseMutationMapper clickhouseMutationMapper;

  @Test
  public void getMutationsInMultipleMolecularProfilesIdProjection() {
    var samplesByProfile = Map.of("study_tcga_pub_mutations", Set.of("tcga-a1-a0sh-01"));
    var entrezGeneIds = List.of(672);

    var result =
        clickhouseMutationMapper.getMutationsInMultipleMolecularProfilesId(
            samplesByProfile, entrezGeneIds, false, "ID", "", null, null);

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
    var samplesByProfile = Map.of("study_tcga_pub_mutations", Set.of("tcga-a1-a0sh-01"));
    var entrezGeneIds = List.of(672);

    var result =
        clickhouseMutationMapper.getMetaMutationsInMultipleMolecularProfiles(
            samplesByProfile, entrezGeneIds, false);

    assertEquals((Integer) 2, result.getTotalCount());
    assertEquals((Integer) 1, result.getSampleCount());
  }

  @Test
  public void getMetaMutationsInMultipleMolecularProfiles_SampleIdEmpty() {
    var samplesByProfile = Map.of("study_tcga_pub_mutations", Set.<String>of());
    var entrezGeneIds = List.of(672);

    var result =
        clickhouseMutationMapper.getMetaMutationsInMultipleMolecularProfiles(
            samplesByProfile, entrezGeneIds, false);

    assertEquals((Integer) 5, result.getTotalCount());
    assertEquals((Integer) 4, result.getSampleCount());
  }

  @Test
  public void getMetaMutationsInMultipleMolecularProfiles_ProjectionSize() {
    var samplesByProfile = Map.of("study_tcga_pub_mutations", Set.<String>of());
    var entrezGeneIds = List.of(672);

    // Calling meta,detailed and summary projection
    List<Mutation> resultID =
        clickhouseMutationMapper.getMutationsInMultipleMolecularProfilesId(
            samplesByProfile, entrezGeneIds, false, "ID", "", null, null);

    List<Mutation> resultDetailed =
        clickhouseMutationMapper.getDetailedMutationsInMultipleMolecularProfiles(
            samplesByProfile, entrezGeneIds, false, "DETAILED", null, null, null, null);

    List<Mutation> resultSummary =
        clickhouseMutationMapper.getSummaryMutationsInMultipleMolecularProfiles(
            samplesByProfile, entrezGeneIds, false, "SUMMARY", null, null, null, null);

    // Should produce the same amount of result
    assertEquals(5, resultDetailed.size());
    assertEquals(5, resultSummary.size());
    assertEquals(5, resultID.size());
  }

  // The mapper reads the flat mutation_derived table. These tests check that it returns exactly
  // what the original joins over the base tables (mutation, mutation_event, gene, driver
  // annotation, allele specific copy number, sample, patient, study) return.
  private static final String PROFILE = "study_tcga_pub_mutations";
  private static final Map<String, Set<String>> ALL_OF_PROFILE = Map.of(PROFILE, Set.of());

  private static final String ORIGINAL_JOINS =
      """
      FROM mutation m
      INNER JOIN genetic_profile gp ON m.genetic_profile_id = gp.genetic_profile_id
      INNER JOIN sample s ON m.sample_id = s.internal_id
      INNER JOIN patient p ON s.patient_id = p.internal_id
      INNER JOIN cancer_study cs ON p.cancer_study_id = cs.cancer_study_id
      INNER JOIN mutation_event me ON m.mutation_event_id = me.mutation_event_id
      INNER JOIN gene g ON m.entrez_gene_id = g.entrez_gene_id
      LEFT JOIN alteration_driver_annotation ada ON m.genetic_profile_id = ada.genetic_profile_id
        AND m.sample_id = ada.sample_id AND m.mutation_event_id = ada.alteration_event_id
      LEFT JOIN allele_specific_copy_number ascn ON m.mutation_event_id = ascn.mutation_event_id
        AND m.genetic_profile_id = ascn.genetic_profile_id AND m.sample_id = ascn.sample_id
      WHERE gp.stable_id = ?
      """;

  @Autowired private DataSource dataSource;

  private List<String> expectedFromOriginalJoins() {
    return new JdbcTemplate(dataSource)
        .queryForList(
            "SELECT concat(s.stable_id, '|', p.stable_id, '|', toString(m.entrez_gene_id), '|', cs.cancer_study_identifier, '|',"
                + " ifNull(m.center, '-'), '|', ifNull(m.mutation_status, '-'), '|', ifNull(toString(m.tumor_alt_count), '-'), '|',"
                + " ifNull(me.protein_change, '-'), '|', ifNull(toString(me.start_position), '-'), '|', ifNull(me.mutation_type, '-'), '|',"
                + " ifNull(me.keyword, '-'), '|', ifNull(ada.driver_filter, '-'), '|', ifNull(ada.driver_tiers_filter, '-'), '|',"
                + " ifNull(g.hugo_gene_symbol, '-'), '|', ifNull(toString(ascn.total_copy_number), '-'), '|',"
                + " ifNull(ascn.clonal, '-'), '|', ifNull(nullIf(ascn.ascn_method, ''), '-'), '|',"
                + " ifNull(toString(ascn.ccf_expected_copies), '-')) "
                + ORIGINAL_JOINS,
            String.class,
            PROFILE);
  }

  private static String dash(Object value) {
    return value == null ? "-" : value.toString();
  }

  private static String describe(Mutation m) {
    var ascn = m.getAlleleSpecificCopyNumber();
    return String.join(
        "|",
        m.getSampleId(),
        m.getPatientId(),
        dash(m.getEntrezGeneId()),
        m.getStudyId(),
        dash(m.getCenter()),
        dash(m.getMutationStatus()),
        dash(m.getTumorAltCount()),
        dash(m.getProteinChange()),
        dash(m.getStartPosition()),
        dash(m.getMutationType()),
        dash(m.getKeyword()),
        dash(m.getDriverFilter()),
        dash(m.getDriverTiersFilter()),
        dash(m.getGene().getHugoGeneSymbol()),
        dash(ascn == null ? null : ascn.getTotalCopyNumber()),
        dash(ascn == null ? null : ascn.getClonal()),
        dash(ascn == null ? null : ascn.getAscnMethod()),
        dash(ascn == null ? null : ascn.getCcfExpectedCopies()));
  }

  @Test
  public void detailedProjectionReturnsTheSameRowsAndFieldsAsTheOriginalJoins() {
    List<Mutation> result =
        clickhouseMutationMapper.getDetailedMutationsInMultipleMolecularProfiles(
            ALL_OF_PROFILE, List.of(), false, "DETAILED", null, null, null, null);

    var expected = expectedFromOriginalJoins();
    assertFalse(expected.isEmpty());
    assertEquals(
        expected.stream().sorted().toList(),
        result.stream().map(m -> describe(m)).sorted().toList());
    // the fixtures contain driver annotations and allele specific copy numbers
    assertTrue(result.stream().anyMatch(m -> m.getDriverFilter() != null));
    assertTrue(
        result.stream()
            .anyMatch(
                m ->
                    m.getAlleleSpecificCopyNumber() != null
                        && m.getAlleleSpecificCopyNumber().getClonal() != null));
    assertTrue(result.stream().anyMatch(m -> m.getAlleleSpecificCopyNumber() == null));
  }

  @Test
  public void summaryAndIdProjectionsReturnTheSameRowsAsTheDetailedOne() {
    var summary =
        clickhouseMutationMapper.getSummaryMutationsInMultipleMolecularProfiles(
            ALL_OF_PROFILE, List.of(), false, "SUMMARY", null, null, null, null);
    var id =
        clickhouseMutationMapper.getMutationsInMultipleMolecularProfilesId(
            ALL_OF_PROFILE, List.of(), false, "ID", "", null, null);

    assertEquals(expectedFromOriginalJoins().size(), summary.size());
    assertEquals(summary.size(), id.size());
    summary.forEach(
        m -> {
          assertNull(m.getGene());
          assertNull(m.getAlleleSpecificCopyNumber());
        });
    assertTrue(id.stream().allMatch(m -> m.getMutationType() == null));
  }

  @Test
  public void metaCountsMatchTheRowsAndDistinctSamplesOfTheOriginalJoins() {
    var meta =
        clickhouseMutationMapper.getMetaMutationsInMultipleMolecularProfiles(
            ALL_OF_PROFILE, List.of(), false);

    Integer distinctSamples =
        new JdbcTemplate(dataSource)
            .queryForObject(
                "SELECT uniqExact(m.sample_id) " + ORIGINAL_JOINS, Integer.class, PROFILE);
    assertEquals((Integer) expectedFromOriginalJoins().size(), meta.getTotalCount());
    assertEquals(distinctSamples, meta.getSampleCount());
  }

  @Test
  public void defaultOrderIsProfileThenSampleThenGeneAndSortingAndPagingAreApplied() {
    var all =
        clickhouseMutationMapper.getSummaryMutationsInMultipleMolecularProfiles(
            ALL_OF_PROFILE, List.of(), false, "SUMMARY", null, null, null, null);
    assertTrue(all.size() > 1);
    for (int i = 1; i < all.size(); i++) {
      var a = all.get(i - 1);
      var b = all.get(i);
      assertTrue(
          a.getSampleId().compareTo(b.getSampleId()) < 0
              || (a.getSampleId().equals(b.getSampleId())
                  && a.getEntrezGeneId() <= b.getEntrezGeneId()));
    }

    var desc =
        clickhouseMutationMapper.getSummaryMutationsInMultipleMolecularProfiles(
            ALL_OF_PROFILE, List.of(), false, "SUMMARY", null, null, "entrezGeneId", "DESC");
    for (int i = 1; i < desc.size(); i++) {
      assertTrue(desc.get(i - 1).getEntrezGeneId() >= desc.get(i).getEntrezGeneId());
    }

    var page =
        clickhouseMutationMapper.getSummaryMutationsInMultipleMolecularProfiles(
            ALL_OF_PROFILE, List.of(), false, "SUMMARY", 2, 1, null, null);
    assertEquals(2, page.size());
    assertEquals(all.get(1).getSampleId(), page.get(0).getSampleId());
    assertEquals(all.get(2).getEntrezGeneId(), page.get(1).getEntrezGeneId());
  }

  @Test
  public void sampleAndGeneFiltersCombineAndSnpOnlyOnlyKeepsUppercaseSingleNucleotides() {
    var sample = "tcga-a1-a0sh-01";
    var filtered =
        clickhouseMutationMapper.getSummaryMutationsInMultipleMolecularProfiles(
            Map.of(PROFILE, Set.of(sample)),
            List.of(672),
            false,
            "SUMMARY",
            null,
            null,
            null,
            null);
    assertEquals(2, filtered.size());
    assertTrue(filtered.stream().allMatch(m -> m.getSampleId().equals(sample)));

    var wrongSample =
        clickhouseMutationMapper.getSummaryMutationsInMultipleMolecularProfiles(
            Map.of(PROFILE, Set.of("no_such_sample")),
            List.of(),
            false,
            "SUMMARY",
            null,
            null,
            null,
            null);
    assertTrue(wrongSample.isEmpty());

    var snp =
        clickhouseMutationMapper.getSummaryMutationsInMultipleMolecularProfiles(
            ALL_OF_PROFILE, List.of(), true, "SUMMARY", null, null, null, null);
    assertTrue(
        snp.stream()
            .allMatch(
                m ->
                    Set.of("A", "T", "C", "G").contains(m.getReferenceAllele())
                        && Set.of("A", "T", "C", "G").contains(m.getTumorSeqAllele())));
  }

  // Sample stable ids are not unique across studies, so a requested (profile, sample) pair must
  // not return the mutations of the same sample id in another requested profile.
  @Test
  public void aSampleIsOnlyReturnedInTheProfilesItWasRequestedIn() {
    var jdbc = new JdbcTemplate(dataSource);
    var existing =
        jdbc.queryForObject(
            "SELECT count() FROM mutation_derived WHERE molecularProfileId = 'pair_profile_a'",
            Integer.class);
    if (existing == null || existing == 0) {
      insertMutation(jdbc, "pair_profile_a", "pair_study_a", "shared_sample", 9101, 100);
      insertMutation(jdbc, "pair_profile_a", "pair_study_a", "only_a", 9102, 101);
      insertMutation(jdbc, "pair_profile_b", "pair_study_b", "shared_sample", 9103, 102);
      insertMutation(jdbc, "pair_profile_b", "pair_study_b", "only_b", 9104, 103);
    }

    // shared_sample requested in profile b only, only_a requested in profile a
    var requested =
        Map.of("pair_profile_a", Set.of("only_a"), "pair_profile_b", Set.of("shared_sample"));
    var result =
        clickhouseMutationMapper.getSummaryMutationsInMultipleMolecularProfiles(
            requested, List.of(), false, "SUMMARY", null, null, null, null);
    assertEquals(
        List.of("pair_profile_a|only_a", "pair_profile_b|shared_sample"),
        result.stream()
            .map(m -> m.getMolecularProfileId() + "|" + m.getSampleId())
            .sorted()
            .toList());

    var meta =
        clickhouseMutationMapper.getMetaMutationsInMultipleMolecularProfiles(
            requested, List.of(), false);
    assertEquals((Integer) 2, meta.getTotalCount());
    assertEquals((Integer) 2, meta.getSampleCount());

    // a profile with an empty sample set selects all of its samples, others stay restricted
    var mixed =
        clickhouseMutationMapper.getMutationsInMultipleMolecularProfilesId(
            Map.of("pair_profile_a", Set.of(), "pair_profile_b", Set.of("only_b")),
            List.of(),
            false,
            "ID",
            "",
            null,
            null);
    assertEquals(
        List.of("pair_profile_a|only_a", "pair_profile_a|shared_sample", "pair_profile_b|only_b"),
        mixed.stream()
            .map(m -> m.getMolecularProfileId() + "|" + m.getSampleId())
            .sorted()
            .toList());
  }

  @Test
  public void anEmptyRequestReturnsNothing() {
    assertTrue(
        clickhouseMutationMapper
            .getMutationsInMultipleMolecularProfilesId(
                Map.of(), List.of(), false, "ID", "", null, null)
            .isEmpty());
    assertEquals(
        (Integer) 0,
        clickhouseMutationMapper
            .getMetaMutationsInMultipleMolecularProfiles(Map.of(), List.of(), false)
            .getTotalCount());
  }

  private void insertMutation(
      JdbcTemplate jdbc, String profile, String study, String sample, int internalId, int start) {
    jdbc.update(
        "INSERT INTO mutation_derived (molecularProfileId, sampleId, sampleInternalId, patientId, entrezGeneId, studyId, startPosition) VALUES (?, ?, ?, ?, 1, ?, ?)",
        profile,
        sample,
        internalId,
        "patient_" + sample,
        study,
        start);
  }
}
