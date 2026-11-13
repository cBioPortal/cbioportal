package org.cbioportal.infrastructure.repository.clickhouse.resource;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.domain.resource.ResourceContractRow;
import org.cbioportal.domain.resource.ResourceMetadataFacetValue;
import org.cbioportal.domain.resource.ResourceMetadataKeyStats;
import org.cbioportal.domain.resource.ResourceMetadataRange;
import org.cbioportal.domain.resource.ResourceScopeRow;
import org.cbioportal.domain.resource.ResourceTableCounts;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.ResourceTableTab;
import org.cbioportal.domain.resource.ResourceTabsRequest;
import org.cbioportal.domain.studyview.StudyViewFilterContext;

/**
 * Statements over {@code resource}.
 *
 * <p>Every statement filtered by the request's cohort takes a {@code studyViewFilterContext}: when
 * it is non-null the cohort is that study-view filter, resolved by the shared study-view filter
 * SQL, and the request's identifier lists are ignored; when it is null the cohort is the request's
 * identifier lists.
 */
public interface ClickhouseResourceDataMapper {
  List<ResourceTableTab> getResourceTableTabs(@Param("request") ResourceTabsRequest request);

  List<ResourceTableRow> getResourceTableRows(
      @Param("query") ResourceTableQuery query,
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);

  /**
   * Facet values for every requested key in one pass over resource, capped per key. Returns a flat
   * list; the caller groups it.
   */
  List<ResourceMetadataFacetValue> getResourceTableMetadataFacets(
      @Param("query") ResourceTableQuery query,
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext,
      @Param("metadataKeys") String[] metadataKeys,
      @Param("limitPerKey") int limitPerKey);

  /**
   * Facet values for a grouped query: each value counts the groups (patients or samples) with at
   * least one row carrying it, rather than the rows.
   */
  List<ResourceMetadataFacetValue> getResourceTableGroupedMetadataFacets(
      @Param("query") ResourceTableQuery query,
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext,
      @Param("metadataKeys") String[] metadataKeys,
      @Param("limitPerKey") int limitPerKey,
      @Param("maxMemoryBytes") long maxMemoryBytes,
      @Param("externalGroupByBytes") long externalGroupByBytes);

  /**
   * Per-key stats (non-blank count, numeric-parseable count) used to decide whether a metadata key
   * should be treated as numeric or categorical. Computed in one pass over a sample of the current
   * tab's rows (before any column-level filters), see {@link
   * org.cbioportal.infrastructure.repository.clickhouse.resource.ClickhouseResourceDataRepository}.
   * With {@code metadataKeys} null every key in the sampled documents is discovered; otherwise only
   * the named keys are counted.
   */
  List<ResourceMetadataKeyStats> getResourceTableMetadataKeyStats(
      @Param("query") ResourceTableQuery query,
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext,
      @Param("metadataKeys") String[] metadataKeys,
      @Param("sampleRows") int sampleRows,
      @Param("maxMemoryBytes") long maxMemoryBytes);

  /** Exact min/max for keys already classified numeric; see the mapper XML for why. */
  List<ResourceMetadataRange> getResourceTableMetadataRanges(
      @Param("query") ResourceTableQuery query,
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext,
      @Param("metadataKeys") String[] metadataKeys);

  /**
   * The current resource tab's {@code resource_definition.custom_metadata} JSON schema, if any
   * study/row in scope has one set. Returns null when no override schema is present.
   */
  List<ResourceContractRow> getResourceDefinitionCustomMetadata(
      @Param("query") ResourceTableQuery query,
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);

  ResourceTableCounts getResourceTableCounts(
      @Param("query") ResourceTableQuery query,
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);

  /**
   * One (resource, study) row per requested resource that a requested study defines or holds rows
   * of, with its contract. Reads the definitions and the sorting-key columns of {@code resource},
   * not the rows' metadata.
   */
  List<ResourceScopeRow> getResourceScope(@Param("query") ResourceTableQuery query);

  /**
   * The requested page of groups, each carrying the totals over every group and, with a {@code
   * locate} group, its zero-based position (null if the filters remove it).
   */
  List<ResourceTableGroupRow> getResourceTableGroupPage(
      @Param("query") ResourceTableQuery query,
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);

  /**
   * Value counts for the page's groups only, named by their keys. Counting every group and then
   * discarding all but a page would parse every row's metadata on every page.
   */
  List<ResourceGroupCountsRow> getResourceTableGroupCounts(
      @Param("query") ResourceTableQuery query,
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext,
      @Param("groupKeys") String[] groupKeys,
      @Param("groupKeyTuples") String[] groupKeyTuples);
}
