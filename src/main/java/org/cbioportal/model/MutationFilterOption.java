package org.cbioportal.model;

public enum MutationFilterOption {
    MUTATED("Mutated"), // Samples that have mutations
    NOT_MUTATED("Not Mutated"), // Samples that are profiled and not mutated
    NOT_PROFILED("Not Profiled"), // Samples that are not profiled
    ;

    private final String selectedOption;

    MutationFilterOption(String selectedOption) {
        this.selectedOption = selectedOption;
    }

    public String getSelectedOption() {
        return selectedOption;
    }
}
