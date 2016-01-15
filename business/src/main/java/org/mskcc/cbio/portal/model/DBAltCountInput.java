/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.model;

import java.io.Serializable;
import java.util.List;
/**
 *
 * @author jiaojiao
 */
public class DBAltCountInput implements Serializable {
    
    public String type;
    public Boolean per_study ;
    public List<DBAltCountInputData> data; 
    public List<String> echo;

    public List<String> getEcho() {
        return echo;
    }

    public void setEcho(List<String> echo) {
        this.echo = echo;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getPer_study() {
        return per_study;
    }

    public void setPer_study(Boolean per_study) {
        this.per_study = per_study;
    }

    public List<DBAltCountInputData> getData() {
        return data;
    }

    public void setData(List<DBAltCountInputData> data) {
        this.data = data;
    }

    
}
