/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.model;

import java.io.Serializable;

/**
 *
 * @author abeshoua
 */
public class DBGeneticProfile implements Serializable {
    public String id;
    public String name;
    public String description;
    public String datatype;
    public String study_id;
    public String genetic_alteration_type;
    public String show_profile_in_analysis_tab;
}
