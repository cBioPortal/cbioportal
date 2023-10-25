package org.cbioportal.web.parameter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


import jakarta.validation.constraints.Size;
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
            List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers) {
        
        MolecularProfileCaseIdentifiers = new ArrayList<>(
                new HashSet<>(molecularProfileCaseIdentifiers));
    }

}
