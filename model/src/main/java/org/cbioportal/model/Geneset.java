package org.cbioportal.model;

import java.io.Serializable;

public class Geneset extends GeneticEntity implements Serializable {

    private Integer internalId;
    private String genesetId;
    private String nameShort;
    private String name;
    private String refLink;
    private String version;


	public Integer getInternalId() {
		return internalId;
	}

	public void setInternalId(Integer internalId) {
		this.internalId = internalId;
	}

	public String getGenesetId() {
		return genesetId;
	}

	public void setGenesetId(String genesetId) {
		this.genesetId = genesetId;
	}

	public String getNameShort() {
		return nameShort;
	}

	public void setNameShort(String nameShort) {
		this.nameShort = nameShort;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRefLink() {
		return refLink;
	}

	public void setRefLink(String refLink) {
		this.refLink = refLink;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String getEntityStableId() {
		return genesetId;
	}
	
	@Override
	public EntityType getEntityType() {
		return EntityType.GENESET;
	}
}