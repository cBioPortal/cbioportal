package org.cbioportal.web.parameter;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.io.Serializable;
import java.util.Set;

public class MolecularProfileFilter implements Serializable {

    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<String> studyIds;
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private Set<String> molecularProfileIds;

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

    public Set<String> getMolecularProfileIds() {
        return molecularProfileIds;
    }

    public void setMolecularProfileIds(Set<String> molecularProfileIds) {
        this.molecularProfileIds = molecularProfileIds;
    }
}
