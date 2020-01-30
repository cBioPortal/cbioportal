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
public class DBGeneSet implements Serializable {
    public String id;
    public String name;
    public String gene_list; // space-delimited

    public DBGeneSet() {}

    public DBGeneSet(String _id, String _name, String _list) {
        this.id = _id;
        this.name = _name;
        this.gene_list = _list;
    }

    public DBGeneSet stripList() {
        return new DBGeneSet(this.id, this.name, "");
    }
}
