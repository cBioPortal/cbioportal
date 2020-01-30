package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Size;

public class GroupFilter implements Serializable {
    @Size(min = 2)
    @Valid
    private List<Group> groups;

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}
