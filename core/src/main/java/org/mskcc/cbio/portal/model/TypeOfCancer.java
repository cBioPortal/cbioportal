/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.model;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    @JsonProperty("id")
    // to preserve json output in DumpPortalInfo.java after migrating from ApiService
    private String typeOfCancerId;
    @JsonIgnore
    // to preserve json output in DumpPortalInfo.java after migrating from ApiService
    private String clinicalTrialKeywords = ""; // Separated by commas
    @JsonProperty("color")
    // to preserve json output in DumpPortalInfo.java after migrating from ApiService
    private String dedicatedColor = "white";
    @JsonIgnore
    // to preserve json output in DumpPortalInfo.java after migrating from ApiService
    private String shortName = "";
    @JsonIgnore
    // to preserve json output in DumpPortalInfo.java after migrating from ApiService
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

    public String getClinicalTrialKeywords() {
        return clinicalTrialKeywords;
    }

    public void setClinicalTrialKeywords(String clinicalTrialKeywords) {
        this.clinicalTrialKeywords = clinicalTrialKeywords;
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
