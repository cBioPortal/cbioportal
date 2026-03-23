package org.cbioportal.legacy.web.validation;

public final class VirtualStudyValidationMessages {

  public static final String SCHEMA_HINT =
      "See https://docs.cbioportal.org/virtual-study-data-schema/#virtual-study-json-schema";

  public static final String NAME_REQUIRED =
      "Virtual study name cannot be null or blank. Check that you are passing virtual study data only (data field content) and not the whole session object (with _id). "
          + SCHEMA_HINT;

  public static final String STUDIES_REQUIRED =
      "Virtual study must contain at least one study with samples. " + SCHEMA_HINT;

  public static final String STUDY_ID_REQUIRED =
      "Each study in virtual study must have a non-null and non-blank id. " + SCHEMA_HINT;

  public static final String DYNAMIC_FILTER_REQUIRED =
      "Virtual study with dynamic=true must have a defined study view filter. " + SCHEMA_HINT;

  public static final String STATIC_SAMPLES_REQUIRED =
      "Static virtual study (dynamic=false) must contain at least one sample in each study. "
          + SCHEMA_HINT;

  public static final String INVALID_FILTERS =
      "The provided virtual study data is not valid. Check that the filters are correct and that the studies and samples you are referring to in virtual study data exist in the system. "
          + SCHEMA_HINT;

  public static final String NO_FILTER_RESULTS =
      "The provided virtual study data is not valid: no samples were found for the applied filters. Check that the filters are correct and that the studies and samples you are referring to in virtual study data exist in the system. "
          + SCHEMA_HINT;

  private VirtualStudyValidationMessages() {}
}
