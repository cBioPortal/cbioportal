/*
 * Copyright (c) 2019 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cbioportal.legacy.web.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.application.rest.error.ErrorResponse;
import org.cbioportal.legacy.model.AlterationFilter;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.model.SampleList;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.legacy.web.parameter.ClinicalAttributeCountFilter;
import org.cbioportal.legacy.web.parameter.ClinicalDataBinCountFilter;
import org.cbioportal.legacy.web.parameter.ClinicalDataCountFilter;
import org.cbioportal.legacy.web.parameter.ClinicalDataIdentifier;
import org.cbioportal.legacy.web.parameter.ClinicalDataMultiStudyFilter;
import org.cbioportal.legacy.web.parameter.ClinicalEventAttributeRequest;
import org.cbioportal.legacy.web.parameter.GenePanelDataMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.GenericAssayDataBinCountFilter;
import org.cbioportal.legacy.web.parameter.GenericAssayDataCountFilter;
import org.cbioportal.legacy.web.parameter.GenericAssayDataMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.GenomicDataBinCountFilter;
import org.cbioportal.legacy.web.parameter.GenomicDataCountFilter;
import org.cbioportal.legacy.web.parameter.GroupFilter;
import org.cbioportal.legacy.web.parameter.MolecularDataMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.MolecularProfileCasesGroupAndAlterationTypeFilter;
import org.cbioportal.legacy.web.parameter.MolecularProfileCasesGroupFilter;
import org.cbioportal.legacy.web.parameter.MolecularProfileFilter;
import org.cbioportal.legacy.web.parameter.MutationMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.NamespaceAttributeCountFilter;
import org.cbioportal.legacy.web.parameter.NamespaceDataCountFilter;
import org.cbioportal.legacy.web.parameter.PatientFilter;
import org.cbioportal.legacy.web.parameter.PatientIdentifier;
import org.cbioportal.legacy.web.parameter.SampleFilter;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
import org.cbioportal.legacy.web.parameter.StructuralVariantFilter;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.cbioportal.legacy.web.parameter.SurvivalRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

public class InvolvedCancerStudyExtractorInterceptor implements HandlerInterceptor {

  private ObjectMapper objectMapper = new ObjectMapper();

  @Autowired private CacheMapUtil cacheMapUtil;

  private static final Logger LOG =
      LoggerFactory.getLogger(InvolvedCancerStudyExtractorInterceptor.class);
  public static final String PATIENT_FETCH_PATH = "/patients/fetch";
  public static final String SAMPLE_FETCH_PATH = "/samples/fetch";
  public static final String MOLECULAR_PROFILE_FETCH_PATH = "/molecular-profiles/fetch";
  public static final String CLINICAL_ATTRIBUTE_COUNT_FETCH_PATH =
      "/clinical-attributes/counts/fetch";
  public static final String NAMESPACE_ATTRIBUTE_COUNT_FETCH_PATH =
      "/namespace-attributes/counts/fetch";
  public static final String CLINICAL_DATA_FETCH_PATH = "/clinical-data/fetch";
  public static final String GENE_PANEL_DATA_FETCH_PATH = "/gene-panel-data/fetch";
  public static final String MOLECULAR_DATA_MULTIPLE_STUDY_FETCH_PATH = "/molecular-data/fetch";
  public static final String MUTATION_MULTIPLE_STUDY_FETCH_PATH = "/mutations/fetch";
  public static final String COPY_NUMBER_SEG_FETCH_PATH = "/copy-number-segments/fetch";
  public static final String STUDY_VIEW_CLINICAL_DATA_BIN_COUNTS_PATH =
      "/clinical-data-bin-counts/fetch";
  public static final String STUDY_VIEW_CUSTOM_DATA_BIN_COUNTS_PATH =
      "/custom-data-bin-counts/fetch";
  public static final String STUDY_VIEW_GENOMICL_DATA_BIN_COUNTS_PATH =
      "/genomic-data-bin-counts/fetch";
  public static final String STUDY_VIEW_GENOMICL_DATA_COUNTS_PATH = "/genomic-data-counts/fetch";
  public static final String STUDY_VIEW_MUTATION_DATA_COUNTS_PATH = "/mutation-data-counts/fetch";
  public static final String STUDY_VIEW_GENERIC_ASSAY_DATA_BIN_COUNTS_PATH =
      "/generic-assay-data-bin-counts/fetch";
  public static final String STUDY_VIEW_GENERIC_ASSAY_DATA_COUNTS_PATH =
      "/generic-assay-data-counts/fetch";
  public static final String STUDY_VIEW_CLINICAL_DATA_COUNTS_PATH = "/clinical-data-counts/fetch";
  public static final String STUDY_VIEW_NAMESPACE_DATA_COUNTS_PATH = "/namespace-data-counts/fetch";
  public static final String STUDY_VIEW_CUSTOM_DATA_COUNTS_PATH = "/custom-data-counts/fetch";
  public static final String STUDY_VIEW_CLINICAL_DATA_DENSITY_PATH =
      "/clinical-data-density-plot/fetch";
  public static final String STUDY_VIEW_CLINICAL_DATA_VIOLIN_PATH =
      "/clinical-data-violin-plots/fetch";
  public static final String STUDY_VIEW_CNA_GENES = "/cna-genes/fetch";
  public static final String STUDY_VIEW_FILTERED_SAMPLES = "/filtered-samples/fetch";
  public static final String STUDY_VIEW_MUTATED_GENES = "/mutated-genes/fetch";
  public static final String STUDY_VIEW_STRUCTURAL_VARIANT_GENES = "/structuralvariant-genes/fetch";
  public static final String STUDY_VIEW_STRUCTURAL_VARIANT_COUNTS =
      "/structuralvariant-counts/fetch";
  public static final String STUDY_VIEW_SAMPLE_COUNTS = "/sample-counts/fetch";
  public static final String STUDY_VIEW_SAMPLE_LIST_COUNTS_PATH = "/sample-lists-counts/fetch";
  public static final String STUDY_VIEW_CLINICAL_TABLE_DATA_FETCH_PATH =
      "/clinical-data-table/fetch";
  public static final String STUDY_VIEW_PROFILE_SAMPLE_COUNTS_PATH =
      "/molecular-profile-sample-counts/fetch";
  public static final String CLINICAL_DATA_ENRICHMENT_FETCH_PATH =
      "/clinical-data-enrichments/fetch";
  public static final String MUTATION_ENRICHMENT_FETCH_PATH = "/mutation-enrichments/fetch";
  public static final String COPY_NUMBER_ENRICHMENT_FETCH_PATH = "/copy-number-enrichments/fetch";
  public static final String EXPRESSION_ENRICHMENT_FETCH_PATH = "/expression-enrichments/fetch";
  public static final String ALTERATION_ENRICHMENT_FETCH_PATH = "/alteration-enrichments/fetch";
  public static final String TREATMENT_FETCH_PATH = "/treatments/fetch";
  public static final String STRUCTURAL_VARIANT_FETCH_PATH = "/structural-variant/fetch";
  public static final String GENERIC_ASSAY_DATA_MULTIPLE_STUDY_FETCH_PATH =
      "/generic_assay_data/fetch";
  public static final String TREATMENTS_PATIENT_PATH = "/treatments/patient";
  public static final String TREATMENTS_SAMPLE_PATH = "/treatments/sample";
  public static final String GENERIC_ASSAY_ENRICHMENT_FETCH_PATH =
      "/generic-assay-enrichments/fetch";
  public static final String GENERIC_ASSAY_CATEGORICAL_ENRICHMENT_FETCH_PATH =
      "/generic-assay-categorical-enrichments/fetch";
  public static final String GENERIC_ASSAY_BINARY_ENRICHMENT_FETCH_PATH =
      "/generic-assay-binary-enrichments/fetch";
  public static final String CLINICAL_EVENT_TYPE_COUNT_FETCH_PATH =
      "/clinical-event-type-counts/fetch";
  public static final String SURVIVAL_DATA_FETCH_PATH = "/survival-data/fetch";
  public static final String CLINICAL_EVENT_META_FETCH_PATH = "/clinical-events-meta/fetch";
  public static final String TREATMENTS_PATIENT_COUNT_FETCH_PATH =
      "/treatments/patient-counts/fetch";
  public static final String TREATMENTS_SAMPLE_COUNT_FETCH_PATH = "/treatments/sample-counts/fetch";

  /**
   * Sends an HTTP 400 Bad Request response with a JSON error message.
   *
   * @param response the HttpServletResponse object
   * @param message the error message to include in the response body
   */
  private void sendBadRequestResponse(HttpServletResponse response, String message) {
    if (response.isCommitted()) {
      LOG.warn("Cannot send error response - response already committed");
      return;
    }

    try {
      // Null-safe handling for message (Exception.getMessage() can return null)
      String safeMessage = message == null ? "Unknown error" : message;

      // Truncate message before Jackson source location info if present
      int sourceIndex = safeMessage.indexOf(" at [Source:");
      if (sourceIndex != -1) {
        safeMessage = safeMessage.substring(0, sourceIndex);
      }

      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");

      // Use ObjectMapper for proper JSON escaping (matches GlobalExceptionHandler pattern)
      ErrorResponse errorResponse = new ErrorResponse("Invalid request body: " + safeMessage);
      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
      response.getWriter().flush();
    } catch (IOException ex) {
      LOG.error("Failed to write error response: {}", ex.getMessage());
    }
  }

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (!request.getMethod().equals("POST")) {
      return true; // no attribute extraction needed because all user supplied filter objects are in
      // POST requests
    }
    // TODO when reimplemeting different dispatcherservlets with different context roots
    // reset this to  'String requestPathInfo = request.getPathInfo();'
    String requestPathInfo =
        request.getPathInfo() == null ? request.getServletPath() : request.getPathInfo();
    requestPathInfo = requestPathInfo.replaceFirst("^/api", "");
    // requestPathInfo = StringUtils.removeStart(requestPathInfo, "/column-store");
    if (requestPathInfo.equals(PATIENT_FETCH_PATH)) {
      return extractAttributesFromPatientFilter(request, response);
    } else if (requestPathInfo.equals(SAMPLE_FETCH_PATH)) {
      return extractAttributesFromSampleFilter(request, response);
    } else if (requestPathInfo.equals(MOLECULAR_PROFILE_FETCH_PATH)) {
      return extractAttributesFromMolecularProfileFilter(request, response);
    } else if (requestPathInfo.equals(CLINICAL_ATTRIBUTE_COUNT_FETCH_PATH)) {
      return extractAttributesFromClinicalAttributeCountFilter(request, response);
    } else if (requestPathInfo.equals(NAMESPACE_ATTRIBUTE_COUNT_FETCH_PATH)) {
      return extractAttributesFromNamespaceAttributeCountFilter(request, response);
    } else if (requestPathInfo.equals(CLINICAL_DATA_FETCH_PATH)) {
      return extractAttributesFromClinicalDataMultiStudyFilter(request, response);
    } else if (requestPathInfo.equals(GENE_PANEL_DATA_FETCH_PATH)) {
      return extractAttributesFromGenePanelDataMultipleStudyFilter(request, response);
    } else if (requestPathInfo.equals(MOLECULAR_DATA_MULTIPLE_STUDY_FETCH_PATH)) {
      return extractAttributesFromMolecularDataMultipleStudyFilter(request, response);
    } else if (requestPathInfo.equals(MUTATION_MULTIPLE_STUDY_FETCH_PATH)) {
      return extractAttributesFromMutationMultipleStudyFilter(request, response);
    } else if (requestPathInfo.equals(COPY_NUMBER_SEG_FETCH_PATH)) {
      return extractAttributesFromSampleIdentifiers(request, response);
    } else if (Arrays.asList(
            STUDY_VIEW_CLINICAL_DATA_BIN_COUNTS_PATH, STUDY_VIEW_CUSTOM_DATA_BIN_COUNTS_PATH)
        .contains(requestPathInfo)) {
      return extractAttributesFromClinicalDataBinCountFilter(request, response);
    } else if (requestPathInfo.equals(STUDY_VIEW_GENOMICL_DATA_BIN_COUNTS_PATH)) {
      return extractAttributesFromGenomicDataBinCountFilter(request, response);
    } else if (Arrays.asList(
            STUDY_VIEW_GENOMICL_DATA_COUNTS_PATH, STUDY_VIEW_MUTATION_DATA_COUNTS_PATH)
        .contains(requestPathInfo)) {
      return extractAttributesFromGenomicDataCountFilter(request, response);
    } else if (requestPathInfo.equals(STUDY_VIEW_GENERIC_ASSAY_DATA_BIN_COUNTS_PATH)) {
      return extractAttributesFromGenericAssayDataBinCountFilter(request, response);
    } else if (requestPathInfo.equals(STUDY_VIEW_GENERIC_ASSAY_DATA_COUNTS_PATH)) {
      return extractAttributesFromGenericAssayDataCountFilter(request, response);
    } else if (Arrays.asList(
            STUDY_VIEW_CLINICAL_DATA_COUNTS_PATH, STUDY_VIEW_CUSTOM_DATA_COUNTS_PATH)
        .contains(requestPathInfo)) {
      return extractAttributesFromClinicalDataCountFilter(request, response);
    } else if (requestPathInfo.equals(STUDY_VIEW_NAMESPACE_DATA_COUNTS_PATH)) {
      return extractAttributesFromNamespaceDataCountFilter(request, response);
    } else if (Arrays.asList(
            STUDY_VIEW_CLINICAL_DATA_DENSITY_PATH,
            STUDY_VIEW_CLINICAL_DATA_VIOLIN_PATH,
            STUDY_VIEW_CNA_GENES,
            STUDY_VIEW_FILTERED_SAMPLES,
            STUDY_VIEW_MUTATED_GENES,
            STUDY_VIEW_STRUCTURAL_VARIANT_GENES,
            STUDY_VIEW_STRUCTURAL_VARIANT_COUNTS,
            STUDY_VIEW_SAMPLE_COUNTS,
            STUDY_VIEW_SAMPLE_LIST_COUNTS_PATH,
            STUDY_VIEW_CLINICAL_TABLE_DATA_FETCH_PATH,
            TREATMENTS_PATIENT_PATH,
            TREATMENTS_SAMPLE_PATH,
            STUDY_VIEW_PROFILE_SAMPLE_COUNTS_PATH,
            CLINICAL_EVENT_TYPE_COUNT_FETCH_PATH,
            TREATMENTS_PATIENT_COUNT_FETCH_PATH,
            TREATMENTS_SAMPLE_COUNT_FETCH_PATH)
        .contains(requestPathInfo)) {
      return extractAttributesFromStudyViewFilter(request, response);
    } else if (requestPathInfo.equals(CLINICAL_DATA_ENRICHMENT_FETCH_PATH)) {
      return extractAttributesFromGroupFilter(request, response);
    } else if (requestPathInfo.equals(MUTATION_ENRICHMENT_FETCH_PATH)
        || requestPathInfo.equals(COPY_NUMBER_ENRICHMENT_FETCH_PATH)
        || requestPathInfo.equals(EXPRESSION_ENRICHMENT_FETCH_PATH)
        || requestPathInfo.equals(GENERIC_ASSAY_ENRICHMENT_FETCH_PATH)
        || requestPathInfo.equals(GENERIC_ASSAY_CATEGORICAL_ENRICHMENT_FETCH_PATH)
        || requestPathInfo.equals(GENERIC_ASSAY_BINARY_ENRICHMENT_FETCH_PATH)) {
      return extractAttributesFromMolecularProfileCasesGroups(request, response);
    } else if (requestPathInfo.equals(ALTERATION_ENRICHMENT_FETCH_PATH)) {
      return extractAttributesFromMolecularProfileCasesGroupsAndAlterationTypes(request, response);
    } else if (requestPathInfo.equals(STRUCTURAL_VARIANT_FETCH_PATH)) {
      return extractAttributesFromStructuralVariantFilter(request, response);
    } else if (requestPathInfo.equals(GENERIC_ASSAY_DATA_MULTIPLE_STUDY_FETCH_PATH)) {
      return extractAttributesFromGenericAssayDataMultipleStudyFilter(request, response);
    } else if (requestPathInfo.equals(SURVIVAL_DATA_FETCH_PATH)) {
      return extractCancerStudyIdsFromSurvivalRequest(request, response);
    } else if (requestPathInfo.equals(CLINICAL_EVENT_META_FETCH_PATH)) {
      return extractCancerStudyIdsFromClinicalEventAttributeRequest(request, response);
    }
    return true;
  }

  private boolean extractAttributesFromPatientFilter(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      PatientFilter patientFilter =
          objectMapper.readValue(request.getInputStream(), PatientFilter.class);
      LOG.debug("extracted patientFilter: {}", patientFilter);
      LOG.debug("setting interceptedPatientFilter to {}", patientFilter);
      request.setAttribute("interceptedPatientFilter", patientFilter);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromPatientFilter(patientFilter);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error("exception thrown during extraction of patientFilter: {}", e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private Collection<String> extractCancerStudyIdsFromPatientFilter(PatientFilter patientFilter) {
    // use hashset as the study list in the patientFilter will usually be populated with many
    // duplicate values
    Set<String> studyIdSet = new HashSet<>();
    if (patientFilter.getPatientIdentifiers() != null) {
      for (PatientIdentifier patientIdentifier : patientFilter.getPatientIdentifiers()) {
        studyIdSet.add(patientIdentifier.getStudyId());
      }
    } else {
      UniqueKeyExtractor.extractUniqueKeys(patientFilter.getUniquePatientKeys(), studyIdSet);
    }
    return studyIdSet;
  }

  private boolean extractAttributesFromSampleFilter(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      SampleFilter sampleFilter =
          objectMapper.readValue(request.getInputStream(), SampleFilter.class);
      LOG.debug("extracted sampleFilter: {}", sampleFilter);
      LOG.debug("setting interceptedSampleFilter to {}", sampleFilter);
      request.setAttribute("interceptedSampleFilter", sampleFilter);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromSampleFilter(sampleFilter);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error("exception thrown during extraction of sampleFilter: {}", e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private Collection<String> extractCancerStudyIdsFromSampleFilter(SampleFilter sampleFilter) {
    // use hashset as the study list in the sampleFilter will usually be populated with many
    // duplicate values
    Set<String> studyIdSet = new HashSet<>();
    if (sampleFilter.getSampleListIds() != null) {
      extractCancerStudyIdsFromSampleListIds(sampleFilter.getSampleListIds(), studyIdSet);
    } else if (sampleFilter.getSampleIdentifiers() != null) {
      extractCancerStudyIdsFromSampleIdentifiers(sampleFilter.getSampleIdentifiers(), studyIdSet);
    } else {
      UniqueKeyExtractor.extractUniqueKeys(sampleFilter.getUniqueSampleKeys(), studyIdSet);
    }
    return studyIdSet;
  }

  private boolean extractAttributesFromMolecularProfileFilter(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      MolecularProfileFilter molecularProfileFilter =
          objectMapper.readValue(request.getInputStream(), MolecularProfileFilter.class);
      LOG.debug("extracted molecularProfileFilter: {}", molecularProfileFilter);
      LOG.debug("setting interceptedMolecularProfileFilter to {}", molecularProfileFilter);
      request.setAttribute("interceptedMolecularProfileFilter", molecularProfileFilter);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromMolecularProfileFilter(molecularProfileFilter);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error("exception thrown during extraction of molecularProfileFilter: {}", e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private Collection<String> extractCancerStudyIdsFromMolecularProfileFilter(
      MolecularProfileFilter molecularProfileFilter) {
    // use hashset as the study list in the molecularProfileFilter may be populated with many
    // duplicate values
    Set<String> studyIdSet = new HashSet<>();
    if (molecularProfileFilter.getStudyIds() != null) {
      studyIdSet.addAll(molecularProfileFilter.getStudyIds());
    } else {
      extractCancerStudyIdsFromMolecularProfileIds(
          molecularProfileFilter.getMolecularProfileIds(), studyIdSet);
    }
    return studyIdSet;
  }

  private boolean extractAttributesFromClinicalAttributeCountFilter(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      ClinicalAttributeCountFilter clinicalAttributeCountFilter =
          objectMapper.readValue(request.getInputStream(), ClinicalAttributeCountFilter.class);
      LOG.debug("extracted clinicalAttributeCountFilter: {}", clinicalAttributeCountFilter);
      LOG.debug(
          "setting interceptedClinicalAttributeCountFilter to {}", clinicalAttributeCountFilter);
      request.setAttribute("interceptedClinicalAttributeCountFilter", clinicalAttributeCountFilter);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromClinicalAttributeCountFilter(clinicalAttributeCountFilter);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error(
          "exception thrown during extraction of clinicalAttributeCountFilter: {}", e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private Collection<String> extractCancerStudyIdsFromClinicalAttributeCountFilter(
      ClinicalAttributeCountFilter clinicalAttributeCountFilter) {
    // use hashset as the study list in the clinicalAttributeCountFilter may be populated with many
    // duplicate values
    Set<String> studyIdSet = new HashSet<>();
    if (clinicalAttributeCountFilter.getSampleListId() != null) {
      extractCancerStudyIdsFromSampleListIds(
          Collections.singletonList(clinicalAttributeCountFilter.getSampleListId()), studyIdSet);
    } else {
      extractCancerStudyIdsFromSampleIdentifiers(
          clinicalAttributeCountFilter.getSampleIdentifiers(), studyIdSet);
    }
    return studyIdSet;
  }

  private boolean extractAttributesFromNamespaceAttributeCountFilter(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      NamespaceAttributeCountFilter namespaceAttributeCountFilter =
          objectMapper.readValue(request.getInputStream(), NamespaceAttributeCountFilter.class);
      LOG.debug("extracted namespaceAttributeCountFilter: {}", namespaceAttributeCountFilter);
      LOG.debug(
          "setting interceptedNamespaceAttributeCountFilter to {}", namespaceAttributeCountFilter);
      request.setAttribute(
          "interceptedNamespaceAttributeCountFilter", namespaceAttributeCountFilter);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromNamespaceAttributeCountFilter(namespaceAttributeCountFilter);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error(
          "exception thrown during extraction of namespaceAttributeCountFilter: {}",
          e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private Collection<String> extractCancerStudyIdsFromNamespaceAttributeCountFilter(
      NamespaceAttributeCountFilter namespaceAttributeCountFilter) {
    // use hashset as the study list in the namespaceAttributeCountFilter may be populated with many
    // duplicate values
    Set<String> studyIdSet = new HashSet<>();
    extractCancerStudyIdsFromSampleIdentifiers(
        namespaceAttributeCountFilter.getSampleIdentifiers(), studyIdSet);
    return studyIdSet;
  }

  private boolean extractAttributesFromClinicalDataMultiStudyFilter(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      ClinicalDataMultiStudyFilter clinicalDataMultiStudyFilter =
          objectMapper.readValue(request.getInputStream(), ClinicalDataMultiStudyFilter.class);
      LOG.debug("extracted clinicalDataMultiStudyFilter: {}", clinicalDataMultiStudyFilter);
      LOG.debug(
          "setting interceptedClinicalDataMultiStudyFilter to {}", clinicalDataMultiStudyFilter);
      request.setAttribute("interceptedClinicalDataMultiStudyFilter", clinicalDataMultiStudyFilter);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromClinicalDataMultiStudyFilter(clinicalDataMultiStudyFilter);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error(
          "exception thrown during extraction of clinicalDataMultiStudyFilter: {}", e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private Collection<String> extractCancerStudyIdsFromClinicalDataMultiStudyFilter(
      ClinicalDataMultiStudyFilter clinicalDataMultiStudyFilter) {
    // use hashset as the study list in the clinicalDataMultiStudyFilter may be populated with many
    // duplicate values
    Set<String> studyIdSet = new HashSet<>();
    for (ClinicalDataIdentifier clinicalDataIdentifier :
        clinicalDataMultiStudyFilter.getIdentifiers()) {
      studyIdSet.add(clinicalDataIdentifier.getStudyId());
    }
    return studyIdSet;
  }

  private boolean extractAttributesFromGenePanelDataMultipleStudyFilter(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      GenePanelDataMultipleStudyFilter genePanelDataMultipleStudyFilter =
          objectMapper.readValue(request.getInputStream(), GenePanelDataMultipleStudyFilter.class);
      LOG.debug("extracted genePanelDataMultipleStudyFilter: {}", genePanelDataMultipleStudyFilter);
      LOG.debug(
          "setting interceptedGenePanelDataMultipleStudyFilter to {}",
          genePanelDataMultipleStudyFilter);
      request.setAttribute(
          "interceptedGenePanelDataMultipleStudyFilter", genePanelDataMultipleStudyFilter);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromGenePanelDataMultipleStudyFilter(
                genePanelDataMultipleStudyFilter);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error(
          "exception thrown during extraction of genePanelSampleMolecularIdentifiers: {}",
          e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private Collection<String> extractCancerStudyIdsFromGenePanelDataMultipleStudyFilter(
      GenePanelDataMultipleStudyFilter genePanelDataMultipleStudyFilter) {
    Set<String> studyIdSet = new HashSet<>();
    if (genePanelDataMultipleStudyFilter.getMolecularProfileIds() != null) {
      extractCancerStudyIdsFromMolecularProfileIds(
          genePanelDataMultipleStudyFilter.getMolecularProfileIds(), studyIdSet);
    } else {
      extractCancerStudyIdsFromSampleMolecularIdentifiers(
          genePanelDataMultipleStudyFilter.getSampleMolecularIdentifiers(), studyIdSet);
    }
    return studyIdSet;
  }

  private boolean extractAttributesFromMolecularDataMultipleStudyFilter(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      MolecularDataMultipleStudyFilter molecularDataMultipleStudyFilter =
          objectMapper.readValue(request.getInputStream(), MolecularDataMultipleStudyFilter.class);
      LOG.debug("extracted molecularDataMultipleStudyFilter: {}", molecularDataMultipleStudyFilter);
      LOG.debug(
          "setting interceptedMolecularDataMultipleStudyFilter to {}",
          molecularDataMultipleStudyFilter);
      request.setAttribute(
          "interceptedMolecularDataMultipleStudyFilter", molecularDataMultipleStudyFilter);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromMolecularDataMultipleStudyFilter(
                molecularDataMultipleStudyFilter);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error(
          "exception thrown during extraction of molecularDataMultipleStudyFilter: {}",
          e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private Collection<String> extractCancerStudyIdsFromMolecularDataMultipleStudyFilter(
      MolecularDataMultipleStudyFilter molecularDataMultipleStudyFilter) {
    Set<String> studyIdSet = new HashSet<>();
    if (molecularDataMultipleStudyFilter.getMolecularProfileIds() != null) {
      extractCancerStudyIdsFromMolecularProfileIds(
          molecularDataMultipleStudyFilter.getMolecularProfileIds(), studyIdSet);
    } else {
      extractCancerStudyIdsFromSampleMolecularIdentifiers(
          molecularDataMultipleStudyFilter.getSampleMolecularIdentifiers(), studyIdSet);
    }
    return studyIdSet;
  }

  private boolean extractAttributesFromGenericAssayDataMultipleStudyFilter(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      GenericAssayDataMultipleStudyFilter genericAssayDataMultipleStudyFilter =
          objectMapper.readValue(
              request.getInputStream(), GenericAssayDataMultipleStudyFilter.class);
      LOG.debug(
          "extracted genericAssayDataMultipleStudyFilter: {}", genericAssayDataMultipleStudyFilter);
      LOG.debug(
          "setting interceptedGenericAssayDataMultipleStudyFilter to {}",
          genericAssayDataMultipleStudyFilter);
      request.setAttribute(
          "interceptedGenericAssayDataMultipleStudyFilter", genericAssayDataMultipleStudyFilter);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromGenericAssayDataMultipleStudyFilter(
                genericAssayDataMultipleStudyFilter);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error(
          "exception thrown during extraction of genericAssayDataMultipleStudyFilter: {}",
          e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private Collection<String> extractCancerStudyIdsFromGenericAssayDataMultipleStudyFilter(
      GenericAssayDataMultipleStudyFilter genericAssayDataMultipleStudyFilter) {
    Set<String> studyIdSet = new HashSet<>();
    if (genericAssayDataMultipleStudyFilter.getMolecularProfileIds() != null) {
      extractCancerStudyIdsFromMolecularProfileIds(
          genericAssayDataMultipleStudyFilter.getMolecularProfileIds(), studyIdSet);
    } else {
      extractCancerStudyIdsFromSampleMolecularIdentifiers(
          genericAssayDataMultipleStudyFilter.getSampleMolecularIdentifiers(), studyIdSet);
    }
    return studyIdSet;
  }

  private boolean extractAttributesFromMutationMultipleStudyFilter(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      MutationMultipleStudyFilter mutationMultipleStudyFilter =
          objectMapper.readValue(request.getInputStream(), MutationMultipleStudyFilter.class);
      LOG.debug("extracted mutationMultipleStudyFilter: {}", mutationMultipleStudyFilter);
      LOG.debug(
          "setting interceptedMutationMultipleStudyFilter to {}", mutationMultipleStudyFilter);
      request.setAttribute("interceptedMutationMultipleStudyFilter", mutationMultipleStudyFilter);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromMutationMultipleStudyFilter(mutationMultipleStudyFilter);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error(
          "exception thrown during extraction of mutationMultipleStudyFilter: {}", e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private Set<String> extractCancerStudyIdsFromMutationMultipleStudyFilter(
      MutationMultipleStudyFilter mutationMultipleStudyFilter) {
    Set<String> studyIdSet = new HashSet<>();
    if (mutationMultipleStudyFilter.getMolecularProfileIds() != null) {
      extractCancerStudyIdsFromMolecularProfileIds(
          mutationMultipleStudyFilter.getMolecularProfileIds(), studyIdSet);
    } else {
      extractCancerStudyIdsFromSampleMolecularIdentifiers(
          mutationMultipleStudyFilter.getSampleMolecularIdentifiers(), studyIdSet);
    }
    return studyIdSet;
  }

  private boolean extractAttributesFromSampleIdentifiers(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      List<SampleIdentifier> sampleIdentifiers =
          Arrays.asList(objectMapper.readValue(request.getInputStream(), SampleIdentifier[].class));
      LOG.debug("extracted sampleIdentifiers: {}", sampleIdentifiers);
      LOG.debug("setting interceptedSampleIdentifiers to {}", sampleIdentifiers);
      request.setAttribute("interceptedSampleIdentifiers", sampleIdentifiers);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromSampleIdentifiers(sampleIdentifiers);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error("exception thrown during extraction of sampleIdentifiers: {}", e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private boolean extractAttributesFromClinicalDataBinCountFilter(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      ClinicalDataBinCountFilter clinicalDataBinCountFilter =
          objectMapper.readValue(request.getInputStream(), ClinicalDataBinCountFilter.class);
      LOG.debug("extracted clinicalDataBinCountFilter: {}", clinicalDataBinCountFilter);
      LOG.debug("setting interceptedClinicalDataBinCountFilter to {}", clinicalDataBinCountFilter);
      request.setAttribute("interceptedClinicalDataBinCountFilter", clinicalDataBinCountFilter);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromClinicalDataBinCountFilter(clinicalDataBinCountFilter);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error(
          "exception thrown during extraction of clinicalDataBinCountFilter: {}", e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private boolean extractAttributesFromGenomicDataBinCountFilter(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      GenomicDataBinCountFilter genomicDataBinCountFilter =
          objectMapper.readValue(request.getInputStream(), GenomicDataBinCountFilter.class);
      LOG.debug("extracted genomicDataBinCountFilter: {}", genomicDataBinCountFilter);
      LOG.debug("setting interceptedGenomicDataBinCountFilter to {}", genomicDataBinCountFilter);
      request.setAttribute("interceptedGenomicDataBinCountFilter", genomicDataBinCountFilter);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromGenomicDataBinCountFilter(genomicDataBinCountFilter);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error(
          "exception thrown during extraction of genomicDataBinCountFilter: {}", e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private boolean extractAttributesFromGenomicDataCountFilter(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      GenomicDataCountFilter genomicDataCountFilter =
          objectMapper.readValue(request.getInputStream(), GenomicDataCountFilter.class);
      LOG.debug("extracted genomicDataCountFilter: {}", genomicDataCountFilter);
      LOG.debug("setting interceptedGenomicDataCountFilter to {}", genomicDataCountFilter);
      request.setAttribute("interceptedGenomicDataCountFilter", genomicDataCountFilter);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromGenomicDataCountFilter(genomicDataCountFilter);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error("exception thrown during extraction of genomicDataCountFilter: {}", e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private boolean extractAttributesFromGenericAssayDataBinCountFilter(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      GenericAssayDataBinCountFilter genericAssayDataBinCountFilter =
          objectMapper.readValue(request.getInputStream(), GenericAssayDataBinCountFilter.class);
      LOG.debug("extracted genericAssayDataBinCountFilter: {}", genericAssayDataBinCountFilter);
      LOG.debug(
          "setting interceptedGenericAssayDataBinCountFilter to {}",
          genericAssayDataBinCountFilter);
      request.setAttribute(
          "interceptedGenericAssayDataBinCountFilter", genericAssayDataBinCountFilter);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromGenericAssayDataBinCountFilter(genericAssayDataBinCountFilter);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error(
          "exception thrown during extraction of genericAssayDataBinCountFilter: {}",
          e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private boolean extractAttributesFromGenericAssayDataCountFilter(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      GenericAssayDataCountFilter genericAssayDataCountFilter =
          objectMapper.readValue(request.getInputStream(), GenericAssayDataCountFilter.class);
      LOG.debug("extracted genericAssayDataCountFilter: {}", genericAssayDataCountFilter);
      LOG.debug(
          "setting interceptedGenericAssayDataCountFilter to {}", genericAssayDataCountFilter);
      request.setAttribute("interceptedGenericAssayDataCountFilter", genericAssayDataCountFilter);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromGenericAssayDataCountFilter(genericAssayDataCountFilter);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error(
          "exception thrown during extraction of genericAssayDataCountFilter: {}", e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private boolean extractAttributesFromClinicalDataCountFilter(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      ClinicalDataCountFilter clinicalDataCountFilter =
          objectMapper.readValue(request.getInputStream(), ClinicalDataCountFilter.class);
      LOG.debug("extracted clinicalDataCountFilter: {}", clinicalDataCountFilter);
      LOG.debug("setting interceptedClinicalDataCountFilter to {}", clinicalDataCountFilter);
      request.setAttribute("interceptedClinicalDataCountFilter", clinicalDataCountFilter);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromClinicalDataCountFilter(clinicalDataCountFilter);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error(
          "exception thrown during extraction of clinicalDataCountFilter: {}", e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private boolean extractAttributesFromNamespaceDataCountFilter(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      NamespaceDataCountFilter namespaceDataCountFilter =
          objectMapper.readValue(request.getInputStream(), NamespaceDataCountFilter.class);
      LOG.debug("extracted namespaceDataCountFilter: {}", namespaceDataCountFilter);
      LOG.debug("setting interceptedNamespaceDataCountFilter to {}", namespaceDataCountFilter);
      request.setAttribute("interceptedNamespaceDataCountFilter", namespaceDataCountFilter);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromNamespaceDataCountFilter(namespaceDataCountFilter);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error(
          "exception thrown during extraction of namespaceDataCountFilter: {}", e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private boolean extractAttributesFromGroupFilter(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      GroupFilter groupFilter = objectMapper.readValue(request.getInputStream(), GroupFilter.class);
      LOG.debug("extracted groupFilter: {}", groupFilter);
      LOG.debug("setting interceptedGroupFilter to {}", groupFilter);
      request.setAttribute("interceptedGroupFilter", groupFilter);
      if (cacheMapUtil.hasCacheEnabled()) {
        List<SampleIdentifier> sampleIdentifiers =
            groupFilter.getGroups().stream()
                .flatMap(group -> group.getSampleIdentifiers().stream())
                .collect(Collectors.toList());
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromSampleIdentifiers(sampleIdentifiers);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error("exception thrown during extraction of groupFilter: {}", e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private boolean extractAttributesFromStudyViewFilter(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      StudyViewFilter studyViewFilter =
          objectMapper.readValue(request.getInputStream(), StudyViewFilter.class);
      if (studyViewFilter.getAlterationFilter() == null) {
        // For backwards compatibility an inactive filter is set
        // when the AlterationFilter is not part of the request.
        studyViewFilter.setAlterationFilter(new AlterationFilter());
      }
      if (studyViewFilter.getStructuralVariantFilters() == null) {
        // For backwards compatibility an inactive filter is set
        // when the StructuralVariantFilters are not part of the request.
        studyViewFilter.setStructuralVariantFilters(new ArrayList<>());
      }
      LOG.debug("extracted studyViewFilter: {}", studyViewFilter);
      LOG.debug("setting interceptedStudyViewFilter to {}", studyViewFilter);
      request.setAttribute("interceptedStudyViewFilter", studyViewFilter);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromStudyViewFilter(studyViewFilter);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error("exception thrown during extraction of studyViewFilter: {}", e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private boolean extractAttributesFromMolecularProfileCasesGroups(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      List<MolecularProfileCasesGroupFilter> molecularProfileCasesGroupFilters =
          Arrays.asList(
              objectMapper.readValue(
                  request.getInputStream(), MolecularProfileCasesGroupFilter[].class));
      LOG.debug(
          "extracted molecularProfileCasesGroupFilters: {}", molecularProfileCasesGroupFilters);
      LOG.debug(
          "setting interceptedMolecularProfileCasesGroupFilters to {}",
          molecularProfileCasesGroupFilters);
      request.setAttribute(
          "interceptedMolecularProfileCasesGroupFilters", molecularProfileCasesGroupFilters);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromMolecularProfileCasesGroups(molecularProfileCasesGroupFilters);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error(
          "exception thrown during extraction of molecularProfileCasesGroupFilters: {}",
          e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private boolean extractAttributesFromMolecularProfileCasesGroupsAndAlterationTypes(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      MolecularProfileCasesGroupAndAlterationTypeFilter
          molecularProfileCasesAndAlterationTypesGroupFilters =
              objectMapper.readValue(
                  request.getInputStream(),
                  MolecularProfileCasesGroupAndAlterationTypeFilter.class);
      List<MolecularProfileCasesGroupFilter> molecularProfileCasesGroupFilters =
          molecularProfileCasesAndAlterationTypesGroupFilters.getMolecularProfileCasesGroupFilter();
      LOG.debug(
          "extracted molecularProfileCasesGroupFilters: {}", molecularProfileCasesGroupFilters);
      LOG.debug(
          "setting interceptedMolecularProfileCasesGroupFilters to {}",
          molecularProfileCasesGroupFilters);
      request.setAttribute(
          "interceptedMolecularProfileCasesGroupFilters", molecularProfileCasesGroupFilters);
      if (molecularProfileCasesAndAlterationTypesGroupFilters.getAlterationEventTypes() != null) {
        AlterationFilter alterationEnrichmentEventTypes =
            molecularProfileCasesAndAlterationTypesGroupFilters.getAlterationEventTypes();
        LOG.debug("extracted alterationEventTypes: {}", alterationEnrichmentEventTypes);
        LOG.debug("setting alterationEventTypes to {}", alterationEnrichmentEventTypes);
        request.setAttribute("alterationEventTypes", alterationEnrichmentEventTypes);
      }
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromMolecularProfileCasesGroups(molecularProfileCasesGroupFilters);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error(
          "exception thrown during extraction of molecularProfileCasesGroupFilters: {}",
          e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private boolean extractAttributesFromStructuralVariantFilter(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      StructuralVariantFilter structuralVariantFilter =
          objectMapper.readValue(request.getInputStream(), StructuralVariantFilter.class);
      LOG.debug("extracted structuralVariantFilter: {}", structuralVariantFilter);
      if (structuralVariantFilter.getStructuralVariantQueries() == null) {
        // For backwards compatibility an empty set of queries is inferred
        // when the StructuralVariantFilter is not part of the request.
        structuralVariantFilter.setStructuralVariantQueries(new ArrayList<>());
      }
      LOG.debug("setting interceptedStructuralVariantFilter to {}", structuralVariantFilter);
      request.setAttribute("interceptedStructuralVariantFilter", structuralVariantFilter);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            extractCancerStudyIdsFromStructuralVariantFilter(structuralVariantFilter);
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error(
          "exception thrown during extraction of structuralVariantFilter: {}", e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private Collection<String> extractCancerStudyIdsFromStructuralVariantFilter(
      StructuralVariantFilter structuralVariantFilter) {
    Set<String> studyIdSet = new HashSet<>();
    if (structuralVariantFilter.getSampleMolecularIdentifiers() != null) {
      // controller handler will preferentially use SampleMolecularIdentifiers if they are present
      // in the filter
      extractCancerStudyIdsFromSampleMolecularIdentifiers(
          structuralVariantFilter.getSampleMolecularIdentifiers(), studyIdSet);
    } else {
      // otherwise, handler will use the list of MolecularProfileIds in the filter
      if (structuralVariantFilter.getMolecularProfileIds() != null) {
        extractCancerStudyIdsFromMolecularProfileIds(
            structuralVariantFilter.getMolecularProfileIds(), studyIdSet);
      }
    }
    return studyIdSet;
  }

  private Set<String> extractCancerStudyIdsFromSampleIdentifiers(
      Collection<SampleIdentifier> sampleIdentifiers) {
    Set<String> studyIdSet = new HashSet<>();
    extractCancerStudyIdsFromSampleIdentifiers(sampleIdentifiers, studyIdSet);
    return studyIdSet;
  }

  private void extractCancerStudyIdsFromSampleIdentifiers(
      Collection<SampleIdentifier> sampleIdentifiers, Set<String> studyIdSet) {
    for (SampleIdentifier sampleIdentifier : sampleIdentifiers) {
      studyIdSet.add(sampleIdentifier.getStudyId());
    }
  }

  private void extractCancerStudyIdsFromSampleListIds(
      List<String> sampleListIds, Set<String> studyIdSet) {
    for (String sampleListId : sampleListIds) {
      SampleList sampleList = cacheMapUtil.getSampleListMap().get(sampleListId);
      studyIdSet.add(sampleList.getCancerStudyIdentifier());
    }
  }

  private void extractCancerStudyIdsFromMolecularProfileIds(
      Collection<String> molecularProfileIds, Set<String> studyIdSet) {
    for (String molecularProfileId : molecularProfileIds) {
      MolecularProfile molecularProfile =
          cacheMapUtil.getMolecularProfileMap().get(molecularProfileId);
      studyIdSet.add(molecularProfile.getCancerStudyIdentifier());
    }
  }

  private void extractCancerStudyIdsFromSampleMolecularIdentifiers(
      List<SampleMolecularIdentifier> sampleMolecularIdentifiers, Set<String> studyIdSet) {
    // use hashset as the study list in sampleMolecularIdentifiers may be populated with duplicate
    // values
    Set<String> molecularProfileIds = new HashSet<>();
    for (SampleMolecularIdentifier sampleMolecularIdentifier : sampleMolecularIdentifiers) {
      molecularProfileIds.add(sampleMolecularIdentifier.getMolecularProfileId());
    }
    extractCancerStudyIdsFromMolecularProfileIds(molecularProfileIds, studyIdSet);
  }

  private Set<String> extractCancerStudyIdsFromClinicalDataBinCountFilter(
      ClinicalDataBinCountFilter clinicalDataBinCountFilter) {
    if (clinicalDataBinCountFilter.getStudyViewFilter() != null) {
      return extractCancerStudyIdsFromStudyViewFilter(
          clinicalDataBinCountFilter.getStudyViewFilter());
    }
    return new HashSet<>();
  }

  private Set<String> extractCancerStudyIdsFromGenomicDataBinCountFilter(
      GenomicDataBinCountFilter genomicDataBinCountFilter) {
    if (genomicDataBinCountFilter.getStudyViewFilter() != null) {
      return extractCancerStudyIdsFromStudyViewFilter(
          genomicDataBinCountFilter.getStudyViewFilter());
    }
    return new HashSet<>();
  }

  private Set<String> extractCancerStudyIdsFromGenomicDataCountFilter(
      GenomicDataCountFilter genomicDataCountFilter) {
    if (genomicDataCountFilter.getStudyViewFilter() != null) {
      return extractCancerStudyIdsFromStudyViewFilter(genomicDataCountFilter.getStudyViewFilter());
    }
    return new HashSet<String>();
  }

  private Set<String> extractCancerStudyIdsFromGenericAssayDataBinCountFilter(
      GenericAssayDataBinCountFilter genericAssayDataBinCountFilter) {
    if (genericAssayDataBinCountFilter.getStudyViewFilter() != null) {
      return extractCancerStudyIdsFromStudyViewFilter(
          genericAssayDataBinCountFilter.getStudyViewFilter());
    }
    return new HashSet<>();
  }

  private Set<String> extractCancerStudyIdsFromGenericAssayDataCountFilter(
      GenericAssayDataCountFilter genericAssayDataCountFilter) {
    if (genericAssayDataCountFilter.getStudyViewFilter() != null) {
      return extractCancerStudyIdsFromStudyViewFilter(
          genericAssayDataCountFilter.getStudyViewFilter());
    }
    return new HashSet<>();
  }

  private Set<String> extractCancerStudyIdsFromClinicalDataCountFilter(
      ClinicalDataCountFilter clinicalDataCountFilter) {
    if (clinicalDataCountFilter.getStudyViewFilter() != null) {
      return extractCancerStudyIdsFromStudyViewFilter(clinicalDataCountFilter.getStudyViewFilter());
    }
    return new HashSet<>();
  }

  private Set<String> extractCancerStudyIdsFromNamespaceDataCountFilter(
      NamespaceDataCountFilter namespaceDataCountFilter) {
    if (namespaceDataCountFilter.getStudyViewFilter() != null) {
      return extractCancerStudyIdsFromStudyViewFilter(
          namespaceDataCountFilter.getStudyViewFilter());
    }
    return new HashSet<>();
  }

  private Set<String> extractCancerStudyIdsFromStudyViewFilter(StudyViewFilter studyViewFilter) {
    Set<String> studyIdSet = new HashSet<>();
    if (studyViewFilter.getSampleIdentifiers() != null
        && !studyViewFilter.getSampleIdentifiers().isEmpty()) {
      extractCancerStudyIdsFromSampleIdentifiers(
          studyViewFilter.getSampleIdentifiers(), studyIdSet);
    } else {
      studyIdSet.addAll(studyViewFilter.getStudyIds());
    }
    return studyIdSet;
  }

  private Set<String> extractCancerStudyIdsFromMolecularProfileCasesGroups(
      Collection<MolecularProfileCasesGroupFilter> molecularProfileCasesGroupFilters) {
    Set<String> molecularProfileIds =
        molecularProfileCasesGroupFilters.stream()
            .flatMap(
                group -> {
                  return group.getMolecularProfileCaseIdentifiers().stream()
                      .map(MolecularProfileCaseIdentifier::getMolecularProfileId);
                })
            .collect(Collectors.toSet());
    Set<String> studyIdSet = new HashSet<>();
    extractCancerStudyIdsFromMolecularProfileIds(molecularProfileIds, studyIdSet);
    return studyIdSet;
  }

  private boolean extractCancerStudyIdsFromSurvivalRequest(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      SurvivalRequest survivalRequest =
          objectMapper.readValue(request.getInputStream(), SurvivalRequest.class);
      LOG.debug("extracted survivalRequest: {}", survivalRequest);
      LOG.debug("setting interceptedSurvivalRequest to {}", survivalRequest);
      request.setAttribute("interceptedSurvivalRequest", survivalRequest);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            survivalRequest.getPatientIdentifiers().stream()
                .map(PatientIdentifier::getStudyId)
                .collect(Collectors.toSet());
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error("exception thrown during extraction of survivalRequest: {}", e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }

  private boolean extractCancerStudyIdsFromClinicalEventAttributeRequest(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      ClinicalEventAttributeRequest clinicalEventAttributeRequest =
          objectMapper.readValue(request.getInputStream(), ClinicalEventAttributeRequest.class);
      LOG.debug("extracted clinicalEventAttributeRequest: {}", clinicalEventAttributeRequest);
      LOG.debug(
          "setting interceptedClinicalEventAttributeRequest to {}", clinicalEventAttributeRequest);
      request.setAttribute(
          "interceptedClinicalEventAttributeRequest", clinicalEventAttributeRequest);
      if (cacheMapUtil.hasCacheEnabled()) {
        Collection<String> cancerStudyIdCollection =
            clinicalEventAttributeRequest.getPatientIdentifiers().stream()
                .map(PatientIdentifier::getStudyId)
                .collect(Collectors.toSet());
        LOG.debug("setting involvedCancerStudies to {}", cancerStudyIdCollection);
        request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
      }
    } catch (Exception e) {
      LOG.error(
          "exception thrown during extraction of clinicalEventAttributeRequest: {}",
          e.getMessage());
      sendBadRequestResponse(response, e.getMessage());
      return false;
    }
    return true;
  }
}
