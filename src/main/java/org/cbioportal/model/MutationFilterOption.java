package org.cbioportal.model;

public enum MutationFilterOption {
    MUTATED("Mutated vs. Wild Type"),
    WILD_TYPE("Wild Type"),
    NA("NA"),
    ;

    private final String selectedOption;

    MutationFilterOption(String selectedOption) {
        this.selectedOption = selectedOption;
    }

    public String getSelectedOption() {
        return selectedOption;
    }
}
