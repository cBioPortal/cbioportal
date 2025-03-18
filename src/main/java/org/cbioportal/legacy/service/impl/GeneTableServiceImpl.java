package org.cbioportal.service.impl;

import org.cbioportal.persistence.GeneTableRepository;
import org.cbioportal.service.GeneTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeneTableServiceImpl implements GeneTableService {

	@Autowired
	private GeneTableRepository geneTableRepository;

	@Override
	public String getGenetableVersion() {

		String geneTableVersion = geneTableRepository.getGenetableVersion();
		return geneTableVersion;
	}

}
