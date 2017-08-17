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
public class DBStudy implements Serializable {
    public String id;
    public String type_of_cancer;
    public String name;
    public String short_name;
    public String description;
    public String pmid;
    public String citation;
    public String groups;
    public Integer internal_id;
}
