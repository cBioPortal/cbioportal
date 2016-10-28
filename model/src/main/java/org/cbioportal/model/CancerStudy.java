package org.cbioportal.model;

import org.cbioportal.model.summary.CancerStudySummary;

import java.io.Serializable;
import java.util.Date;

public class CancerStudy extends CancerStudySummary {

    private TypeOfCancer typeOfCancer;
    private Integer sampleCount;

    public TypeOfCancer getTypeOfCancer() {
        return typeOfCancer;
    }

    public void setTypeOfCancer(TypeOfCancer typeOfCancer) {
        this.typeOfCancer = typeOfCancer;
    }

    public Integer getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Integer sampleCount) {
        this.sampleCount = sampleCount;
    }
}