package org.cbioportal.web.parameter;

import java.util.List;

public class SequenceQuery {
    private String firstEventValue;
    private List<String> subsequentEventValues, studyIds;

    public String getFirstEventValue() {
        return firstEventValue;
    }

    public void setFirstEventValue(String firstEventValue) {
        this.firstEventValue = firstEventValue;
    }

    public List<String> getSubsequentEventValues() {
        return subsequentEventValues;
    }

    public void setSubsequentEventValues(List<String> subsequentEventValues) {
        this.subsequentEventValues = subsequentEventValues;
    }

    public List<String> getStudyIds() {
        return studyIds;
    }

    public void setStudyIds(List<String> studyIds) {
        this.studyIds = studyIds;
    }
}
