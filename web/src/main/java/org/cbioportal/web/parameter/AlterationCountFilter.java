package org.cbioportal.web.parameter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.MolecularProfileCaseIdentifier;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlterationCountFilter {
    private AlterationFilter alterationFilter;
    private List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers;

    public AlterationFilter getAlterationFilter() {
        return alterationFilter;
    }

    public void setAlterationFilter(AlterationFilter alterationFilter) {
        this.alterationFilter = alterationFilter;
    }

    public List<MolecularProfileCaseIdentifier> getMolecularProfileCaseIdentifiers() {
        return molecularProfileCaseIdentifiers;
    }

    public void setMolecularProfileCaseIdentifiers(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers) {
        this.molecularProfileCaseIdentifiers = molecularProfileCaseIdentifiers;
    }
}
