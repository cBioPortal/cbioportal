package org.cbioportal.web.parameter;

import org.cbioportal.model.MolecularProfileCase;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class MultipleStudiesEnrichmentFilter {
    
    @NotNull
    @Size(min = 1)
    private List<MolecularProfileCase> molecularProfileCaseSet1;
    @NotNull
    @Size(min = 1)
    private List<MolecularProfileCase> molecularProfileCaseSet2;

    public List<MolecularProfileCase> getMolecularProfileCaseSet1() {
        return molecularProfileCaseSet1;
    }

    public void setMolecularProfileCaseSet1(List<MolecularProfileCase> molecularProfileCaseSet1) {
        this.molecularProfileCaseSet1 = molecularProfileCaseSet1;
    }

    public List<MolecularProfileCase> getMolecularProfileCaseSet2() {
        return molecularProfileCaseSet2;
    }

    public void setMolecularProfileCaseSet2(List<MolecularProfileCase> molecularProfileCaseSet2) {
        this.molecularProfileCaseSet2 = molecularProfileCaseSet2;
    }
}
