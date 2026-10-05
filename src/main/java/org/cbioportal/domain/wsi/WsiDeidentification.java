package org.cbioportal.domain.wsi;

import java.util.Set;
import java.util.regex.Pattern;

/** Shared de-identification rules for the browser-facing WSI contract (wsi-serving-v5). */
public final class WsiDeidentification {

  /** Opaque per-slide key: the first 32 hex chars of a salted SHA-256 of the image identifier. */
  public static final Pattern SLIDE_KEY = Pattern.compile("^[0-9a-f]{32}$");

  /**
   * The resource_data resources that hold one row per slide. Their rows carry the private {@code
   * wsi_serving} object and are read only by the WSI hierarchy and access endpoints; the generic
   * resource APIs never return them.
   */
  public static final Set<String> WSI_RESOURCE_IDS = Set.of("WSI_SAMPLE", "WSI_PATIENT");

  private WsiDeidentification() {}

  public static boolean isSlideKey(String value) {
    return value != null && SLIDE_KEY.matcher(value).matches();
  }

  public static boolean isWsiResourceId(String resourceId) {
    return resourceId != null && WSI_RESOURCE_IDS.contains(resourceId);
  }
}
