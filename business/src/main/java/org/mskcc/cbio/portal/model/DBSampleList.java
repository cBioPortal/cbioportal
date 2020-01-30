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
public class DBSampleList implements Serializable {
    public String id;
    public String name;
    public String description;
    public String study_id;
    public List<String> sample_ids;

    public DBSampleList discardList() {
        this.sample_ids = null;
        return this;
    }
}
