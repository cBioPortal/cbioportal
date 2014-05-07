

package org.mskcc.cbio.portal.model;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author gaoj
 */
public class ClinicalEvent {
    private Long clinicalEventId;
    private Integer cancerStudyId;
    private String patientId;
    private String eventType;
    private Long startDate;
    private Long stopDate;
    private Map<String, String> eventData = new HashMap<String, String>(0);

    public Long getClinicalEventId() {
        return clinicalEventId;
    }

    public void setClinicalEventId(Long clinicalEventId) {
        this.clinicalEventId = clinicalEventId;
    }

    public Integer getCancerStudyId() {
        return cancerStudyId;
    }

    public void setCancerStudyId(Integer cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getStopDate() {
        return stopDate;
    }

    public void setStopDate(Long stopDate) {
        this.stopDate = stopDate;
    }

    public Map<String, String> getEventData() {
        return eventData;
    }

    public void setEventData(Map<String, String> eventData) {
        this.eventData = eventData;
    }
    
    public void addEventDatum(String key, String value) {
        eventData.put(key, value);
    }
}
