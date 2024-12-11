package org.cbioportal.file.model;

import java.util.Optional;

public interface GenericProfileDatatypeMetadata extends GenericDatatypeMetadata {
    String stableId();
    Boolean showProfileInAnalysisTab();
    String profileName();
    String profileDescription();
    Optional<String> genePanel();
}
