package org.cbioportal.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.io.Serializable;
import org.mskcc.cbio.portal.model.Mutation;

public class MutationWithSampleListId implements Serializable {
    @JsonUnwrapped
    private Mutation mutation;

    private String sampleListId;

    public MutationWithSampleListId(Mutation mutation, String sampleListId) {
        this.mutation = mutation;
        this.sampleListId = sampleListId;
    }

    public String getSampleListId() {
        return sampleListId;
    }

    public void setSampleListId(String sampleListId) {
        this.sampleListId = sampleListId;
    }

    public Mutation getMutation() {
        return mutation;
    }

    public void setMutation(Mutation mutation) {
        this.mutation = mutation;
    }
}
