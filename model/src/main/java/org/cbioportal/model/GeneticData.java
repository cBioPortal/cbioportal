package org.cbioportal.model;

import java.io.Serializable;

public class GeneticData implements Serializable {

    private Integer geneticProfileId;
    private String geneticProfileStableId;
    private Integer geneticEntityId;
    private String entityStableId;
    private Integer sampleId;
    private String sampleStableId;
    private String value;
    private GeneticProfile geneticProfile;
    private GeneticEntity geneticEntity;
    private Sample sample;

    public Integer getGeneticProfileId() {
        return geneticProfileId;
    }

    public void setGeneticProfileId(Integer geneticProfileId) {
        this.geneticProfileId = geneticProfileId;
    }

    public String getGeneticProfileStableId() {
        return geneticProfileStableId;
    }

    public void setGeneticProfileStableId(String geneticProfileStableId) {
        this.geneticProfileStableId = geneticProfileStableId;
    }

    public Integer getGeneticEntityId() {
        return geneticEntityId;
    }

    public void setGeneticEntityId(Integer geneticEntityId) {
        this.geneticEntityId = geneticEntityId;
    }
    
    public String getGeneticEntityStableId() {
		return entityStableId;
	}
    
	public void setGeneticEntityStableId(String entityStableId) {
		this.entityStableId = entityStableId;
	}
    
    public Integer getSampleId() {
        return sampleId;
    }

    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }

    public String getSampleStableId() {
        return sampleStableId;
    }

    public void setSampleStableId(String sampleStableId) {
        this.sampleStableId = sampleStableId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public GeneticProfile getGeneticProfile() {
        return geneticProfile;
    }

    public void setGeneticProfile(GeneticProfile geneticProfile) {
        this.geneticProfile = geneticProfile;
    }

    public GeneticEntity getGeneticEntity() {
        return geneticEntity;
    }

    public void setGeneticEntity(GeneticEntity geneticEntity) {
        this.geneticEntity = geneticEntity;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    /**
     * Compare based on the fields that should always be filled
     */
	@Override
	public boolean equals(Object o){
	    if (o == null) return false;
	    if (o == this) return true;
	    if (!(o instanceof GeneticData))return false;
	    GeneticData other = (GeneticData)o;
	    
		boolean result = true;
		//check mandatory fields for equality:
		result = this.getGeneticEntity().getEntityId().equals(other.getGeneticEntity().getEntityId());
		result = this.getGeneticEntity().getEntityStableId().equals(other.getGeneticEntity().getEntityStableId());
		result = this.getGeneticEntityId().equals(other.getGeneticEntityId());
		result = this.getGeneticEntityStableId().equals(other.getGeneticEntityStableId());
		
		result = this.getGeneticProfileId().equals(other.getGeneticProfileId()) && result;
		result = this.getGeneticProfileStableId().equals(other.getGeneticProfileStableId()) && result;
		
		result = this.getSampleId().equals(other.getSampleId()) && result;
		result = this.getSampleStableId().equals(other.getSampleStableId()) && result;
		
		result = this.getValue().equals(other.getValue()) && result;
		
		return result;
	}
	
}
