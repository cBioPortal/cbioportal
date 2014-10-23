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
public class DBProteinArrayData implements Serializable {
    public String array_id;
    public String array_type;
    public Integer entrez_gene_id;
    public String residue;
    public Integer internal_case_id;
    public Double abundance;
}
