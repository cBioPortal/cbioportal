package org.cbioportal.web.parameter;

import io.swagger.annotations.ApiModelProperty;
import org.cbioportal.model.AlterationFilter;

import javax.validation.constraints.Size;
import java.util.List;

public class MolecularProfileCasesGroupAndAlterationTypeFilter {

    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    @ApiModelProperty(required = true)
    private List<MolecularProfileCasesGroupFilter> molecularProfileCasesGroupFilter;
    @ApiModelProperty(required = true)
    private AlterationFilter alterationFilter;

    public List<MolecularProfileCasesGroupFilter> getMolecularProfileCasesGroupFilter() {
        return this.molecularProfileCasesGroupFilter;
    }

    public void setMolecularProfileCasesGroupFilter(
            List<MolecularProfileCasesGroupFilter> molecularProfileCasesGroupFilter) {
                this.molecularProfileCasesGroupFilter = molecularProfileCasesGroupFilter;
    }

    public AlterationFilter getAlterationFilter() {
        return this.alterationFilter;
    }

    public void setAlterationFilter(AlterationFilter alterationFilter) {
        this.alterationFilter = alterationFilter;
    }
}
