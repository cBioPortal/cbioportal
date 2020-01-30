package org.cbioportal.web.parameter;

import java.util.List;
import javax.validation.constraints.Size;
import org.cbioportal.model.MolecularProfileCaseIdentifier;

public class MolecularProfileCasesGroupFilter {
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<MolecularProfileCaseIdentifier> MolecularProfileCaseIdentifiers;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MolecularProfileCaseIdentifier> getMolecularProfileCaseIdentifiers() {
        return MolecularProfileCaseIdentifiers;
    }

    public void setMolecularProfileCaseIdentifiers(
        List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers
    ) {
        MolecularProfileCaseIdentifiers = molecularProfileCaseIdentifiers;
    }
}
