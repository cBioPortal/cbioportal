package org.cbioportal.infrastructure.repository.clickhouse.alteration;

import org.cbioportal.domain.alteration.repository.AlterationRepository;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.GenePanelToGene;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.persistence.helper.AlterationFilterHelper;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Profile("clickhouse")
public class ClickhouseAlterationRepository implements AlterationRepository {

    private final ClickhouseAlterationMapper mapper;

    public ClickhouseAlterationRepository(ClickhouseAlterationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getMutatedGenes(studyViewFilterContext,
                AlterationFilterHelper.build(studyViewFilterContext.alterationFilter()));
    }

    @Override
    public List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getStructuralVariantGenes(studyViewFilterContext, AlterationFilterHelper.build(studyViewFilterContext.alterationFilter()));
    }

    @Override
    public List<CopyNumberCountByGene> getCnaGenes(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getCnaGenes(studyViewFilterContext, AlterationFilterHelper.build(studyViewFilterContext.alterationFilter()));
    }

    @Override
    public int getTotalProfiledCountsByAlterationType(StudyViewFilterContext studyViewFilterContext, String alterationType) {
        return mapper.getTotalProfiledCountByAlterationType(studyViewFilterContext, alterationType);
    }

    @Override
    public Map<String, Integer> getTotalProfiledCounts(StudyViewFilterContext studyViewFilterContext, String alterationType, List<MolecularProfile> molecularProfiles) {
        return mapper.getTotalProfiledCounts(studyViewFilterContext,alterationType,molecularProfiles)
                .stream()
                .collect(Collectors.groupingBy(AlterationCountByGene::getHugoGeneSymbol,
                Collectors.mapping(AlterationCountByGene::getNumberOfProfiledCases, Collectors.summingInt(Integer::intValue))));
    }

    @Override
    public Map<String, Set<String>> getMatchingGenePanelIds(StudyViewFilterContext studyViewFilterContext, String alterationType) {
        return mapper.getMatchingGenePanelIds(studyViewFilterContext, alterationType)
                .stream()
                .collect(Collectors.groupingBy(GenePanelToGene::getHugoGeneSymbol,
                        Collectors.mapping(GenePanelToGene::getGenePanelId, Collectors.toSet())));
    }

    @Override
    public int getSampleProfileCountWithoutPanelData(StudyViewFilterContext studyViewFilterContext, String alterationType) {
        return mapper.getSampleProfileCountWithoutPanelData(studyViewFilterContext, alterationType);
    }
}
