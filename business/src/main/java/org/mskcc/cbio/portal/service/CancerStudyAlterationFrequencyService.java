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

/**
 *
 * @author abeshoua
 */
@Service
public class CancerStudyAlterationFrequencyService 
{
	private CancerStudyAlterationFrequencyMapper mapper;
  @Autowired
  public List<CancerStudyAlterationFrequency> get(Long entrezGeneId, List<Integer> internalStudyIds) {
	  return mapper.get(entrezGeneId, internalStudyIds);
  }

}
