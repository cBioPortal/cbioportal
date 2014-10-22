/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.portal.model.GeneticAlterationType;

/**
 *
 * @author abeshoua
 */
public class MutationDataJSON extends ProfileDataJSON {
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
    public Long start_position;
    public Long end_position;
    public String reference_allele;
    public Integer reference_read_count_tumor;
    public Integer variant_read_count_tumor;
    public Integer reference_read_count_normal;
    public Integer variant_read_count_normal;
    
    public MutationDataJSON(ExtendedMutation model) {
        this.genetic_profile_id = model.getGeneticProfileId();
        this.genetic_alteration_type = GeneticAlterationType.MUTATION_EXTENDED;
        this.internal_case_id = model.getSampleId();
        this.sequencing_center = model.getSequencingCenter();
        this.entrez_id = model.getGene().getEntrezGeneId();
        this.gene = model.getGeneSymbol();
        this.internal_case_id = model.getSampleId();
        this.mutation_status = model.getMutationStatus();
        this.mutation_type = model.getMutationType();
        this.validation_status = model.getValidationStatus();
        this.amino_acid_change = model.getProteinChange();
        this.functional_impact_score = model.getFunctionalImpactScore();
        this.xvar_link = model.getLinkXVar();
        this.xvar_link_msa = model.getLinkMsa();
        this.xvar_link_pdb = model.getLinkPdb();
        this.chr = model.getChr();
        this.start_position = model.getStartPosition();
        this.end_position = model.getEndPosition();
        this.reference_allele = model.getReferenceAllele();
        this.reference_read_count_tumor = model.getTumorRefCount();
        this.variant_read_count_tumor = model.getTumorAltCount();
        this.reference_read_count_normal = model.getNormalRefCount();
        this.variant_read_count_normal = model.getNormalAltCount();
    }
}
