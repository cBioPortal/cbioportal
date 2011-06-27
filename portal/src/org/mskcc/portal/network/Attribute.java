package org.mskcc.portal.network;

/**
 *
 * @author jj
 */
public class Attribute {
    private String name;
    private Object value;
    
    /**
     * 
     * @param name attribute name
     * @param value attribute value
     */
    Attribute(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    /**
     * 
     * @return attribute name
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @return attribute value
     */
    public Object getValue() {
        return value;
    }
}
