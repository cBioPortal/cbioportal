package org.cbioportal.service.impl.clickhouse;

import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationCountByStructuralVariant;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.GenericAssayDataCountItem;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.service.StudyViewService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.service.impl.mysql.SampleServiceMySQLImpl;
import org.cbioportal.service.impl.mysql.StudyViewServiceMySQLImpl;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public class StudyViewServiceClickHouseImpl extends StudyViewServiceMySQLImpl {
	
	@Autowired
	private MolecularDataRepository molecularDataRepository;

	@Override
	public List<GenomicDataCount> getGenomicDataCounts(StudyViewFilter studyViewFilter,
			boolean singleStudyUnfiltered) {

		return molecularDataRepository.getMolecularProfileSampleCounts(studyViewFilter, singleStudyUnfiltered);
	}

}
