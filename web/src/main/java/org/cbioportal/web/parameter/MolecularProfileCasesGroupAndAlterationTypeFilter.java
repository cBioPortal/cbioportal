package org.cbioportal.web.parameter;

import io.swagger.annotations.ApiModelProperty;
import org.cbioportal.model.AlterationFilter;

import javax.validation.constraints.Size;
import java.util.List;

public class MolecularProfileCasesGroupAndAlterationTypeFilter {

    // TODO alterationEventTypes and alterationFilter are both 
    // implemented because of backwards compatibility
    // after merge of PR https://github.com/cBioPortal/cbioportal-frontend/pull/3555
    // the alterationEventTypes param can be removed
    private AlterationEventTypeFilter alterationEventTypes;
    private AlterationFilter alterationFilter;
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

    public AlterationFilter getAlterationFilter() {
        if (this.alterationFilter != null)
            return this.alterationFilter;
        // TODO code below can be removed after merge of PR https://github.com/cBioPortal/cbioportal-frontend/pull/3555
        AlterationFilter alterationFilter = new AlterationFilter();
        if (this.alterationEventTypes != null) {
            alterationFilter.setMutationBooleanMap(this.alterationEventTypes.getMutationEventTypes());
            alterationFilter.setCnaBooleanMap(this.alterationEventTypes.getCopyNumberAlterationEventTypes());
        }
        return alterationFilter;
    }

    public void setAlterationFilter(AlterationFilter alterationFilter) {
        this.alterationFilter = alterationFilter;
    }

    // TODO code below can be removed after merge of PR https://github.com/cBioPortal/cbioportal-frontend/pull/3555
    public void setAlterationEventTypes(AlterationEventTypeFilter alterationEventTypes) {
        this.alterationEventTypes = alterationEventTypes;
    }
    
}
