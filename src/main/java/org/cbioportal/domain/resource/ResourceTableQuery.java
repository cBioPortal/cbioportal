package org.cbioportal.domain.resource;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.cbioportal.legacy.web.parameter.PatientIdentifier;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;

/**
 * The request body of the resource-table endpoints: one kind of resource, restricted to the studies
 * and to the patient and sample selection the caller names, with the table's search box, column
 * filters, sort and page applied.
 *
 * <p>This is the request only. Nothing here describes what comes back: the rows are {@link
 * ResourceTableRow}, the groups are {@link ResourceTableGroupPage}, and the columns, filter options
 * and counts are {@link ResourceTableMetadataResult}.
 *
 * @param studyIds the studies to read, by study identifier. Every select in {@code
 *     ResourceDataMapper.xml} restricts {@code cancer_study_identifier} to this list, so the rows
 *     read are the ones the controller's {@code @PreAuthorize} check authorized.
 * @param resourceId which kind of resource to show, e.g. {@code PATHOLOGY_SLIDE}. The table shows
 *     one kind at a time, so this is the tab the user is on -- not the id of an individual item.
 * @param patientIdentifiers the patients to restrict to, as (study identifier, patient id) pairs.
 *     Empty means every patient in {@code studyIds}.
 * @param sampleIdentifiers the samples to restrict to, as (study identifier, sample id) pairs.
 *     Empty means every sample in {@code studyIds}. Study view sends the samples its filters
 *     selected; patient view sends one patient and that patient's samples. When both lists are
 *     given, a row matches on whichever level it lives at: a sample-level row by its sample id, a
 *     patient-level row by its patient id.
 * @param search free text from the table's search box. A row matches when the text occurs anywhere
 *     in its patient id, sample id, URL or metadata. It is not a sort direction -- that is {@code
 *     direction} -- and not a per-column filter -- those are {@code filters}.
 * @param pageNumber zero-based index of the page of rows to return; see {@link #offset()}. The
 *     metadata endpoint ignores it, since it describes the whole result rather than a page.
 * @param pageSize how many rows that page holds; see {@link #limit()}. Also ignored by the metadata
 *     endpoint.
 * @param sortBy which column to order by: {@code studyId}, {@code patientId}, {@code sampleId},
 *     {@code url}, {@code displayName}, {@code resourceDisplayName}, or {@code metadata:<key>} for
 *     a key inside the row's metadata. The mapper matches this against that fixed list rather than
 *     interpolating it, so an unrecognised value -- or none at all -- falls back to patient then
 *     sample order instead of reaching the SQL.
 * @param direction {@code asc} or {@code desc}; anything else sorts ascending.
 * @param filters per-column filters, all of which a row must satisfy. Each names {@code patientId},
 *     {@code sampleId}, {@code url}, {@code displayName} or a {@code metadata:<key>}; see {@link
 *     ResourceColumnFilter}.
 * @param resourceIds several resources to read together as one table, e.g. a sample-level and a
 *     patient-level resource holding the same kind of item. Combined with {@code resourceId} when
 *     both are given. Every id must exist in at least one of {@code studyIds}.
 * @param studyViewFilter the cohort as a study-view filter, resolved on the server to its samples
 *     and their patients the same way the study-view charts resolve it. When present it replaces
 *     {@code patientIdentifiers} and {@code sampleIdentifiers}: a sample-level row matches when its
 *     sample is in the cohort, a patient-level row when its patient has a sample in the cohort.
 * @param groupBy {@code patient} or {@code sample}, in any case: answer with one entry per patient
 *     or sample instead of one per row. Only the groups endpoint and the metadata endpoint accept
 *     it; see {@link ResourceGroupBy}, which parses it when the body is read and rejects any other
 *     value as a bad request.
 * @param groupCounts columns to count values of within each group, e.g. {@code
 *     metadata:stain_group} or {@code resourceId}; see {@link ResourceGroupCountColumn}. At most
 *     ten, and only with {@code groupBy}.
 * @param locate a group whose position under the current filters and sort the groups endpoint
 *     should report alongside the page. Only with {@code groupBy}; the metadata endpoint ignores
 *     it.
 */
