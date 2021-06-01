package org.cbioportal.model;

import java.io.Serializable;

public class AlterationDriverAnnotation implements Serializable {

    private Integer alterationEventId;
    private Integer geneticProfileId;
    private Integer sampleId;
    private String driverFilter;
    private String driverFilterAnnotation;
    private String driverTiersFilter;
    private String driverTiersFilterAnnotation;

    public Integer getAlterationEventId() {
        return alterationEventId;
    }

    public void setAlterationEventId(Integer alterationEventId) {
        this.alterationEventId = alterationEventId;
    }

    public Integer getGeneticProfileId() {
        return geneticProfileId;
    }

    public void setGeneticProfileId(Integer geneticProfileId) {
        this.geneticProfileId = geneticProfileId;
    }

    public Integer getSampleId() {
        return sampleId;
    }

    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }

    public String getDriverFilter() {
        return driverFilter;
    }

    public void setDriverFilter(String driverFilter) {
        this.driverFilter = driverFilter;
    }

    public String getDriverFilterAnnotation() {
        return driverFilterAnnotation;
    }

    public void setDriverFilterAnnotation(String driverFilterAnnotation) {
        this.driverFilterAnnotation = driverFilterAnnotation;
    }

    public String getDriverTiersFilter() {
        return driverTiersFilter;
    }

    public void setDriverTiersFilter(String driverTiersFilter) {
        this.driverTiersFilter = driverTiersFilter;
    }

    public String getDriverTiersFilterAnnotation() {
        return driverTiersFilterAnnotation;
    }

    public void setDriverTiersFilterAnnotation(String driverTiersFilterAnnotation) {
        this.driverTiersFilterAnnotation = driverTiersFilterAnnotation;
    }
}