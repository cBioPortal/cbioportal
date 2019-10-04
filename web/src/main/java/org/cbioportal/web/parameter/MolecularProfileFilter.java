package org.cbioportal.web.parameter;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;
import java.util.List;
import java.io.Serializable;

public class MolecularProfileFilter implements Serializable {

    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<String> studyIds;
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<String> molecularProfileIds;

    @AssertTrue
    private boolean isEitherStudyIdsOrMolecularProfileIdsPresent() {
        return studyIds != null ^ molecularProfileIds != null;
    }

    public List<String> getStudyIds() {
        return studyIds;
    }

    public void setStudyIds(List<String> studyIds) {
        this.studyIds = studyIds;
    }

    public List<String> getMolecularProfileIds() {
        return molecularProfileIds;
    }

    public void setMolecularProfileIds(List<String> molecularProfileIds) {
        this.molecularProfileIds = molecularProfileIds;
    }
}
