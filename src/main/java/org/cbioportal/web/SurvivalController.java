package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.SurvivalEvent;
import org.cbioportal.service.ClinicalEventService;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.ClinicalEventRequestIdentifier;
import org.cbioportal.web.parameter.OccurrencePosition;
import org.cbioportal.web.parameter.PatientIdentifier;
import org.cbioportal.web.parameter.SurvivalRequest;
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
import java.util.function.ToIntFunction;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = "Survival", description = " ")
public class SurvivalController {
    private final ClinicalEventService clinicalEventService;
    
    @Autowired
    public SurvivalController(ClinicalEventService clinicalEventService) {
        this.clinicalEventService = clinicalEventService;
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/survival-data/fetch",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch survival data")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalData.class))))
    public ResponseEntity<List<ClinicalData>> fetchSurvivalData(
        @Parameter(required = true, description = "Survival Data Request")
        @Valid @RequestBody(required = false) SurvivalRequest survivalRequest,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true)
        // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedSurvivalRequest") SurvivalRequest interceptedSurvivalRequest) {

        List<String> studyIds = new ArrayList<>();
        List<String> patientIds = new ArrayList<>();
        for (PatientIdentifier patientIdentifier : interceptedSurvivalRequest.getPatientIdentifiers()) {
            studyIds.add(patientIdentifier.getStudyId());
            patientIds.add(patientIdentifier.getPatientId());
        }

        List<ClinicalEvent> endClinicalEventsMeta = new ArrayList<>();
        ToIntFunction<ClinicalEvent> endPositionIdentifier = ClinicalEvent::getStopDate;
        if (interceptedSurvivalRequest.getEndEventRequestIdentifier() != null) {
            endClinicalEventsMeta = getToClinicalEvents(interceptedSurvivalRequest.getEndEventRequestIdentifier());
            endPositionIdentifier = getPositionIdentifier(interceptedSurvivalRequest.getEndEventRequestIdentifier().getPosition());
        }

        List<ClinicalEvent> censoredClinicalEventsMeta = new ArrayList<>();
        ToIntFunction<ClinicalEvent> censoredPositionIdentifier = ClinicalEvent::getStopDate;
        if (interceptedSurvivalRequest.getCensoredEventRequestIdentifier() != null) {
            censoredClinicalEventsMeta = getToClinicalEvents(interceptedSurvivalRequest.getCensoredEventRequestIdentifier());
            censoredPositionIdentifier = getPositionIdentifier(interceptedSurvivalRequest.getCensoredEventRequestIdentifier().getPosition());
        }

        SurvivalEvent survivalEvent = new SurvivalEvent();
        survivalEvent.setStartClinicalEventsMeta(getToClinicalEvents(interceptedSurvivalRequest.getStartEventRequestIdentifier()));
        survivalEvent.setStartPositionIdentifier(getPositionIdentifier(interceptedSurvivalRequest.getStartEventRequestIdentifier().getPosition()));
        survivalEvent.setEndClinicalEventsMeta(endClinicalEventsMeta);
        survivalEvent.setEndPositionIdentifier(endPositionIdentifier);
        survivalEvent.setCensoredClinicalEventsMeta(censoredClinicalEventsMeta);
        survivalEvent.setCensoredPositionIdentifier(censoredPositionIdentifier);

        List<ClinicalData> result = clinicalEventService.getSurvivalData(studyIds,
            patientIds,
            interceptedSurvivalRequest.getAttributeIdPrefix(),
            survivalEvent);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private static List<ClinicalEvent> getToClinicalEvents(ClinicalEventRequestIdentifier clinicalEventRequestIdentifier) {
        return clinicalEventRequestIdentifier.getClinicalEventRequests().stream().map(x -> {
            ClinicalEvent clinicalEvent = new ClinicalEvent();
            clinicalEvent.setEventType(x.getEventType());
            clinicalEvent.setAttributes(x.getAttributes());

            return clinicalEvent;
        }).toList();
    }

    private ToIntFunction<ClinicalEvent> getPositionIdentifier(OccurrencePosition position) {
        return position.equals(OccurrencePosition.FIRST) ? ClinicalEvent::getStartDate : ClinicalEvent::getStopDate;
    }
}
