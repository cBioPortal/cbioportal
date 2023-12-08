package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class GenericAssayEnrichment extends ExpressionEnrichment implements Serializable {

	@NotNull
	private String stableId;
	@NotNull
	private String name;
    @NotNull
    private BigDecimal qValue;
	@NotNull
	private HashMap<String, String> genericEntityMetaProperties;

	public String getStableId() {
		return stableId;
	}

	public void setStableId(String stableId) {
		this.stableId = stableId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashMap<String, String> getGenericEntityMetaProperties() {
		return genericEntityMetaProperties;
	}

	public void setGenericEntityMetaProperties(HashMap<String, String> genericEntityMetaProperties) {
		this.genericEntityMetaProperties = genericEntityMetaProperties;
	}

    @JsonProperty("qValue")
    public BigDecimal getqValue() {
        return qValue;
    }

    public void setqValue(BigDecimal qValue) {
        this.qValue = qValue;
    }
    
    public static int compare(GenericAssayEnrichment c1, GenericAssayEnrichment c2) {
        return c1.getpValue().compareTo(c2.getpValue());
    }
}
