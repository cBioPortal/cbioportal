/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.cgds.model;

public class DrugInteraction {
    private String drug;
    private long targetGene;
    private String interactionType;
    private String dataSource;
    private String experimentTypes;
    private String pubMedIDs;

    public DrugInteraction() {
    }

    public DrugInteraction(String drug,
                           Integer targetGene,
                           String interactionType,
                           String dataSource,
                           String experimentTypes,
                           String pubMedIDs) {

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
