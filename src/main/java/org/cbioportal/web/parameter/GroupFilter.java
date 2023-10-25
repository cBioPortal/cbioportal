package org.cbioportal.web.parameter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;


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
