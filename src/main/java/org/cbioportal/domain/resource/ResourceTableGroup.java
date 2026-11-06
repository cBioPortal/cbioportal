package org.cbioportal.domain.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * One patient or sample of a grouped resource table.
 *
 * @param studyId the study the group belongs to
 * @param patientId the patient; under sample grouping, the patient the sample belongs to
 * @param sampleId the sample under sample grouping; absent under patient grouping
 * @param rowCount how many rows the group holds once filters and search are applied
 * @param counts for each requested {@code groupCounts} column, how many of the group's rows carry
 *     each value. Rows with no value for the column are not counted.
 */
public record ResourceTableGroup(
    String studyId,
    String patientId,
    @JsonInclude(JsonInclude.Include.NON_NULL) String sampleId,
    long rowCount,
    Map<String, Map<String, Long>> counts) {}
