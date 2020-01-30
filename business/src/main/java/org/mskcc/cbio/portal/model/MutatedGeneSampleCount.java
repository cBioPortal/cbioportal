package org.mskcc.cbio.portal.model;

import java.io.Serializable;

public class MutatedGeneSampleCount implements Serializable {
    private Integer entrezGeneId;
    private Integer count;

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
