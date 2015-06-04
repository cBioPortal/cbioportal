/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.service;

import java.util.LinkedList;
import java.util.List;
import org.mskcc.cbio.portal.model.CancerStudyAlterationFrequency;
import org.mskcc.cbio.portal.persistence.CancerStudyAlterationFrequencyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author abeshoua
 */
@Service
public class CancerStudyAlterationFrequencyService 
{
	@Autowired
	private CancerStudyAlterationFrequencyMapper mapper;
  @Transactional
  public List<CancerStudyAlterationFrequency> getMut(List<Long> entrez_gene_ids, List<Integer> internal_study_ids) {
	  return mapper.getMut(entrez_gene_ids, internal_study_ids);
  }
  @Transactional
  public List<CancerStudyAlterationFrequency> getCna(List<Long> entrez_gene_ids, List<Integer> internal_study_ids) {
	  return mapper.getCna(entrez_gene_ids, internal_study_ids);
  }
  @Transactional
  public List<CancerStudyAlterationFrequency> getMutCna(List<Long> entrez_gene_ids, List<Integer> internal_study_ids) {
	  return mapper.getMutCna(entrez_gene_ids, internal_study_ids);
  }
  
  public List<String> getMutationTypes() {
	// TODO: complete
	List<String> types = new LinkedList<>();
	types.add("Missense_Mutation");
	return types;
  }
  
  @Transactional
  public List<CancerStudyAlterationFrequency> getMutByPosition(Long entrez_gene_id, Integer internal_study_id, Integer position) {
	  int numPatients = mapper.getNumPatients(internal_study_id);
	  List<CancerStudyAlterationFrequency> ret = mapper.getMutByPosition(entrez_gene_id, internal_study_id, position);
	  for (CancerStudyAlterationFrequency f: ret) {
		  f.num_patients = numPatients;
	  }
	  return ret;
  }
  @Transactional
  public List<CancerStudyAlterationFrequency> getMutByType(Long entrez_gene_id, Integer internal_study_id, String type) {
	  int numPatients = mapper.getNumPatients(internal_study_id);
	  List<CancerStudyAlterationFrequency> ret = mapper.getMutByType(entrez_gene_id, internal_study_id, type);
	  for (CancerStudyAlterationFrequency f: ret) {
		  f.num_patients = numPatients;
	  }
	  return ret;
  }
  @Transactional
  public List<CancerStudyAlterationFrequency> getMutByPositionAndType(Long entrez_gene_id, Integer internal_study_id, Integer position, String type) {
	  int numPatients = mapper.getNumPatients(internal_study_id);
	  List<CancerStudyAlterationFrequency> ret = mapper.getMutByPositionAndType(entrez_gene_id, internal_study_id, position, type);
	  for (CancerStudyAlterationFrequency f: ret) {
		  f.num_patients = numPatients;
	  }
	  return ret;
  }

}
