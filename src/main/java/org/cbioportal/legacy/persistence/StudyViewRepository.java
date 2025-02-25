package org.cbioportal.legacy.persistence;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.CaseListDataCount;
import org.cbioportal.legacy.model.ClinicalAttribute;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.cbioportal.legacy.model.ClinicalEventTypeCount;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.cbioportal.legacy.model.GenomicDataCount;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.PatientTreatment;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.model.SampleTreatment;
import org.cbioportal.legacy.model.StudyViewFilterContext;
import org.cbioportal.legacy.web.parameter.ClinicalDataType;
import org.cbioportal.legacy.web.parameter.GenericAssayDataBinFilter;
import org.cbioportal.legacy.web.parameter.GenericAssayDataFilter;
import org.cbioportal.legacy.web.parameter.GenomicDataBinFilter;
import org.cbioportal.legacy.web.parameter.GenomicDataFilter;

public interface StudyViewRepository {
  List<Sample> getFilteredSamples(StudyViewFilterContext studyViewFilterContext);

  List<String> getFilteredStudyIds(StudyViewFilterContext studyViewFilterContext);

  List<ClinicalData> getSampleClinicalData(
      StudyViewFilterContext studyViewFilterContext, List<String> attributeIds);

  List<ClinicalData> getPatientClinicalData(
      StudyViewFilterContext studyViewFilterContext, List<String> attributeIds);

  List<AlterationCountByGene> getMutatedGenes(StudyViewFilterContext studyViewFilterContext);

  List<AlterationCountByGene> getStructuralVariantGenes(
      StudyViewFilterContext studyViewFilterContext);

  List<CopyNumberCountByGene> getCnaGenes(StudyViewFilterContext studyViewFilterContext);

  List<ClinicalDataCountItem> getClinicalDataCounts(
      StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes);

  List<GenomicDataCount> getMolecularProfileSampleCounts(
      StudyViewFilterContext studyViewFilterContext);

  Map<String, ClinicalDataType> getClinicalAttributeDatatypeMap();

  List<CaseListDataCount> getCaseListDataCountsPerStudy(
      StudyViewFilterContext studyViewFilterContext);

  Map<String, Integer> getTotalProfiledCounts(
      StudyViewFilterContext studyViewFilterContext,
      String alterationType,
      List<MolecularProfile> molecularProfiles);

  List<ClinicalAttribute> getClinicalAttributes();

  List<ClinicalAttribute> getClinicalAttributesForStudies(List<String> studyIds);

  int getFilteredSamplesCount(StudyViewFilterContext studyViewFilterContext);

  int getFilteredPatientCount(StudyViewFilterContext studyViewFilterContext);

  Map<String, Set<String>> getMatchingGenePanelIds(
      StudyViewFilterContext studyViewFilterContext, String alterationType);

  int getTotalProfiledCountsByAlterationType(
      StudyViewFilterContext studyViewFilterContext, String alterationType);

  int getSampleProfileCountWithoutPanelData(
      StudyViewFilterContext studyViewFilterContext, String alterationType);

  List<ClinicalEventTypeCount> getClinicalEventTypeCounts(
      StudyViewFilterContext studyViewFilterContext);

  List<PatientTreatment> getPatientTreatments(StudyViewFilterContext studyViewFilterContext);

  int getTotalPatientTreatmentCount(StudyViewFilterContext studyViewFilterContext);

  List<SampleTreatment> getSampleTreatments(StudyViewFilterContext studyViewFilterContext);

  int getTotalSampleTreatmentCount(StudyViewFilterContext studyViewFilterContext);

  List<GenomicDataCountItem> getCNACounts(
      StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters);

  List<GenericAssayDataCountItem> getGenericAssayDataCounts(
      StudyViewFilterContext studyViewFilterContext,
      List<GenericAssayDataFilter> genericAssayDataFilters);

  Map<String, Integer> getMutationCounts(
      StudyViewFilterContext studyViewFilterContext, GenomicDataFilter genomicDataFilter);

  List<GenomicDataCountItem> getMutationCountsByType(
      StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters);

  List<ClinicalDataCount> getGenomicDataBinCounts(
      StudyViewFilterContext studyViewFilterContext,
      List<GenomicDataBinFilter> genomicDataBinFilters);

  List<ClinicalDataCount> getGenericAssayDataBinCounts(
      StudyViewFilterContext studyViewFilterContext,
      List<GenericAssayDataBinFilter> genericAssayDataBinFilters);

  List<MolecularProfile> getGenericAssayProfiles();

  List<MolecularProfile> getFilteredMolecularProfilesByAlterationType(
      StudyViewFilterContext studyViewFilterContext, String alterationType);
}
