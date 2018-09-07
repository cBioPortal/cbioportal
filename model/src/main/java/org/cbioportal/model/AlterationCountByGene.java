package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class AlterationCountByGene implements Serializable {

    private Integer entrezGeneId;
    private String hugoGeneSymbol;
    private Integer countByEntity;
	private Integer totalCount;
	private BigDecimal frequency;
	private BigDecimal qValue;

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getHugoGeneSymbol() {
		return hugoGeneSymbol;
	}

	public void setHugoGeneSymbol(String hugoGeneSymbol) {
		this.hugoGeneSymbol = hugoGeneSymbol;
	}

	public Integer getCountByEntity() {
		return countByEntity;
	}

	public void setCountByEntity(Integer countByEntity) {
		this.countByEntity = countByEntity;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public BigDecimal getFrequency() {
		return frequency;
	}

	public void setFrequency(BigDecimal frequency) {
		this.frequency = frequency;
	}

	public BigDecimal getqValue() {
		return qValue;
	}

	public void setqValue(BigDecimal qValue) {
		this.qValue = qValue;
	}	
}
