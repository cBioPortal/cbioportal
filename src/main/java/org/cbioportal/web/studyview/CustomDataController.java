package org.cbioportal.web.studyview;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.Patient;
import org.cbioportal.service.CustomDataService;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.util.CustomDataSession;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.ClinicalDataCountFilter;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.util.StudyViewFilterApplier;
import org.cbioportal.web.util.StudyViewFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = "Study View", description = " ")
public class CustomDataController {

    @Autowired
    private StudyViewFilterApplier studyViewFilterApplier;
    @Autowired
    private StudyViewFilterUtil studyViewFilterUtil;
    @Autowired
    private CustomDataService customDataService;
    @Autowired
    private PatientService patientService;

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/custom-data-counts/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch custom data counts by study view filter")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalDataCountItem.class))))
    public ResponseEntity<List<ClinicalDataCountItem>> fetchCustomDataCounts(
            @Parameter(required = true, description = "Custom data count filter") @Valid @RequestBody(required = false) ClinicalDataCountFilter clinicalDataCountFilter,
            @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui
                       // interface
            @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
            @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui
                       // interface. this attribute is needed for the
                       // @PreAuthorize tag above.
            @Valid @RequestAttribute(required = false, value = "interceptedClinicalDataCountFilter") ClinicalDataCountFilter interceptedClinicalDataCountFilter) {

        List<ClinicalDataFilter> attributes = interceptedClinicalDataCountFilter.getAttributes();
        StudyViewFilter studyViewFilter = interceptedClinicalDataCountFilter.getStudyViewFilter();
        if (attributes.size() == 1) {
            studyViewFilterUtil.removeSelfCustomDataFromFilter(attributes.get(0).getAttributeId(), studyViewFilter);
        }
        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(studyViewFilter);

        if (filteredSampleIdentifiers.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }

        final List<String> attributeIds = attributes.stream().map(ClinicalDataFilter::getAttributeId).collect(Collectors.toList());
        Map<String, CustomDataSession> customDataSessionsMap = customDataService.getCustomDataSessions(attributeIds);

        Map<String, SampleIdentifier> filteredSamplesMap = filteredSampleIdentifiers.stream()
            .collect(Collectors.toMap(sampleIdentifier -> studyViewFilterUtil.getCaseUniqueKey(
                sampleIdentifier.getStudyId(),
                sampleIdentifier.getSampleId()
            ), Function.identity()));

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);

        long patientCustomDataSessionsCount = customDataSessionsMap.values().stream()
                .filter(customDataSession -> customDataSession.getData().getPatientAttribute()).count();
        List<Patient> patients = new ArrayList<>();
        if (patientCustomDataSessionsCount > 0) {
            patients.addAll(patientService.getPatientsOfSamples(studyIds, sampleIds));
        }

        List<ClinicalDataCountItem> result = studyViewFilterUtil.getClinicalDataCountsFromCustomData(customDataSessionsMap.values(),
                filteredSamplesMap, patients);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
