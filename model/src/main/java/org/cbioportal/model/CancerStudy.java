package org.cbioportal.model;

import org.cbioportal.model.summary.CancerStudySummary;

import java.io.Serializable;
import java.util.Date;

public class CancerStudy extends CancerStudySummary {

    private TypeOfCancer typeOfCancer;

    public TypeOfCancer getTypeOfCancer() {
        return typeOfCancer;
    }

    public void setTypeOfCancer(TypeOfCancer typeOfCancer) {
        this.typeOfCancer = typeOfCancer;
    }
}