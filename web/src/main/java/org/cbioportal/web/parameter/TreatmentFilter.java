package org.cbioportal.web.parameter;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;
import java.util.List;
import java.io.Serializable;

public class TreatmentFilter implements Serializable {

    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<String> studyIds;
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<String> treatmentIds;

    @AssertTrue
    private boolean isEitherStudyIdsOrTreatmentIdsPresent() {
        return studyIds != null ^ treatmentIds != null;
    }

    public List<String> getStudyIds() {
        return studyIds;
    }

    public void setStudyIds(List<String> studyIds) {
        this.studyIds = studyIds;
    }

    public List<String> getTreatmentIds() {
        return treatmentIds;
    }

    public void setTreatmentIds(List<String> treatmentIds) {
        this.treatmentIds = treatmentIds;
    }
}
