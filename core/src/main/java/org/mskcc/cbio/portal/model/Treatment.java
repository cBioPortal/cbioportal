/*
 * Copyright (c) 2019 The Hyve B.V.
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

/**
 * @author Pim van Nierop, pim@thehyve.nl
 */

public class Treatment implements Serializable {
    
    private int id;
    private int geneticEntityId;
    private String stableId;
    private String name;
    private String description;
    private String refLink;

    /**
     * Create a Treatment object from fields
     * 
     * @param treatmentId       Treatment table ID key of the treament
     * @param geneticEntityId   Genetic_entity table ID key associated to the treament record
     * @param stableId          Stable identifier of the treatment used in the cBioPortal instance
     * @param name              Name of the treatment
     * @param description       Description of the treatment
     * @param refLink           Url for the treatment
    */
    public Treatment(int id, int geneticEntityId, String stableId, String name, String description, String refLink) {
        this.geneticEntityId = geneticEntityId;
        this.geneticEntityId = geneticEntityId;
        this.stableId = stableId;
        this.name = name;
        this.description = description;
        this.refLink = refLink;
    }

    /**
     * Create a Treatment object from fields
     * 
     * @param treatmentId       Treatment table ID key of the treament
     * @param geneticEntityId   Genetic_entity table ID key associated to the treament record
     * @param stableId          Stable identifier of the treatment used in the cBioPortal instance
     * @param name              Name of the treatment
     * @param description       Description of the treatment
     * @param refLink           Url for the treatment
    */
    public Treatment(String stableId, String name, String description, String refLink) {
        this.stableId = stableId;
        this.name = name;
        this.description = description;
        this.refLink = refLink;
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
     * @return the geneticEntityId
     */
    public int getGeneticEntityId() {
        return geneticEntityId;
    }

    /**
     * @param geneticEntityId the geneticEntityId to set
     */
    public void setGeneticEntityId(Integer geneticEntityId) {
        this.geneticEntityId = geneticEntityId;
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
     * @return the refLink
     */
    public String getRefLink() {
        return refLink;
    }

    /**
     * @param refLink the refLink to set
     */
    public void setRefLink(String refLink) {
        this.refLink = refLink;
    }

}
