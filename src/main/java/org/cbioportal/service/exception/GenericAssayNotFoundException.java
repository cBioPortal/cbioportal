package org.cbioportal.service.exception;

public class GenericAssayNotFoundException extends Exception {

    private String stableId;

    public GenericAssayNotFoundException(String stableId) {
        super();
        this.setStableId(stableId);
    }

    /**
     * @return the stableId
     */
    public String getStableId() {
        return stableId;
    }

    /**
     * @param stableId the stableId to set
     */
    public void setStableId(String stableId) {
        this.stableId = stableId;
    }
}
