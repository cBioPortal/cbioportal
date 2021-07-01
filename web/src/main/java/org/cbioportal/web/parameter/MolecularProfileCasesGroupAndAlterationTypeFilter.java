package org.cbioportal.web.parameter;

import io.swagger.annotations.ApiModelProperty;
import org.cbioportal.model.AlterationFilter;

import java.util.List;

import javax.validation.constraints.Size;

public class MolecularProfileCasesGroupAndAlterationTypeFilter {


    private AlterationFilter alterationEventTypes;
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

    public AlterationFilter getAlterationEventTypes() {
        return this.alterationEventTypes;
    }

    public void setAlterationEventTypes(AlterationFilter alterationEventTypes) {
        this.alterationEventTypes = alterationEventTypes;
    }
}
