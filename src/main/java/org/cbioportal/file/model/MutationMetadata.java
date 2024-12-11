package org.cbioportal.file.model;

public record MutationMetadata(
    String cancerStudyIdentifier,
    String dataFilename
) implements GenericStudyDataDescriptor {
    public String geneticAlterationType() {
        return "MUTATION_EXTENDED";
    }

    public String datatype() {
        return "MAF";
    }

    public String stableId() {
        return "mutations";
    }
}
