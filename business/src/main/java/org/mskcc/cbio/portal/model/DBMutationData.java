/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

/**
 *
 * @author abeshoua
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DBMutationData extends DBProfileData implements Serializable {
    public String sequencing_center;
    public String mutation_status;
    public String mutation_type;
    public String validation_status;
    public String amino_acid_change;
    public String functional_impact_score;
    public String xvar_link;
    public String xvar_link_pdb;
    public String xvar_link_msa;
    public String chr;
    public String start_position;
    public String end_position;
    public String protein_start_position;
    public String protein_end_position;
    public String reference_allele;
    public String variant_allele;
    public String reference_read_count_tumor;
    public String variant_read_count_tumor;
    public String reference_read_count_normal;
    public String variant_read_count_normal;
    public String cosmic_count;
    public String keyword;
    public String driver_filter;
    public String driver_filter_annotation;
    public String driver_tiers_filter;
    public String driver_tiers_filter_annotation;
}
