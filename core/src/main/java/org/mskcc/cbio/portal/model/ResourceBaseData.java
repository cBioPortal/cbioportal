package org.mskcc.cbio.portal.model;

/**
 * Encapsulates Resource Base Data.
 */
public class ResourceBaseData {
    private int cancerStudyId;
    private String stableId;
    private String resourceId;
    private String url;

    /**
     * Constructor
     */
    public ResourceBaseData() {
        this(-1, "", "", "");
    }

    /**
     * Constructor
     *
     * @param cancerStudyId database id of cancer study
     * @param stableId stable id of the patient or sample or study
     * @param resourceId resource id
     * @param url        url of the resource
     */
    
    public ResourceBaseData(int cancerStudyId, String stableId, String resourceId, String url) {
        this.setCancerStudyId(cancerStudyId);
        this.setStableId(stableId);
        this.setResourceId(resourceId);
        this.setUrl(url);
    }

    public ResourceBaseData(ResourceBaseData other) {
        this(other.getCancerStudyId(), other.getStableId(), other.getResourceId(), other.getUrl());
    }

    public int getCancerStudyId() {
        return cancerStudyId;
    }

    public void setCancerStudyId(int cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
