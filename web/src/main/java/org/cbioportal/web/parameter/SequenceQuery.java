package org.cbioportal.web.parameter;

import java.util.List;

public class SequenceQuery {
    private List<String> events, studyIds;

    public List<String> getEventValues() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }

    public List<String> getStudyIds() {
        return studyIds;
    }

    public void setStudyIds(List<String> studyIds) {
        this.studyIds = studyIds;
    }
}
