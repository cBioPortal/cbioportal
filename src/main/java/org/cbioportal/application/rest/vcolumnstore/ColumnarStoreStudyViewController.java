package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Hidden;
import org.cbioportal.application.rest.mapper.SampleMapper;
import org.cbioportal.application.rest.response.SampleDTO;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.cbioportal.studyview.StudyViewService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/column-store")
@Profile("clickhouse")
public class ColumnarStoreStudyViewController {

    private final StudyViewService studyViewService;

    public ColumnarStoreStudyViewController(StudyViewService studyViewService) {
        this.studyViewService = studyViewService;
    }

    @Hidden
    @PostMapping(value = "/v1/filtered-samples/fetch",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SampleDTO>> fetchFilteredSamples(
            @RequestParam(defaultValue = "false") Boolean negateFilters,
            @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
            @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter,
            @RequestBody(required = false) StudyViewFilter studyViewFilter) {
        return ResponseEntity.ok(SampleMapper.INSTANCE.toDtos(studyViewService.getFilteredSamples(studyViewFilter))
        );
    }

    @Hidden // should unhide when we remove legacy controller
    @PostMapping(value = "/v1/mutated-genes/fetch",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AlterationCountByGene>> fetchMutatedGenes(
            @RequestBody(required = false) StudyViewFilter studyViewFilter
    ) throws StudyNotFoundException {
        return ResponseEntity.ok(studyViewService.getMutatedGenes(studyViewFilter)
        );
    }
}
