package org.cbioportal.file.model;

import java.util.Optional;

public record GenericProfileDatatypeMetadata(
    String stableId,
    String geneticAlterationType,
    String datatype,
    String cancerStudyIdentifier,
    String dataFilename,
    String profileName,
    String profileDescription,
    Optional<String> genePanel,
    Boolean showProfileInAnalysisTab
) implements GenericDatatypeMetadata {
    
}
