package org.cbioportal.web.parameter;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;
import java.util.List;

public class MolecularDataMultipleStudyFilter {

    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<SampleMolecularIdentifier> sampleMolecularIdentifiers;
    private List<String> molecularProfileIds;
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<Integer> entrezGeneIds;

    @AssertTrue
    private boolean isEitherMolecularProfileIdsOrSampleMolecularIdentifiersPresent() {
        return molecularProfileIds != null ^ sampleMolecularIdentifiers != null;
    }

    public List<SampleMolecularIdentifier> getSampleMolecularIdentifiers() {
        return sampleMolecularIdentifiers;
    }

    public void setSampleMolecularIdentifiers(List<SampleMolecularIdentifier> sampleMolecularIdentifiers) {
        this.sampleMolecularIdentifiers = sampleMolecularIdentifiers;
    }

    public List<String> getMolecularProfileIds() {
        return molecularProfileIds;
    }

    public void setMolecularProfileIds(List<String> molecularProfileIds) {
        this.molecularProfileIds = molecularProfileIds;
    }

    public List<Integer> getEntrezGeneIds() {
        return entrezGeneIds;
    }

    public void setEntrezGeneIds(List<Integer> entrezGeneIds) {
        this.entrezGeneIds = entrezGeneIds;
    }
}
