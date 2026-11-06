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
 * Statements over {@code resource}, and for grouped queries over {@code resource_compact_derived}.
 *
 * <p>Every statement filtered by the request's cohort takes a {@code studyViewFilterContext}: when
 * it is non-null the cohort is that study-view filter, resolved by the shared study-view filter
 * SQL, and the request's identifier lists are ignored; when it is null the cohort is the request's
 * identifier lists. Statements that can read the compact table take a {@code compact} flag that
 * switches the table they read.
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
   *
   * @param facetTupleType the named tuple type, one String element per key in {@code metadataKeys},
   *     that each document is parsed into; see {@link #facetTupleType(String[])}
   */
  List<ResourceMetadataFacetValue> getResourceTableGroupedMetadataFacets(
      @Param("query") ResourceTableQuery query,
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext,
      @Param("compact") boolean compact,
      @Param("metadataKeys") String[] metadataKeys,
      @Param("facetTupleType") String facetTupleType,
      @Param("limitPerKey") int limitPerKey,
      @Param("maxMemoryBytes") long maxMemoryBytes,
      @Param("externalGroupByBytes") long externalGroupByBytes);

  /**
   * The ClickHouse type {@code Tuple(`k1` String, `k2` String, ...)} that parses just these keys
   * out of a metadata document. Keys come from curator contracts, so each is backquoted with its
   * backslashes and backquotes escaped; the type is then bound as a string literal.
   */
  static String facetTupleType(String[] keys) {
    StringBuilder type = new StringBuilder("Tuple(");
    for (int i = 0; i < keys.length; i++) {
      if (i > 0) {
        type.append(", ");
      }
      type.append('`').append(keys[i].replace("\\", "\\\\").replace("`", "\\`")).append("` String");
    }
    return type.append(')').toString();
  }

  /**
   * Per-key stats (non-blank count, numeric-parseable count, min/max) used to decide whether a
   * metadata key should be treated as numeric or categorical. Computed in one pass over the current
   * tab's rows (before any column-level filters), see {@link
   * org.cbioportal.infrastructure.repository.clickhouse.resource.ClickhouseResourceDataRepository}.
   */
  List<ResourceMetadataKeyStats> getResourceTableMetadataKeyStats(
      @Param("query") ResourceTableQuery query,
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext,
      @Param("compact") boolean compact,
      @Param("sampleRows") int sampleRows,
      @Param("maxMemoryBytes") long maxMemoryBytes);

  /** Exact min/max for keys already classified numeric; see the mapper XML for why. */
  List<ResourceMetadataRange> getResourceTableMetadataRanges(
      @Param("query") ResourceTableQuery query,
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext,
      @Param("compact") boolean compact,
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
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext,
      @Param("compact") boolean compact);

  /**
   * One (resource, study) row per requested resource that a requested study defines or holds rows
   * of, with its contract. Reads the definitions and the sorting-key columns of {@code resource},
   * not the rows' metadata.
   */
  List<ResourceScopeRow> getResourceScope(@Param("query") ResourceTableQuery query);

  /**
   * Whether the compact table holds a current copy of every requested resource in each requested
   * study: the same rows, by count and a fingerprint of their internal ids, copied under the
   * study's present contract, as {@code resource} holds. Reads the sorting keys and {@code
   * internal_id} only.
   */
  Boolean isCompactTableCurrent(@Param("query") ResourceTableQuery query);

  /**
   * The requested page of groups, each carrying the totals over every group and, with a {@code
   * locate} group, its zero-based position (null if the filters remove it).
   */
  List<ResourceTableGroupRow> getResourceTableGroupPage(
      @Param("query") ResourceTableQuery query,
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext,
      @Param("compact") boolean compact);

  /**
   * Value counts for the page's groups only, named by their keys. Counting every group and then
   * discarding all but a page would parse every row's metadata on every page.
   */
  List<ResourceGroupCountsRow> getResourceTableGroupCounts(
      @Param("query") ResourceTableQuery query,
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext,
      @Param("compact") boolean compact,
      @Param("groupKeys") String[] groupKeys,
      @Param("groupKeyTuples") String[] groupKeyTuples);
}
