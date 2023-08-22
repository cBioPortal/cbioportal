package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.model.ClinicalEventKeyCode;
import org.cbioportal.service.TreatmentService;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.util.StudyViewFilterApplier;
import org.cbioportal.web.util.StudyViewFilterUtil;
import org.cbioportal.webparam.PagingConstants;
import org.cbioportal.webparam.SampleIdentifier;
import org.cbioportal.webparam.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@PublicApi
@RestController
@Validated
@Api(tags = "Treatments", description = " ")
public class TreatmentController {
    @Autowired
    private ApplicationContext applicationContext;
    TreatmentController instance;
    @PostConstruct
    private void init() {
        instance = applicationContext.getBean(TreatmentController.class);
    }
    
    @Autowired
    private StudyViewFilterUtil studyViewFilterUtil;
    @Autowired
    private StudyViewFilterUtil filterUtil;

    @Autowired
    private TreatmentService treatmentService;
    
    @Autowired
    private StudyViewFilterApplier studyViewFilterApplier;

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/treatments/patient", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all patient level treatments")
    public ResponseEntity<List<PatientTreatmentRow>> getAllPatientTreatments(
        @ApiParam(required = false, defaultValue = "Agent")
        @RequestParam(name = "tier", required = false, defaultValue = "Agent")
        ClinicalEventKeyCode tier,
        
        @ApiParam(required = true, value = "Study view filter")
        @Valid
        @RequestBody(required = false)
        StudyViewFilter studyViewFilter,
        
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies")
        Collection<String> involvedCancerStudies,
        
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid
        @RequestAttribute(required = false, value = "interceptedStudyViewFilter")
        StudyViewFilter interceptedStudyViewFilter
    ) {
        boolean singleStudyUnfiltered = studyViewFilterUtil.isSingleStudyUnfiltered(interceptedStudyViewFilter);
        List<PatientTreatmentRow> treatments = 
            instance.cachableGetAllPatientTreatments(tier, interceptedStudyViewFilter, singleStudyUnfiltered);
        return new ResponseEntity<>(treatments, HttpStatus.OK);
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabled() && #singleStudyUnfiltered"
    )
    public List<PatientTreatmentRow> cachableGetAllPatientTreatments(
        ClinicalEventKeyCode tier, StudyViewFilter interceptedStudyViewFilter, boolean singleStudyUnfiltered
    ) {
        List<SampleIdentifier> sampleIdentifiers = studyViewFilterApplier.apply(interceptedStudyViewFilter);
        List<String> sampleIds = new ArrayList<>();
        List<String> studyIds = new ArrayList<>();
        filterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);

        return treatmentService.getAllPatientTreatmentRows(sampleIds, studyIds, tier);
    }


    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/treatments/sample", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all sample level treatments")
    public ResponseEntity<List<SampleTreatmentRow>> getAllSampleTreatments(
        @ApiParam(required = false, defaultValue = "Agent")
        @RequestParam(name = "tier", required = false, defaultValue = "Agent")
        ClinicalEventKeyCode tier,

        @ApiParam(required = true, value = "Study view filter")
        @Valid
        @RequestBody(required = false) 
        StudyViewFilter studyViewFilter,
        
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies")
        Collection<String> involvedCancerStudies,
        
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid
        @RequestAttribute(required = false, value = "interceptedStudyViewFilter")
        StudyViewFilter interceptedStudyViewFilter
    ) {
        boolean singleStudyUnfiltered = studyViewFilterUtil.isSingleStudyUnfiltered(interceptedStudyViewFilter);
        List<SampleTreatmentRow> treatments = 
            instance.cacheableGetAllSampleTreatments(tier, interceptedStudyViewFilter, singleStudyUnfiltered);
        return new ResponseEntity<>(treatments, HttpStatus.OK);
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabled() && #singleStudyUnfiltered"
    )
    public List<SampleTreatmentRow> cacheableGetAllSampleTreatments(
        ClinicalEventKeyCode tier, StudyViewFilter interceptedStudyViewFilter, boolean singleStudyUnfiltered
    ) {
        List<SampleIdentifier> sampleIdentifiers = studyViewFilterApplier.apply(interceptedStudyViewFilter);
        List<String> sampleIds = new ArrayList<>();
        List<String> studyIds = new ArrayList<>();
        filterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);

        return treatmentService.getAllSampleTreatmentRows(sampleIds, studyIds, tier);
    }

    @PreAuthorize("hasPermission(#studyIds, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/treatments/display-patient", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Should patient level treatments be displayed")
    public ResponseEntity<Boolean> getContainsTreatmentData(
        @ApiParam(required = false, defaultValue = "Agent")
        @RequestParam(name = "tier", required = false, defaultValue = "Agent")
        ClinicalEventKeyCode tier,
        
        @ApiParam(required = true, value = "List of Study IDs")
        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
        @RequestBody
        Set<String> studyIds
    ) {
        Boolean containsTreatmentData = instance.cacheableGetContainsTreatmentData(studyIds, tier);
        return new ResponseEntity<>(containsTreatmentData, HttpStatus.OK);
    }

    // Caching enabled for any number of studies as the requests contains only studyIds and the response is a boolean
    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    public Boolean cacheableGetContainsTreatmentData(Set<String> studyIds, ClinicalEventKeyCode tier) {
        return treatmentService.containsTreatmentData(new ArrayList<>(studyIds), tier);
    }

    @PreAuthorize("hasPermission(#studyIds, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/treatments/display-sample", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Should sample level treatments be displayed")
    public ResponseEntity<Boolean> getContainsSampleTreatmentData(
        @ApiParam(required = false, defaultValue = "Agent")
        @RequestParam(name = "tier", required = false, defaultValue = "Agent")
        ClinicalEventKeyCode tier,
        
        @ApiParam(required = true, value = "List of Study IDs")
        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
        @RequestBody
        Set<String> studyIds
    ) {
        Boolean containsTreatmentData = instance.cacheableGetContainsSampleTreatmentData(studyIds, tier);
        return new ResponseEntity<>(containsTreatmentData, HttpStatus.OK);
    }

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    public Boolean cacheableGetContainsSampleTreatmentData(Set<String> studyIds, ClinicalEventKeyCode tier) {
        return treatmentService.containsSampleTreatmentData(new ArrayList<>(studyIds), tier);
    }
}
