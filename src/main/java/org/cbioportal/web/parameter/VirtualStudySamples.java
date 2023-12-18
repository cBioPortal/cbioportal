package org.cbioportal.web.parameter;

import java.util.Set;

public class VirtualStudySamples {

    private String id;
    private Set<String> samples;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<String> getSamples() {
        return samples;
    }

    public void setSamples(Set<String> samples) {
        this.samples = samples;
    }

}
