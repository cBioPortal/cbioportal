package org.cbioportal.model;

import java.io.Serializable;  

public class GeneticEntity implements Serializable {

    private int id;
    private String stableId;
    private String entityType;

    /**
     * Create a GeneticEntity object from fields
     * @param id                          Internal Id
     * @param entityType                  Type of the genetic entity
     * @param stableId                    Stable identifier
     */
    public GeneticEntity(Integer id, String entityType, String stableId) {
        this.id = id;
        this.entityType = entityType;
        this.stableId = stableId;
    }

    /**
     * Create a GeneticEntity object from fields
     * @param entityType                  Type of the genetic entity
     * @param stableId                    Stable identifier
     */
    public GeneticEntity(String entityType, String stableId) {
        this.entityType = entityType;
        this.stableId = stableId;
    }

    public GeneticEntity(String entityType) {
        this.entityType = entityType;
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
}
