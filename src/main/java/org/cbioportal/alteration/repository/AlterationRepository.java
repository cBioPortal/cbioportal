package org.cbioportal.alteration.repository;

import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.studyview.StudyViewFilterContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AlterationRepository {
    List<AlterationCountByGene> getMutatedGenes(StudyViewFilterContext studyViewFilterContext);
    List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilterContext studyViewFilterContext);
    List<CopyNumberCountByGene> getCnaGenes(StudyViewFilterContext studyViewFilterContext);
    int getTotalProfiledCountsByAlterationType(StudyViewFilterContext studyViewFilterContext, String alterationType);
    Map<String,Integer> getTotalProfiledCounts(StudyViewFilterContext studyViewFilterContext,
                                                        String alterationType, List<MolecularProfile> molecularProfiles);
    Map<String, Set<String>> getMatchingGenePanelIds(StudyViewFilterContext studyViewFilterContext,
                                                     String alterationType);
    int getSampleProfileCountWithoutPanelData(StudyViewFilterContext studyViewFilterContext, String alterationType);

}
