package org.cbioportal.web.parameter.filter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cbioportal.web.parameter.SampleIdentifier;

public class AndedPatientTreatmentFilters implements Serializable {
  private List<OredPatientTreatmentFilters> filters;

  public boolean filter(SampleIdentifier sampleId, Map<String, Set<String>> treatments) {
    if (filters == null || filters.isEmpty()) {
      return true;
    }

    return filters.stream().allMatch(f -> f.filter(sampleId, treatments));
  }

  public List<OredPatientTreatmentFilters> getFilters() {
    return filters;
  }

  public void setFilters(List<OredPatientTreatmentFilters> filters) {
    this.filters = filters;
  }
}
