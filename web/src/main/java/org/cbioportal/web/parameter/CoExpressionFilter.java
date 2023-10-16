package org.cbioportal.web.parameter;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import java.util.List;

public class CoExpressionFilter {

    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<String> sampleIds;
    private String sampleListId;
    private Integer entrezGeneId;
    private String genesetId;

    @AssertTrue
    private boolean isEitherSampleListIdOrSampleIdsPresent() {
        return sampleListId != null ^ sampleIds != null;
    }

    @AssertTrue
    private boolean isEitherEntrezGeneIdOrGenesetIdPresent() {
        return entrezGeneId != null ^ genesetId != null;
    }

    public List<String> getSampleIds() {
        return sampleIds;
    }

    public void setSampleIds(List<String> sampleIds) {
        this.sampleIds = sampleIds;
    }

    public String getSampleListId() {
        return sampleListId;
    }

    public void setSampleListId(String sampleListId) {
        this.sampleListId = sampleListId;
    }

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getGenesetId() {
        return genesetId;
    }

    public void setGenesetId(String genesetId) {
        this.genesetId = genesetId;
    }
}
