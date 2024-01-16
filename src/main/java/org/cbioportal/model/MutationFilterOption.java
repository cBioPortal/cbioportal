package org.cbioportal.model;

public enum MutationFilterOption {
    MUTATED("Mutated vs. Wild Type"),
    WILD_TYPE("Wild Type"),
    NA("NA"),
    ;

    private final String mutationFilterOption;

    MutationFilterOption(String mutationFilterOption) {
        this.mutationFilterOption = mutationFilterOption;
    }

    public String getMutationFilterOption() {
        return mutationFilterOption;
    }
}
