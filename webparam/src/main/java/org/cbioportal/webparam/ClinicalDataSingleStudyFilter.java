package org.cbioportal.webparam;

import javax.validation.constraints.Size;
import java.util.List;

import static org.cbioportal.webparam.Constants.CLINICAL_DATA_MAX_PAGE_SIZE;
import static org.cbioportal.webparam.PagingConstants.MAX_PAGE_SIZE;

public class ClinicalDataSingleStudyFilter {

    @Size(min = 1, max = CLINICAL_DATA_MAX_PAGE_SIZE)
    private List<String> ids;
    @Size(min = 1, max = MAX_PAGE_SIZE)
    private List<String> attributeIds;

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public List<String> getAttributeIds() {
        return attributeIds;
    }

    public void setAttributeIds(List<String> attributeIds) {
        this.attributeIds = attributeIds;
    }
}
