package org.cbioportal.legacy.persistence.mybatis;

import org.cbioportal.legacy.persistence.GeneTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GeneTableMyBatisRepository implements GeneTableRepository {

	@Autowired
	private GeneTableMapper geneTableMapper;

	@Override
	public String getGenetableVersion() {

		String geneTableVersion = geneTableMapper.getGenetableVersion();
		return geneTableVersion;

	}

}
