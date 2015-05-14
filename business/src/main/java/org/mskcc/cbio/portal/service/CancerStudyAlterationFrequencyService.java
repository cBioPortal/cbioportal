/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.service;

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
	void insert(int cancerStudyId, long entrezGeneId, String freqType, float frequency, int numPatients) {
		mapper.insert(cancerStudyId, entrezGeneId, freqType, frequency, numPatients);
	}
  @Transactional
  public List<CancerStudyAlterationFrequency> get(List<Long> entrezGeneIds, List<Integer> internalStudyIds) {
	  return mapper.get(entrezGeneIds, internalStudyIds);
  }

}
