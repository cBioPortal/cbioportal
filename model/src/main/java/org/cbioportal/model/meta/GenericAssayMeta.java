package org.cbioportal.model.meta;

import java.io.Serializable;
import java.util.HashMap;   

public class GenericAssayMeta implements Serializable {
    
    private String stableId;
    private String entityType;
    private HashMap<String, String> genericEntityMetaProperties;

    /**
     * Create a generic assay meta object from fields
     * 
     * @param entityType                  Type of the generic assay meta
     * @param stableId                    Stable identifier of the generic assay
     *                                    meta used in the cBioPortal instance
     * @param genericEntityMetaProperties generic entity meta properties are the
     *                                    additional properties, may differ between
     *                                    different generic assay data
     */

    public GenericAssayMeta(String stableId) {
        this.stableId = stableId;
    }

    public GenericAssayMeta(String entityType, String stableId) {
        this.entityType = entityType;
        this.stableId = stableId;
        this.genericEntityMetaProperties = new HashMap<>();
    }

    public GenericAssayMeta(String entityType, String stableId, HashMap<String, String> genericEntityMetaProperties) {
        this.entityType = entityType;
        this.stableId = stableId;
        this.genericEntityMetaProperties = genericEntityMetaProperties;
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
     * @return the generic entity meta properties
     */
    public HashMap<String, String> getGenericEntityMetaProperties() {
        return genericEntityMetaProperties;
    }
    
    /**
     * @param genericEntityMetaProperties the genericEntityMetaProperties to set
     */
    public void setGenericEntityMetaProperties(HashMap<String, String> genericEntityMetaProperties) {
        this.genericEntityMetaProperties = genericEntityMetaProperties;
    }
}
