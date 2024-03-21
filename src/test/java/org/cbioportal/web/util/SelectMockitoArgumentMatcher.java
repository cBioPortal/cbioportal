package org.cbioportal.web.util;

import org.cbioportal.model.util.Select;
import org.mockito.ArgumentMatcher;

public class SelectMockitoArgumentMatcher implements ArgumentMatcher<Select> {
    private String checkWhat;

    public SelectMockitoArgumentMatcher(String checkWhat) {
        this.checkWhat = checkWhat;
    }

    @Override
    public boolean matches(Select select) {
        switch (checkWhat) {
            case "ALL":
                return select.hasAll();
            case "EMPTY":
                return select.hasNone();
            case "SOME":
                return select.hasValues();
            default:
                return false;
        }
    }
}
