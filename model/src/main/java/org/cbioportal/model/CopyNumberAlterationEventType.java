package org.cbioportal.model;

public enum CopyNumberAlterationEventType {
    
    HOMDEL(-2),
    AMP(2);

    private Integer alterationType;

    CopyNumberAlterationEventType(Integer alterationType) {
        this.alterationType = alterationType;
    }

    public Integer getAlterationType() {
        return alterationType;
    }
}
