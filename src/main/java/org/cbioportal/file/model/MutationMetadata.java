package org.cbioportal.file.model;

import java.util.Optional;

public record MutationMetadata(
    String cancerStudyIdentifier,
    String dataFilename,
    String profileName,
    String profileDescription,
    Optional<String> genePanel
) implements GenericProfileDatatypeMetadata {
    public String geneticAlterationType() {
        return "MUTATION_EXTENDED";
    }

    public String datatype() {
        return "MAF";
    }

    public String stableId() {
        return "mutations";
    }
    
    public Boolean showProfileInAnalysisTab() {
        return true;
    }
    
}
