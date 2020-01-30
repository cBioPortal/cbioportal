package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.Size;

public class Group implements Serializable {
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<SampleIdentifier> sampleIdentifiers;

    private String name;

    public List<SampleIdentifier> getSampleIdentifiers() {
        return sampleIdentifiers;
    }

    public void setSampleIdentifiers(List<SampleIdentifier> sampleIdentifiers) {
        this.sampleIdentifiers = sampleIdentifiers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
