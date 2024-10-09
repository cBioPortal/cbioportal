package org.cbioportal.model.enclave;

public class MutationCount {
    public MutationCount(String hugo, int count) {
        this.hugo = hugo;
        this.count = count;
    }

    public String hugo;
    public Integer count;
}
