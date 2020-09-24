package org.cbioportal.web.studyview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.Patient;
import org.cbioportal.service.PatientService;
import org.cbioportal.session_service.domain.SessionType;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.ClinicalDataCountFilter;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.CustomDataSession;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.util.SessionServiceRequestHandler;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@InternalApi
@RestController
@Validated
@Api(tags = "Study View", description = " ")
public class CustomDataController {

    @Autowired
    private StudyViewFilterApplier studyViewFilterApplier;
    @Autowired
    private StudyViewFilterUtil studyViewFilterUtil;
    @Autowired
    private SessionServiceRequestHandler sessionServiceRequestHandler;
    @Autowired
    private PatientService patientService;

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/custom-data-counts/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch custom data counts by study view filter")
    public ResponseEntity<List<ClinicalDataCountItem>> fetchCustomDataCounts(
            @ApiParam(required = true, value = "Custom data count filter") @Valid @RequestBody(required = false) ClinicalDataCountFilter clinicalDataCountFilter,
            @ApiIgnore // prevent reference to this attribute in the swagger-ui
                       // interface
            @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
            @ApiIgnore // prevent reference to this attribute in the swagger-ui
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

        List<CompletableFuture<CustomDataSession>> postFutures = attributes.stream().map(clinicalDataFilter -> {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return (CustomDataSession) sessionServiceRequestHandler.getSession(SessionType.custom_data,
                            clinicalDataFilter.getAttributeId());
                } catch (Exception e) {
                    return null;
                }
            });
        }).collect(Collectors.toList());

        CompletableFuture.allOf(postFutures.toArray(new CompletableFuture[postFutures.size()])).join();
        
        List<CustomDataSession> customDataSessions = postFutures
        .stream()
        .map(CompletableFuture::join)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

        Map<String, SampleIdentifier> filteredSamplesMap = filteredSampleIdentifiers.stream()
                .collect(Collectors.toMap(sampleIdentifier -> {
                    return studyViewFilterUtil.getCaseUniqueKey(sampleIdentifier.getStudyId(),
                            sampleIdentifier.getSampleId());
                }, Function.identity()));

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);

        long patientCustomDataSessionsCount = customDataSessions.stream()
                .filter(customDataSession -> customDataSession.getData().getPatientAttribute()).count();
        List<Patient> patients = new ArrayList<Patient>();
        if (patientCustomDataSessionsCount > 0) {
            patients.addAll(patientService.getPatientsOfSamples(studyIds, sampleIds));
        }

        List<ClinicalDataCountItem> result = studyViewFilterUtil.getClinicalDataCountsFromCustomData(customDataSessions,
                filteredSamplesMap, patients);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
