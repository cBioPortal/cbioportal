/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import org.mskcc.cbio.portal.model.GeneticAlterationType;

/**
 *
 * @author abeshoua
 */
public class ProfileDataJSON {
    public Integer genetic_profile_id;
    public GeneticAlterationType genetic_alteration_type;
    public Long entrez_id;
    public String gene;
    public Integer internal_case_id;
    public String profile_data;
}
