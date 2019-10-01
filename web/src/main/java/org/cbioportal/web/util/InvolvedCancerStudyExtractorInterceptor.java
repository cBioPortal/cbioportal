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

package org.cbioportal.web.util;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.SampleList;
import org.cbioportal.persistence.mybatis.util.CacheMapUtil;
import org.cbioportal.web.parameter.ClinicalAttributeCountFilter;
import org.cbioportal.web.parameter.ClinicalDataBinCountFilter;
import org.cbioportal.web.parameter.ClinicalDataCountFilter;
import org.cbioportal.web.parameter.ClinicalDataIdentifier;
import org.cbioportal.web.parameter.ClinicalDataMultiStudyFilter;
import org.cbioportal.web.parameter.GroupFilter;
import org.cbioportal.web.parameter.MolecularDataMultipleStudyFilter;
import org.cbioportal.web.parameter.MolecularProfileCasesGroupFilter;
import org.cbioportal.web.parameter.MolecularProfileFilter;
import org.cbioportal.web.parameter.MutationMultipleStudyFilter;
import org.cbioportal.web.parameter.PatientFilter;
import org.cbioportal.web.parameter.PatientIdentifier;
import org.cbioportal.web.parameter.SampleFilter;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.SampleMolecularIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.parameter.TreatmentFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class InvolvedCancerStudyExtractorInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private UniqueKeyExtractor uniqueKeyExtractor;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CacheMapUtil cacheMapUtil;

    private static final Logger LOG = LoggerFactory.getLogger(InvolvedCancerStudyExtractorInterceptor.class);
    public static final String PATIENT_FETCH_PATH = "/patients/fetch";
    public static final String SAMPLE_FETCH_PATH = "/samples/fetch";
    public static final String MOLECULAR_PROFILE_FETCH_PATH = "/molecular-profiles/fetch";
    public static final String CLINICAL_ATTRIBUTE_COUNT_FETCH_PATH = "/clinical-attributes/counts/fetch";
    public static final String CLINICAL_DATA_FETCH_PATH = "/clinical-data/fetch";
    public static final String GENE_PANEL_DATA_FETCH_PATH = "/gene-panel-data/fetch";
    public static final String MOLECULAR_DATA_MULTIPLE_STUDY_FETCH_PATH = "/molecular-data/fetch";
    public static final String MUTATION_MULTIPLE_STUDY_FETCH_PATH = "/mutations/fetch";
    public static final String COPY_NUMBER_SEG_FETCH_PATH = "/copy-number-segments/fetch";
    public static final String STUDY_VIEW_CLINICAL_DATA_BIN_COUNTS_PATH = "/clinical-data-bin-counts/fetch";
    public static final String STUDY_VIEW_CLINICAL_DATA_COUNTS_PATH = "/clinical-data-counts/fetch";
    public static final String STUDY_VIEW_CLINICAL_DATA_DENSITY_PATH = "/clinical-data-density-plot/fetch";
    public static final String STUDY_VIEW_CNA_GENES = "/cna-genes/fetch";
    public static final String STUDY_VIEW_FILTERED_SAMPLES = "/filtered-samples/fetch";
    public static final String STUDY_VIEW_MUTATED_GENES = "/mutated-genes/fetch";
    public static final String STUDY_VIEW_FUSION_GENES = "/fusion-genes/fetch";
    public static final String STUDY_VIEW_SAMPLE_COUNTS = "/sample-counts/fetch";
    public static final String CLINICAL_DATA_ENRICHMENT_FETCH_PATH = "/clinical-data-enrichments/fetch";
    public static final String MUTATION_ENRICHMENT_FETCH_PATH = "/mutation-enrichments/fetch";
    public static final String COPY_NUMBER_ENRICHMENT_FETCH_PATH = "/copy-number-enrichments/fetch";
    public static final String EXPRESSION_ENRICHMENT_FETCH_PATH = "/expression-enrichments/fetch";
    public static final String TREATMENT_FETCH_PATH = "/treatments/fetch";

    @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!request.getMethod().equals("POST")) {
            return true; // no attribute extraction needed because all user supplied filter objects are in POST requests
        }
        ResettableHttpServletRequestWrapper wrappedRequest = new ResettableHttpServletRequestWrapper((HttpServletRequest) request);
        String requestPathInfo = request.getPathInfo();
        if (requestPathInfo.equals(PATIENT_FETCH_PATH)) {
            return extractAttributesFromPatientFilter(wrappedRequest);
        } else if (requestPathInfo.equals(SAMPLE_FETCH_PATH)) {
            return extractAttributesFromSampleFilter(wrappedRequest);
        } else if (requestPathInfo.equals(MOLECULAR_PROFILE_FETCH_PATH)) {
            return extractAttributesFromMolecularProfileFilter(wrappedRequest);
        } else if (requestPathInfo.equals(CLINICAL_ATTRIBUTE_COUNT_FETCH_PATH)) {
            return extractAttributesFromClinicalAttributeCountFilter(wrappedRequest);
        } else if (requestPathInfo.equals(CLINICAL_DATA_FETCH_PATH)) {
            return extractAttributesFromClinicalDataMultiStudyFilter(wrappedRequest);
        } else if (requestPathInfo.equals(GENE_PANEL_DATA_FETCH_PATH)) {
            return extractAttributesFromGenePanelSampleMolecularIdentifiers(wrappedRequest);
        } else if (requestPathInfo.equals(MOLECULAR_DATA_MULTIPLE_STUDY_FETCH_PATH)) {
            return extractAttributesFromMolecularDataMultipleStudyFilter(wrappedRequest);
        } else if (requestPathInfo.equals(MUTATION_MULTIPLE_STUDY_FETCH_PATH)) {
            return extractAttributesFromMutationMultipleStudyFilter(wrappedRequest);
        } else if (requestPathInfo.equals(COPY_NUMBER_SEG_FETCH_PATH)) {
            return extractAttributesFromSampleIdentifiers(wrappedRequest);
        } else if (requestPathInfo.equals(STUDY_VIEW_CLINICAL_DATA_BIN_COUNTS_PATH)) {
            return extractAttributesFromClinicalDataBinCountFilter(wrappedRequest);
        } else if (requestPathInfo.equals(STUDY_VIEW_CLINICAL_DATA_COUNTS_PATH)) {
            return extractAttributesFromClinicalDataCountFilter(wrappedRequest);
        } else if (Arrays.asList(STUDY_VIEW_CLINICAL_DATA_DENSITY_PATH, STUDY_VIEW_CNA_GENES,
                STUDY_VIEW_FILTERED_SAMPLES, STUDY_VIEW_MUTATED_GENES, STUDY_VIEW_FUSION_GENES, STUDY_VIEW_SAMPLE_COUNTS)
                .contains(requestPathInfo)) {
            return extractAttributesFromStudyViewFilter(wrappedRequest);
        }  else if (requestPathInfo.equals(CLINICAL_DATA_ENRICHMENT_FETCH_PATH)) {
            return extractAttributesFromGroupFilter(wrappedRequest);
        } else if (requestPathInfo.equals(MUTATION_ENRICHMENT_FETCH_PATH) || requestPathInfo.equals(COPY_NUMBER_ENRICHMENT_FETCH_PATH) || requestPathInfo.equals(EXPRESSION_ENRICHMENT_FETCH_PATH)) {
            return extractAttributesFromMolecularProfileCasesGroups(wrappedRequest);
        } else if (requestPathInfo.equals(TREATMENT_FETCH_PATH)) {
            return extractAttributesFromTreatmentFilter(wrappedRequest);
        }
        return true;
    }

    private boolean extractAttributesFromPatientFilter(HttpServletRequest request) {
        try {
            PatientFilter patientFilter = objectMapper.readValue(request.getReader(), PatientFilter.class);
            LOG.debug("extracted patientFilter: " + patientFilter.toString());
            LOG.debug("setting interceptedPatientFilter to " + patientFilter);
            request.setAttribute("interceptedPatientFilter", patientFilter);
            if (cacheMapUtil.hasCacheEnabled()) {
                Collection<String> cancerStudyIdCollection = extractCancerStudyIdsFromPatientFilter(patientFilter);
                LOG.debug("setting involvedCancerStudies to " + cancerStudyIdCollection);
                request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
            }
        } catch (Exception e) {
            LOG.error("exception thrown during extraction of patientFilter: " + e);
            return false;
        }
        return true;
    }

    private Collection<String> extractCancerStudyIdsFromPatientFilter(PatientFilter patientFilter) {
        // use hashset as the study list in the patientFilter will usually be populated with many duplicate values
        Set<String> studyIdSet = new HashSet<String>();
        if (patientFilter.getPatientIdentifiers() != null) {
            for (PatientIdentifier patientIdentifier : patientFilter.getPatientIdentifiers()) {
                studyIdSet.add(patientIdentifier.getStudyId());
            }
        } else {
            uniqueKeyExtractor.extractUniqueKeys(patientFilter.getUniquePatientKeys(), studyIdSet);
        }
        return studyIdSet;
    }

    private boolean extractAttributesFromSampleFilter(HttpServletRequest request) {
        try {
            SampleFilter sampleFilter = objectMapper.readValue(request.getReader(), SampleFilter.class);
            LOG.debug("extracted sampleFilter: " + sampleFilter.toString());
            LOG.debug("setting interceptedSampleFilter to " + sampleFilter);
            request.setAttribute("interceptedSampleFilter", sampleFilter);
            if (cacheMapUtil.hasCacheEnabled()) {
                Collection<String> cancerStudyIdCollection = extractCancerStudyIdsFromSampleFilter(sampleFilter);
                LOG.debug("setting involvedCancerStudies to " + cancerStudyIdCollection);
                request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
            }
        } catch (Exception e) {
            LOG.error("exception thrown during extraction of sampleFilter: " + e);
            return false;
        }
        return true;
    }

    private Collection<String> extractCancerStudyIdsFromSampleFilter(SampleFilter sampleFilter) {
        // use hashset as the study list in the sampleFilter will usually be populated with many duplicate values
        Set<String> studyIdSet = new HashSet<String>();
        if (sampleFilter.getSampleListIds() != null) {
            extractCancerStudyIdsFromSampleListIds(sampleFilter.getSampleListIds(), studyIdSet);
        } else if (sampleFilter.getSampleIdentifiers() != null) {
            extractCancerStudyIdsFromSampleIdentifiers(sampleFilter.getSampleIdentifiers(), studyIdSet);
        } else {
            uniqueKeyExtractor.extractUniqueKeys(sampleFilter.getUniqueSampleKeys(), studyIdSet);
        }
        return studyIdSet;
    }

    private boolean extractAttributesFromMolecularProfileFilter(HttpServletRequest request) {
        try {
            MolecularProfileFilter molecularProfileFilter = objectMapper.readValue(request.getReader(), MolecularProfileFilter.class);
            LOG.debug("extracted molecularProfileFilter: " + molecularProfileFilter.toString());
            LOG.debug("setting interceptedMolecularProfileFilter to " + molecularProfileFilter);
            request.setAttribute("interceptedMolecularProfileFilter", molecularProfileFilter);
            if (cacheMapUtil.hasCacheEnabled()) {
                Collection<String> cancerStudyIdCollection = extractCancerStudyIdsFromMolecularProfileFilter(molecularProfileFilter);
                LOG.debug("setting involvedCancerStudies to " + cancerStudyIdCollection);
                request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
            }
        } catch (Exception e) {
            LOG.error("exception thrown during extraction of molecularProfileFilter: " + e);
            return false;
        }
        return true;
    }

    private Collection<String> extractCancerStudyIdsFromMolecularProfileFilter(MolecularProfileFilter molecularProfileFilter) {
        // use hashset as the study list in the molecularProfileFilter may be populated with many duplicate values
        Set<String> studyIdSet = new HashSet<String>();
        if (molecularProfileFilter.getStudyIds() != null) {
            studyIdSet.addAll(molecularProfileFilter.getStudyIds());
        } else {
            extractCancerStudyIdsFromMolecularProfileIds(molecularProfileFilter.getMolecularProfileIds(), studyIdSet);
        }
        return studyIdSet;
    }

    private boolean extractAttributesFromClinicalAttributeCountFilter(HttpServletRequest request) {
        try {
            ClinicalAttributeCountFilter clinicalAttributeCountFilter = objectMapper.readValue(request.getReader(), ClinicalAttributeCountFilter.class);
            LOG.debug("extracted clinicalAttributeCountFilter: " + clinicalAttributeCountFilter.toString());
            LOG.debug("setting interceptedClinicalAttributeCountFilter to " + clinicalAttributeCountFilter);
            request.setAttribute("interceptedClinicalAttributeCountFilter", clinicalAttributeCountFilter);
            if (cacheMapUtil.hasCacheEnabled()) {
                Collection<String> cancerStudyIdCollection = extractCancerStudyIdsFromClinicalAttributeCountFilter(clinicalAttributeCountFilter);
                LOG.debug("setting involvedCancerStudies to " + cancerStudyIdCollection);
                request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
            }
        } catch (Exception e) {
            LOG.error("exception thrown during extraction of clinicalAttributeCountFilter: " + e);
            return false;
        }
        return true;
    }

    private Collection<String> extractCancerStudyIdsFromClinicalAttributeCountFilter(ClinicalAttributeCountFilter clinicalAttributeCountFilter) {
        // use hashset as the study list in the clinicalAttributeCountFilter may be populated with many duplicate values
        Set<String> studyIdSet = new HashSet<String>();
        if (clinicalAttributeCountFilter.getSampleListId() != null) {
            extractCancerStudyIdsFromSampleListIds(Arrays.asList(clinicalAttributeCountFilter.getSampleListId()), studyIdSet);
        } else {
            extractCancerStudyIdsFromSampleIdentifiers(clinicalAttributeCountFilter.getSampleIdentifiers(), studyIdSet);
        }
        return studyIdSet;
    }

    private boolean extractAttributesFromClinicalDataMultiStudyFilter(HttpServletRequest request) {
        try {
            ClinicalDataMultiStudyFilter clinicalDataMultiStudyFilter = objectMapper.readValue(request.getReader(), ClinicalDataMultiStudyFilter.class);
            LOG.debug("extracted clinicalDataMultiStudyFilter: " + clinicalDataMultiStudyFilter.toString());
            LOG.debug("setting interceptedClinicalDataMultiStudyFilter to " + clinicalDataMultiStudyFilter);
            request.setAttribute("interceptedClinicalDataMultiStudyFilter", clinicalDataMultiStudyFilter);
            if (cacheMapUtil.hasCacheEnabled()) {
                Collection<String> cancerStudyIdCollection = extractCancerStudyIdsFromClinicalDataMultiStudyFilter(clinicalDataMultiStudyFilter);
                LOG.debug("setting involvedCancerStudies to " + cancerStudyIdCollection);
                request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
            }
        } catch (Exception e) {
            LOG.error("exception thrown during extraction of clinicalDataMultiStudyFilter: " + e);
            return false;
        }
        return true;
    }

    private Collection<String> extractCancerStudyIdsFromClinicalDataMultiStudyFilter(ClinicalDataMultiStudyFilter clinicalDataMultiStudyFilter) {
        // use hashset as the study list in the clinicalDataMultiStudyFilter may be populated with many duplicate values
        Set<String> studyIdSet = new HashSet<String>();
        for(ClinicalDataIdentifier clinicalDataIdentifier : clinicalDataMultiStudyFilter.getIdentifiers()) {
            studyIdSet.add(clinicalDataIdentifier.getStudyId());
        }
        return studyIdSet;
    }

    private boolean extractAttributesFromGenePanelSampleMolecularIdentifiers(HttpServletRequest request) {
        try {
            List<SampleMolecularIdentifier> sampleMolecularIdentifiers = Arrays.asList(objectMapper.readValue(request.getReader(), SampleMolecularIdentifier[].class));
            LOG.debug("extracted sampleMolecularIdentifiers: " + sampleMolecularIdentifiers.toString());
            LOG.debug("setting interceptedGenePanelSampleMolecularIdentifers to " + sampleMolecularIdentifiers);
            request.setAttribute("interceptedGenePanelSampleMolecularIdentifiers", sampleMolecularIdentifiers);
            if (cacheMapUtil.hasCacheEnabled()) {
                Collection<String> cancerStudyIdCollection = extractCancerStudyIdsFromGenePanelSampleMolecularIdentifiers(sampleMolecularIdentifiers);
                LOG.debug("setting involvedCancerStudies to " + cancerStudyIdCollection);
                request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
            }
        } catch (Exception e) {
            LOG.error("exception thrown during extraction of genePanelSampleMolecularIdentifiers: " + e);
            return false;
        }
        return true;
    }

    private Collection<String> extractCancerStudyIdsFromGenePanelSampleMolecularIdentifiers(List<SampleMolecularIdentifier> sampleMolecularIdentifiers) {
        Set<String> studyIdSet = new HashSet<String>();
        extractCancerStudyIdsFromSampleMolecularIdentifiers(sampleMolecularIdentifiers, studyIdSet);
        return studyIdSet;
    }

    private boolean extractAttributesFromMolecularDataMultipleStudyFilter(HttpServletRequest request) {
        try {
            MolecularDataMultipleStudyFilter molecularDataMultipleStudyFilter = objectMapper.readValue(request.getReader(), MolecularDataMultipleStudyFilter.class);
            LOG.debug("extracted molecularDataMultipleStudyFilter: " + molecularDataMultipleStudyFilter.toString());
            LOG.debug("setting interceptedMolecularDataMultipleStudyFilter to " + molecularDataMultipleStudyFilter);
            request.setAttribute("interceptedMolecularDataMultipleStudyFilter", molecularDataMultipleStudyFilter);
            if (cacheMapUtil.hasCacheEnabled()) {
                Collection<String> cancerStudyIdCollection = extractCancerStudyIdsFromMolecularDataMultipleStudyFilter(molecularDataMultipleStudyFilter);
                LOG.debug("setting involvedCancerStudies to " + cancerStudyIdCollection);
                request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
            }
        } catch (Exception e) {
            LOG.error("exception thrown during extraction of molecularDataMultipleStudyFilter: " + e);
            return false;
        }
        return true;
    }

    private Collection<String> extractCancerStudyIdsFromMolecularDataMultipleStudyFilter(MolecularDataMultipleStudyFilter molecularDataMultipleStudyFilter) {
        Set<String> studyIdSet = new HashSet<String>();
        if (molecularDataMultipleStudyFilter.getMolecularProfileIds() != null) {
            extractCancerStudyIdsFromMolecularProfileIds(molecularDataMultipleStudyFilter.getMolecularProfileIds(), studyIdSet);
        } else {
            extractCancerStudyIdsFromSampleMolecularIdentifiers(molecularDataMultipleStudyFilter.getSampleMolecularIdentifiers(), studyIdSet);
        }
        return studyIdSet;
    }

    private boolean extractAttributesFromMutationMultipleStudyFilter(HttpServletRequest request) {
        try {
            MutationMultipleStudyFilter mutationMultipleStudyFilter = objectMapper.readValue(request.getReader(), MutationMultipleStudyFilter.class);
            LOG.debug("extracted mutationMultipleStudyFilter: " + mutationMultipleStudyFilter.toString());
            LOG.debug("setting interceptedMutationMultipleStudyFilter to " + mutationMultipleStudyFilter);
            request.setAttribute("interceptedMutationMultipleStudyFilter", mutationMultipleStudyFilter);
            if (cacheMapUtil.hasCacheEnabled()) {
                Collection<String> cancerStudyIdCollection = extractCancerStudyIdsFromMutationMultipleStudyFilter(mutationMultipleStudyFilter);
                LOG.debug("setting involvedCancerStudies to " + cancerStudyIdCollection);
                request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
            }
        } catch (Exception e) {
            LOG.error("exception thrown during extraction of mutationMultipleStudyFilter: " + e);
            return false;
        }
        return true;
    }

    private Set<String> extractCancerStudyIdsFromMutationMultipleStudyFilter(MutationMultipleStudyFilter mutationMultipleStudyFilter) {
        Set<String> studyIdSet = new HashSet<String>();
        if (mutationMultipleStudyFilter.getMolecularProfileIds() != null) {
            extractCancerStudyIdsFromMolecularProfileIds(mutationMultipleStudyFilter.getMolecularProfileIds(), studyIdSet);
        } else {
            extractCancerStudyIdsFromSampleMolecularIdentifiers(mutationMultipleStudyFilter.getSampleMolecularIdentifiers(), studyIdSet);
        }
        return studyIdSet;
    }

    private boolean extractAttributesFromSampleIdentifiers(HttpServletRequest request) {
        try {
            List<SampleIdentifier> sampleIdentifiers = Arrays.asList(objectMapper.readValue(request.getReader(), SampleIdentifier[].class));
            LOG.debug("extracted sampleIdentifiers: " + sampleIdentifiers.toString());
            LOG.debug("setting interceptedSampleIdentifiers to " + sampleIdentifiers);
            request.setAttribute("interceptedSampleIdentifiers", sampleIdentifiers);
            if (cacheMapUtil.hasCacheEnabled()) {
                Collection<String> cancerStudyIdCollection = extractCancerStudyIdsFromSampleIdentifiers(sampleIdentifiers);
                LOG.debug("setting involvedCancerStudies to " + cancerStudyIdCollection);
                request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
            }
        } catch (Exception e) {
            LOG.error("exception thrown during extraction of sampleIdentifiers: " + e);
            return false;
        }
        return true;
    }

    private boolean extractAttributesFromClinicalDataBinCountFilter(HttpServletRequest request) {
        try {
            ClinicalDataBinCountFilter clinicalDataBinCountFilter = objectMapper.readValue(request.getReader(),
                    ClinicalDataBinCountFilter.class);
            LOG.debug("extracted clinicalDataBinCountFilter: " + clinicalDataBinCountFilter.toString());
            LOG.debug("setting interceptedClinicalDataBinCountFilter to " + clinicalDataBinCountFilter);
            request.setAttribute("interceptedClinicalDataBinCountFilter", clinicalDataBinCountFilter);
            if (cacheMapUtil.hasCacheEnabled()) {
                Collection<String> cancerStudyIdCollection = extractCancerStudyIdsFromClinicalDataBinCountFilter(
                        clinicalDataBinCountFilter);
                LOG.debug("setting involvedCancerStudies to " + cancerStudyIdCollection);
                request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
            }
        } catch (Exception e) {
            LOG.error("exception thrown during extraction of clinicalDataBinCountFilter: " + e);
            return false;
        }
        return true;
    }

    private boolean extractAttributesFromClinicalDataCountFilter(HttpServletRequest request) {
        try {
            ClinicalDataCountFilter clinicalDataCountFilter = objectMapper.readValue(request.getReader(),
                    ClinicalDataCountFilter.class);
            LOG.debug("extracted clinicalDataBinCountFilter: " + clinicalDataCountFilter.toString());
            LOG.debug("setting interceptedClinicalDataCountFilter to " + clinicalDataCountFilter);
            request.setAttribute("interceptedClinicalDataCountFilter", clinicalDataCountFilter);
            if (cacheMapUtil.hasCacheEnabled()) {
                Collection<String> cancerStudyIdCollection = extractCancerStudyIdsFromClinicalDataCountFilter(
                        clinicalDataCountFilter);
                LOG.debug("setting involvedCancerStudies to " + cancerStudyIdCollection);
                request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
            }
        } catch (Exception e) {
            LOG.error("exception thrown during extraction of clinicalDataBinCountFilter: " + e);
            return false;
        }
        return true;
    }

    private boolean extractAttributesFromGroupFilter(HttpServletRequest request) {
        try {
            GroupFilter groupFilter = objectMapper.readValue(request.getReader(),
                    GroupFilter.class);
            LOG.debug("extracted groupFilter: " + groupFilter.toString());
            LOG.debug("setting interceptedGroupFilter to " + groupFilter);
            request.setAttribute("interceptedGroupFilter", groupFilter);
            if (cacheMapUtil.hasCacheEnabled()) {
                List<SampleIdentifier> sampleIdentifiers = groupFilter.getGroups().stream()
                        .flatMap(group -> group.getSampleIdentifiers().stream()).collect(Collectors.toList());
                Collection<String> cancerStudyIdCollection = extractCancerStudyIdsFromSampleIdentifiers(sampleIdentifiers);
                LOG.debug("setting involvedCancerStudies to " + cancerStudyIdCollection);
                request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
            }
        } catch (Exception e) {
            LOG.error("exception thrown during extraction of groupFilter: " + e);
            return false;
        }
        return true;
    }

    private boolean extractAttributesFromStudyViewFilter(HttpServletRequest request) {
        try {
            StudyViewFilter studyViewFilter = objectMapper.readValue(request.getReader(), StudyViewFilter.class);
            LOG.debug("extracted studyViewFilter: " + studyViewFilter.toString());
            LOG.debug("setting interceptedStudyViewFilter to " + studyViewFilter);
            request.setAttribute("interceptedStudyViewFilter", studyViewFilter);
            if (cacheMapUtil.hasCacheEnabled()) {
                Collection<String> cancerStudyIdCollection = extractCancerStudyIdsFromStudyViewFilter(studyViewFilter);
                LOG.debug("setting involvedCancerStudies to " + cancerStudyIdCollection);
                request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
            }
        } catch (Exception e) {
            LOG.error("exception thrown during extraction of studyViewFilter: " + e);
            return false;
        }
        return true;
    }

    private boolean extractAttributesFromMolecularProfileCasesGroups(HttpServletRequest request) {
        try {
            List<MolecularProfileCasesGroupFilter> molecularProfileCasesGroupFilters = Arrays
                    .asList(objectMapper.readValue(request.getReader(), MolecularProfileCasesGroupFilter[].class));
            LOG.debug("extracted molecularProfileCasesGroupFilters: " + molecularProfileCasesGroupFilters.toString());
            LOG.debug("setting interceptedMolecularProfileCasesGroupFilters to " + molecularProfileCasesGroupFilters);
            request.setAttribute("interceptedMolecularProfileCasesGroupFilters", molecularProfileCasesGroupFilters);
            if (cacheMapUtil.hasCacheEnabled()) {
                Collection<String> cancerStudyIdCollection = extractCancerStudyIdsFromMolecularProfileCasesGroups(
                        molecularProfileCasesGroupFilters);
                LOG.debug("setting involvedCancerStudies to " + cancerStudyIdCollection);
                request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
            }
        } catch (Exception e) {
            LOG.error("exception thrown during extraction of molecularProfileCasesGroupFilters: " + e);
            return false;
        }
        return true;
    }

    private boolean extractAttributesFromTreatmentFilter(HttpServletRequest request) {
        try {
            TreatmentFilter treatmentFilter = objectMapper.readValue(request.getReader(), TreatmentFilter.class);
            LOG.debug("extracted treatmentFilter: " + treatmentFilter.toString());
            LOG.debug("setting interceptedTreatmentFilter to " + treatmentFilter);
            request.setAttribute("interceptedTreatmentFilter", treatmentFilter);
            if (cacheMapUtil.hasCacheEnabled()) {
                Collection<String> cancerStudyIdCollection = extractCancerStudyIdsFromTreatmentFilter(treatmentFilter);
                LOG.debug("setting involvedCancerStudies to " + cancerStudyIdCollection);
                request.setAttribute("involvedCancerStudies", cancerStudyIdCollection);
            }
        } catch (MismatchedInputException e) {
            if (e.getMessage().startsWith("No content")) {
                LOG.debug("No body present in request : setting interceptedTreatmentFilter to empty TreatmentFilter (which will be considered invalid)");
                request.setAttribute("interceptedTreatmentFilter", new TreatmentFilter());
                return true; // let this empty TreatmentFilter be invalidated/rejected by the controller
            } else {
                LOG.error("exception thrown during extraction of treatmentFilter: " + e);
                return false;
            }
        } catch (Exception e) {
            LOG.error("exception thrown during extraction of treatmentFilter: " + e);
            return false;
        }
        return true;
    }

    private Collection<String> extractCancerStudyIdsFromTreatmentFilter(TreatmentFilter treatmentFilter) {
        // use hashset as the study list in the TreatmentFilter may be populated with many duplicate values
        Set<String> studyIdSet = new HashSet<String>();
        if (treatmentFilter.getStudyIds() != null) {
            studyIdSet.addAll(treatmentFilter.getStudyIds());
        }
        return studyIdSet;
    }

    private Set<String> extractCancerStudyIdsFromSampleIdentifiers(Collection<SampleIdentifier> sampleIdentifiers) {
        Set<String> studyIdSet = new HashSet<String>();
        extractCancerStudyIdsFromSampleIdentifiers(sampleIdentifiers, studyIdSet);
        return studyIdSet;
    }

    private void extractCancerStudyIdsFromSampleIdentifiers(Collection<SampleIdentifier> sampleIdentifiers, Set<String> studyIdSet) {
        for (SampleIdentifier sampleIdentifier : sampleIdentifiers) {
            studyIdSet.add(sampleIdentifier.getStudyId());
        }
    }

    private void extractCancerStudyIdsFromSampleListIds(List<String> sampleListIds, Set<String> studyIdSet) {
        for (String sampleListId : sampleListIds) {
            SampleList sampleList = cacheMapUtil.getSampleListMap().get(sampleListId);
            studyIdSet.add(sampleList.getCancerStudyIdentifier());
        }
    }

    private void extractCancerStudyIdsFromMolecularProfileIds(Collection<String> molecularProfileIds, Set<String> studyIdSet) {
        for (String molecularProfileId : molecularProfileIds) {
            MolecularProfile molecularProfile = cacheMapUtil.getMolecularProfileMap().get(molecularProfileId);
            studyIdSet.add(molecularProfile.getCancerStudyIdentifier());
        }
    }

    private void extractCancerStudyIdsFromSampleMolecularIdentifiers(List<SampleMolecularIdentifier> sampleMolecularIdentifiers, Set<String> studyIdSet) {
        // use hashset as the study list in sampleMolecularIdentifiers may be populated with duplicate values
        Set<String> molecularProfileIds = new HashSet<String>();
        for (SampleMolecularIdentifier sampleMolecularIdentifier: sampleMolecularIdentifiers) {
            molecularProfileIds.add(sampleMolecularIdentifier.getMolecularProfileId());
        }
        extractCancerStudyIdsFromMolecularProfileIds(molecularProfileIds, studyIdSet);
    }

    private Set<String> extractCancerStudyIdsFromClinicalDataBinCountFilter(
            ClinicalDataBinCountFilter clinicalDataBinCountFilter) {
        if (clinicalDataBinCountFilter.getStudyViewFilter() != null) {
            return extractCancerStudyIdsFromStudyViewFilter(clinicalDataBinCountFilter.getStudyViewFilter());
        }
        return new HashSet<String>();
    }

    private Set<String> extractCancerStudyIdsFromClinicalDataCountFilter(
            ClinicalDataCountFilter clinicalDataCountFilter) {
        if (clinicalDataCountFilter.getStudyViewFilter() != null) {
            return extractCancerStudyIdsFromStudyViewFilter(clinicalDataCountFilter.getStudyViewFilter());
        }
        return new HashSet<String>();
    }

    private Set<String> extractCancerStudyIdsFromStudyViewFilter(StudyViewFilter studyViewFilter) {
        Set<String> studyIdSet = new HashSet<String>();
        if (studyViewFilter.getSampleIdentifiers() != null && !studyViewFilter.getSampleIdentifiers().isEmpty()) {
            extractCancerStudyIdsFromSampleIdentifiers(studyViewFilter.getSampleIdentifiers(), studyIdSet);
        } else {
            studyIdSet.addAll(studyViewFilter.getStudyIds());
        }
        return studyIdSet;
    }

    private Set<String> extractCancerStudyIdsFromMolecularProfileCasesGroups(Collection<MolecularProfileCasesGroupFilter> molecularProfileCasesGroupFilters) {
        Set<String> molecularProfileIds = molecularProfileCasesGroupFilters.stream().flatMap(group -> {
            return group.getMolecularProfileCaseIdentifiers().stream()
                    .map(MolecularProfileCaseIdentifier::getMolecularProfileId);
        }).collect(Collectors.toSet());
        Set<String> studyIdSet = new HashSet<>();
        extractCancerStudyIdsFromMolecularProfileIds(molecularProfileIds, studyIdSet);
        return studyIdSet;
    }
}
