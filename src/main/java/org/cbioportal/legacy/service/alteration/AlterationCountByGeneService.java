package org.cbioportal.legacy.service.alteration;

import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.StudyViewFilterContext;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;

import java.util.List;

public interface AlterationCountByGeneService {
    List<AlterationCountByGene> getMutatedGenes(StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException;
    List<CopyNumberCountByGene> getCnaGenes(StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException;
    List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException;
}
