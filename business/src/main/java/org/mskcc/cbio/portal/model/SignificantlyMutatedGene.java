package org.mskcc.cbio.portal.model;

import java.io.Serializable;

public class SignificantlyMutatedGene implements Serializable {
    private Integer entrezGeneId;
    private String concatenatedSampleIds;
    private Integer count;

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getConcatenatedSampleIds() {
        return concatenatedSampleIds;
    }

    public void setConcatenatedSampleIds(String concatenatedSampleIds) {
        this.concatenatedSampleIds = concatenatedSampleIds;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
