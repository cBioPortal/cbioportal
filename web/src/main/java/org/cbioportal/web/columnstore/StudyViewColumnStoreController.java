package org.cbioportal.web.columnstore;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.Sample;
import org.cbioportal.service.StudyViewService;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.webparam.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@InternalApi
@RestController
@Validated
@Api(tags = "Study View Column Store")
public class StudyViewColumnStoreController {

    @Autowired
    private StudyViewService studyViewService;
    
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @PostMapping(value = "/column-store/filtered-samples/fetch",
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch sample IDs by study view filter")
    public ResponseEntity<List<Sample>> fetchFilteredSamples(
        @ApiParam("Whether to negate the study view filters")
        @RequestParam(defaultValue = "false") Boolean negateFilters,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter,
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter) {
        return new ResponseEntity<>(
            studyViewService.getFilteredSamplesFromColumnstore(interceptedStudyViewFilter),
            HttpStatus.OK
        );
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @PostMapping(value = "/column-store/mutated-genes/fetch",
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch mutated genes by study view filter")
    public ResponseEntity<List<AlterationCountByGene>> fetchMutatedGenes(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter
    ) {
        return new ResponseEntity<>(
            studyViewService.getMutatedGenesFromColumnstore(interceptedStudyViewFilter),
            HttpStatus.OK
        );
    }
}
