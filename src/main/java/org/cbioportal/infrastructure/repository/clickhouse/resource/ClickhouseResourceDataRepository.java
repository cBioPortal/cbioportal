package org.cbioportal.infrastructure.repository.clickhouse.resource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.cbioportal.domain.resource.ResourceColumnInfo;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseResourceDataRepository implements ResourceDataRepository {
  private static final Logger LOG = LoggerFactory.getLogger(ClickhouseResourceDataRepository.class);

  // Only non-ID builtin columns that benefit from categorical filtering
  private static final Map<String, String> FACET_COLUMNS = Map.of("type", "rdata.type");

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
      ResourceMetadataSchema schema, Map<String, ResourceMetadataKeyStats> statsByKey) {

    boolean isNumeric(String key) {
      ResourceMetadataKeyStats stats = statsByKey.get(key);
      if (stats == null) {
        return false;
      }
      ResourceMetadataField field = schema.fieldsByKey().get(key);
      String declaredType = field != null ? field.type() : null;
      if ("string".equals(declaredType)) {
        return false;
      }
      if ("number".equals(declaredType)) {
        // Sampled counts are fine for classification; only the slider's bounds have to be exact.
        return stats.numericCount() > 0;
      }
      return stats.isAutoDetectedNumeric();
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
    return new MetadataContext(getSchema(scoped), classifyMetadataKeys(scoped));
  }

  @Override
  public List<ResourceTableTab> getResourceTableTabs(ResourceTabsRequest request) {
    return mapper.getResourceTableTabs(request);
  }

  @Override
  public List<ResourceTableRow> getResourceTableRows(ResourceTableQuery query) {
    return mapper.getResourceTableRows(query);
  }

  @Override
  public ResourceTableMetadataView getResourceTableMetadata(ResourceTableQuery query) {
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
    for (Map.Entry<String, ResourceMetadataKeyStats> entry : context.statsByKey().entrySet()) {
      String key = entry.getKey();
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

  /**
   * Column existence comes from the data, never from the contract: a declared key nobody imported
   * would be an empty column, and an undeclared key still has to show up.
   */
  private List<ResourceColumnInfo> metadataColumns(MetadataContext context) {
    Map<String, ResourceMetadataKeyStats> statsByKey = context.statsByKey();
    Map<String, ResourceMetadataField> declared = context.schema().fieldsByKey();

    // Declared fields first, in the curator's declaration order, then whatever else the data
    // turned up, alphabetically — so a partial contract still produces a coherent ordering.
    List<String> ordered = new ArrayList<>();
    for (String key : declared.keySet()) {
      if (statsByKey.containsKey(key)) {
        ordered.add(key);
      }
    }
    statsByKey.keySet().stream()
        .filter(key -> !declared.containsKey(key))
        .sorted(String.CASE_INSENSITIVE_ORDER)
        .forEach(ordered::add);

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
        byKey.put(stat.key(), stat);
      }
    }
    return byKey;
  }

  private ResourceMetadataSchema getSchema(ResourceTableQuery query) {
    List<String> customMetadata = mapper.getResourceDefinitionCustomMetadata(query);
    if (customMetadata == null || customMetadata.isEmpty()) {
      return ResourceMetadataSchema.empty();
    }
    if (customMetadata.size() > 1) {
      // The contract is per (resource_id, cancer_study_id), so a multi-study cohort can hand us
      // several. Picking one is wrong either way; the mapper orders them so at least the choice is
      // stable rather than arbitrary, and the disagreement is worth surfacing.
      LOG.warn(
          "Resource '{}' has {} differing custom_metadata contracts across the selected studies;"
              + " using the first by study identifier.",
          query.resourceId(),
          customMetadata.size());
    }
    return ResourceMetadataSchema.parse(customMetadata.get(0));
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
    ResourceTableCounts counts = mapper.getResourceTableCounts(query);
    return counts == null ? ResourceTableCounts.empty() : counts;
  }
}
