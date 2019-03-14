package org.cbioportal.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.collections.map.MultiKeyMap;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@InternalApi
@RestController
@Validated
@Api(tags = "Clinical Data Enrichments", description = " ")
public class ClinicalDataEnrichmentController {

    @Autowired
    private ClinicalDataEnrichmentUtil clinicalDataEnrichmentUtil;

    @Autowired
    private ClinicalAttributeService clinicalAttributeService;

    @Autowired
    private SampleService sampleService;

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/clinical-data-enrichments/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch clinical data enrichments for the sample groups")
    public ResponseEntity<List<ClinicalDataEnrichment>> fetchClinicalEnrichments(
            @ApiParam(required = true, value = "List of altered and unaltered Sample/Patient IDs")
            @Valid @RequestBody(required = false) GroupFilter groupFilter,
            @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
            @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
            @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
            @RequestAttribute(required = false, value = "interceptedGroupFilter") GroupFilter interceptedGroupFilter) {

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

            clinicalEnrichments.addAll(
                    clinicalDataEnrichmentUtil.createEnrichmentsForCategoricalData(clinicalAttributes, groupedSamples));

            clinicalEnrichments.addAll(
                    clinicalDataEnrichmentUtil.createEnrichmentsForNumericData(clinicalAttributes, groupedSamples));
        }
        return clinicalEnrichments;
    }

}
