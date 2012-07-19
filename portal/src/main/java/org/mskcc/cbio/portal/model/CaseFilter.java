package org.mskcc.cbio.portal.model;

import java.util.HashSet;

public class CaseFilter {
    private String paramName;
    private HashSet<String> valueSet;

    public CaseFilter(String paramName, HashSet<String> valueSet) {
        this.paramName = paramName;
        this.valueSet = valueSet;
    }

    public String getParamName() {
        return paramName;
    }

    public HashSet<String> getParamValueSet() {
        return valueSet;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Filter:  " + paramName + " [");
        for (String value:  valueSet) {
            buf.append(value + "#");
        }
        buf.append("]");
        return buf.toString();
    }
}
