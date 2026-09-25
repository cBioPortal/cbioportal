package org.cbioportal.legacy.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SampleClinicalDataCollection {

  private final Map<String, List<ClinicalData>> byUniqueSampleKey;
  private final List<String> orderedSampleKeys;

  private SampleClinicalDataCollection(Builder builder) {
    this.byUniqueSampleKey =
        Collections.unmodifiableMap(new LinkedHashMap<>(builder.byUniqueSampleKey));
    this.orderedSampleKeys =
        Collections.unmodifiableList(new ArrayList<>(builder.orderedSampleKeys));
  }

  public Map<String, List<ClinicalData>> getByUniqueSampleKey() {
    return byUniqueSampleKey;
  }

  public List<String> getOrderedSampleKeys() {
    return orderedSampleKeys;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final Map<String, List<ClinicalData>> byUniqueSampleKey = new LinkedHashMap<>();
    private final List<String> orderedSampleKeys = new ArrayList<>();

    public Builder withByUniqueSampleKey(Map<String, List<ClinicalData>> byUniqueSampleKey) {
      this.byUniqueSampleKey.putAll(byUniqueSampleKey);
      return this;
    }

    public Builder withOrderedSampleKeys(List<String> orderedSampleKeys) {
      this.orderedSampleKeys.clear();
      this.orderedSampleKeys.addAll(orderedSampleKeys);
      return this;
    }

    public SampleClinicalDataCollection build() {
      return new SampleClinicalDataCollection(this);
    }
  }
}
