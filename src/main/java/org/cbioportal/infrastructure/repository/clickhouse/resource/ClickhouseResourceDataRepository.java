package org.cbioportal.infrastructure.repository.clickhouse.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cbioportal.domain.resource.ResourceColumnInfo;
import org.cbioportal.domain.resource.ResourceContractRow;
import org.cbioportal.domain.resource.ResourceFacetOption;
import org.cbioportal.domain.resource.ResourceMetadataFacetValue;
import org.cbioportal.domain.resource.ResourceMetadataField;
import org.cbioportal.domain.resource.ResourceMetadataKeyStats;
import org.cbioportal.domain.resource.ResourceMetadataRange;
import org.cbioportal.domain.resource.ResourceMetadataSchema;
import org.cbioportal.domain.resource.ResourceNumericRange;
import org.cbioportal.domain.resource.ResourceTableCounts;
import org.cbioportal.domain.resource.ResourceTableMetadataView;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.ResourceTableTab;
import org.cbioportal.domain.resource.ResourceTabsRequest;
import org.cbioportal.domain.resource.repository.ResourceDataRepository;
import org.cbioportal.domain.wsi.WsiDeidentification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseResourceDataRepository implements ResourceDataRepository {
  private static final Logger LOG = LoggerFactory.getLogger(ClickhouseResourceDataRepository.class);

  // Only non-ID builtin columns that benefit from categorical filtering
  private static final Map<String, String> FACET_COLUMNS = Map.of("type", "rdata.type");

  /**
   * Metadata key holding WSI artifact locations. It is private to the WSI access API for every row,
   * whatever its type, matching the search, filter, sort, facet and key-discovery guards in
   * ResourceDataMapper.xml.
   */
  private static final String WSI_SERVING_KEY = "wsi_serving";

  /**
   * Past this many distinct values a facet is no use as a dropdown, and the payload grows with the
   * data: a key that is unique or near-unique per row enumerates the whole resource. An over-cap
   * column keeps its search box but loses its value list, the same way a numeric column gets a
   * range instead.
   */
  private static final int MAX_FACET_VALUES = 500;

  /**
   * Key discovery and numeric detection read this many rows rather than the whole resource, so
   * their cost stays flat as a resource grows. A key appearing only beyond the sample is not
   * discovered; the bound is large enough that this needs very heterogeneous metadata to matter.
   */
  private static final int KEY_DISCOVERY_SAMPLE_ROWS = 100_000;

  /** Backstop so a single request cannot exhaust server memory if the sample is raised. */
  private static final long KEY_DISCOVERY_MAX_MEMORY_BYTES = 2L * 1024 * 1024 * 1024;

  private final ClickhouseResourceDataMapper mapper;

  public ClickhouseResourceDataRepository(ClickhouseResourceDataMapper mapper) {
    this.mapper = mapper;
  }

  /**
   * The two things every metadata-column decision needs, resolved once per call.
   *
   * <p>Both used to be fetched inside the per-key loops, which turned each request into dozens of
   * identical ClickHouse round trips: the contract lookup alone ran once per key per predicate, and
   * every one of those queries carried the request's full patient/sample IN lists just to return a
   * single unchanging string.
   */
  private record MetadataContext(
      ResourceMetadataSchema schema,
      Map<String, ResourceMetadataKeyStats> statsByKey,
      boolean contractCoversEveryStudy) {

    /**
     * The keys that become columns.
     *
     * <p>When every study in scope declares the resource, the merged contract decides: the column
     * set is what a curator reviewed, not whatever the data happened to carry, and it no longer
     * depends on the discovery sample. The importer rejects a file whose METADATA carries keys the
     * contract does not declare, so data cannot go missing this way without the curator being told.
     *
     * <p>That guarantee is per study, so it does not hold for a cohort where only some studies
     * declare the resource: the rest were never checked against any contract, and restricting the
     * columns would hide their keys. There the contract orders and decorates the columns it
     * declares, and the keys found in the data are added after them.
     *
     * <p>With no contract at all the keys come from the data alone, which is how resources imported
     * before contracts existed keep working.
     */
    Collection<String> columnKeys() {
      Collection<String> declared = schema.fieldsByKey().keySet();
      if (declared.isEmpty()) {
        return statsByKey.keySet();
      }
      if (contractCoversEveryStudy) {
        return declared;
      }
      Set<String> keys = new LinkedHashSet<>(declared);
      statsByKey.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER).forEach(keys::add);
      return keys;
    }

    boolean isNumeric(String key) {
      ResourceMetadataField field = schema.fieldsByKey().get(key);
      String declaredType = field != null ? field.type() : null;
      if ("string".equals(declaredType)) {
        return false;
      }
      if ("number".equals(declaredType)) {
        return true;
      }
      // Undeclared type: fall back to what the data looks like. Sampled counts are fine for
      // classification; only the slider's bounds have to be exact.
      ResourceMetadataKeyStats stats = statsByKey.get(key);
      return stats != null && stats.isAutoDetectedNumeric();
    }

    /**
     * Only an explicit {@code "filterable": false} turns filtering off; an absent contract or an
     * absent flag leaves it on, which is what every existing resource already has.
     */
    boolean isFilterable(String key) {
      ResourceMetadataField field = schema.fieldsByKey().get(key);
      return field == null || field.filterable() == null || field.filterable();
    }
  }

  private MetadataContext resolveMetadataContext(ResourceTableQuery scoped) {
    List<ResourceContractRow> contractRows = contractRows(scoped);
    ResourceMetadataSchema schema = mergedSchema(scoped, contractRows);
    boolean coversEveryStudy =
        !contractRows.isEmpty() && contractRows.stream().allMatch(ResourceContractRow::declared);
    // Key discovery scans the data to learn which keys exist and which look numeric. A contract
    // that covers every study in scope and types every field already answers both, so the scan is
    // skipped rather than repeated.
    Map<String, ResourceMetadataKeyStats> stats =
        coversEveryStudy && fullyTyped(schema) ? Map.of() : classifyMetadataKeys(scoped);
    return new MetadataContext(schema, stats, coversEveryStudy);
  }

  /** True when the contract declares every column and gives each one a type. */
  private static boolean fullyTyped(ResourceMetadataSchema schema) {
    return !schema.fieldsByKey().isEmpty()
        && schema.fieldsByKey().values().stream().allMatch(field -> field.type() != null);
  }

  /*
   * WSI_SAMPLE / WSI_PATIENT rows are excluded in SQL by every statement (ExcludeWsiResourceRows in
   * ResourceDataMapper.xml). Each entry point below checks again here so a future statement that
   * forgets the predicate still cannot hand a slide row, or anything derived from one, to the
   * generic resource table: tabs, rows (query/fetch), and columns, facets, ranges and counts
   * (metadata/fetch).
   */

  @Override
  public List<ResourceTableTab> getResourceTableTabs(ResourceTabsRequest request) {
    List<ResourceTableTab> tabs = mapper.getResourceTableTabs(request);
    if (tabs == null || tabs.isEmpty()) {
      return tabs;
    }
    return tabs.stream()
        .filter(tab -> !WsiDeidentification.isWsiResourceId(tab.resourceId()))
        .toList();
  }

  @Override
  public List<ResourceTableRow> getResourceTableRows(ResourceTableQuery query) {
    if (WsiDeidentification.isWsiResourceId(query.resourceId())) {
      return List.of();
    }
    List<ResourceTableRow> rows = mapper.getResourceTableRows(query);
    if (rows == null || rows.isEmpty()) {
      return rows;
    }
    return rows.stream()
        .filter(row -> !WsiDeidentification.isWsiResourceId(row.resourceId()))
        .map(ClickhouseResourceDataRepository::withoutServingMetadata)
        .toList();
  }

  private static ResourceTableRow withoutServingMetadata(ResourceTableRow row) {
    if (row.metadata() == null || !row.metadata().containsKey(WSI_SERVING_KEY)) {
      return row;
    }
    Map<String, Object> metadata = new LinkedHashMap<>(row.metadata());
    metadata.remove(WSI_SERVING_KEY);
    return new ResourceTableRow(
        row.studyId(),
        row.resourceId(),
        row.resourceDisplayName(),
        row.resourceType(),
        row.patientId(),
        row.sampleId(),
        row.url(),
        row.displayName(),
        row.type(),
        metadata);
  }

  @Override
  public ResourceTableMetadataView getResourceTableMetadata(ResourceTableQuery query) {
    if (WsiDeidentification.isWsiResourceId(query.resourceId())) {
      return ResourceTableMetadataView.empty();
    }
    // Facets and key discovery are always computed against the query with ALL column-level filters
    // removed (only resourceId/study/patient/sample/search scoping kept). This keeps every filter
    // dropdown showing its full, stable option set regardless of what is currently selected in ANY
    // column, itself or another — e.g. deselecting every option in one column (a "1 = 0" filter
    // that zeroes out all matching rows) must not make that column's own options, or any other
    // column's options or keys, disappear.
    ResourceTableQuery scoped = withoutColumnFilters(query);
    MetadataContext context = resolveMetadataContext(scoped);

    Map<String, List<ResourceFacetOption>> facets = new LinkedHashMap<>();
    for (Map.Entry<String, String> entry : FACET_COLUMNS.entrySet()) {
      List<ResourceFacetOption> values =
          mapper.getResourceTableFacetValues(scoped, entry.getValue(), MAX_FACET_VALUES + 1);
      if (withinFacetCap(entry.getKey(), values)) {
        facets.put(entry.getKey(), values);
      }
    }

    Map<String, ResourceNumericRange> facetRanges = new LinkedHashMap<>();
    List<String> categoricalKeys = new ArrayList<>();
    List<String> numericKeys = new ArrayList<>();
    for (String key : context.columnKeys()) {
      if (!context.isFilterable(key)) {
        continue;
      }
      // Numeric columns get a min/max range instead of an enumerated value list, which would be
      // huge and unhelpful for a continuous measurement.
      (context.isNumeric(key) ? numericKeys : categoricalKeys).add(key);
    }
    facetRanges.putAll(metadataRanges(scoped, numericKeys));
    facets.putAll(metadataFacets(scoped, categoricalKeys));

    return new ResourceTableMetadataView(metadataColumns(context), facets, facetRanges);
  }

  /**
   * Exact ranges for the numeric keys, read over the whole filtered set rather than the discovery
   * sample: these bounds are what the slider offers, and a narrowed range would put rows beyond it
   * out of reach.
   */
  private Map<String, ResourceNumericRange> metadataRanges(
      ResourceTableQuery scoped, List<String> keys) {
    if (keys.isEmpty()) {
      return Map.of();
    }
    Map<String, ResourceNumericRange> ranges = new LinkedHashMap<>();
    for (ResourceMetadataRange range :
        mapper.getResourceTableMetadataRanges(scoped, keys.toArray(new String[0]))) {
      if (range.isUsable()) {
        ranges.put(
            ResourceColumnInfo.METADATA_COLUMN_PREFIX + range.metaKey(),
            new ResourceNumericRange(range.minValue(), range.maxValue()));
      }
    }
    return ranges;
  }

  /** All categorical metadata facets in one query, grouped by key, with over-cap keys dropped. */
  private Map<String, List<ResourceFacetOption>> metadataFacets(
      ResourceTableQuery scoped, List<String> keys) {
    if (keys.isEmpty()) {
      return Map.of();
    }
    List<ResourceMetadataFacetValue> rows =
        mapper.getResourceTableMetadataFacets(
            scoped, keys.toArray(new String[0]), MAX_FACET_VALUES + 1);

    Map<String, List<ResourceFacetOption>> byKey = new LinkedHashMap<>();
    for (ResourceMetadataFacetValue row : rows) {
      byKey
          .computeIfAbsent(row.metaKey(), k -> new ArrayList<>())
          .add(new ResourceFacetOption(row.value(), row.count()));
    }

    Map<String, List<ResourceFacetOption>> facets = new LinkedHashMap<>();
    // Iterate the requested keys, not the result, so column order follows the contract.
    for (String key : keys) {
      String columnId = ResourceColumnInfo.METADATA_COLUMN_PREFIX + key;
      List<ResourceFacetOption> values = byKey.get(key);
      if (withinFacetCap(columnId, values)) {
        facets.put(columnId, values);
      }
    }
    return facets;
  }

  /**
   * Whether a facet is small enough to be worth returning. The query asks for one more than the
   * cap, so an over-cap result is recognised without counting the whole column.
   */
  private boolean withinFacetCap(String columnId, List<ResourceFacetOption> values) {
    if (values == null || values.isEmpty()) {
      return false;
    }
    if (values.size() > MAX_FACET_VALUES) {
      LOG.debug(
          "Dropping the facet for '{}': more than {} distinct values. The column stays searchable"
              + " but offers no value list. Declare \"filterable\": false for it in the"
              + " resource's custom_metadata to skip the aggregation entirely.",
          columnId,
          MAX_FACET_VALUES);
      return false;
    }
    return true;
  }

  /** Builds the column list for the key set the contract, or failing that the data, decides on. */
  private List<ResourceColumnInfo> metadataColumns(MetadataContext context) {
    Map<String, ResourceMetadataField> declared = context.schema().fieldsByKey();

    // Declared order is the curator's order. Without a contract, fall back to the keys the data
    // turned up, alphabetically, so the ordering is at least stable.
    List<String> ordered = new ArrayList<>(context.columnKeys());
    if (declared.isEmpty()) {
      ordered.sort(String.CASE_INSENSITIVE_ORDER);
    }

    List<ResourceColumnInfo> columns = new ArrayList<>();
    for (String key : ordered) {
      ResourceMetadataField field = declared.get(key);
      columns.add(
          new ResourceColumnInfo(
              ResourceColumnInfo.METADATA_COLUMN_PREFIX + key,
              field != null && field.label() != null && !field.label().isBlank()
                  ? field.label()
                  : key,
              ResourceColumnInfo.SOURCE_METADATA,
              context.isNumeric(key) ? "number" : "string",
              context.isFilterable(key),
              true,
              // Metadata columns stay opt-in: a resource can carry many keys and showing them all
              // would bury the builtin columns. A curator opts individual columns in by declaring
              // "visibleByDefault": true in the contract.
              field != null && Boolean.TRUE.equals(field.visibleByDefault()),
              field != null ? field.description() : null));
    }
    return columns;
  }

  /**
   * Returns per-key stats for every metadata key discovered in the current tab, keyed by the raw
   * metadata key (not prefixed with "metadata:").
   */
  private Map<String, ResourceMetadataKeyStats> classifyMetadataKeys(ResourceTableQuery query) {
    List<ResourceMetadataKeyStats> stats =
        mapper.getResourceTableMetadataKeyStats(
            query, KEY_DISCOVERY_SAMPLE_ROWS, KEY_DISCOVERY_MAX_MEMORY_BYTES);
    Map<String, ResourceMetadataKeyStats> byKey = new LinkedHashMap<>();
    if (stats != null) {
      for (ResourceMetadataKeyStats stat : stats) {
        if (WSI_SERVING_KEY.equals(stat.key())) {
          continue;
        }
        byKey.put(stat.key(), stat);
      }
    }
    return byKey;
  }

  private List<ResourceContractRow> contractRows(ResourceTableQuery query) {
    List<ResourceContractRow> rows = mapper.getResourceDefinitionCustomMetadata(query);
    return rows == null ? List.of() : rows;
  }

  /**
   * Combines every contract in scope into the one the table is built from.
   *
   * <p>The contract is declared per (resource, study) and a cohort can span studies, so one table
   * can be described by several. They are merged rather than chosen between, so that no study's
   * declared keys are dropped on account of another study's contract.
   */
  private ResourceMetadataSchema mergedSchema(
      ResourceTableQuery query, List<ResourceContractRow> rows) {
    List<ResourceMetadataSchema> schemas =
        rows.stream()
            .filter(ResourceContractRow::declared)
            .map(row -> ResourceMetadataSchema.parse(row.customMetadata()))
            .filter(schema -> !schema.fields().isEmpty())
            .toList();
    if (schemas.isEmpty()) {
      return ResourceMetadataSchema.empty();
    }
    warnOnDivergence(query, rows, schemas);
    return ResourceMetadataSchema.merge(schemas);
  }

  /**
   * Reports contracts that disagree with each other, or cover only part of the cohort.
   *
   * <p>Neither stops the table rendering, and neither is visible in the response, so the log is the
   * only place a curator's mismatch between two studies can surface.
   */
  private void warnOnDivergence(
      ResourceTableQuery query,
      List<ResourceContractRow> rows,
      List<ResourceMetadataSchema> schemas) {
    List<String> undeclaredStudies =
        rows.stream()
            .filter(row -> !row.declared())
            .map(ResourceContractRow::firstStudy)
            .sorted()
            .toList();
    if (!undeclaredStudies.isEmpty()) {
      LOG.warn(
          "Resource '{}' declares custom_metadata in some of the selected studies but not in {};"
              + " showing the declared columns alongside the keys found in the data.",
          query.resourceId(),
          undeclaredStudies);
    }
    if (schemas.size() > 1) {
      LOG.warn(
          "Resource '{}' has {} differing custom_metadata contracts across the selected studies;"
              + " showing the union of their fields, each taking the first declaration by study"
              + " identifier.",
          query.resourceId(),
          schemas.size());
      List<String> conflicts = ResourceMetadataSchema.conflictingTypeKeys(schemas);
      if (!conflicts.isEmpty()) {
        LOG.warn(
            "Resource '{}' has keys typed differently by those contracts: {}. Their type is taken"
                + " from the data instead.",
            query.resourceId(),
            conflicts);
      }
    }
  }

  /** Returns a copy of the query with all column-level filters removed. */
  private ResourceTableQuery withoutColumnFilters(ResourceTableQuery query) {
    if (query.filters() == null || query.filters().isEmpty()) {
      return query;
    }
    return new ResourceTableQuery(
        query.studyIds(),
        query.resourceId(),
        query.patientIdentifiers(),
        query.sampleIdentifiers(),
        query.search(),
        query.pageNumber(),
        query.pageSize(),
        query.sortBy(),
        query.direction(),
        List.of());
  }

  @Override
  public ResourceTableCounts getResourceTableCounts(ResourceTableQuery query) {
    if (WsiDeidentification.isWsiResourceId(query.resourceId())) {
      return ResourceTableCounts.empty();
    }
    ResourceTableCounts counts = mapper.getResourceTableCounts(query);
    return counts == null ? ResourceTableCounts.empty() : counts;
  }
}
