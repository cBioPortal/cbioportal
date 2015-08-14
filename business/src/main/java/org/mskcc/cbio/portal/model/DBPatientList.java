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
 * @author abeshoua
 */
public class DBPatientList implements Serializable {
    public String id;
    public Integer internal_id;
    public String name;
    public String description;
    public Integer internal_study_id;
    public List<Integer> internal_patient_ids;
    
    public DBPatientList discardList() {
        this.internal_patient_ids = null;
        return this;
    }
}
