package org.cbioportal.infrastructure.repository.clickhouse.resource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.cbioportal.domain.resource.ResourceColumnInfo;
import org.cbioportal.domain.resource.ResourceFacetOption;
import org.cbioportal.domain.resource.ResourceMetadataField;
import org.cbioportal.domain.resource.ResourceMetadataKeyStats;
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
  private static final Map<String, String> FACET_COLUMNS = Map.of("type", "rdata.TYPE");

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
        return stats.hasUsableNumericRange();
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
          mapper.getResourceTableFacetValues(scoped, entry.getValue());
      if (values != null && !values.isEmpty()) {
        facets.put(entry.getKey(), values);
      }
    }

    Map<String, ResourceNumericRange> facetRanges = new LinkedHashMap<>();
    for (Map.Entry<String, ResourceMetadataKeyStats> entry : context.statsByKey().entrySet()) {
      String key = entry.getKey();
      String columnId = ResourceColumnInfo.METADATA_COLUMN_PREFIX + key;
      if (!context.isFilterable(key)) {
        continue;
      }
      if (context.isNumeric(key)) {
        // Numeric columns get a min/max range instead of an enumerated value list, which would be
        // huge and unhelpful for a continuous measurement.
        ResourceMetadataKeyStats stats = entry.getValue();
        facetRanges.put(columnId, new ResourceNumericRange(stats.minValue(), stats.maxValue()));
      } else {
        List<ResourceFacetOption> values = mapper.getResourceTableMetadataFacetValues(scoped, key);
        if (values != null && !values.isEmpty()) {
          facets.put(columnId, values);
        }
      }
    }

    return new ResourceTableMetadataView(metadataColumns(context), facets, facetRanges);
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
              // Metadata columns stay opt-in; a resource can carry many keys and showing them all
              // by default would bury the builtin columns.
              false,
              field != null ? field.description() : null));
    }
    return columns;
  }

  /**
   * Returns per-key stats for every metadata key discovered in the current tab, keyed by the raw
   * metadata key (not prefixed with "metadata:").
   */
  private Map<String, ResourceMetadataKeyStats> classifyMetadataKeys(ResourceTableQuery query) {
    List<ResourceMetadataKeyStats> stats = mapper.getResourceTableMetadataKeyStats(query);
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
        query.patientIds(),
        query.sampleIds(),
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
