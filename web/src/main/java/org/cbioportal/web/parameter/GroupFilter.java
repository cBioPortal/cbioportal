package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;

public class GroupFilter implements Serializable {

    @Size(min = 2)
    @Valid
    private List<Group> groups;

    @AssertTrue
    private boolean isGroupsContainsDistinctSamples() {

        List<SampleIdentifier> sampleIdentifiers = groups.stream().flatMap(x -> x.getSampleIdentifiers().stream())
                .collect(Collectors.toList());

        Set<SampleIdentifier> uniqSampleIdentifiers = new HashSet<>(sampleIdentifiers);

        return uniqSampleIdentifiers.size() == sampleIdentifiers.size();
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}
