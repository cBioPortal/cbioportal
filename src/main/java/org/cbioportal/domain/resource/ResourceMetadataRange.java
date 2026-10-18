package org.cbioportal.domain.resource;

/** Exact min/max of one numeric metadata key over the whole filtered set. */
public record ResourceMetadataRange(String metaKey, Double minValue, Double maxValue) {

  /** True when both bounds are known, so the column can offer a range filter. */
  public boolean isUsable() {
    return minValue != null && maxValue != null;
  }
}
