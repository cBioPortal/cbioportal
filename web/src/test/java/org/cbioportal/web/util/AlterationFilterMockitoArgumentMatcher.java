package org.cbioportal.web.util;

import org.cbioportal.model.AlterationFilter;
import org.mockito.ArgumentMatcher;

public class AlterationFilterMockitoArgumentMatcher implements ArgumentMatcher<AlterationFilter> {
    private String checkWhatMutation;
    private String checkWhatCna;

    public AlterationFilterMockitoArgumentMatcher(String checkWhatMutation, String checkWhatCna) {
        this.checkWhatMutation = checkWhatMutation;
        this.checkWhatCna = checkWhatCna;
    }

    @Override
    public boolean matches(AlterationFilter filter) {
        boolean correctMutation;
        boolean correctCna;
        switch (checkWhatMutation) {
            case "ALL":
                correctMutation = filter.getSelectedMutationTypes().hasAll();
                break;
            case "EMPTY":
                correctMutation = filter.getSelectedMutationTypes().hasNone();
                break;
            case "SOME":
                correctMutation = filter.getSelectedMutationTypes().hasValues();
                break;
            default:
                correctMutation = false;
        }
        switch (checkWhatCna) {
            case "ALL":
                correctCna = filter.getSelectedCnaTypes().hasAll();
                break;
            case "EMPTY":
                correctCna = filter.getSelectedCnaTypes().hasNone();
                break;
            case "SOME":
                correctCna = filter.getSelectedCnaTypes().hasValues();
                break;
            default:
                correctCna = false;
        }
        return correctMutation && correctCna;
    }
}
