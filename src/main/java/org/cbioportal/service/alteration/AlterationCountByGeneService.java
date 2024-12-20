package org.cbioportal.service.alteration;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.StudyViewFilterContext;
import org.cbioportal.service.exception.StudyNotFoundException;

import java.util.List;

public interface AlterationCountByGeneService {
    List<AlterationCountByGene> getMutatedGenes(StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException;
    List<CopyNumberCountByGene> getCnaGenes(StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException;
    List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilterContext studyViewFilterContext) throws StudyNotFoundException;
}