public record ResourceTableQuery(
    List<String> studyIds,
    String resourceId,
    List<PatientIdentifier> patientIdentifiers,
    List<SampleIdentifier> sampleIdentifiers,
    String search,
    int pageNumber,
    int pageSize,
    String sortBy,
    String direction,
    List<ResourceColumnFilter> filters,
    List<String> resourceIds,
    @Valid StudyViewFilter studyViewFilter,
    ResourceGroupBy groupBy,
    List<String> groupCounts,
    ResourceGroupLocator locate) {

  /** The same query with every column-level filter removed. */
  public ResourceTableQuery withoutColumnFilters() {
    return new ResourceTableQuery(
        studyIds,
        resourceId,
        patientIdentifiers,
        sampleIdentifiers,
        search,
        pageNumber,
        pageSize,
        sortBy,
        direction,
        List.of(),
        resourceIds,
        studyViewFilter,
        groupBy,
        groupCounts,
        locate);
  }

  /**
   * The same query read as rows rather than groups: without {@code groupBy}, {@code groupCounts}
   * and {@code locate}.
   */
  public ResourceTableQuery withoutGrouping() {
    if (groupBy == null && groupCounts == null && locate == null) {
      return this;
    }
    return new ResourceTableQuery(
        studyIds,
        resourceId,
        patientIdentifiers,
        sampleIdentifiers,
        search,
        pageNumber,
        pageSize,
        sortBy,
        direction,
        filters,
        resourceIds,
        studyViewFilter,
        null,
        null,
        null);
  }

  /**
   * Every resource the query reads: {@code resourceId} followed by {@code resourceIds}, without
   * blanks or repeats.
   */
  public List<String> effectiveResourceIds() {
    Set<String> ids = new LinkedHashSet<>();
    if (resourceId != null && !resourceId.isBlank()) {
      ids.add(resourceId);
    }
    if (resourceIds != null) {
      for (String id : resourceIds) {
        if (id != null && !id.isBlank()) {
          ids.add(id);
        }
      }
    }
    return List.copyOf(ids);
  }

  /**
   * Whether the query names its resources through {@code resourceIds}. A query naming only {@code
   * resourceId} keeps the single-resource equality predicate.
   */
  public boolean hasResourceIds() {
    return resourceIds != null && resourceIds.stream().anyMatch(id -> id != null && !id.isBlank());
  }

  public String[] resourceIdsArray() {
    return effectiveResourceIds().toArray(new String[0]);
  }

  /**
   * Whether the search box holds text. Any text counts, blanks included, since the search matches
   * it verbatim.
   */
  public boolean hasSearch() {
    return search != null && !search.isEmpty();
  }

  /** Whether the cohort is a study-view filter rather than explicit identifier lists. */
  public boolean hasStudyViewFilter() {
    return studyViewFilter != null;
  }

  public boolean grouped() {
    return groupBy != null;
  }

  public boolean groupBySample() {
    return groupBy == ResourceGroupBy.SAMPLE;
  }

  /** {@code groupCounts} parsed, in request order without repeats. */
  public List<ResourceGroupCountColumn> groupCountColumns() {
    if (groupCounts == null || groupCounts.isEmpty()) {
      return List.of();
    }
    List<ResourceGroupCountColumn> columns = new ArrayList<>();
    for (String columnId : new LinkedHashSet<>(groupCounts)) {
      columns.add(ResourceGroupCountColumn.parse(columnId));
    }
    return List.copyOf(columns);
  }

  /** The group key {@link #locate} names: its sample id under sample grouping, else patient id. */
  public String locateKey() {
    if (locate == null) {
      return null;
    }
    return groupBySample() ? locate.sampleId() : locate.patientId();
  }

  /**
   * The id lists above as ClickHouse native arrays, bound as a single JDBC array parameter via
   * MyBatis' {@code ArrayTypeHandler} rather than expanded into one placeholder per id by a {@code
   * <foreach>}. Expanding a selection into one placeholder per id makes the SQL and the bound
   * parameter count grow with the number of ids selected, on every statement the request issues.
   * See cBioPortal/cbioportal#11296, which established this pattern for study-view sample
   * filtering.
   *
   * <p>{@code patientIdentifiers} and {@code sampleIdentifiers} are each projected into two forms:
   * the bare stable ids, which the sorting key can prune on, and the same ids qualified by their
   * study, which make the match correct when more than one study is selected. See {@link
   * ResourceCohort}.
   *
   * <p>Accessors are record-style on purpose: for records MyBatis' Reflector registers properties
   * under the raw method name, so a bean-style getter would be exposed as {@code getStudyIdsArray}.
   */
  public String[] studyIdsArray() {
    return studyIds == null ? new String[0] : studyIds.toArray(new String[0]);
  }

  public String[] patientIdsArray() {
    return ResourceCohort.patientIds(patientIdentifiers);
  }

  public String[] sampleIdsArray() {
    return ResourceCohort.sampleIds(sampleIdentifiers);
  }

  public String[] patientTuplesArray() {
    return ResourceCohort.patientTuples(patientIdentifiers);
  }

  public String[] sampleTuplesArray() {
    return ResourceCohort.sampleTuples(sampleIdentifiers);
  }

  /** Whether the study-qualified tuple predicate is needed; see {@link ResourceCohort}. */
  public boolean multiStudy() {
    return ResourceCohort.spansMultipleStudies(studyIds);
  }

  public boolean hasPatients() {
    return patientIdentifiers != null && !patientIdentifiers.isEmpty();
  }

  public boolean hasSamples() {
    return sampleIdentifiers != null && !sampleIdentifiers.isEmpty();
  }

  /**
   * Zero-based page index times page size: the row this page starts at. Computed in {@code long},
   * which the two {@code int} factors cannot overflow, and bound as a {@code long}, so a page far
   * past the end reads nothing rather than wrapping to a negative offset.
   */
  public long offset() {
    return (long) Math.max(pageNumber, 0) * limit();
  }

  /** The page size, floored at zero so a negative request reads nothing rather than erroring. */
  public int limit() {
    return Math.max(pageSize, 0);
  }
}
