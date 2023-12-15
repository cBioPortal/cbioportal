package org.cbioportal.web.parameter;

public class MutationPositionIdentifier {

    private Integer entrezGeneId;
    private Integer proteinPosStart;
    private Integer proteinPosEnd;

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
}
