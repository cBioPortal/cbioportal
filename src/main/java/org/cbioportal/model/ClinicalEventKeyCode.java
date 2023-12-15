package org.cbioportal.model;

/**
 * Clinical event data objects are key value pairs.
 * Lots of different services consume specific keys. This is an attempt
 * to keep those key constants in one enum.
 */
public enum ClinicalEventKeyCode {
    Agent("AGENT", null), AgentClass ("AGENT_CLASS", null), AgentTarget("AGENT_TARGET", ", *");

    private final String key;
    private final String delimiter;

    ClinicalEventKeyCode(String key, String delimiter) {
        this.key = key;
        this.delimiter = delimiter;
    }

    public String getKey() {
        return key;
    }
    
    public String getPropertyReference() {
        return name().toLowerCase();
    }
    
    public String getDelimiter() {
        return delimiter;
    }
    
    public boolean isDelimited() {
        return delimiter != null;
    }
}
