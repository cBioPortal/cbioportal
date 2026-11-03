package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generates and executes ClickHouse SQL that reports constraint violations
 * (foreign keys and unique keys) for the cBioPortal ClickHouse schema.
 * <p>
 * Foreign-key output columns per violation row:
 * referer_table, referer_columns, referer_values, referred_table, referred_columns
 * <p>
 * Unique-key output columns per violation row:
 * table_name, key_columns, key_values, duplicate_count
 * <p>
 * Notes:
 * - ClickHouse does not enforce FKs, so orphans are detected via LEFT JOIN + isNull().
 * - Unique keys are detected via GROUP BY + HAVING count() > 1.
 * - Table/column names are normalized to lower-case in the generated SQL.
 * - Rows are checked only when all FK/unique-key columns are non-NULL.
 * - "NULL" in referer/key values means the column value was NULL.
 * <p>
 * The constraint lists are hard-coded for the ClickHouse schema (see clickhouse_cgds.sql).
 * Update {@link #schemaForeignKeys()} and {@link #schemaUniqueKeys()} when the schema changes.
 */
public class ClickHouseConstraintChecker {

    public static final String REFERER_TABLE = "referer_table";
    public static final String REFERER_COLUMNS = "referer_columns";
    public static final String REFERER_VALUES = "referer_values";
    public static final String REFERRED_TABLE = "referred_table";
    public static final String REFERRED_COLUMNS = "referred_columns";

    public static final String UNIQUE_TABLE = "table_name";
    public static final String UNIQUE_COLUMNS = "key_columns";
    public static final String UNIQUE_VALUES = "key_values";
    public static final String UNIQUE_DUPLICATE_COUNT = "duplicate_count";

    private static final String VALUE_SEPARATOR = "|";

    private static final List<ForeignKey> SCHEMA_FOREIGN_KEYS = schemaForeignKeys();
    private static final List<UniqueKey> SCHEMA_UNIQUE_KEYS = schemaUniqueKeys();
    private static final String FOREIGN_KEY_MASTER_QUERY = generateMasterUnionQuery(
            SCHEMA_FOREIGN_KEYS,
            fk -> fk.childTable,
            fk -> generateForeignKeyViolationSelect(fk, "c", "p")
    );
    private static final String UNIQUE_KEY_MASTER_QUERY = generateMasterUnionQuery(
            SCHEMA_UNIQUE_KEYS,
            uk -> uk.table,
            uk -> generateUniqueKeyViolationSelect(uk, "t")
    );

    private ClickHouseConstraintChecker() {
    }

    public record ForeignKeyViolation(
            String refererTable,
            String refererColumns,
            String refererValues,
            String referredTable,
            String referredColumns
    ) {
    }

    public record UniqueKeyViolation(
            String table,
            String columns,
            String keyValues,
            long duplicateCount
    ) {
    }

    public static List<ForeignKeyViolation> findForeignKeyViolations() throws DaoException {
        return runQuery(FOREIGN_KEY_MASTER_QUERY, rs -> new ForeignKeyViolation(
                rs.getString(REFERER_TABLE),
                rs.getString(REFERER_COLUMNS),
                rs.getString(REFERER_VALUES),
                rs.getString(REFERRED_TABLE),
                rs.getString(REFERRED_COLUMNS)
        ));
    }

    public static List<UniqueKeyViolation> findUniqueKeyViolations() throws DaoException {
        return runQuery(UNIQUE_KEY_MASTER_QUERY, rs -> new UniqueKeyViolation(
                rs.getString(UNIQUE_TABLE),
                rs.getString(UNIQUE_COLUMNS),
                rs.getString(UNIQUE_VALUES),
                rs.getLong(UNIQUE_DUPLICATE_COUNT)
        ));
    }

    private interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private static <T> List<T> runQuery(String sql, ResultSetMapper<T> mapper) throws DaoException {
        if (sql.isBlank()) {
            return List.of();
        }
        List<T> results = new ArrayList<>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(ClickHouseConstraintChecker.class);
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(mapper.map(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(ClickHouseConstraintChecker.class, con, pstmt, rs);
        }
    }

    /**
     * Represents one foreign-key relationship (supports composite keys).
     */
    private static final class ForeignKey {
        private final String childTable;       // referer_table
        private final List<String> childCols;   // referer_columns (possibly composite)
        private final String parentTable;      // referred_table
        private final List<String> parentCols; // referred_columns (same arity as childCols)

        /**
         * A key whose parent is a derived relation rather than a base table. Needed where the
         * columns that identify the parent do not all live on one table -- a sample is identified
         * by its stable id plus the study it belongs to, and the study reaches it only through
         * patient. {@code parentSource} is the SQL joined against; {@code parentTable} stays the
         * name reported for the violation.
         */
        private String parentSourceOverride;   // set when the parent is a derived relation

        private ForeignKey(String childTable, List<String> childCols, String parentTable,
                           List<String> parentCols, String parentSource) {
            this(childTable, childCols, parentTable, parentCols);
            this.parentSourceOverride = parentSource;
        }

        /** The SQL to join: the parent table, or the derived relation standing in for it. */
        private String parentSource() {
            return parentSourceOverride != null ? parentSourceOverride : parentTable;
        }

        private ForeignKey(String childTable, List<String> childCols, String parentTable, List<String> parentCols) {
            if (childCols.isEmpty() || parentCols.isEmpty()) {
                throw new IllegalArgumentException("FK column list cannot be empty: " + childTable + " -> " + parentTable);
            }
            if (childCols.size() != parentCols.size()) {
                throw new IllegalArgumentException("FK arity mismatch: childCols=" + childCols + " parentCols=" + parentCols);
            }
            this.childTable = childTable.toLowerCase(Locale.ROOT);
            this.childCols = childCols.stream().map(s -> s.toLowerCase(Locale.ROOT)).toList();
            this.parentTable = parentTable.toLowerCase(Locale.ROOT);
            this.parentCols = parentCols.stream().map(s -> s.toLowerCase(Locale.ROOT)).toList();
        }

        private String childColsCsv() {
            return String.join(",", childCols);
        }

        private String parentColsCsv() {
            return String.join(",", parentCols);
        }
    }

    /**
     * Represents one unique-key constraint (supports composite keys).
     */
    private static final class UniqueKey {
        private final String table;
        private final List<String> columns;

        private UniqueKey(String table, List<String> columns) {
            if (columns.isEmpty()) {
                throw new IllegalArgumentException("Unique key column list cannot be empty: " + table);
            }
            this.table = table.toLowerCase(Locale.ROOT);
            this.columns = columns.stream().map(s -> s.toLowerCase(Locale.ROOT)).toList();
        }

        private String columnsCsv() {
            return String.join(",", columns);
        }
    }

    /**
     * Foreign key list for the ClickHouse schema (lower-case names).
     * Keep this in sync with clickhouse_cgds.sql.
     */
    private static List<ForeignKey> schemaForeignKeys() {
        List<ForeignKey> fks = new ArrayList<>();

        // cancer_study
        fks.add(new ForeignKey("cancer_study", List.of("type_of_cancer_id"), "type_of_cancer", List.of("type_of_cancer_id")));
        fks.add(new ForeignKey("cancer_study", List.of("reference_genome_id"), "reference_genome", List.of("reference_genome_id")));

        // cancer_study_tags
        fks.add(new ForeignKey("cancer_study_tags", List.of("cancer_study_id"), "cancer_study", List.of("cancer_study_id")));

        // patient
        fks.add(new ForeignKey("patient", List.of("cancer_study_id"), "cancer_study", List.of("cancer_study_id")));

        // sample
        fks.add(new ForeignKey("sample", List.of("patient_id"), "patient", List.of("internal_id")));

        // sample_list
        fks.add(new ForeignKey("sample_list", List.of("cancer_study_id"), "cancer_study", List.of("cancer_study_id")));

        // sample_list_list (only sample_id has an FK in the ClickHouse DDL)
        fks.add(new ForeignKey("sample_list_list", List.of("sample_id"), "sample", List.of("internal_id")));

        // gene
        fks.add(new ForeignKey("gene", List.of("genetic_entity_id"), "genetic_entity", List.of("id")));

        // gene_alias
        fks.add(new ForeignKey("gene_alias", List.of("entrez_gene_id"), "gene", List.of("entrez_gene_id")));

        // geneset
        fks.add(new ForeignKey("geneset", List.of("genetic_entity_id"), "genetic_entity", List.of("id")));

        // geneset_gene
        fks.add(new ForeignKey("geneset_gene", List.of("entrez_gene_id"), "gene", List.of("entrez_gene_id")));
        fks.add(new ForeignKey("geneset_gene", List.of("geneset_id"), "geneset", List.of("id")));

        // geneset_hierarchy_leaf
        fks.add(new ForeignKey("geneset_hierarchy_leaf", List.of("node_id"), "geneset_hierarchy_node", List.of("node_id")));
        fks.add(new ForeignKey("geneset_hierarchy_leaf", List.of("geneset_id"), "geneset", List.of("id")));

        // generic_entity_properties
        fks.add(new ForeignKey("generic_entity_properties", List.of("genetic_entity_id"), "genetic_entity", List.of("id")));

        // genetic_profile
        fks.add(new ForeignKey("genetic_profile", List.of("cancer_study_id"), "cancer_study", List.of("cancer_study_id")));

        // genetic_profile_link
        fks.add(new ForeignKey("genetic_profile_link", List.of("referring_genetic_profile_id"), "genetic_profile", List.of("genetic_profile_id")));
        fks.add(new ForeignKey("genetic_profile_link", List.of("referred_genetic_profile_id"), "genetic_profile", List.of("genetic_profile_id")));

        // genetic_alteration
        fks.add(new ForeignKey("genetic_alteration", List.of("genetic_profile_id"), "genetic_profile", List.of("genetic_profile_id")));
        fks.add(new ForeignKey("genetic_alteration", List.of("genetic_entity_id"), "genetic_entity", List.of("id")));

        // genetic_profile_samples
        fks.add(new ForeignKey("genetic_profile_samples", List.of("genetic_profile_id"), "genetic_profile", List.of("genetic_profile_id")));

        // gene_panel_list
        fks.add(new ForeignKey("gene_panel_list", List.of("internal_id"), "gene_panel", List.of("internal_id")));
        fks.add(new ForeignKey("gene_panel_list", List.of("gene_id"), "gene", List.of("entrez_gene_id")));

        // sample_profile
        fks.add(new ForeignKey("sample_profile", List.of("genetic_profile_id"), "genetic_profile", List.of("genetic_profile_id")));
        fks.add(new ForeignKey("sample_profile", List.of("sample_id"), "sample", List.of("internal_id")));
        fks.add(new ForeignKey("sample_profile", List.of("panel_id"), "gene_panel", List.of("internal_id")));

        // structural_variant
        fks.add(new ForeignKey("structural_variant", List.of("sample_id"), "sample", List.of("internal_id")));
        fks.add(new ForeignKey("structural_variant", List.of("site1_entrez_gene_id"), "gene", List.of("entrez_gene_id")));
        fks.add(new ForeignKey("structural_variant", List.of("site2_entrez_gene_id"), "gene", List.of("entrez_gene_id")));
        fks.add(new ForeignKey("structural_variant", List.of("genetic_profile_id"), "genetic_profile", List.of("genetic_profile_id")));

        // alteration_driver_annotation
        fks.add(new ForeignKey("alteration_driver_annotation", List.of("genetic_profile_id"), "genetic_profile", List.of("genetic_profile_id")));
        fks.add(new ForeignKey("alteration_driver_annotation", List.of("sample_id"), "sample", List.of("internal_id")));

        // mutation_event
        fks.add(new ForeignKey("mutation_event", List.of("entrez_gene_id"), "gene", List.of("entrez_gene_id")));

        // mutation
        fks.add(new ForeignKey("mutation", List.of("mutation_event_id"), "mutation_event", List.of("mutation_event_id")));
        fks.add(new ForeignKey("mutation", List.of("entrez_gene_id"), "gene", List.of("entrez_gene_id")));
        fks.add(new ForeignKey("mutation", List.of("genetic_profile_id"), "genetic_profile", List.of("genetic_profile_id")));
        fks.add(new ForeignKey("mutation", List.of("sample_id"), "sample", List.of("internal_id")));

        // mutation_count_by_keyword
        fks.add(new ForeignKey("mutation_count_by_keyword", List.of("genetic_profile_id"), "genetic_profile", List.of("genetic_profile_id")));
        fks.add(new ForeignKey("mutation_count_by_keyword", List.of("entrez_gene_id"), "gene", List.of("entrez_gene_id")));

        // clinical_patient / clinical_sample
        fks.add(new ForeignKey("clinical_patient", List.of("internal_id"), "patient", List.of("internal_id")));
        fks.add(new ForeignKey("clinical_sample", List.of("internal_id"), "sample", List.of("internal_id")));

        // clinical_attribute_meta
        fks.add(new ForeignKey("clinical_attribute_meta", List.of("cancer_study_id"), "cancer_study", List.of("cancer_study_id")));

        // mut_sig
        fks.add(new ForeignKey("mut_sig", List.of("cancer_study_id"), "cancer_study", List.of("cancer_study_id")));
        fks.add(new ForeignKey("mut_sig", List.of("entrez_gene_id"), "gene", List.of("entrez_gene_id")));

        // gistic / gistic_to_gene
        fks.add(new ForeignKey("gistic", List.of("cancer_study_id"), "cancer_study", List.of("cancer_study_id")));
        fks.add(new ForeignKey("gistic_to_gene", List.of("entrez_gene_id"), "gene", List.of("entrez_gene_id")));
        fks.add(new ForeignKey("gistic_to_gene", List.of("gistic_roi_id"), "gistic", List.of("gistic_roi_id")));

        // cna_event / sample_cna_event
        fks.add(new ForeignKey("cna_event", List.of("entrez_gene_id"), "gene", List.of("entrez_gene_id")));
        fks.add(new ForeignKey("sample_cna_event", List.of("cna_event_id"), "cna_event", List.of("cna_event_id")));
        fks.add(new ForeignKey("sample_cna_event", List.of("genetic_profile_id"), "genetic_profile", List.of("genetic_profile_id")));
        fks.add(new ForeignKey("sample_cna_event", List.of("sample_id"), "sample", List.of("internal_id")));

        // copy_number_seg / copy_number_seg_file
        fks.add(new ForeignKey("copy_number_seg", List.of("cancer_study_id"), "cancer_study", List.of("cancer_study_id")));
        fks.add(new ForeignKey("copy_number_seg", List.of("sample_id"), "sample", List.of("internal_id")));
        fks.add(new ForeignKey("copy_number_seg_file", List.of("cancer_study_id"), "cancer_study", List.of("cancer_study_id")));

        // clinical_event / clinical_event_data
        fks.add(new ForeignKey("clinical_event", List.of("patient_id"), "patient", List.of("internal_id")));
        fks.add(new ForeignKey("clinical_event_data", List.of("clinical_event_id"), "clinical_event", List.of("clinical_event_id")));

        // reference_genome_gene
        fks.add(new ForeignKey("reference_genome_gene", List.of("reference_genome_id"), "reference_genome", List.of("reference_genome_id")));
        fks.add(new ForeignKey("reference_genome_gene", List.of("entrez_gene_id"), "gene", List.of("entrez_gene_id")));

        // data_access_tokens
        fks.add(new ForeignKey("data_access_tokens", List.of("username"), "users", List.of("email")));

        // allele_specific_copy_number
        fks.add(new ForeignKey("allele_specific_copy_number", List.of("mutation_event_id"), "mutation_event", List.of("mutation_event_id")));
        fks.add(new ForeignKey("allele_specific_copy_number", List.of("genetic_profile_id"), "genetic_profile", List.of("genetic_profile_id")));
        fks.add(new ForeignKey("allele_specific_copy_number", List.of("sample_id"), "sample", List.of("internal_id")));

        // resource_definition
        fks.add(new ForeignKey("resource_definition", List.of("cancer_study_id"), "cancer_study", List.of("cancer_study_id")));

        // resource, which replaced resource_sample / resource_patient / resource_study.
        // Those tables keyed rows by internal id, so one column reached the parent directly.
        // resource stores stable ids, which are unique only within a study, so a reference
        // means nothing without the study beside it.
        fks.add(new ForeignKey("resource", List.of("cancer_study_id"), "cancer_study", List.of("cancer_study_id")));
        fks.add(new ForeignKey("resource", List.of("patient_id", "cancer_study_id"), "patient", List.of("stable_id", "cancer_study_id")));
        // The sample key needs the same (stable id, study) pair, but sample carries no
        // cancer_study_id -- it reaches the study through patient. Joining the two gives a parent
        // with both columns, so a sample reference is checked against the study it was imported
        // into rather than against every study that happens to reuse the barcode.
        fks.add(new ForeignKey("resource", List.of("sample_id", "cancer_study_id"),
                "sample", List.of("stable_id", "cancer_study_id"),
                "(SELECT s.`stable_id` AS `stable_id`, p.`cancer_study_id` AS `cancer_study_id`"
                        + " FROM sample s INNER JOIN patient p ON p.`internal_id` = s.`patient_id`)"));
        // Rows are checked only when every key column is non-NULL, so patient-level rows (no
        // sample) and study-level rows (neither) fall out of the keys that do not apply to them.
        // There is no key to resource_definition: a study carried over by the 3.1.0 migration can
        // hold rows for a resource it never declared, and the portal renders those by falling back
        // to the resource id rather than treating them as broken.

        return List.copyOf(fks);
    }

    /**
     * Unique key list for the ClickHouse schema (lower-case names).
     * Keep this in sync with clickhouse_cgds.sql.
     */
    private static List<UniqueKey> schemaUniqueKeys() {
        List<UniqueKey> uniqueKeys = new ArrayList<>();

        // type_of_cancer
        uniqueKeys.add(new UniqueKey("type_of_cancer", List.of("type_of_cancer_id")));

        // reference_genome
        uniqueKeys.add(new UniqueKey("reference_genome", List.of("reference_genome_id")));
        uniqueKeys.add(new UniqueKey("reference_genome", List.of("build_name")));

        // cancer_study
        uniqueKeys.add(new UniqueKey("cancer_study", List.of("cancer_study_id")));
        uniqueKeys.add(new UniqueKey("cancer_study", List.of("cancer_study_identifier")));

        // cancer_study_tags
        uniqueKeys.add(new UniqueKey("cancer_study_tags", List.of("cancer_study_id")));

        // users
        uniqueKeys.add(new UniqueKey("users", List.of("email")));

        // patient
        uniqueKeys.add(new UniqueKey("patient", List.of("internal_id")));

        // sample
        uniqueKeys.add(new UniqueKey("sample", List.of("internal_id")));

        // sample_list
        uniqueKeys.add(new UniqueKey("sample_list", List.of("list_id")));
        uniqueKeys.add(new UniqueKey("sample_list", List.of("stable_id")));

        // sample_list_list
        uniqueKeys.add(new UniqueKey("sample_list_list", List.of("list_id", "sample_id")));

        // genetic_entity
        uniqueKeys.add(new UniqueKey("genetic_entity", List.of("id")));

        // gene
        uniqueKeys.add(new UniqueKey("gene", List.of("entrez_gene_id")));
        uniqueKeys.add(new UniqueKey("gene", List.of("genetic_entity_id")));

        // gene_alias
        uniqueKeys.add(new UniqueKey("gene_alias", List.of("entrez_gene_id", "gene_alias")));

        // geneset
        uniqueKeys.add(new UniqueKey("geneset", List.of("id")));
        uniqueKeys.add(new UniqueKey("geneset", List.of("name")));
        uniqueKeys.add(new UniqueKey("geneset", List.of("external_id")));
        uniqueKeys.add(new UniqueKey("geneset", List.of("genetic_entity_id")));

        // geneset_gene
        uniqueKeys.add(new UniqueKey("geneset_gene", List.of("geneset_id", "entrez_gene_id")));

        // geneset_hierarchy_node
        uniqueKeys.add(new UniqueKey("geneset_hierarchy_node", List.of("node_id")));
        uniqueKeys.add(new UniqueKey("geneset_hierarchy_node", List.of("node_name", "parent_id")));

        // geneset_hierarchy_leaf
        uniqueKeys.add(new UniqueKey("geneset_hierarchy_leaf", List.of("node_id", "geneset_id")));

        // generic_entity_properties
        uniqueKeys.add(new UniqueKey("generic_entity_properties", List.of("id")));
        uniqueKeys.add(new UniqueKey("generic_entity_properties", List.of("genetic_entity_id", "name")));

        // genetic_profile
        uniqueKeys.add(new UniqueKey("genetic_profile", List.of("genetic_profile_id")));
        uniqueKeys.add(new UniqueKey("genetic_profile", List.of("stable_id")));

        // genetic_profile_link
        uniqueKeys.add(new UniqueKey("genetic_profile_link", List.of("referring_genetic_profile_id", "referred_genetic_profile_id")));

        // genetic_alteration
        uniqueKeys.add(new UniqueKey("genetic_alteration", List.of("genetic_profile_id", "genetic_entity_id")));

        // gene_panel
        uniqueKeys.add(new UniqueKey("gene_panel", List.of("internal_id")));
        uniqueKeys.add(new UniqueKey("gene_panel", List.of("stable_id")));

        // gene_panel_list
        uniqueKeys.add(new UniqueKey("gene_panel_list", List.of("internal_id", "gene_id")));

        // genetic_profile_samples
        uniqueKeys.add(new UniqueKey("genetic_profile_samples", List.of("genetic_profile_id")));

        // sample_profile
        uniqueKeys.add(new UniqueKey("sample_profile", List.of("sample_id", "genetic_profile_id")));

        // structural_variant
        uniqueKeys.add(new UniqueKey("structural_variant", List.of("internal_id")));

        // alteration_driver_annotation
        uniqueKeys.add(new UniqueKey("alteration_driver_annotation", List.of("alteration_event_id", "genetic_profile_id", "sample_id")));

        // mutation_event
        uniqueKeys.add(new UniqueKey("mutation_event", List.of("mutation_event_id")));

        // mutation
        uniqueKeys.add(new UniqueKey("mutation", List.of("mutation_event_id", "genetic_profile_id", "sample_id")));

        // clinical_patient / clinical_sample
        uniqueKeys.add(new UniqueKey("clinical_patient", List.of("internal_id", "attr_id")));
        uniqueKeys.add(new UniqueKey("clinical_sample", List.of("internal_id", "attr_id")));

        // clinical_attribute_meta
        uniqueKeys.add(new UniqueKey("clinical_attribute_meta", List.of("attr_id", "cancer_study_id")));

        // mut_sig
        uniqueKeys.add(new UniqueKey("mut_sig", List.of("cancer_study_id", "entrez_gene_id")));

        // gistic / gistic_to_gene
        uniqueKeys.add(new UniqueKey("gistic", List.of("gistic_roi_id")));
        uniqueKeys.add(new UniqueKey("gistic_to_gene", List.of("gistic_roi_id", "entrez_gene_id")));

        // cna_event
        uniqueKeys.add(new UniqueKey("cna_event", List.of("cna_event_id")));
        uniqueKeys.add(new UniqueKey("cna_event", List.of("entrez_gene_id", "alteration")));

        // sample_cna_event
        uniqueKeys.add(new UniqueKey("sample_cna_event", List.of("cna_event_id", "sample_id", "genetic_profile_id")));

        // copy_number_seg / copy_number_seg_file
        uniqueKeys.add(new UniqueKey("copy_number_seg", List.of("seg_id")));
        uniqueKeys.add(new UniqueKey("copy_number_seg_file", List.of("seg_file_id")));

        // clinical_event
        uniqueKeys.add(new UniqueKey("clinical_event", List.of("clinical_event_id")));

        // reference_genome_gene
        uniqueKeys.add(new UniqueKey("reference_genome_gene", List.of("entrez_gene_id", "reference_genome_id")));

        // data_access_tokens
        uniqueKeys.add(new UniqueKey("data_access_tokens", List.of("token")));

        // resource_definition
        uniqueKeys.add(new UniqueKey("resource_definition", List.of("resource_id", "cancer_study_id")));

        // resource. Deletions select rows by internal_id, so a duplicate id would make
        // one row's deletion take another row with it. The natural key is not listed: it spans
        // patient_id and sample_id, which are NULL at the levels that do not use them, and the
        // generated SQL skips a row unless every key column is non-NULL.
        uniqueKeys.add(new UniqueKey("resource", List.of("internal_id")));

        // allele_specific_copy_number
        uniqueKeys.add(new UniqueKey("allele_specific_copy_number", List.of("mutation_event_id", "genetic_profile_id", "sample_id")));

        return List.copyOf(uniqueKeys);
    }

    /**
     * Builds a ClickHouse SELECT that returns one row per orphaned FK value.
     */
    private static String generateForeignKeyViolationSelect(ForeignKey fk, String childAlias, String parentAlias) {
        String joinPredicate = buildJoinPredicate(fk, childAlias, parentAlias);
        String childHasAllValues = buildAllNotNullPredicate(fk.childCols, childAlias);
        String parentMissing = buildAllNullPredicate(fk.parentCols, parentAlias);
        String valuesExpr = buildKeyValuesExpression(fk.childCols, childAlias);

        return ""
                + "SELECT\n"
                + "  '" + fk.childTable + "' AS " + REFERER_TABLE + ",\n"
                + "  '" + fk.childColsCsv() + "' AS " + REFERER_COLUMNS + ",\n"
                + "  " + valuesExpr + " AS " + REFERER_VALUES + ",\n"
                + "  '" + fk.parentTable + "' AS " + REFERRED_TABLE + ",\n"
                + "  '" + fk.parentColsCsv() + "' AS " + REFERRED_COLUMNS + "\n"
                + "FROM " + fk.childTable + " " + childAlias + "\n"
                + "LEFT JOIN " + fk.parentSource() + " " + parentAlias + "\n"
                + "  ON " + joinPredicate + "\n"
                + "WHERE " + childHasAllValues + "\n"
                + "  AND " + parentMissing;
    }

    /**
     * Builds a ClickHouse SELECT that returns one row per duplicated unique-key value.
     */
    private static String generateUniqueKeyViolationSelect(UniqueKey uniqueKey, String tableAlias) {
        String allNotNull = buildAllNotNullPredicate(uniqueKey.columns, tableAlias);
        String valuesExpr = buildKeyValuesExpression(uniqueKey.columns, tableAlias);
        String groupBy = buildColumnReferenceList(uniqueKey.columns, tableAlias);

        return ""
                + "SELECT\n"
                + "  '" + uniqueKey.table + "' AS " + UNIQUE_TABLE + ",\n"
                + "  '" + uniqueKey.columnsCsv() + "' AS " + UNIQUE_COLUMNS + ",\n"
                + "  " + valuesExpr + " AS " + UNIQUE_VALUES + ",\n"
                + "  count() AS " + UNIQUE_DUPLICATE_COUNT + "\n"
                + "FROM " + uniqueKey.table + " " + tableAlias + "\n"
                + "WHERE " + allNotNull + "\n"
                + "GROUP BY " + groupBy + "\n"
                + "HAVING count() > 1";
    }

    /**
     * Generates one master UNION ALL query for a list of constraints.
     */
    private static <T> String generateMasterUnionQuery(
            List<T> constraints,
            Function<T, String> tableExtractor,
            Function<T, String> selectBuilder
    ) {
        if (constraints.isEmpty()) {
            return "";
        }
        Map<String, List<T>> byTable = constraints.stream()
                .collect(Collectors.groupingBy(tableExtractor, LinkedHashMap::new, Collectors.toList()));

        List<String> blocks = new ArrayList<>();
        for (Map.Entry<String, List<T>> e : byTable.entrySet()) {
            String table = e.getKey();
            List<T> entries = e.getValue();

            List<String> selects = new ArrayList<>();
            for (T entry : entries) {
                selects.add(selectBuilder.apply(entry));
            }
            String union = String.join("\nUNION ALL\n", selects);
            blocks.add("-- ===============================\n"
                    + "-- table: " + table + "\n"
                    + "-- ===============================\n"
                    + union + ";\n");
        }

        List<String> blockSelects = blocks.stream()
                .map(s -> s.replaceAll(";\\s*$", ""))
                .toList();
        return String.join("\nUNION ALL\n", blockSelects) + ";\n";
    }

// -------------------------
// Helpers
// -------------------------

    private static String buildJoinPredicate(ForeignKey fk, String childAlias, String parentAlias) {
        return zip(fk.childCols, fk.parentCols).stream()
                .map(pair -> childAlias + "." + quote(pair.left) + " = " + parentAlias + "." + quote(pair.right))
                .collect(Collectors.joining(" AND "));
    }

    private static String buildAllNotNullPredicate(List<String> columns, String tableAlias) {
        return columns.stream()
                .map(col -> "NOT isNull(" + tableAlias + "." + quote(col) + ")")
                .collect(Collectors.joining(" AND ", "(", ")"));
    }

    private static String buildAllNullPredicate(List<String> columns, String tableAlias) {
        return columns.stream()
                .map(col -> "isNull(" + tableAlias + "." + quote(col) + ")")
                .collect(Collectors.joining(" AND ", "(", ")"));
    }

    /**
     * Builds the key-values expression, joining values with '|' and rendering NULL as "NULL".
     */
    private static String buildKeyValuesExpression(List<String> columns, String tableAlias) {
        List<String> values = columns.stream()
                .map(col -> "ifNull(toString(" + tableAlias + "." + quote(col) + "), 'NULL')")
                .toList();
        return buildDelimitedConcat(values, VALUE_SEPARATOR);
    }

    private static String buildDelimitedConcat(List<String> args, String separatorLiteral) {
        if (args.isEmpty()) {
            throw new IllegalArgumentException("Cannot build concatenation for empty argument list.");
        }
        if (args.size() == 1) {
            return args.get(0);
        }
        StringBuilder sb = new StringBuilder("concat(");
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) {
                sb.append(", '").append(separatorLiteral).append("', ");
            }
            sb.append(args.get(i));
        }
        sb.append(")");
        return sb.toString();
    }

    private static String buildColumnReferenceList(List<String> columns, String tableAlias) {
        return columns.stream()
                .map(col -> tableAlias + "." + quote(col))
                .collect(Collectors.joining(", "));
    }

    private static String quote(String identifier) {
        // ClickHouse supports backticks for identifiers; safe for reserved words.
        return "`" + identifier.replace("`", "``") + "`";
    }

    private record Pair(String left, String right) {
    }

    private static List<Pair> zip(List<String> a, List<String> b) {
        if (a.size() != b.size()) {
            throw new IllegalArgumentException("Mismatched list sizes: " + a.size() + " vs " + b.size());
        }
        List<Pair> out = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            out.add(new Pair(a.get(i), b.get(i)));
        }
        return out;
    }
}
