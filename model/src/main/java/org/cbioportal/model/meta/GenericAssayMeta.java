package org.cbioportal.model.meta;

import java.io.Serializable;

public class GenericAssayMeta implements Serializable {
    
    private int id; 
    private String stableId;
    private String entityType;
    private String name;
    private String description;
    private String addtionalFields;

    /**
     * Create a Treatment object from fields
     * 
     * @param entityType        Type of the generic assay meta
     * @param stableId          Stable identifier of the generic assay meta used in the cBioPortal instance
     * @param name              Name of the generic assay meta
     * @param description       Description of the generic assay meta
     * @param addtionalFields   Additional fields of the generic assay meta
    */
    public GenericAssayMeta(String entityType, String stableId, String name, String description, String addtionalFields) {
        this.entityType = entityType;
        this.stableId = stableId;
        this.name = name;
        this.description = description;
        this.addtionalFields = addtionalFields;
    }

    public GenericAssayMeta(Integer id, String entityType, String stableId, String name, String description, String addtionalFields) {
        this.id = id;
        this.entityType = entityType;
        this.stableId = stableId;
        this.name = name;
        this.description = description;
        this.addtionalFields = addtionalFields;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
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

    /**
     * @return the entity type
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * @param entityType the entityType to set
     */
    public void setEntityType(String entityType) {
        this.entityType = entityType;
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
