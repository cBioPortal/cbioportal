
package org.mskcc.cgds.model;

/**
 *
 * @author jgao
 */
public class CnaEvent {
    private String caseId;
    private int cnaProfileId;
    private long eventId;
    private long entrezGeneId;
    private String alteration; // "-2","2"

    public CnaEvent(String caseId, int cnaProfileId, long entrezGeneId, String alteration) {
        this.caseId = caseId;
        this.cnaProfileId = cnaProfileId;
        this.entrezGeneId = entrezGeneId;
        this.alteration = alteration;
    }

    public String getAlteration() {
        return alteration;
    }

    public void setAlteration(String alteration) {
        this.alteration = alteration;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public int getCnaProfileId() {
        return cnaProfileId;
    }

    public void setCnaProfileId(int cnaProfileId) {
        this.cnaProfileId = cnaProfileId;
    }

    public long getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(long entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }
}
