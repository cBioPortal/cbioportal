package org.cbioportal.web.parameter;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import org.cbioportal.web.SampleController;

import java.util.List;
import java.io.Serializable;

public class SampleFilter implements Serializable {

    @Size(min = 1, max = SampleController.SAMPLE_MAX_PAGE_SIZE)
    private List<SampleIdentifier> sampleIdentifiers;
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<String> sampleListIds;
    @Size(min = 1, max = SampleController.SAMPLE_MAX_PAGE_SIZE)
    private List<String> uniqueSampleKeys;

    @AssertTrue
    private boolean isOnlyOneTypeOfFilterPresent() {
        return sampleIdentifiers != null ^ sampleListIds != null ^ uniqueSampleKeys != null;
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

    public List<String> getUniqueSampleKeys() {
        return uniqueSampleKeys;
    }

    public void setUniqueSampleKeys(List<String> uniqueSampleKeys) {
        this.uniqueSampleKeys = uniqueSampleKeys;
    }
}
