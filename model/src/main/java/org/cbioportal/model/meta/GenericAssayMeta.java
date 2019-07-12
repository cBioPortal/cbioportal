package org.cbioportal.model.meta;

import java.io.Serializable;

public class GenericAssayMeta implements Serializable {
    
    private String stableId;
    private String name;
    private String description;
    private String addtionalFields;

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

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the addtionalFields
     */
    public String getAddtionalFields() {
        return addtionalFields;
    }

    /**
     * @param addtionalFields the addtionalFields to set
     */
    public void setAddtionalFields(String addtionalFields) {
        this.addtionalFields = addtionalFields;
    }
}
