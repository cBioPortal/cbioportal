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

import ca.drugbank.ExternalLink;

import java.util.List;

public class Drug {
    private String id;
    private String name;
    private String description;
    private String synonyms;
    private String externalReference;
    private String resource;
    private boolean isApprovedFDA;
    private boolean isCancerDrug;
    private boolean isNutraceuitical;
    private Integer numberOfClinicalTrials = -1;
    private String ATCCode;

    public Drug() {
    }

    public Drug(String id,
                String name,
                String description,
                String synonyms,
                String externalReference,
                String resource,
                String ATCCode,
                boolean approvedFDA,
                boolean nutraceuitical,
                boolean cancerDrug,
                Integer numberOfClinicalTrials) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.synonyms = synonyms;
        this.externalReference = externalReference;
        this.resource = resource;
        this.isApprovedFDA = approvedFDA;
        this.ATCCode = ATCCode;
        this.isCancerDrug = cancerDrug;
        this.isNutraceuitical = nutraceuitical;
        this.numberOfClinicalTrials = numberOfClinicalTrials;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(String synonyms) {
        this.synonyms = synonyms;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public boolean isApprovedFDA() {
        return isApprovedFDA;
    }

    public void setApprovedFDA(boolean approvedFDA) {
        isApprovedFDA = approvedFDA;
    }

    public String getATCCode() {
        return ATCCode;
    }

    public void setATCCode(String ATCCode) {
        this.ATCCode = ATCCode;
    }

    public boolean isCancerDrug() {
        return isCancerDrug;
    }

    public void setCancerDrug(boolean cancerDrug) {
        isCancerDrug = cancerDrug;
    }

    public boolean isNutraceuitical() {
        return isNutraceuitical;
    }

    public void setNutraceuitical(boolean nutraceuitical) {
        isNutraceuitical = nutraceuitical;
    }

    public Integer getNumberOfClinicalTrials() {
        return numberOfClinicalTrials;
    }

    public void setNumberOfClinicalTrials(Integer numberOfClinicalTrials) {
        this.numberOfClinicalTrials = numberOfClinicalTrials;
    }
}
