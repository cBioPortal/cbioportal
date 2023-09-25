package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.service.ClinicalEventService;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@InternalApi
@RestController
@Validated
@Api(tags = "Survival", description = " ")
public class SurvivalController {

    @Autowired
    private ClinicalEventService clinicalEventService;

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/survival-data/fetch",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch survival data")
    public ResponseEntity<List<ClinicalData>> fetchSurvivalData(
        @ApiParam(required = true, value = "Survival Data Request")
        @Valid @RequestBody(required = false) SurvivalRequest survivalRequest,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore
        // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedSurvivalRequest") SurvivalRequest interceptedSurvivalRequest) {

        List<String> studyIds = new ArrayList<>();
        List<String> patientIds = new ArrayList<>();
        for (PatientIdentifier patientIdentifier : interceptedSurvivalRequest.getPatientIdentifiers()) {
            studyIds.add(patientIdentifier.getStudyId());
            patientIds.add(patientIdentifier.getPatientId());
        }

        List<ClinicalEvent> endClinicalEventsMeta = null;
        Function<ClinicalEvent, Integer> endPositionIdentifier = ClinicalEvent::getStopDate;
        if(interceptedSurvivalRequest.getEndEventRequestIdentifier() != null) {
            endClinicalEventsMeta =  getToClinicalEvents(interceptedSurvivalRequest.getEndEventRequestIdentifier());
            endPositionIdentifier = getPositionIdentifier(interceptedSurvivalRequest.getEndEventRequestIdentifier().getPosition());
        }

        List<ClinicalData> result = clinicalEventService.getSurvivalData(studyIds,
            patientIds,
            interceptedSurvivalRequest.getAttributeIdPrefix(),
            getToClinicalEvents(interceptedSurvivalRequest.getStartEventRequestIdentifier()),
            getPositionIdentifier(interceptedSurvivalRequest.getStartEventRequestIdentifier().getPosition()),
            endClinicalEventsMeta,
            endPositionIdentifier);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private static List<ClinicalEvent> getToClinicalEvents(ClinicalEventRequestIdentifier clinicalEventRequestIdentifier) {
        return clinicalEventRequestIdentifier.getClinicalEventRequests().stream().map(x -> {
            ClinicalEvent clinicalEvent = new ClinicalEvent();
            clinicalEvent.setEventType(x.getEventType());
            clinicalEvent.setAttributes(x.getAttributes());

            return clinicalEvent;
        }).collect(Collectors.toList());
    }

    private static Function<ClinicalEvent, Integer> getPositionIdentifier(OccurrencePosition position) {
        return position.equals(OccurrencePosition.FIRST) ? ClinicalEvent::getStartDate : ClinicalEvent::getStopDate;
    }
}
