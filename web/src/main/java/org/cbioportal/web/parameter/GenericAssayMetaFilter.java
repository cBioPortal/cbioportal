package org.cbioportal.web.parameter;

import javax.validation.constraints.Size;
import java.util.List;
import java.io.Serializable;

public class GenericAssayMetaFilter implements Serializable {

    private List<String> molecularProfileIds;
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