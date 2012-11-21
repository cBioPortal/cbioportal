package org.mskcc.mondrian.client;

public class CaseList {
    private String[] cases;
    private String id;
    private String name;
    private String description;

    public CaseList(String id, String name, String description, String[] cases) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cases = cases;
    }

    public String[] getCases() {
        return cases;
    }

    public void setCases(String[] cases) {
        this.cases = cases;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
    	return name;
    }
    
    
}