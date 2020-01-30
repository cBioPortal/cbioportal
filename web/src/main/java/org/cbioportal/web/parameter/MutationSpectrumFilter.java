package org.cbioportal.web.parameter;

import java.util.List;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;

public class MutationSpectrumFilter {
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<String> sampleIds;

    private String sampleListId;

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
}
