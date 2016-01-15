package org.mskcc.cbio.portal.model;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jiaojiao
 
@JsonInclude(JsonInclude.Include.NON_NULL)*/
@JsonInclude(JsonInclude.Include.NON_DEFAULT)

public class DBAltCount implements Serializable {
    public int count;
    public int studyID;
    public String id;
    public String gene;
    public int start;
    public int end;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getStudyID() {
        return studyID;
    }

    public void setStudyID(int studyID) {
        this.studyID = studyID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

   
    
    
     
}
