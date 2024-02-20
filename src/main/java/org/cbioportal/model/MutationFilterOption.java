package org.cbioportal.model;

public enum MutationFilterOption {
  MUTATED("Mutated vs. Wild Type"), // Samples that have mutations
  WILD_TYPE("Wild Type"), // Samples that are profiled and not mutated
  NA("NA"), // Samples that are not profiled
  ;

  private final String selectedOption;

  MutationFilterOption(String selectedOption) {
    this.selectedOption = selectedOption;
  }

  public String getSelectedOption() {
    return selectedOption;
  }
}
