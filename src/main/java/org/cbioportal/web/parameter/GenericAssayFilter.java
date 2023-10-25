package org.cbioportal.web.parameter;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import java.util.List;

public class GenericAssayFilter {

    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<String> sampleIds;
    private String sampleListId;
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<String> genericAssayStableId;

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

    public List<String> getGenericAssayStableIds() {
        return genericAssayStableId;
    }

    public void setGenericAssayStableIds(List<String> genericAssayStableId) {
        this.genericAssayStableId = genericAssayStableId;
    }
}
