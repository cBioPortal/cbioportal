package org.cbioportal.legacy.model;

import java.io.Serializable;

public class NamespaceAttribute implements Serializable {
    
    private String outerKey;
    private String innerKey;

    public NamespaceAttribute() {}

    public NamespaceAttribute(String outerKey, String innerKey) {
        this.outerKey = outerKey;
        this.innerKey = innerKey;
    }
    
    public String getOuterKey() {
        return outerKey;
    }

    public void setOuterKey(String outerKey) {
        this.outerKey = outerKey;
    }

    public String getInnerKey() {
        return innerKey;
    }

    public void setInnerKey(String innerKey) {
        this.innerKey = innerKey;
    }
}
