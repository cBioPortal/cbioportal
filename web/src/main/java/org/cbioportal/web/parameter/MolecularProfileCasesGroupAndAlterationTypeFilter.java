package org.cbioportal.web.parameter;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

import javax.validation.constraints.Size;

public class MolecularProfileCasesGroupAndAlterationTypeFilter {


    private AlterationEventTypeFilter alterationEventTypes;
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    @ApiModelProperty(required = true)
    private List<MolecularProfileCasesGroupFilter> molecularProfileCasesGroupFilter;
 

    public List<MolecularProfileCasesGroupFilter> getMolecularProfileCasesGroupFilter() {
        return this.molecularProfileCasesGroupFilter;
    }

    public void setMolecularProfileCasesGroupFilter(
            List<MolecularProfileCasesGroupFilter> molecularProfileCasesGroupFilter) {
                this.molecularProfileCasesGroupFilter = molecularProfileCasesGroupFilter;
    }

    public AlterationEventTypeFilter getAlterationEventTypes() {
        return this.alterationEventTypes;
    }

    public void setAlterationEventTypes(AlterationEventTypeFilter alterationEventTypes) {
        this.alterationEventTypes = alterationEventTypes;
    }
}
