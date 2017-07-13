package org.cbioportal.model;

import java.io.Serializable;

public class MutationCountByPosition implements Serializable {
    
    private Integer entrezGeneId;
    private Integer proteinPosStart;
    private Integer proteinPosEnd;
    private Integer count;

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public Integer getProteinPosStart() {
        return proteinPosStart;
    }

    public void setProteinPosStart(Integer proteinPosStart) {
        this.proteinPosStart = proteinPosStart;
    }

    public Integer getProteinPosEnd() {
        return proteinPosEnd;
    }

    public void setProteinPosEnd(Integer proteinPosEnd) {
        this.proteinPosEnd = proteinPosEnd;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
