package org.cbioportal.persistence.clickhouse.mapper;

import java.util.List;

import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.web.parameter.StudyViewFilter;

public interface MolecularDataMapper {

	List<GenomicDataCount> getMolecularProfileSampleCounts(StudyViewFilter studyViewFilter,
			boolean singleStudyUnfiltered);

}
