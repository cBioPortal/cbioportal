package org.cbioportal.web.parameter;

import org.cbioportal.web.ClinicalDataController;

import javax.validation.constraints.Size;
import java.util.List;

import static org.cbioportal.web.parameter.PagingConstants.MAX_PAGE_SIZE;

public class ClinicalDataMultiStudyFilter {

    @Size(min = 1, max = ClinicalDataController.CLINICAL_DATA_MAX_PAGE_SIZE)
    List<ClinicalDataIdentifier> identifiers;
    @Size(min = 1, max = MAX_PAGE_SIZE)
    List<String> attributeIds;

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
