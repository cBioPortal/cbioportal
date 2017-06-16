
package org.mskcc.cbio.portal.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * This represents a group of cancer studies, with a set of cases and other
 * data.
 *
 * @author Karthik Kalletla
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cohort {
    @JsonProperty
    private String id;

    private boolean isVirtualCohort;

    @JsonProperty("selectedCases")
    private List<CohortStudyCasesMap> cohortStudyCasesMap;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<CohortStudyCasesMap> getCohortStudyCasesMap() {
        return cohortStudyCasesMap;
    }

    public void setCohortStudyCasesMap(List<CohortStudyCasesMap> cohortStudyCasesMap) {
        this.cohortStudyCasesMap = cohortStudyCasesMap;
    }

    public boolean isVirtualCohort() {
        return isVirtualCohort;
    }

    public void setVirtualCohort(boolean isVirtualCohort) {
        this.isVirtualCohort = isVirtualCohort;
    }

}