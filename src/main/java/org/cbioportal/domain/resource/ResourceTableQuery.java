package org.cbioportal.domain.resource;

import java.util.List;
import org.cbioportal.legacy.web.parameter.PatientIdentifier;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;

/**
 * The request body of both resource-table endpoints: one kind of resource, restricted to the
 * studies and to the patient and sample selection the caller names, with the table's search box,
 * column filters, sort and page applied.
 *
 * <p>This is the request only. Nothing here describes what comes back: the rows are {@link
 * ResourceTableRow}, and the columns, filter options and counts are {@link
 * ResourceTableMetadataResult}.
 *
 * @param studyIds the studies to read, by study identifier. Every select in {@code
 *     ResourceDataMapper.xml} restricts {@code cancer_study_identifier} to this list, which is what
 *     ties the controller's {@code @PreAuthorize} check to the rows the query actually reads.
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
    List<ResourceColumnFilter> filters) {

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

  /** Zero-based page index times page size: the row this page starts at. */
  public int offset() {
    return Math.max(pageNumber, 0) * limit();
  }

  /** The page size, floored at zero so a negative request reads nothing rather than erroring. */
  public int limit() {
    return Math.max(pageSize, 0);
  }
}
