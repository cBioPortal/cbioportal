package org.cbioportal.file.model;

import java.util.Optional;

public interface GenericProfileDatatypeFileMetadata extends GenericDatatypeFileMetadata {
    String stableId();
    Boolean showProfileInAnalysisTab();
    String profileName();
    String profileDescription();
    Optional<String> genePanel();
}
