package org.cbioportal.infrastructure.repository.clickhouse.mutation;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.model.MutationSpectrum;
import org.junit.Before;
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
public class ClickhouseMutationSpectrumMapperTest {

  // The alleles in the shared test data are lower case, so deterministic mutations are added.
  //   sample_a: C>A, G>T, C>T, G>A, G>A, T>A, A>C, T>G, and the variants that are no single
  //             nucleotide substitution or have lower case alleles (not counted)
  //   sample_b: only variants that are not single nucleotide substitutions (no entry expected)
  //   sample_c: C>G, G>C, T>C, A>G

  private static final String PROFILE = "spectrum_profile";

  @Autowired private ClickhouseMutationMapper mapper;
  @Autowired private DataSource dataSource;

  @Before
  public void addMutations() {
    JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    Integer existing =
        jdbc.queryForObject(
            "SELECT count() FROM mutation_derived WHERE molecularProfileId = '" + PROFILE + "'",
            Integer.class);
    if (existing != null && existing > 0) {
      return;
    }
    insert(jdbc, "sample_a", 9001, "C", "A");
    insert(jdbc, "sample_a", 9001, "G", "T");
    insert(jdbc, "sample_a", 9001, "C", "T");
    insert(jdbc, "sample_a", 9001, "G", "A");
    insert(jdbc, "sample_a", 9001, "G", "A");
    insert(jdbc, "sample_a", 9001, "T", "A");
    insert(jdbc, "sample_a", 9001, "A", "C");
    insert(jdbc, "sample_a", 9001, "T", "G");
    insert(jdbc, "sample_a", 9001, "-", "A");
    insert(jdbc, "sample_a", 9001, "AT", "A");
    insert(jdbc, "sample_a", 9001, "c", "a");
    insert(jdbc, "sample_b", 9002, "-", "A");
    insert(jdbc, "sample_b", 9002, "AT", "A");
    insert(jdbc, "sample_c", 9003, "C", "G");
    insert(jdbc, "sample_c", 9003, "G", "C");
    insert(jdbc, "sample_c", 9003, "T", "C");
    insert(jdbc, "sample_c", 9003, "A", "G");
    jdbc.update(
        "INSERT INTO sample_list (list_id, stable_id, category, cancer_study_id, name, description) VALUES (9001, 'spectrum_list', 'other', 1, 'n', 'd')");
    jdbc.update(
        "INSERT INTO sample_list_list (list_id, sample_id) VALUES (9001, 9001), (9001, 9002)");
  }

  @Test
  public void countsSubstitutionsAndTheirComplementaryStrand() {
    Map<String, MutationSpectrum> bySample =
        bySample(mapper.getMutationSpectrums(PROFILE, null, null));

    assertEquals(
        "only samples with a single nucleotide variant",
        Set.of("sample_a", "sample_c"),
        bySample.keySet());
    MutationSpectrum a = bySample.get("sample_a");
    assertEquals(PROFILE, a.getMolecularProfileId());
    assertEquals("patient_sample_a", a.getPatientId());
    assertEquals("spectrum_study", a.getStudyId());
    assertEquals(Integer.valueOf(2), a.getCtoA()); // C>A, G>T
    assertEquals(Integer.valueOf(0), a.getCtoG());
    assertEquals(Integer.valueOf(3), a.getCtoT()); // C>T, G>A, G>A
    assertEquals(Integer.valueOf(1), a.getTtoA()); // T>A
    assertEquals(Integer.valueOf(0), a.getTtoC());
    assertEquals(Integer.valueOf(2), a.getTtoG()); // T>G, A>C
    MutationSpectrum c = bySample.get("sample_c");
    assertEquals(Integer.valueOf(2), c.getCtoG()); // C>G, G>C
    assertEquals(Integer.valueOf(2), c.getTtoC()); // T>C, A>G
    assertEquals(Integer.valueOf(0), c.getCtoA());
  }

  @Test
  public void samplesAreSelectedBySampleIdsOrSampleList() {
    assertEquals(
        Set.of("sample_c"),
        bySample(
                mapper.getMutationSpectrums(PROFILE, null, List.of("sample_c", "sample_b", "nope")))
            .keySet());
    // the list contains sample_a and sample_b; sample_b has no single nucleotide variant
    assertEquals(
        Set.of("sample_a"),
        bySample(mapper.getMutationSpectrums(PROFILE, "spectrum_list", null)).keySet());
  }

  @Test
  public void unknownProfileOrSampleListReturnNothing() {
    assertTrue(mapper.getMutationSpectrums("no_such_profile", null, null).isEmpty());
    assertTrue(mapper.getMutationSpectrums(PROFILE, "no_such_list", null).isEmpty());
  }

  private void insert(JdbcTemplate jdbc, String sample, int internalId, String ref, String alt) {
    jdbc.update(
        "INSERT INTO mutation_derived (molecularProfileId, sampleId, sampleInternalId, patientId, entrezGeneId, studyId, referenceAllele, tumorSeqAllele) VALUES (?, ?, ?, ?, 1, 'spectrum_study', ?, ?)",
        PROFILE,
        sample,
        internalId,
        "patient_" + sample,
        ref,
        alt);
  }

  private Map<String, MutationSpectrum> bySample(List<MutationSpectrum> spectrums) {
    return spectrums.stream().collect(Collectors.toMap(MutationSpectrum::getSampleId, s -> s));
  }
}
