package org.cbioportal.service;

import org.cbioportal.model.*;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;

import java.util.List;

public interface StudyViewService {
    List<GenomicDataCount> getGenomicDataCounts(List<String> studyIds, List<String> sampleIds);

    List<AlterationCountByGene> getMutationAlterationCountByGenes(List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilter)
        throws StudyNotFoundException;

    List<AlterationCountByGene> getStructuralVariantAlterationCountByGenes(List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilter)
        throws StudyNotFoundException;
    
    List<AlterationCountByStructuralVariant> getStructuralVariantAlterationCounts(List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilters);

    List<CopyNumberCountByGene> getCNAAlterationCountByGenes(List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilter)
        throws StudyNotFoundException;
    
//    List<GenomicDataCount> getCNAAlterationCountsByEvent(List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilter)
//        throws StudyNotFoundException;

    List<GenomicDataCount> getCNAAlterationCountsByEvent(String molecularProfileId, List<Integer> entrezGeneIds,
                                                         List<Integer> alterations) throws MolecularProfileNotFoundException;

    List<GenericAssayDataCountItem> fetchGenericAssayDataCounts(List<String> sampleIds, List<String> studyIds, List<String> stableIds, List<String> profileTypes);

}
