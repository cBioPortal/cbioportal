package org.cbioportal.web.parameter;

import org.cbioportal.model.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class MultipleStudiesEnrichmentFilter {
    
    @NotNull
    @Size(min = 1)
    private List<Entity> set1;
    @NotNull
    @Size(min = 1)
    private List<Entity> set2;

    public List<Entity> getSet1() {
        return set1;
    }

    public void setSet1(List<Entity> set1) {
        this.set1 = set1;
    }

    public List<Entity> getSet2() {
        return set2;
    }

    public void setSet2(List<Entity> set2) {
        this.set2 = set2;
    }
}
