package org.cbioportal.service;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface StudyViewService {
    List<GenomicDataCount> getGenomicDataCounts(List<String> studyIds, List<String> sampleIds);

    List<AlterationCountByGene> getMutationAlterationCountByGenes(List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilter)
        throws StudyNotFoundException;

    List<AlterationCountByGene> getStructuralVariantAlterationCountByGenes(List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilter)
        throws StudyNotFoundException;

    List<CopyNumberCountByGene> getCNAAlterationCountByGenes(List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilter)
        throws StudyNotFoundException;
}
