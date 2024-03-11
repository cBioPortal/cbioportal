package org.cbioportal.web.parameter.filter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cbioportal.web.parameter.SampleIdentifier;

public class AndedSampleTreatmentFilters implements Serializable {
  private List<OredSampleTreatmentFilters> filters;

  public boolean filter(SampleIdentifier sampleId, Map<String, Set<String>> treatments) {
    if (filters == null || filters.isEmpty()) {
      return true;
    }

    return filters.stream().allMatch(f -> f.filter(sampleId, treatments));
  }

  public List<OredSampleTreatmentFilters> getFilters() {
    return filters;
  }

  public void setFilters(List<OredSampleTreatmentFilters> filters) {
    this.filters = filters;
  }
}
