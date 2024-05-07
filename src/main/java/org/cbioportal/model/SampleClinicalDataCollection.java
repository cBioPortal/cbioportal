package org.cbioportal.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SampleClinicalDataCollection {

    private final Map<String, List<ClinicalData>> byUniqueSampleKey;

    private SampleClinicalDataCollection(Builder builder) {
        this.byUniqueSampleKey = Collections.unmodifiableMap(new HashMap<>(builder.byUniqueSampleKey));
    }

    public Map<String, List<ClinicalData>> getByUniqueSampleKey() {
        return byUniqueSampleKey;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, List<ClinicalData>> byUniqueSampleKey = new HashMap<>();

        public Builder withByUniqueSampleKey(Map<String, List<ClinicalData>> byUniqueSampleKey) {
            this.byUniqueSampleKey.putAll(byUniqueSampleKey);
            return this;
        }

        public SampleClinicalDataCollection build() {
            return new SampleClinicalDataCollection(this);
        }
    }
}
