package org.cbioportal.service.impl.vs;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.*;
import org.cbioportal.service.StudyViewService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.parameter.VirtualStudy;
import org.cbioportal.web.parameter.VirtualStudySamples;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class VMAwareStudyViewService implements StudyViewService {

    private final StudyViewService studyViewService;
    private final PublishedVirtualStudyService publishedVirtualStudyService;

    public VMAwareStudyViewService(StudyViewService studyViewService, PublishedVirtualStudyService publishedVirtualStudyService) {
        this.studyViewService = studyViewService;
        this.publishedVirtualStudyService = publishedVirtualStudyService;
    }

    @Override
    public List<GenomicDataCount> getGenomicDataCounts(List<String> studyIds, List<String> sampleIds) {
        List<String> harmonisedStudyIds = getMaterialisedStudyIds(studyIds, sampleIds);
        return studyViewService.getGenomicDataCounts(harmonisedStudyIds, sampleIds);
    }

    private List<String> getMaterialisedStudyIds(List<String> studyIds, List<String> sampleIds) {
        Map<String, VirtualStudy> virtualStudiesById = publishedVirtualStudyService.getAllPublishedVirtualStudies().stream().collect(Collectors.toMap(VirtualStudy::getId, Function.identity()));
        return IntStream.range(0, sampleIds.size())
            .mapToObj(pos -> {
                String studyId = studyIds.get(pos);
                if (virtualStudiesById.containsKey(studyId)) {
                    String sampleId = sampleIds.get(pos);
                    VirtualStudy virtualStudy = virtualStudiesById.get(studyId);
                    return virtualStudy.getData().getStudies().stream()
                        .filter(virtualStudySamples -> virtualStudySamples.getSamples().contains(sampleId))
                        .findFirst()
                        .map(VirtualStudySamples::getId).orElse(studyId);
                }
                return studyId;
            }).toList();
    }

    @Override
    public List<AlterationCountByGene> getMutationAlterationCountByGenes(List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilter) throws StudyNotFoundException {
        return studyViewService.getMutationAlterationCountByGenes(getMaterialisedStudyIds(studyIds, sampleIds), sampleIds, annotationFilter);
    }

    @Override
    public List<GenomicDataCountItem> getMutationCountsByGeneSpecific(List<String> studyIds, List<String> sampleIds, List<Pair<String, String>> genomicDataFilters, AlterationFilter annotationFilter) {
        return studyViewService.getMutationTypeCountsByGeneSpecific(getMaterialisedStudyIds(studyIds, sampleIds), sampleIds, genomicDataFilters);
    }

    @Override
    public List<GenomicDataCountItem> getMutationTypeCountsByGeneSpecific(List<String> studyIds, List<String> sampleIds, List<Pair<String, String>> genomicDataFilters) {
        return studyViewService.getMutationTypeCountsByGeneSpecific(getMaterialisedStudyIds(studyIds, sampleIds), sampleIds, genomicDataFilters);
    }

    @Override
    public List<AlterationCountByGene> getStructuralVariantAlterationCountByGenes(List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilter) throws StudyNotFoundException {
        return studyViewService.getStructuralVariantAlterationCountByGenes(getMaterialisedStudyIds(studyIds, sampleIds), sampleIds, annotationFilter);
    }

    @Override
    public List<AlterationCountByStructuralVariant> getStructuralVariantAlterationCounts(List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilters) {
        return studyViewService.getStructuralVariantAlterationCounts(getMaterialisedStudyIds(studyIds, sampleIds), sampleIds, annotationFilters);
    }

    @Override
    public List<CopyNumberCountByGene> getCNAAlterationCountByGenes(List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilter) throws StudyNotFoundException {
        return studyViewService.getCNAAlterationCountByGenes(getMaterialisedStudyIds(studyIds, sampleIds), sampleIds, annotationFilter);
    }

    @Override
    public List<GenomicDataCountItem> getCNAAlterationCountsByGeneSpecific(List<String> studyIds, List<String> sampleIds, List<Pair<String, String>> genomicDataFilters) {
        return studyViewService.getCNAAlterationCountsByGeneSpecific(getMaterialisedStudyIds(studyIds, sampleIds), sampleIds, genomicDataFilters);
    }

    @Override
    public List<GenericAssayDataCountItem> fetchGenericAssayDataCounts(List<String> sampleIds, List<String> studyIds, List<String> stableIds, List<String> profileTypes) {
        return studyViewService.fetchGenericAssayDataCounts(sampleIds, getMaterialisedStudyIds(studyIds, sampleIds), stableIds, profileTypes);
    }
}