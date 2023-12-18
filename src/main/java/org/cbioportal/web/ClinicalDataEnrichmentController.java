package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataEnrichment;
import org.cbioportal.model.Sample;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.SampleService;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.GroupFilter;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.util.ClinicalDataEnrichmentUtil;
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
import java.util.Set;
import java.util.stream.Collectors;

@InternalApi
@RestController
@RequestMapping("/api")
@Validated
@Tag(name = "Clinical Data Enrichments", description = " ")
public class ClinicalDataEnrichmentController {

    @Autowired
    private ClinicalDataEnrichmentUtil clinicalDataEnrichmentUtil;

    @Autowired
    private ClinicalAttributeService clinicalAttributeService;

    @Autowired
    private SampleService sampleService;

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/clinical-data-enrichments/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch clinical data enrichments for the sample groups")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalDataEnrichment.class))))
    public ResponseEntity<List<ClinicalDataEnrichment>> fetchClinicalEnrichments(
            @Parameter(required = true, description = "List of altered and unaltered Sample/Patient IDs")
            @Valid @RequestBody(required = false) GroupFilter groupFilter,
            @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
            @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
            @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
            @Valid @RequestAttribute(required = false, value = "interceptedGroupFilter") GroupFilter interceptedGroupFilter) {

        List<String> studyIds = interceptedGroupFilter.getGroups().stream()
                .flatMap(group -> group.getSampleIdentifiers().stream().map(SampleIdentifier::getStudyId))
                .collect(Collectors.toList());

        List<String> sampleIds = interceptedGroupFilter.getGroups().stream()
                .flatMap(group -> group.getSampleIdentifiers().stream().map(SampleIdentifier::getSampleId))
                .collect(Collectors.toList());

        List<Sample> samples = sampleService.fetchSamples(studyIds, sampleIds, Projection.SUMMARY.name());

        MultiKeyMap sampleSet = new MultiKeyMap();

        samples.stream()
                .forEach(sample -> sampleSet.put(sample.getCancerStudyIdentifier(), sample.getStableId(), sample));

        // samples for each group
        List<List<Sample>> groupedSamples = interceptedGroupFilter.getGroups().stream()
                .map(group -> group.getSampleIdentifiers().stream()
                        .map(sampleIdentifier -> (Sample) sampleSet.get(sampleIdentifier.getStudyId(),
                                sampleIdentifier.getSampleId()))
                        .filter(sample -> sample != null).collect(Collectors.toList()))
                .filter(validSamples -> validSamples.size() > 0).collect(Collectors.toList());

        return new ResponseEntity<>(fetchClinicalDataEnrichemnts(groupedSamples), HttpStatus.OK);
    }

    private List<ClinicalDataEnrichment> fetchClinicalDataEnrichemnts(List<List<Sample>> groupedSamples) {

        List<ClinicalDataEnrichment> clinicalEnrichments = new ArrayList<ClinicalDataEnrichment>();

        if (!groupedSamples.isEmpty()) {
            Set<String> studyIds = groupedSamples.stream()
                    .flatMap(samples -> samples.stream().map(Sample::getCancerStudyIdentifier))
                    .collect(Collectors.toSet());

            List<ClinicalAttribute> clinicalAttributes = clinicalAttributeService
                    .fetchClinicalAttributes(new ArrayList<String>(studyIds), "SUMMARY");

            // remove all duplicate attributes
            Map<String, ClinicalAttribute> clinicalAttributesByUniqId = clinicalAttributes.stream()
                    .collect(Collectors.toMap(c -> c.getAttrId() + c.getPatientAttribute(), c -> c, (e1, e2) -> e2));

            clinicalAttributes = new ArrayList<>(clinicalAttributesByUniqId.values());

            clinicalEnrichments.addAll(
                    clinicalDataEnrichmentUtil.createEnrichmentsForCategoricalData(clinicalAttributes, groupedSamples));

            clinicalEnrichments.addAll(
                    clinicalDataEnrichmentUtil.createEnrichmentsForNumericData(clinicalAttributes, groupedSamples));
        }
        return clinicalEnrichments;
    }

}
