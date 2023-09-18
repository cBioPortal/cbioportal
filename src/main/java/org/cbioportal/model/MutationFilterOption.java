package org.cbioportal.model;

public enum MutationFilterOption {
    MUTATED("Mutated"),
    NOT_MUTATED("Not mutated"),
    NOT_PROFILED("Not profiled"),
    ;

    private final String mutationFilterOption;

    MutationFilterOption(String mutationFilterOption) {
        this.mutationFilterOption = mutationFilterOption;
    }

    public String getMutationType() {
        return mutationFilterOption;
    }
}
