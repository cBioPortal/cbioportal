package org.cbioportal.web.parameter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class DiscreteCopyNumberFilter {
    
    @NotNull
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<String> sampleIds;
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<Integer> entrezGeneIds;

    public List<String> getSampleIds() {
        return sampleIds;
    }

    public void setSampleIds(List<String> sampleIds) {
        this.sampleIds = sampleIds;
    }

    public List<Integer> getEntrezGeneIds() {
        return entrezGeneIds;
    }

    public void setEntrezGeneIds(List<Integer> entrezGeneIds) {
        this.entrezGeneIds = entrezGeneIds;
    }
}
