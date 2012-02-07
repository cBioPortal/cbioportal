package org.mskcc.cgds.model;

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
    private String ATCCode;

    public Drug() {
    }

    public Drug(String id,
                String name,
                String description,
                String synonyms,
                String externalReference,
                String resource,
                boolean approvedFDA,
                String ATCCode) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.synonyms = synonyms;
        this.externalReference = externalReference;
        this.resource = resource;
        isApprovedFDA = approvedFDA;
        this.ATCCode = ATCCode;
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
}
