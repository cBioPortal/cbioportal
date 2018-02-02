package org.cbioportal.web.parameter;

import org.cbioportal.web.SampleController;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;
import java.util.List;

public class SampleFilter {
    
    @Size(min = 1, max = SampleController.SAMPLE_MAX_PAGE_SIZE)
    private List<SampleIdentifier> sampleIdentifiers;
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<String> sampleListIds;
    
    @AssertTrue
    private boolean isEitherSampleIdentifiersOrSampleListIdsPresent() {
        return sampleIdentifiers != null ^ sampleListIds != null;
    }

    public List<SampleIdentifier> getSampleIdentifiers() {
        return sampleIdentifiers;
    }

    public void setSampleIdentifiers(List<SampleIdentifier> sampleIdentifiers) {
        this.sampleIdentifiers = sampleIdentifiers;
    }

    public List<String> getSampleListIds() {
        return sampleListIds;
    }

    public void setSampleListIds(List<String> sampleListIds) {
        this.sampleListIds = sampleListIds;
    }
}
