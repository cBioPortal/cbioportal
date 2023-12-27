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
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public class StudyViewServiceClickHouseImpl implements StudyViewService {
	
	@Autowired
	private MolecularDataRepository molecularDataRepository;

	@Override
	public List<GenomicDataCount> getGenomicDataCounts(List<String> studyIds, List<String> sampleIds) {
		return null;
	}

	@Override
	public List<AlterationCountByGene> getMutationAlterationCountByGenes(List<String> studyIds, List<String> sampleIds,
			AlterationFilter annotationFilter) throws StudyNotFoundException {
		// TODO Auto-generated method stub
		
		return null;
	}

	@Override
	public List<AlterationCountByGene> getStructuralVariantAlterationCountByGenes(List<String> studyIds,
			List<String> sampleIds, AlterationFilter annotationFilter) throws StudyNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AlterationCountByStructuralVariant> getStructuralVariantAlterationCounts(List<String> studyIds,
			List<String> sampleIds, AlterationFilter annotationFilters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CopyNumberCountByGene> getCNAAlterationCountByGenes(List<String> studyIds, List<String> sampleIds,
			AlterationFilter annotationFilter) throws StudyNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GenomicDataCountItem> getCNAAlterationCountsByGeneSpecific(List<String> studyIds,
			List<String> sampleIds, List<Pair<String, String>> genomicDataFilters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GenericAssayDataCountItem> fetchGenericAssayDataCounts(List<String> sampleIds, List<String> studyIds,
			List<String> stableIds, List<String> profileTypes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GenomicDataCount> getGenomicDataCounts(StudyViewFilter studyViewFilter,
			boolean singleStudyUnfiltered) {

		return molecularDataRepository.getMolecularProfileSampleCounts(studyViewFilter, singleStudyUnfiltered);
	}

}
