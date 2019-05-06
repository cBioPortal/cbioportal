package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

public class MultipleStudiesEnrichmentFilter implements Serializable {

    @Size(min = 2)
    @Valid
    private List<MolecularProfileCasesGroup> groups;

    public List<MolecularProfileCasesGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<MolecularProfileCasesGroup> groups) {
        this.groups = groups;
    }
}
