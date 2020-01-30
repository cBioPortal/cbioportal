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

public class DrugInteraction {
    private String drug;
    private long targetGene;
    private String interactionType;
    private String dataSource;
    private String experimentTypes;
    private String pubMedIDs;

    public DrugInteraction() {}

    public DrugInteraction(
        String drug,
        Integer targetGene,
        String interactionType,
        String dataSource,
        String experimentTypes,
        String pubMedIDs
    ) {
        this.drug = drug;
        this.targetGene = targetGene;
        this.interactionType = interactionType;
        this.dataSource = dataSource;
        this.experimentTypes = experimentTypes;
        this.pubMedIDs = pubMedIDs;
    }

    public String getDrug() {
        return drug;
    }

    public void setDrug(String drug) {
        this.drug = drug;
    }

    public long getTargetGene() {
        return targetGene;
    }

    public void setTargetGene(long targetGene) {
        this.targetGene = targetGene;
    }

    public String getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(String interactionType) {
        this.interactionType = interactionType;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getExperimentTypes() {
        return experimentTypes;
    }

    public void setExperimentTypes(String experimentTypes) {
        this.experimentTypes = experimentTypes;
    }

    public String getPubMedIDs() {
        return pubMedIDs;
    }

    public void setPubMedIDs(String pubMedIDs) {
        this.pubMedIDs = pubMedIDs;
    }
}
