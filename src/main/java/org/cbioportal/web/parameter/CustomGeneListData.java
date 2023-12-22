package org.cbioportal.web.parameter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomGeneListData implements Serializable {

    private String name;
    private String description;
    private Set<String> geneList = new HashSet<>();
    private Set<String> users = new HashSet<>();

    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getGeneList() {
        return geneList;
    }

    public void setGeneList(Set<String> geneList) { this.geneList = geneList; }

}
