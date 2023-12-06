package org.cbioportal.model;

public enum MutationFilterOption {
    MUTATED("Mutated"),
    NOT_MUTATED("Not Mutated"),
    NOT_PROFILED("Not Profiled"),
    ;

    private final String mutationFilterOption;

    MutationFilterOption(String mutationFilterOption) {
        this.mutationFilterOption = mutationFilterOption;
    }

    public String getMutationType() {
        return mutationFilterOption;
    }
}
