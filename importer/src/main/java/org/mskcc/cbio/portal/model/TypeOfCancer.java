/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * A TypeOfCancer is a clinical cancer type, such as Glioblastoma, Ovarian, etc.
 * Eventually, we'll have ontology problems with this, but initially the dbms
 * will be loaded from a file with a static table of types.
 *
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 * @author Arman Aksoy
 */
public class TypeOfCancer implements Serializable {

    private String name;
    @JsonProperty("cancerTypeId")
    private String typeOfCancerId;
    // to preserve json output in DumpPortalInfo.java after migrating from ApiService
    @JsonProperty("dedicatedColor")
    private String dedicatedColor = "white";
    @JsonIgnore
    private String shortName = "";
    @JsonIgnore
    private String parentTypeOfCancerId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeOfCancerId() {
        return typeOfCancerId;
    }

    public void setTypeOfCancerId(String typeOfCancerId) {
        this.typeOfCancerId = typeOfCancerId;
    }

    public String getDedicatedColor() {
        return dedicatedColor;
    }

    public void setDedicatedColor(String dedicatedColor) {
        this.dedicatedColor = dedicatedColor;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getParentTypeOfCancerId() {
        return parentTypeOfCancerId;
    }

    public void setParentTypeOfCancerId(String typeOfCancerId) {
        this.parentTypeOfCancerId = typeOfCancerId;
    }
}
