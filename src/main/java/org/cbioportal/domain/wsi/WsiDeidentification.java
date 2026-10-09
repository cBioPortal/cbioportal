package org.cbioportal.domain.wsi;

import java.util.Base64;
import java.util.Set;
import java.util.regex.Pattern;

/** Shared de-identification rules for the browser-facing WSI contract (wsi-serving-v6). */
public final class WsiDeidentification {

  /** Opaque per-slide key: the first 32 hex chars of a salted SHA-256 of the image identifier. */
  public static final Pattern SLIDE_KEY = Pattern.compile("^[0-9a-f]{32}$");

  /**
   * The resource_data resources that hold one row per slide. Their rows carry the private {@code
   * wsi_serving} object and are read only by the WSI hierarchy and access endpoints; the generic
   * resource APIs never return them.
   */
  public static final Set<String> WSI_RESOURCE_IDS = Set.of("WSI_SAMPLE", "WSI_PATIENT");

  /** Sealed slide source: unpadded base64url of nonce(12) || ciphertext || tag(16). */
  public static final Pattern SEALED_SOURCE = Pattern.compile("^[A-Za-z0-9_-]+$");

  /** Nonce, GCM tag and at least one byte of ciphertext. */
  public static final int MIN_SEALED_SOURCE_BYTES = 12 + 16 + 1;

  public static final int MAX_SEALED_SOURCE_LENGTH = 4096;

  private static final Pattern ABSOLUTE_DATE =
      Pattern.compile(
          "(?<!\\d)(?:19|20)\\d{2}[-_/](?:0?[1-9]|1[0-2])[-_/](?:0?[1-9]|[12]\\d|3[01])(?!\\d)");
  private static final Pattern MONTH_FIRST_DATE =
      Pattern.compile(
          "(?<!\\d)(?:0?[1-9]|1[0-2])[-_/](?:0?[1-9]|[12]\\d|3[01])[-_/](?:19|20)\\d{2}(?!\\d)");
  private static final Pattern DAY_FIRST_DATE =
      Pattern.compile(
          "(?<!\\d)(?:0?[1-9]|[12]\\d|3[01])[-_/](?:0?[1-9]|1[0-2])[-_/](?:19|20)\\d{2}(?!\\d)");
  private static final Pattern NAMED_MONTH_DATE =
      Pattern.compile(
          "(?i)(?<![a-z0-9])(?:(?:jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|"
              + "may|jun(?:e)?|jul(?:y)?|aug(?:ust)?|sep(?:t(?:ember)?)?|oct(?:ober)?|"
              + "nov(?:ember)?|dec(?:ember)?)\\s+(?:0?[1-9]|[12]\\d|3[01])(?:st|nd|rd|th)?"
              + "(?:,)?\\s+(?:19|20)\\d{2}|(?:0?[1-9]|[12]\\d|3[01])[-/\\s]+"
              + "(?:jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|jun(?:e)?|"
              + "jul(?:y)?|aug(?:ust)?|sep(?:t(?:ember)?)?|oct(?:ober)?|nov(?:ember)?|"
              + "dec(?:ember)?)[-/\\s]+(?:19|20)\\d{2})(?![a-z0-9])");
  private static final Pattern COMPACT_DATE = Pattern.compile("(?<!\\d)(?:19|20)\\d{6}(?!\\d)");
  private static final Pattern LABELLED_MRN =
      Pattern.compile(
          "(?i)\\b(?:mrn|medical[ _-]?record(?:[ _-]?number)?)\\b\\s*[:=#-]?\\s*\\d{4,}");

  private WsiDeidentification() {}

  public static boolean isSlideKey(String value) {
    return value != null && SLIDE_KEY.matcher(value).matches();
  }

  /**
   * Whether {@code value} has the shape of a sealed slide source. cBioPortal cannot decrypt it; the
   * tile server authenticates it against the slide key.
   */
  public static boolean isSealedSource(String value) {
    if (value == null
        || value.length() > MAX_SEALED_SOURCE_LENGTH
        || !SEALED_SOURCE.matcher(value).matches()) {
      return false;
    }
    try {
      return Base64.getUrlDecoder().decode(value).length >= MIN_SEALED_SOURCE_BYTES;
    } catch (IllegalArgumentException exception) {
      return false;
    }
  }

  /**
   * Whether free text served to the browser contains a labelled medical record number or an
   * absolute date (ISO, month-first, day-first, named-month or compact YYYYMMDD). WSI metadata that
   * does fails closed.
   */
  public static boolean containsIdentifyingText(String value) {
    return value != null
        && (LABELLED_MRN.matcher(value).find()
            || ABSOLUTE_DATE.matcher(value).find()
            || MONTH_FIRST_DATE.matcher(value).find()
            || DAY_FIRST_DATE.matcher(value).find()
            || NAMED_MONTH_DATE.matcher(value).find()
            || COMPACT_DATE.matcher(value).find());
  }

  public static boolean isWsiResourceId(String resourceId) {
    return resourceId != null && WSI_RESOURCE_IDS.contains(resourceId);
  }
}
