package org.cbioportal.service.exception;

public class CancerTypeNotFoundException extends Exception {

    private String cancerTypeId;

    public CancerTypeNotFoundException(String cancerTypeId) {
        super();
        this.cancerTypeId = cancerTypeId;
    }

    public String getCancerTypeId() {
        return cancerTypeId;
    }

    public void setCancerTypeId(String cancerTypeId) {
        this.cancerTypeId = cancerTypeId;
    }
}
