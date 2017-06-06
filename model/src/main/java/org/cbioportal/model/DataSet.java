/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.model;
import java.io.Serializable;
/**
 *
 * @author jiaojiao
 */
public class DataSet implements Serializable{
    private String name;
    private String citation;
    private String cancer_study_identifier;
    private String PMID;
    private String stable_id;
    private int count;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCitation() {
        return citation;
    }

    public void setCitation(String citation) {
        this.citation = citation;
    }

    public String getCancer_study_identifier() {
        return cancer_study_identifier;
    }

    public void setCancer_study_identifier(String cancer_study_identifier) {
        this.cancer_study_identifier = cancer_study_identifier;
    }

    public String getPMID() {
        return PMID;
    }

    public void setPMID(String PMID) {
        this.PMID = PMID;
    }

    public String getStable_id() {
        return stable_id;
    }

    public void setStable_id(String stable_id) {
        this.stable_id = stable_id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
    
    
}
