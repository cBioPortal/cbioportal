package org.cbioportal.legacy.service;

import java.util.List;
import org.apache.commons.math3.util.Pair;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.AlterationCountByStructuralVariant;
import org.cbioportal.legacy.model.AlterationFilter;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.cbioportal.legacy.model.GenomicDataCount;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.cbioportal.legacy.model.NamespaceAttribute;
import org.cbioportal.legacy.model.NamespaceDataCountItem;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;

public interface StudyViewService {
  List<GenomicDataCount> getGenomicDataCounts(List<String> studyIds, List<String> sampleIds);

  List<AlterationCountByGene> getMutationAlterationCountByGenes(
      List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilter)
      throws StudyNotFoundException;

  List<GenomicDataCountItem> getMutationCountsByGeneSpecific(
      List<String> studyIds,
      List<String> sampleIds,
      List<Pair<String, String>> genomicDataFilters,
      AlterationFilter annotationFilter);

  List<GenomicDataCountItem> getMutationTypeCountsByGeneSpecific(
      List<String> studyIds, List<String> sampleIds, List<Pair<String, String>> genomicDataFilters);

  List<AlterationCountByGene> getStructuralVariantAlterationCountByGenes(
      List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilter)
      throws StudyNotFoundException;

  List<AlterationCountByStructuralVariant> getStructuralVariantAlterationCounts(
      List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilters);

  List<CopyNumberCountByGene> getCNAAlterationCountByGenes(
      List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilter)
      throws StudyNotFoundException;

  List<GenomicDataCountItem> getCNAAlterationCountsByGeneSpecific(
      List<String> studyIds, List<String> sampleIds, List<Pair<String, String>> genomicDataFilters);

  List<GenericAssayDataCountItem> fetchGenericAssayDataCounts(
      List<String> sampleIds,
      List<String> studyIds,
      List<String> stableIds,
      List<String> profileTypes);

  List<NamespaceDataCountItem> fetchNamespaceDataCounts(
      List<String> studyIds, List<String> sampleIds, List<NamespaceAttribute> namespaceAttributes);
}
