package org.cbioportal.web.parameter;

import jakarta.validation.constraints.Size;
import java.util.List;
import java.io.Serializable;

public class GenericAssayMetaFilter implements Serializable {

    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<String> molecularProfileIds;
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<String> genericAssayStableIds;

    public List<String> getMolecularProfileIds() {
        return molecularProfileIds;
    }

    public void setMolecularProfileIds(List<String> molecularProfileIds) {
        this.molecularProfileIds = molecularProfileIds;
    }

    public List<String> getGenericAssayStableIds() {
        return genericAssayStableIds;
    }

    public void setGenericAssayStableIds(List<String> genericAssayStableIds) {
        this.genericAssayStableIds = genericAssayStableIds;
    }
}