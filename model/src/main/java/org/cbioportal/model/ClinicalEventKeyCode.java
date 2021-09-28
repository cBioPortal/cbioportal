package org.cbioportal.model;

/**
 * Clinical event data objects are key value pairs.
 * Lots of different services consume specific keys. This is an attempt
 * to keep those key constants in one enum.
 */
public enum ClinicalEventKeyCode {
    Agent("AGENT"), AgentClass ("AGENT_CLASS");

    private final String key;
    
    ClinicalEventKeyCode(String key) {
        this.key = key;    
    }

    public String getKey() {
        return key;
    }
    
    public String getPropertyReference() {
        return name().toLowerCase();
    }
}
