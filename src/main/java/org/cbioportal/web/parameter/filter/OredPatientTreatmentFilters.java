package org.cbioportal.web.parameter.filter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cbioportal.web.parameter.SampleIdentifier;

public class OredPatientTreatmentFilters implements Serializable {
  private List<PatientTreatmentFilter> filters;

  public boolean filter(SampleIdentifier sampleId, Map<String, Set<String>> treatments) {
    if (filters == null || filters.isEmpty()) {
      return true;
    }

    return filters.stream().anyMatch(f -> f.filter(sampleId, treatments));
  }

  public List<PatientTreatmentFilter> getFilters() {
    return filters;
  }

  public void setFilters(List<PatientTreatmentFilter> filters) {
    this.filters = filters;
  }
}
