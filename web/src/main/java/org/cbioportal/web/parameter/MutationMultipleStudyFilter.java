package org.cbioportal.web.parameter;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;
import java.util.List;
    
public class MutationMultipleStudyFilter {

    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<SampleGeneticIdentifier> sampleGeneticIdentifiers;
    private List<String> geneticProfileIds;
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<Integer> entrezGeneIds;

    @AssertTrue
    private boolean isEitherSampleListIdOrSampleIdsPresent() {
        return geneticProfileIds != null ^ sampleGeneticIdentifiers != null;
    }

    public List<SampleGeneticIdentifier> getSampleGeneticIdentifiers() {
        return sampleGeneticIdentifiers;
    }

    public void setSampleGeneticIdentifiers(List<SampleGeneticIdentifier> sampleGeneticIdentifiers) {
        this.sampleGeneticIdentifiers = sampleGeneticIdentifiers;
    }

    public List<String> getGeneticProfileIds() {
        return geneticProfileIds;
    }

    public void setGeneticProfileIds(List<String> geneticProfileIds) {
        this.geneticProfileIds = geneticProfileIds;
    }

    public List<Integer> getEntrezGeneIds() {
        return entrezGeneIds;
    }

    public void setEntrezGeneIds(List<Integer> entrezGeneIds) {
        this.entrezGeneIds = entrezGeneIds;
    }
}