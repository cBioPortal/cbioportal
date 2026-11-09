package org.cbioportal.infrastructure.repository.clickhouse.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cbioportal.domain.resource.ResourceColumnInfo;
import org.cbioportal.domain.resource.ResourceContractRow;
import org.cbioportal.domain.resource.ResourceFacetOption;
import org.cbioportal.domain.resource.ResourceGroupCountColumn;
import org.cbioportal.domain.resource.ResourceMetadataFacetValue;
import org.cbioportal.domain.resource.ResourceMetadataField;
import org.cbioportal.domain.resource.ResourceMetadataKeyStats;
import org.cbioportal.domain.resource.ResourceMetadataRange;
import org.cbioportal.domain.resource.ResourceMetadataSchema;
import org.cbioportal.domain.resource.ResourceNumericRange;
import org.cbioportal.domain.resource.ResourceQueryContext;
import org.cbioportal.domain.resource.ResourceScope;
import org.cbioportal.domain.resource.ResourceTableCounts;
import org.cbioportal.domain.resource.ResourceTableGroup;
import org.cbioportal.domain.resource.ResourceTableGroupPage;
import org.cbioportal.domain.resource.ResourceTableMetadataView;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.ResourceTableTab;
import org.cbioportal.domain.resource.ResourceTabsRequest;
import org.cbioportal.domain.resource.repository.ResourceDataRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseResourceDataRepository implements ResourceDataRepository {
  private static final Logger LOG = LoggerFactory.getLogger(ClickhouseResourceDataRepository.class);

  // Only non-ID builtin columns that benefit from categorical filtering

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

  /**
   * Backstop for the grouped facets, which keep a set of groups per distinct value until the cap
   * per key applies: a near-unique key holds about one entry per row. Past the first bound the
   * aggregation spills to disk; past the second the request fails rather than the server.
   */
  private static final long GROUPED_FACETS_MAX_MEMORY_BYTES = 2L * 1024 * 1024 * 1024;

  private static final long GROUPED_FACETS_EXTERNAL_GROUP_BY_BYTES = 1024L * 1024 * 1024;

  /** The declared types that decide a column's kind; any other leaves it to the data. */
  private static final Set<String> DECLARED_TYPES = Set.of("string", "number");

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final TypeReference<Map<String, Map<String, Long>>> GROUP_COUNTS_TYPE =
      new TypeReference<>() {};

  private final ClickhouseResourceDataMapper mapper;

  public ClickhouseResourceDataRepository(ClickhouseResourceDataMapper mapper) {
    this.mapper = mapper;
  }

  /**
   * The two things every metadata-column decision needs, resolved once per call. Resolving them
   * inside the per-key loops would repeat the same ClickHouse query per key, each one carrying the
   * request's full patient and sample IN lists to return an unchanging result.
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
     * Only an explicit {@code "filterable": false} turns filtering off. An absent contract or an
     * absent flag leaves it on, which is the state of every resource without a contract.
     */
    boolean isFilterable(String key) {
      ResourceMetadataField field = schema.fieldsByKey().get(key);
      return field == null || field.filterable() == null || field.filterable();
    }
  }

  /**
   * How one request reads: its query and what it resolved to, the study-view cohort and the
   * resource scope. Carried together because every statement below takes the query and cohort.
   */
  private record Reads(ResourceTableQuery query, ResourceQueryContext context) {

    static Reads of(ResourceTableQuery query, ResourceQueryContext context) {
      return new Reads(query, context == null ? ResourceQueryContext.none() : context);
    }

    StudyViewFilterContext cohort() {
      return context.cohort();
    }

    Reads withQuery(ResourceTableQuery other) {
      return new Reads(other, context);
    }
  }

  private MetadataContext resolveMetadataContext(Reads scoped, boolean grouped) {
    List<ResourceContractRow> contractRows = contractRows(scoped, grouped);
    ResourceMetadataSchema schema = mergedSchema(scoped, contractRows);
    boolean coversEveryStudy =
        !contractRows.isEmpty() && contractRows.stream().allMatch(ResourceContractRow::declared);
    return new MetadataContext(
        schema, keyStats(scoped, schema, coversEveryStudy), coversEveryStudy);
  }

  /**
   * The stats that classify the keys the contract leaves untyped.
   *
   * <p>Key discovery scans each row's raw document to learn which keys exist and which look
   * numeric. A contract that covers every study in scope already names the columns, so only the
   * keys whose type it leaves to the data need classifying, and they are counted from their own
   * subcolumns rather than by parsing every document; with every key typed nothing is read at all.
   * Without such a contract the keys found in the data become columns too, so they are discovered.
   */
  private Map<String, ResourceMetadataKeyStats> keyStats(
      Reads scoped, ResourceMetadataSchema schema, boolean coversEveryStudy) {
    if (!coversEveryStudy || schema.fieldsByKey().isEmpty()) {
      return classifyMetadataKeys(scoped, null);
    }
    if (fullyTyped(schema)) {
      return Map.of();
    }
    List<String> classified =
        schema.fieldsByKey().entrySet().stream()
            .filter(
                field ->
                    field.getValue().type() == null
                        || !DECLARED_TYPES.contains(field.getValue().type()))
            .map(Map.Entry::getKey)
            .toList();
    return classifyMetadataKeys(scoped, classified);
  }

  /** True when the contract gives every column a type. */
  private static boolean fullyTyped(ResourceMetadataSchema schema) {
    return schema.fieldsByKey().values().stream().allMatch(field -> field.type() != null);
  }

  @Override
  public List<ResourceTableTab> getResourceTableTabs(ResourceTabsRequest request) {
    return mapper.getResourceTableTabs(request);
  }

  @Override
  public List<ResourceTableRow> getResourceTableRows(
      ResourceTableQuery query, ResourceQueryContext context) {
    return mapper.getResourceTableRows(query, Reads.of(query, context).cohort());
  }

  @Override
  public ResourceTableMetadataView getResourceTableMetadata(
      ResourceTableQuery query, ResourceQueryContext context) {
    // Facets and key discovery are always computed against the query with ALL column-level filters
    // removed (only resourceId/study/patient/sample/search scoping kept). This keeps every filter
    // dropdown showing its full, stable option set regardless of what is currently selected in ANY
    // column, itself or another — e.g. deselecting every option in one column (a "1 = 0" filter
    // that zeroes out all matching rows) must not make that column's own options, or any other
    // column's options or keys, disappear.
    //
    // Grouping changes only what a facet value counts: the groups carrying it rather than the rows.
    // The key classification and the numeric ranges describe the rows, so they are read exactly as
    // for the same query without groupBy; only the facets keep the grouping, and with it the
    // restriction to rows that belong to a group. The contracts are the one exception; see
    // contractRows.
    Reads reads = Reads.of(query, context);
    Reads grouped = reads.withQuery(withoutColumnFilters(query));
    Reads scoped = reads.withQuery(withoutColumnFilters(query).withoutGrouping());
    MetadataContext metadataContext = resolveMetadataContext(scoped, query.grouped());

    Map<String, List<ResourceFacetOption>> facets = new LinkedHashMap<>();

    Map<String, ResourceNumericRange> facetRanges = new LinkedHashMap<>();
    List<String> categoricalKeys = new ArrayList<>();
    List<String> numericKeys = new ArrayList<>();
    for (String key : metadataContext.columnKeys()) {
      // A blank key is never offered as a facet.
      if (key.isBlank() || !metadataContext.isFilterable(key)) {
        continue;
      }
      // Numeric columns get a min/max range instead of an enumerated value list, which would be
      // huge and unhelpful for a continuous measurement.
      (metadataContext.isNumeric(key) ? numericKeys : categoricalKeys).add(key);
    }
    facetRanges.putAll(metadataRanges(scoped, numericKeys));
    facets.putAll(metadataFacets(grouped, categoricalKeys));

    return new ResourceTableMetadataView(metadataColumns(metadataContext), facets, facetRanges);
  }

  /**
   * Exact ranges for the numeric keys, read over the whole filtered set rather than the discovery
   * sample: these bounds are what the slider offers, and a narrowed range would put rows beyond it
   * out of reach.
   */
  private Map<String, ResourceNumericRange> metadataRanges(Reads scoped, List<String> keys) {
    if (keys.isEmpty()) {
      return Map.of();
    }
    Map<String, ResourceNumericRange> ranges = new LinkedHashMap<>();
    for (ResourceMetadataRange range :
        mapper.getResourceTableMetadataRanges(
            scoped.query(), scoped.cohort(), keys.toArray(new String[0]))) {
      if (range.isUsable()) {
        ranges.put(
            ResourceColumnInfo.METADATA_COLUMN_PREFIX + range.metaKey(),
            new ResourceNumericRange(range.minValue(), range.maxValue()));
      }
    }
    return ranges;
  }

  /** All categorical metadata facets in one query, grouped by key, with over-cap keys dropped. */
  private Map<String, List<ResourceFacetOption>> metadataFacets(Reads scoped, List<String> keys) {
    if (keys.isEmpty()) {
      return Map.of();
    }
    String[] keyArray = keys.toArray(new String[0]);
    List<ResourceMetadataFacetValue> rows =
        scoped.query().grouped()
            ? mapper.getResourceTableGroupedMetadataFacets(
                scoped.query(),
                scoped.cohort(),
                keyArray,
                MAX_FACET_VALUES + 1,
                GROUPED_FACETS_MAX_MEMORY_BYTES,
                GROUPED_FACETS_EXTERNAL_GROUP_BY_BYTES)
            : mapper.getResourceTableMetadataFacets(
                scoped.query(), scoped.cohort(), keyArray, MAX_FACET_VALUES + 1);

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
   * Returns per-key stats for the given keys, or for every metadata key discovered in the current
   * tab when {@code keys} is null, keyed by the raw metadata key (not prefixed with "metadata:").
   */
  private Map<String, ResourceMetadataKeyStats> classifyMetadataKeys(
      Reads scoped, List<String> keys) {
    List<ResourceMetadataKeyStats> stats =
        mapper.getResourceTableMetadataKeyStats(
            scoped.query(),
            scoped.cohort(),
            keys == null ? null : keys.toArray(new String[0]),
            KEY_DISCOVERY_SAMPLE_ROWS,
            KEY_DISCOVERY_MAX_MEMORY_BYTES);
    Map<String, ResourceMetadataKeyStats> byKey = new LinkedHashMap<>();
    if (stats != null) {
      for (ResourceMetadataKeyStats stat : stats) {
        // A blank key in the data names no column anyone could have declared or filter on.
        if (stat.key() != null && !stat.key().isBlank()) {
          byKey.put(stat.key(), stat);
        }
      }
    }
    return byKey;
  }

  /**
   * The contracts in scope.
   *
   * <p>A grouped request has already read its resource scope from the definitions, so it takes the
   * contracts from there: the contract of every (resource, study) pair that may hold rows. Any
   * other request reads them over the distinct (study, resource) pairs of the rows it matches. The
   * two agree unless the cohort or the search removes every row of a study whose contract differs
   * from the rest; a grouped table then still shows that study's declared columns, empty.
   *
   * <p>An ungrouped request naming {@code resourceIds} has read the scope too, and takes the
   * contracts from it when reading them over the rows could not give a different answer: when
   * nothing narrows the rows below the requested studies, and the pairs that may hold rows all
   * carry the same contract, or none. Over the rows the statement would then return that one
   * contract again, or nothing at all when the requested studies hold no row of the requested
   * resources; in that case the table lists the declared columns, empty, rather than no columns.
   * Every other request reads them over its rows.
   */
  private List<ResourceContractRow> contractRows(Reads scoped, boolean grouped) {
    ResourceScope scope = scoped.context().scope();
    if (scope != null) {
      List<ResourceContractRow> fromScope = scope.contractRows();
      if (grouped || (!narrowsRows(scoped) && fromScope.size() <= 1)) {
        return fromScope;
      }
    }
    List<ResourceContractRow> rows =
        mapper.getResourceDefinitionCustomMetadata(scoped.query(), scoped.cohort());
    return rows == null ? List.of() : rows;
  }

  /** Whether the request reads fewer rows than its studies hold of its resources. */
  private static boolean narrowsRows(Reads reads) {
    ResourceTableQuery query = reads.query();
    return reads.cohort() != null
        || query.hasPatients()
        || query.hasSamples()
        || query.hasSearch()
        || (query.filters() != null && !query.filters().isEmpty());
  }

  /**
   * Combines every contract in scope into the one the table is built from.
   *
   * <p>The contract is declared per (resource, study) and a cohort can span studies, so one table
   * can be described by several. They are merged rather than chosen between, so that no study's
   * declared keys are dropped on account of another study's contract.
   */
  private ResourceMetadataSchema mergedSchema(Reads scoped, List<ResourceContractRow> rows) {
    ResourceTableQuery query = scoped.query();
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
          query.effectiveResourceIds(),
          undeclaredStudies);
    }
    if (schemas.size() > 1) {
      LOG.warn(
          "Resource '{}' has {} differing custom_metadata contracts across the selected studies;"
              + " showing the union of their fields, each taking the first declaration by study"
              + " identifier.",
          query.effectiveResourceIds(),
          schemas.size());
      List<String> conflicts = ResourceMetadataSchema.conflictingTypeKeys(schemas);
      if (!conflicts.isEmpty()) {
        LOG.warn(
            "Resource '{}' has keys typed differently by those contracts: {}. Their type is taken"
                + " from the data instead.",
            query.effectiveResourceIds(),
            conflicts);
      }
    }
  }

  /** Returns a copy of the query with all column-level filters removed. */
  private ResourceTableQuery withoutColumnFilters(ResourceTableQuery query) {
    if (query.filters() == null || query.filters().isEmpty()) {
      return query;
    }
    return query.withoutColumnFilters();
  }

  @Override
  public ResourceTableCounts getResourceTableCounts(
      ResourceTableQuery query, ResourceQueryContext context) {
    // Counts are over rows whether or not the query groups them; see getResourceTableMetadata.
    Reads reads = Reads.of(query, context).withQuery(query.withoutGrouping());
    ResourceTableCounts counts = mapper.getResourceTableCounts(reads.query(), reads.cohort());
    return counts == null ? ResourceTableCounts.empty() : counts;
  }

  @Override
  public ResourceScope getResourceScope(ResourceTableQuery query) {
    return new ResourceScope(mapper.getResourceScope(query));
  }

  /**
   * A page of groups in at most two statements: the page with its totals and located index, and the
   * value counts for that page's groups. Counting values only for the page's groups is what keeps a
   * page cheap: the page itself is ordered on keys and row counts, which need no metadata parsed.
   */
  @Override
  public ResourceTableGroupPage getResourceTableGroups(
      ResourceTableQuery query, ResourceQueryContext context) {
    Reads reads = Reads.of(query, context);
    List<ResourceTableGroupRow> page = mapper.getResourceTableGroupPage(query, reads.cohort());

    // Every row of the page carries the totals and the located index. A page past the last group
    // has no row to carry them, so the first group is read alone for them instead.
    ResourceTableGroupRow carrier = page.isEmpty() ? null : page.getFirst();
    if (carrier == null && query.offset() > 0) {
      List<ResourceTableGroupRow> first =
          mapper.getResourceTableGroupPage(firstGroupOnly(query), reads.cohort());
      carrier = first.isEmpty() ? null : first.getFirst();
    }

    Map<GroupRef, Map<String, Map<String, Long>>> countsByGroup = groupCounts(reads, page);
    List<ResourceTableGroup> groups = new ArrayList<>(page.size());
    for (ResourceTableGroupRow row : page) {
      groups.add(
          new ResourceTableGroup(
              row.studyId(),
              row.patientId(),
              row.sampleId(),
              row.rowCount(),
              countsByGroup.getOrDefault(
                  GroupRef.of(query, row), emptyCounts(query.groupCountColumns()))));
    }
    return new ResourceTableGroupPage(
        carrier == null ? 0L : carrier.totalGroups(),
        carrier == null ? 0L : carrier.totalRows(),
        carrier == null || query.locate() == null ? null : carrier.locatedIndex(),
        List.copyOf(groups));
  }

  /** The same query reduced to its first group, which carries the totals of every page. */
  private static ResourceTableQuery firstGroupOnly(ResourceTableQuery query) {
    return new ResourceTableQuery(
        query.studyIds(),
        query.resourceId(),
        query.patientIdentifiers(),
        query.sampleIdentifiers(),
        query.search(),
        0,
        1,
        query.sortBy(),
        query.direction(),
        query.filters(),
        query.resourceIds(),
        query.studyViewFilter(),
        query.groupBy(),
        query.groupCounts(),
        query.locate());
  }

  /**
   * A group as its study and its key, the patient or sample id it groups on. Stable ids repeat
   * across studies, so the key alone does not identify a group.
   */
  private record GroupRef(String studyId, String key) {

    static GroupRef of(ResourceTableQuery query, ResourceTableGroupRow row) {
      return new GroupRef(row.studyId(), query.groupBySample() ? row.sampleId() : row.patientId());
    }

    /** The study-qualified key, composed as the SQL composes it; see ResourceCohort. */
    String tuple() {
      return studyId + "_" + key;
    }
  }

  /** Value counts for the page's groups. */
  private Map<GroupRef, Map<String, Map<String, Long>>> groupCounts(
      Reads reads, List<ResourceTableGroupRow> page) {
    List<ResourceGroupCountColumn> columns = reads.query().groupCountColumns();
    if (columns.isEmpty() || page.isEmpty()) {
      return Map.of();
    }
    Set<String> keys = new LinkedHashSet<>();
    Set<String> tuples = new LinkedHashSet<>();
    for (ResourceTableGroupRow row : page) {
      GroupRef group = GroupRef.of(reads.query(), row);
      keys.add(group.key());
      tuples.add(group.tuple());
    }
    Map<GroupRef, Map<String, Map<String, Long>>> byGroup = new HashMap<>();
    for (ResourceGroupCountsRow row :
        mapper.getResourceTableGroupCounts(
            reads.query(),
            reads.cohort(),
            keys.toArray(new String[0]),
            tuples.toArray(new String[0]))) {
      byGroup.put(new GroupRef(row.studyId(), row.groupKey()), parseCounts(row.counts(), columns));
    }
    return byGroup;
  }

  /** Every requested column, in request order, each with its value counts (possibly none). */
  private static Map<String, Map<String, Long>> parseCounts(
      String json, List<ResourceGroupCountColumn> columns) {
    Map<String, Map<String, Long>> parsed;
    try {
      parsed =
          json == null || json.isBlank()
              ? Map.of()
              : OBJECT_MAPPER.readValue(json, GROUP_COUNTS_TYPE);
    } catch (Exception e) {
      throw new IllegalStateException("Unreadable group counts from ClickHouse: " + json, e);
    }
    Map<String, Map<String, Long>> counts = new LinkedHashMap<>();
    for (ResourceGroupCountColumn column : columns) {
      Map<String, Long> values = parsed.get(column.columnId());
      counts.put(column.columnId(), values == null ? Map.of() : values);
    }
    return counts;
  }

  private static Map<String, Map<String, Long>> emptyCounts(
      List<ResourceGroupCountColumn> columns) {
    Map<String, Map<String, Long>> counts = new LinkedHashMap<>();
    columns.forEach(column -> counts.put(column.columnId(), Map.of()));
    return counts;
  }
}
