package org.cbioportal.web.parameter;

import static org.cbioportal.web.parameter.PagingConstants.MAX_PAGE_SIZE;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.Size;
import org.cbioportal.web.ClinicalDataController;

public class ClinicalDataMultiStudyFilter implements Serializable {
    @Size(min = 1, max = ClinicalDataController.CLINICAL_DATA_MAX_PAGE_SIZE)
    private List<ClinicalDataIdentifier> identifiers;

    @Size(min = 1, max = MAX_PAGE_SIZE)
    private List<String> attributeIds;

    public List<ClinicalDataIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<ClinicalDataIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    public List<String> getAttributeIds() {
        return attributeIds;
    }

    public void setAttributeIds(List<String> attributeIds) {
        this.attributeIds = attributeIds;
    }
}
