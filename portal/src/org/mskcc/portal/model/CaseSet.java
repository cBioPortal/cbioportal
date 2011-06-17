package org.mskcc.portal.model;

import java.util.ArrayList;

/**
 * Encapsulates a Set of Cases.
 */
public class CaseSet {
    private String id;
    private String name;
    private String description;
    private ArrayList<String> caseList = new ArrayList<String>();

    /**
     * Gets the description of the case set.
     *
     * @return case set description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the ID of the case set.
     *
     * @return case set ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the case set.
     *
     * @param id case set ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the name of the case set.
     *
     * @return case set name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the case set.
     *
     * @param name case set name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the description of the case set.
     *
     * @param description case set description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets list of all case IDs in the set.
     *
     * @return ArrayList of String Objects.
     */
    public ArrayList<String> getCaseList() {
        return caseList;
    }

    /**
     * Gets list of all case IDs in the set as one string.
     *
     * @return space-delimited list of case IDs.
     */
    public String getCaseListAsString() {
        StringBuffer str = new StringBuffer();
        for (String caseId : caseList) {
            str.append(caseId + " ");
        }
        return str.toString();
    }

    /**
     * Sets list of case IDs in the set.
     *
     * @param cases white-space delimited list of case IDs.
     */
    public void setCaseList(String cases) {
        String parts[] = cases.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            caseList.add(parts[i]);
        }
    }
}
