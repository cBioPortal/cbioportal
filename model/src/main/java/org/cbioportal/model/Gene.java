package org.cbioportal.model;

import org.cbioportal.model.summary.GeneSummary;

import java.util.List;

public class Gene extends GeneSummary {

    private List<String> aliases;

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }
}