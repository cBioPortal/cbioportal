/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
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

package org.cbioportal.model;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author ochoaa
 */
public class GeneSet implements Serializable {
    
    private Integer id;
    private Integer geneticEntityId;
    private String externalId;
    private String nameShort;
    private String name;
    private String refLink;
    private String version;
    private List<Integer> genesetGenes;

    /**
     * @return the id
     */
    public Integer getId() {
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
    public Integer getGeneticEntityId() {
        return geneticEntityId;
    }

    /**
     * @param geneticEntityId the geneticEntityId to set
     */
    public void setGeneticEntityId(Integer geneticEntityId) {
        this.geneticEntityId = geneticEntityId;
    }

    /**
     * @return the externalId
     */
    public String getExternalId() {
        return externalId;
    }

    /**
     * @param externalId the externalId to set
     */
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    /**
     * @return the nameShort
     */
    public String getNameShort() {
        return nameShort;
    }

    /**
     * @param nameShort the nameShort to set
     */
    public void setNameShort(String nameShort) {
        this.nameShort = nameShort;
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

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the genesetGenes
     */
    public List<Integer> getGenesetGenes() {
        return genesetGenes;
    }

    /**
     * @param genesetGenes the genesetGenes to set
     */
    public void setGenesetGenes(List<Integer> genesetGenes) {
        this.genesetGenes = genesetGenes;
    }
    
}
