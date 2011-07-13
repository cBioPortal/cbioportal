package org.mskcc.portal.model;

/**
 * Encapsulates Cancer Type Information.
 */
// TODO: Later: ACCESS CONTROL: change to CancerStudy, etc.
public class CancerType {
    private String cancerTypeId;
    private String cancerName;
    private String description;

    /**
     * Constructor.
     *
     * @param cancerTypeId Cancer Type ID.
     * @param cancerName   Cancer Type Name.
     */
    public CancerType(String cancerTypeId, String cancerName) {
        this.cancerTypeId = cancerTypeId;
        this.cancerName = cancerName;
    }

    /**
     * Gets the Cancer Type ID.
     *
     * @return Cancer Type ID.
     */
    public String getCancerTypeId() {
        return cancerTypeId;
    }

    /**
     * Sets the Cancer Type ID.
     *
     * @param cancerTypeId Cancer Type ID.
     */
    public void setCancerTypeId(String cancerTypeId) {
        this.cancerTypeId = cancerTypeId;
    }

    /**
     * Gets the Cancer Type Name.
     *
     * @return Cancer Type Name.
     */
    public String getCancerName() {
        return cancerName;
    }

    /**
     * Sets the Cancer Type Name.
     *
     * @param cancerName Cancer Type Name.
     */
    public void setCancerName(String cancerName) {
        this.cancerName = cancerName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
