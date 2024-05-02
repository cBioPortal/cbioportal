package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.cbioportal.model.ClinicalEventKeyCode;
import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.service.TreatmentService;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.util.StudyViewFilterApplier;
import org.cbioportal.web.util.StudyViewFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = "Treatments", description = " ")
public class TreatmentController {
    @Autowired
    private ApplicationContext applicationContext;
    TreatmentController instance;
    
     
    @Autowired
    private StudyViewFilterUtil studyViewFilterUtil;
    @Autowired
    private StudyViewFilterUtil filterUtil;

    @Autowired
    private TreatmentService treatmentService;
    
    @Autowired
    private StudyViewFilterApplier studyViewFilterApplier;

    private TreatmentController getInstance() {
        if (Objects.isNull(instance)) {
           instance = applicationContext.getBean(TreatmentController.class); 
        }
        return instance;
    }
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/treatments/patient", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all patient level treatments")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = PatientTreatmentRow.class))))
    public ResponseEntity<List<PatientTreatmentRow>> getAllPatientTreatments(
        @Parameter(required = false )
        @RequestParam(name = "tier", required = false, defaultValue = "Agent")
        ClinicalEventKeyCode tier,
        
        @Parameter(required = true, description = "Study view filter")
        @Valid
        @RequestBody(required = false) 
        StudyViewFilter studyViewFilter,
        
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies")
        Collection<String> involvedCancerStudies,
        
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid
        @RequestAttribute(required = false, value = "interceptedStudyViewFilter")
        StudyViewFilter interceptedStudyViewFilter
    ) {
        boolean unfilteredQuery = studyViewFilterUtil.isUnfilteredQuery(interceptedStudyViewFilter);
        List<PatientTreatmentRow> treatments = 
            this.getInstance().cachableGetAllPatientTreatments(tier, interceptedStudyViewFilter, unfilteredQuery);
        return new ResponseEntity<>(treatments, HttpStatus.OK);
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabled() && #unfilteredQuery"
    )
    public List<PatientTreatmentRow> cachableGetAllPatientTreatments(
        ClinicalEventKeyCode tier, StudyViewFilter interceptedStudyViewFilter, boolean unfilteredQuery
    ) {
        List<SampleIdentifier> sampleIdentifiers = studyViewFilterApplier.apply(interceptedStudyViewFilter);
        List<String> sampleIds = new ArrayList<>();
        List<String> studyIds = new ArrayList<>();
        filterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);

        return treatmentService.getAllPatientTreatmentRows(sampleIds, studyIds, tier);
    }


    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/treatments/sample", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all sample level treatments")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = SampleTreatmentRow.class))))
    public ResponseEntity<List<SampleTreatmentRow>> getAllSampleTreatments(
        @Schema(defaultValue = "Agent")
        @Parameter(required = false)
        @RequestParam(name = "tier", required = false, defaultValue = "Agent")
        ClinicalEventKeyCode tier,

        @Parameter(required = true, description = "Study view filter")
        @Valid
        @RequestBody(required = false) 
        StudyViewFilter studyViewFilter,
        
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies")
        Collection<String> involvedCancerStudies,
        
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid
        @RequestAttribute(required = false, value = "interceptedStudyViewFilter")
        StudyViewFilter interceptedStudyViewFilter
    ) {
        boolean unfilteredQuery = studyViewFilterUtil.isUnfilteredQuery(interceptedStudyViewFilter);
        List<SampleTreatmentRow> treatments = 
            this.getInstance().cacheableGetAllSampleTreatments(tier, interceptedStudyViewFilter, unfilteredQuery);
        return new ResponseEntity<>(treatments, HttpStatus.OK);
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabled() && #unfilteredQuery"
    )
    public List<SampleTreatmentRow> cacheableGetAllSampleTreatments(
        ClinicalEventKeyCode tier, StudyViewFilter interceptedStudyViewFilter, boolean unfilteredQuery
    ) {
        List<SampleIdentifier> sampleIdentifiers = studyViewFilterApplier.apply(interceptedStudyViewFilter);
        List<String> sampleIds = new ArrayList<>();
        List<String> studyIds = new ArrayList<>();
        filterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);

        return treatmentService.getAllSampleTreatmentRows(sampleIds, studyIds, tier);
    }

    @PreAuthorize("hasPermission(#studyIds, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/treatments/display-patient", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Should patient level treatments be displayed")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = Boolean.class)))
    public ResponseEntity<Boolean> getContainsTreatmentData(
        @Schema(defaultValue = "Agent")
        @Parameter(required = false)
        @RequestParam(name = "tier", required = false, defaultValue = "Agent")
        ClinicalEventKeyCode tier,
        
        @Parameter(required = true, description = "List of Study IDs")
        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
        @RequestBody
        Set<String> studyIds
    ) {
        Boolean containsTreatmentData = this.getInstance().cacheableGetContainsTreatmentData(studyIds, tier);
        return new ResponseEntity<>(containsTreatmentData, HttpStatus.OK);
    }

    // Caching enabled for any number of studies as the requests contains only studyIds and the response is a boolean
    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    public Boolean cacheableGetContainsTreatmentData(Set<String> studyIds, ClinicalEventKeyCode tier) {
        return treatmentService.containsTreatmentData(new ArrayList<>(studyIds), tier);
    }

    @PreAuthorize("hasPermission(#studyIds, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/treatments/display-sample", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Should sample level treatments be displayed")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = Boolean.class)))
    public ResponseEntity<Boolean> getContainsSampleTreatmentData(
        @Schema(defaultValue = "Agent")
        @Parameter(required = false)
        @RequestParam(name = "tier", required = false, defaultValue = "Agent")
        ClinicalEventKeyCode tier,
        
        @Parameter(required = true, description = "List of Study IDs")
        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
        @RequestBody
        Set<String> studyIds
    ) {
        Boolean containsTreatmentData = this.getInstance().cacheableGetContainsSampleTreatmentData(studyIds, tier);
        return new ResponseEntity<>(containsTreatmentData, HttpStatus.OK);
    }

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    public Boolean cacheableGetContainsSampleTreatmentData(Set<String> studyIds, ClinicalEventKeyCode tier) {
        return treatmentService.containsSampleTreatmentData(new ArrayList<>(studyIds), tier);
    }
}
