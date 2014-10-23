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
public class DBProfileData implements Serializable {
    public Integer internal_id;
    public Long entrez_gene_id; //protein_array_target
    public Integer internal_case_id;
}
