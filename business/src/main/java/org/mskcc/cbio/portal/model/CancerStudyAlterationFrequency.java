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
public class CancerStudyAlterationFrequency implements Serializable
{
	public Long entrez_gene_id = null;
	public Integer internal_study_id = null;
	public Integer position = null;
	public String mutation_type = null;
	public Integer mut = null;
	public Integer amp = null;
	public Integer del = null;
	public Integer mut_amp = null;
	public Integer mut_del = null;
	public Integer num_patients = null;
}
