package org.cbioportal.web.parameter;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;
import java.util.List;

public class GenericAssayDateFilter {

    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<String> sampleIds;
    private String sampleListId;
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<Integer> genericAssayStableId;

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

    public List<Integer> getGenericAssayStableIds() {
        return genericAssayStableId;
    }

    public void setGenericAssayStableIds(List<Integer> genericAssayStableId) {
        this.genericAssayStableId = genericAssayStableId;
    }
}
