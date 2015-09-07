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
    public String name;
    public String description;
    public String study_id;
    public List<String> patient_ids;
    
    public DBPatientList discardList() {
        this.patient_ids = null;
        return this;
    }
}
