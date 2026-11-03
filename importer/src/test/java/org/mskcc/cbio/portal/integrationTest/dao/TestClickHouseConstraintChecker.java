package org.mskcc.cbio.portal.integrationTest.dao;

import org.junit.Test;
import org.mskcc.cbio.portal.dao.ClickHouseConstraintChecker;
import org.mskcc.cbio.portal.dao.ClickHouseConstraintChecker.ForeignKeyViolation;
import org.mskcc.cbio.portal.dao.ClickHouseConstraintChecker.UniqueKeyViolation;
import org.mskcc.cbio.portal.dao.JdbcUtil;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class TestClickHouseConstraintChecker extends IntegrationTestBase {

    private static final String FK_VIOLATIONS_SQL = "clickhouse_fk_violations.sql";
    private static final String UNIQUE_VIOLATIONS_SQL = "clickhouse_unique_violations.sql";

    @Test
    public void testForeignKeyViolations() throws Exception {
        assertTrue("Expected no FK violations on clean data",
                ClickHouseConstraintChecker.findForeignKeyViolations().isEmpty());

        applySqlResource(FK_VIOLATIONS_SQL, List.of());

        List<ForeignKeyViolation> violations = ClickHouseConstraintChecker.findForeignKeyViolations();
        Set<String> actual = new LinkedHashSet<>();
        for (ForeignKeyViolation violation : violations) {
            actual.add(fkKey(
                    violation.refererTable(),
                    violation.refererColumns(),
                    violation.referredTable(),
                    violation.referredColumns()));
        }

        Set<String> expected = expectedForeignKeyConstraints();
        Set<String> missing = new LinkedHashSet<>(expected);
        missing.removeAll(actual);
        assertTrue("Missing foreign-key violations: " + missing, missing.isEmpty());
        Set<String> unexpected = new LinkedHashSet<>(actual);
        unexpected.removeAll(expected);
        assertTrue("Unexpected foreign-key violations: " + unexpected, unexpected.isEmpty());
    }

    @Test
    public void testUniqueKeyViolations() throws Exception {
        assertTrue("Expected no unique-key violations on clean data",
                ClickHouseConstraintChecker.findUniqueKeyViolations().isEmpty());

        // ReplacingMergeTree tables can collapse duplicate keys during merges.
        setMergesEnabled(false);
        try {
            applySqlResource(UNIQUE_VIOLATIONS_SQL, List.of("SET optimize_on_insert = 0"));

            List<UniqueKeyViolation> violations = ClickHouseConstraintChecker.findUniqueKeyViolations();
            Set<String> actual = new LinkedHashSet<>();
            for (UniqueKeyViolation violation : violations) {
                actual.add(ukKey(violation.table(), violation.columns()));
            }

            Set<String> expected = expectedUniqueKeyConstraints();
            Set<String> missing = new LinkedHashSet<>(expected);
            missing.removeAll(actual);
            assertTrue("Missing unique-key violations: " + missing, missing.isEmpty());
            Set<String> unexpected = new LinkedHashSet<>(actual);
            unexpected.removeAll(expected);
            assertTrue("Unexpected unique-key violations: " + unexpected, unexpected.isEmpty());
        } finally {
            setMergesEnabled(true);
        }
    }

    private static void applySqlResource(String resourceName, List<String> preludeStatements) throws Exception {
        String sql = readResource(resourceName);
        List<String> statements = splitStatements(sql);
        if (statements.isEmpty()) {
            throw new IllegalStateException("No SQL statements found in " + resourceName);
        }
        try (Connection connection = JdbcUtil.getDbConnection(TestClickHouseConstraintChecker.class);
             Statement statement = connection.createStatement()) {
            for (String prelude : preludeStatements) {
                statement.execute(prelude);
            }
            for (String sqlStatement : statements) {
                statement.execute(sqlStatement);
            }
        }
    }

    private static void setMergesEnabled(boolean enabled) throws Exception {
        String command = enabled ? "SYSTEM START MERGES" : "SYSTEM STOP MERGES";
        try (Connection connection = JdbcUtil.getDbConnection(TestClickHouseConstraintChecker.class);
             Statement statement = connection.createStatement()) {
            statement.execute(command);
        }
    }

    private static String readResource(String resourceName) throws IOException {
        try (InputStream input = TestClickHouseConstraintChecker.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            if (input == null) {
                throw new IllegalStateException("Resource not found: " + resourceName);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static List<String> splitStatements(String sql) {
        StringBuilder cleaned = new StringBuilder();
        String[] lines = sql.split("\\R");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }
            cleaned.append(line).append('\n');
        }
        List<String> statements = new ArrayList<>();
        for (String statement : cleaned.toString().split(";")) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty()) {
                statements.add(trimmed);
            }
        }
        return statements;
    }

    private static String fkKey(String childTable, String childCols, String parentTable, String parentCols) {
        return childTable + "|" + childCols + "|" + parentTable + "|" + parentCols;
    }

    private static String ukKey(String table, String columns) {
        return table + "|" + columns;
    }

    private static Set<String> expectedForeignKeyConstraints() {
        Set<String> keys = new LinkedHashSet<>();
        addFk(keys, "cancer_study", "type_of_cancer_id", "type_of_cancer", "type_of_cancer_id");
        addFk(keys, "cancer_study", "reference_genome_id", "reference_genome", "reference_genome_id");
        addFk(keys, "cancer_study_tags", "cancer_study_id", "cancer_study", "cancer_study_id");
        addFk(keys, "patient", "cancer_study_id", "cancer_study", "cancer_study_id");
        addFk(keys, "sample", "patient_id", "patient", "internal_id");
        addFk(keys, "sample_list", "cancer_study_id", "cancer_study", "cancer_study_id");
        addFk(keys, "sample_list_list", "sample_id", "sample", "internal_id");
        addFk(keys, "gene", "genetic_entity_id", "genetic_entity", "id");
        addFk(keys, "gene_alias", "entrez_gene_id", "gene", "entrez_gene_id");
        addFk(keys, "geneset", "genetic_entity_id", "genetic_entity", "id");
        addFk(keys, "geneset_gene", "entrez_gene_id", "gene", "entrez_gene_id");
        addFk(keys, "geneset_gene", "geneset_id", "geneset", "id");
        addFk(keys, "geneset_hierarchy_leaf", "node_id", "geneset_hierarchy_node", "node_id");
        addFk(keys, "geneset_hierarchy_leaf", "geneset_id", "geneset", "id");
        addFk(keys, "generic_entity_properties", "genetic_entity_id", "genetic_entity", "id");
        addFk(keys, "genetic_profile", "cancer_study_id", "cancer_study", "cancer_study_id");
        addFk(keys, "genetic_profile_link", "referring_genetic_profile_id", "genetic_profile", "genetic_profile_id");
        addFk(keys, "genetic_profile_link", "referred_genetic_profile_id", "genetic_profile", "genetic_profile_id");
        addFk(keys, "genetic_alteration", "genetic_profile_id", "genetic_profile", "genetic_profile_id");
        addFk(keys, "genetic_alteration", "genetic_entity_id", "genetic_entity", "id");
        addFk(keys, "genetic_profile_samples", "genetic_profile_id", "genetic_profile", "genetic_profile_id");
        addFk(keys, "gene_panel_list", "internal_id", "gene_panel", "internal_id");
        addFk(keys, "gene_panel_list", "gene_id", "gene", "entrez_gene_id");
        addFk(keys, "sample_profile", "genetic_profile_id", "genetic_profile", "genetic_profile_id");
        addFk(keys, "sample_profile", "sample_id", "sample", "internal_id");
        addFk(keys, "sample_profile", "panel_id", "gene_panel", "internal_id");
        addFk(keys, "structural_variant", "sample_id", "sample", "internal_id");
        addFk(keys, "structural_variant", "site1_entrez_gene_id", "gene", "entrez_gene_id");
        addFk(keys, "structural_variant", "site2_entrez_gene_id", "gene", "entrez_gene_id");
        addFk(keys, "structural_variant", "genetic_profile_id", "genetic_profile", "genetic_profile_id");
        addFk(keys, "alteration_driver_annotation", "genetic_profile_id", "genetic_profile", "genetic_profile_id");
        addFk(keys, "alteration_driver_annotation", "sample_id", "sample", "internal_id");
        addFk(keys, "mutation_event", "entrez_gene_id", "gene", "entrez_gene_id");
        addFk(keys, "mutation", "mutation_event_id", "mutation_event", "mutation_event_id");
        addFk(keys, "mutation", "entrez_gene_id", "gene", "entrez_gene_id");
        addFk(keys, "mutation", "genetic_profile_id", "genetic_profile", "genetic_profile_id");
        addFk(keys, "mutation", "sample_id", "sample", "internal_id");
        addFk(keys, "mutation_count_by_keyword", "genetic_profile_id", "genetic_profile", "genetic_profile_id");
        addFk(keys, "mutation_count_by_keyword", "entrez_gene_id", "gene", "entrez_gene_id");
        addFk(keys, "clinical_patient", "internal_id", "patient", "internal_id");
        addFk(keys, "clinical_sample", "internal_id", "sample", "internal_id");
        addFk(keys, "clinical_attribute_meta", "cancer_study_id", "cancer_study", "cancer_study_id");
        addFk(keys, "mut_sig", "cancer_study_id", "cancer_study", "cancer_study_id");
        addFk(keys, "mut_sig", "entrez_gene_id", "gene", "entrez_gene_id");
        addFk(keys, "gistic", "cancer_study_id", "cancer_study", "cancer_study_id");
        addFk(keys, "gistic_to_gene", "entrez_gene_id", "gene", "entrez_gene_id");
        addFk(keys, "gistic_to_gene", "gistic_roi_id", "gistic", "gistic_roi_id");
        addFk(keys, "cna_event", "entrez_gene_id", "gene", "entrez_gene_id");
        addFk(keys, "sample_cna_event", "cna_event_id", "cna_event", "cna_event_id");
        addFk(keys, "sample_cna_event", "genetic_profile_id", "genetic_profile", "genetic_profile_id");
        addFk(keys, "sample_cna_event", "sample_id", "sample", "internal_id");
        addFk(keys, "copy_number_seg", "cancer_study_id", "cancer_study", "cancer_study_id");
        addFk(keys, "copy_number_seg", "sample_id", "sample", "internal_id");
        addFk(keys, "copy_number_seg_file", "cancer_study_id", "cancer_study", "cancer_study_id");
        addFk(keys, "clinical_event", "patient_id", "patient", "internal_id");
        addFk(keys, "clinical_event_data", "clinical_event_id", "clinical_event", "clinical_event_id");
        addFk(keys, "reference_genome_gene", "reference_genome_id", "reference_genome", "reference_genome_id");
        addFk(keys, "reference_genome_gene", "entrez_gene_id", "gene", "entrez_gene_id");
        addFk(keys, "data_access_tokens", "username", "users", "email");
        addFk(keys, "allele_specific_copy_number", "mutation_event_id", "mutation_event", "mutation_event_id");
        addFk(keys, "allele_specific_copy_number", "genetic_profile_id", "genetic_profile", "genetic_profile_id");
        addFk(keys, "allele_specific_copy_number", "sample_id", "sample", "internal_id");
        addFk(keys, "resource_definition", "cancer_study_id", "cancer_study", "cancer_study_id");
        addFk(keys, "resource", "cancer_study_id", "cancer_study", "cancer_study_id");
        addFk(keys, "resource", "patient_id,cancer_study_id", "patient", "stable_id,cancer_study_id");
        addFk(keys, "resource", "sample_id,cancer_study_id", "sample", "stable_id,cancer_study_id");
        return keys;
    }

    private static Set<String> expectedUniqueKeyConstraints() {
        Set<String> keys = new LinkedHashSet<>();
        addUk(keys, "type_of_cancer", "type_of_cancer_id");
        addUk(keys, "reference_genome", "reference_genome_id");
        addUk(keys, "cancer_study", "cancer_study_id");
        addUk(keys, "cancer_study_tags", "cancer_study_id");
        addUk(keys, "users", "email");
        addUk(keys, "patient", "internal_id");
        addUk(keys, "sample", "internal_id");
        addUk(keys, "sample_list", "list_id");
        addUk(keys, "sample_list_list", "list_id,sample_id");
        addUk(keys, "genetic_entity", "id");
        addUk(keys, "gene", "entrez_gene_id");
        addUk(keys, "gene_alias", "entrez_gene_id,gene_alias");
        addUk(keys, "geneset", "id");
        addUk(keys, "geneset_gene", "geneset_id,entrez_gene_id");
        addUk(keys, "geneset_hierarchy_node", "node_id");
        addUk(keys, "geneset_hierarchy_leaf", "node_id,geneset_id");
        addUk(keys, "generic_entity_properties", "id");
        addUk(keys, "genetic_profile", "genetic_profile_id");
        addUk(keys, "genetic_profile_link", "referring_genetic_profile_id,referred_genetic_profile_id");
        addUk(keys, "genetic_alteration", "genetic_profile_id,genetic_entity_id");
        addUk(keys, "gene_panel", "internal_id");
        addUk(keys, "gene_panel_list", "internal_id,gene_id");
        addUk(keys, "structural_variant", "internal_id");
        addUk(keys, "alteration_driver_annotation", "alteration_event_id,genetic_profile_id,sample_id");
        addUk(keys, "mutation_event", "mutation_event_id");
        addUk(keys, "clinical_patient", "internal_id,attr_id");
        addUk(keys, "clinical_sample", "internal_id,attr_id");
        addUk(keys, "clinical_attribute_meta", "attr_id,cancer_study_id");
        addUk(keys, "mut_sig", "cancer_study_id,entrez_gene_id");
        addUk(keys, "gistic", "gistic_roi_id");
        addUk(keys, "gistic_to_gene", "gistic_roi_id,entrez_gene_id");
        addUk(keys, "cna_event", "cna_event_id");
        addUk(keys, "sample_cna_event", "cna_event_id,sample_id,genetic_profile_id");
        addUk(keys, "copy_number_seg", "seg_id");
        addUk(keys, "copy_number_seg_file", "seg_file_id");
        addUk(keys, "clinical_event", "clinical_event_id");
        addUk(keys, "reference_genome_gene", "entrez_gene_id,reference_genome_id");
        addUk(keys, "data_access_tokens", "token");
        addUk(keys, "resource_definition", "resource_id,cancer_study_id");
        addUk(keys, "resource", "internal_id");
        addUk(keys, "reference_genome", "build_name");
        addUk(keys, "cancer_study", "cancer_study_identifier");
        addUk(keys, "sample_list", "stable_id");
        addUk(keys, "gene", "genetic_entity_id");
        addUk(keys, "geneset", "name");
        addUk(keys, "geneset", "external_id");
        addUk(keys, "geneset", "genetic_entity_id");
        addUk(keys, "geneset_hierarchy_node", "node_name,parent_id");
        addUk(keys, "generic_entity_properties", "genetic_entity_id,name");
        addUk(keys, "genetic_profile", "stable_id");
        addUk(keys, "genetic_profile_samples", "genetic_profile_id");
        addUk(keys, "gene_panel", "stable_id");
        addUk(keys, "sample_profile", "sample_id,genetic_profile_id");
        addUk(keys, "mutation", "mutation_event_id,genetic_profile_id,sample_id");
        addUk(keys, "cna_event", "entrez_gene_id,alteration");
        addUk(keys, "allele_specific_copy_number", "mutation_event_id,genetic_profile_id,sample_id");
        return keys;
    }

    private static void addFk(Set<String> keys, String childTable, String childCols, String parentTable, String parentCols) {
        keys.add(fkKey(childTable, childCols, parentTable, parentCols));
    }

    private static void addUk(Set<String> keys, String table, String columns) {
        keys.add(ukKey(table, columns));
    }
}
