/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;

/**
 *
 * @author abeshoua
 */

@JsonInclude(Include.NON_NULL)
public class DBProfileData implements Serializable {
    public String genetic_profile_id;
    public String entrez_gene_id;
    public String hugo_gene_symbol;
    public String sample_id;
    public String study_id;
    public String sample_list_id; // null unless querying by sample list id
}
