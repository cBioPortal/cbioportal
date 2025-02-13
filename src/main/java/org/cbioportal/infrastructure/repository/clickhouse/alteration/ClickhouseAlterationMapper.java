package org.cbioportal.infrastructure.repository.clickhouse.alteration;

import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.GenePanelToGene;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.persistence.helper.AlterationFilterHelper;
import org.cbioportal.studyview.StudyViewFilterContext;

import java.util.List;

public interface ClickhouseAlterationMapper {
    List<AlterationCountByGene> getMutatedGenes(StudyViewFilterContext studyViewFilterContext, AlterationFilterHelper alterationFilterHelper);

    List<CopyNumberCountByGene> getCnaGenes(StudyViewFilterContext studyViewFilterContext, AlterationFilterHelper alterationFilterHelper);

    List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilterContext studyViewFilterContext,
                                                          AlterationFilterHelper alterationFilterHelper);

    int getTotalProfiledCountByAlterationType(StudyViewFilterContext studyViewFilterContext, String alterationType);
    List<GenePanelToGene> getMatchingGenePanelIds(StudyViewFilterContext studyViewFilterContext, String alterationType);
    List<AlterationCountByGene> getTotalProfiledCounts(StudyViewFilterContext studyViewFilterContext, String alterationType, List<MolecularProfile> molecularProfiles);
    int getSampleProfileCountWithoutPanelData(StudyViewFilterContext studyViewFilterContext, String alterationType);

}
