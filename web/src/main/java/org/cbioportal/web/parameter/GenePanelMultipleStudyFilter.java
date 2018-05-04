package org.cbioportal.web.parameter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.io.Serializable;

public class GenePanelMultipleStudyFilter implements Serializable {

    @NotNull
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<SampleMolecularIdentifier> sampleMolecularIdentifiers;

    public List<SampleMolecularIdentifier> getSampleMolecularIdentifiers() {
        return sampleMolecularIdentifiers;
    }

    public void setSampleMolecularIdentifiers(List<SampleMolecularIdentifier> sampleMolecularIdentifiers) {
        this.sampleMolecularIdentifiers = sampleMolecularIdentifiers;
    }
}
