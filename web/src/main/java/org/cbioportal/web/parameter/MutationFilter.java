package org.cbioportal.web.parameter;

import org.cbioportal.web.MutationController;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;
import java.util.List;

public class MutationFilter {

    @Size(min = 1, max = MutationController.MUTATION_MAX_PAGE_SIZE)
    private List<String> sampleIds;
    private String sampleListId;
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<Integer> entrezGeneIds;

    @AssertTrue
    private boolean isEitherSampleListIdOrSampleIdsPresent() {
        return sampleListId != null ^ sampleIds != null;
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

    public List<Integer> getEntrezGeneIds() {
        return entrezGeneIds;
    }

    public void setEntrezGeneIds(List<Integer> entrezGeneIds) {
        this.entrezGeneIds = entrezGeneIds;
    }
}
