/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.SNPCount;
import org.cbioportal.persistence.MutationalSignatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MutationalSignatureMyBatisRepository implements MutationalSignatureRepository {
	
	@Autowired
	MutationalSignatureMapper mutationalSignatureMapper;
	
	public List<SNPCount> getSNPCounts(String geneticProfileStableId, List<String> sampleStableIds) {
		return mutationalSignatureMapper.getSNPCountsBySampleId(geneticProfileStableId, sampleStableIds);
	}

	public List<SNPCount> getSNPCounts(String geneticProfileStableId) {
		return mutationalSignatureMapper.getSNPCountsBySampleId(geneticProfileStableId, null);
	}		
}
