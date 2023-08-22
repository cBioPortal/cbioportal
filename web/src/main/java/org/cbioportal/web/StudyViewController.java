package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.*;
import org.cbioportal.service.*;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.service.util.ClinicalAttributeUtil;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.util.*;
import org.cbioportal.webparam.ClinicalDataBinCountFilter;
import org.cbioportal.webparam.ClinicalDataCountFilter;
import org.cbioportal.webparam.ClinicalDataFilter;
import org.cbioportal.webparam.ClinicalDataType;
import org.cbioportal.webparam.DataBinMethod;
import org.cbioportal.webparam.Direction;
import org.cbioportal.webparam.GenericAssayDataBinCountFilter;
import org.cbioportal.webparam.GenericAssayDataCountFilter;
import org.cbioportal.webparam.GenericAssayDataFilter;
import org.cbioportal.webparam.GenomicDataBinCountFilter;
import org.cbioportal.webparam.PagingConstants;
import org.cbioportal.webparam.Projection;
import org.cbioportal.webparam.SampleIdentifier;
import org.cbioportal.webparam.StudyViewFilter;
import org.cbioportal.webparam.sort.ClinicalDataSortBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@InternalApi
@RestController
@Validated
@Api(tags = "Study View", description = " ")
public class StudyViewController {

    public static final int CLINICAL_TAB_MAX_PAGE_SIZE = 1000000;
    
    @Autowired
    private ApplicationContext applicationContext;
    StudyViewController instance;
    @PostConstruct
    private void init() {
        instance = applicationContext.getBean(StudyViewController.class);
    }

    @Autowired
    private StudyViewFilterApplier studyViewFilterApplier;
    @Autowired
    private ClinicalDataService clinicalDataService;
    @Autowired
    private ClinicalDataFetcher clinicalDataFetcher;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private StudyViewFilterUtil studyViewFilterUtil;
    @Autowired
    private ClinicalAttributeService clinicalAttributeService;
    @Autowired
    private ViolinPlotService violinPlotService;
    @Autowired
    private ClinicalAttributeUtil clinicalAttributeUtil;
    @Autowired
    private SampleListService sampleListService;
    @Autowired
    private StudyViewService studyViewService;
    @Autowired
    private ClinicalDataBinUtil clinicalDataBinUtil;
    @Autowired
    private ClinicalEventService clinicalEventService;

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/clinical-data-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch clinical data counts by study view filter")
    public ResponseEntity<List<ClinicalDataCountItem>> fetchClinicalDataCounts(
        @ApiParam(required = true, value = "Clinical data count filter")
        @Valid @RequestBody(required = false) ClinicalDataCountFilter clinicalDataCountFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedClinicalDataCountFilter") ClinicalDataCountFilter interceptedClinicalDataCountFilter) {

        List<ClinicalDataFilter> attributes = interceptedClinicalDataCountFilter.getAttributes();
        StudyViewFilter studyViewFilter = interceptedClinicalDataCountFilter.getStudyViewFilter();
        
        if (attributes.size() == 1) {
            studyViewFilterUtil.removeSelfFromFilter(attributes.get(0).getAttributeId(), studyViewFilter);
        }
        boolean singleStudyUnfiltered = studyViewFilterUtil.isSingleStudyUnfiltered(studyViewFilter);
        List<ClinicalDataCountItem> result = 
                   instance.cachedClinicalDataCounts(interceptedClinicalDataCountFilter,singleStudyUnfiltered);
        return new ResponseEntity<>(result, HttpStatus.OK);
                        
    }
    
    @Cacheable(
               cacheResolver = "staticRepositoryCacheOneResolver",
               condition = "@cacheEnabledConfig.getEnabled() && #singleStudyUnfiltered" 
    )
    public List<ClinicalDataCountItem> cachedClinicalDataCounts(
                 ClinicalDataCountFilter interceptedClinicalDataCountFilter, boolean singleStudyUnfiltered
    ) {                
        List<ClinicalDataFilter> attributes = interceptedClinicalDataCountFilter.getAttributes();  
        StudyViewFilter studyViewFilter = interceptedClinicalDataCountFilter.getStudyViewFilter();                            
        if (attributes.size() == 1) {
            studyViewFilterUtil.removeSelfFromFilter(attributes.get(0).getAttributeId(), studyViewFilter);
        }
        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(studyViewFilter);
        
        if (filteredSampleIdentifiers.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);
        
        List<ClinicalDataCountItem> result = clinicalDataService.fetchClinicalDataCounts(
            studyIds, sampleIds, attributes.stream().map(a -> a.getAttributeId()).collect(Collectors.toList()));
        
        return result;
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/clinical-data-bin-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch clinical data bin counts by study view filter")
    public ResponseEntity<List<ClinicalDataBin>> fetchClinicalDataBinCounts(
        @ApiParam("Method for data binning")
        @RequestParam(defaultValue = "DYNAMIC") DataBinMethod dataBinMethod,
        @ApiParam(required = true, value = "Clinical data bin count filter")
        @Valid @RequestBody(required = false) ClinicalDataBinCountFilter clinicalDataBinCountFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedClinicalDataBinCountFilter") ClinicalDataBinCountFilter interceptedClinicalDataBinCountFilter
    ) {
        StudyViewFilter studyViewFilter = clinicalDataBinUtil.removeSelfFromFilter(interceptedClinicalDataBinCountFilter);
        boolean singleStudyUnfiltered = studyViewFilterUtil.isSingleStudyUnfiltered(studyViewFilter);
        List<ClinicalDataBin> clinicalDataBins = 
            instance.cachableFetchClinicalDataBinCounts(dataBinMethod, interceptedClinicalDataBinCountFilter, singleStudyUnfiltered);

        return new ResponseEntity<>(clinicalDataBins, HttpStatus.OK);
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabled() && #singleStudyUnfiltered"
    )
    public List<ClinicalDataBin> cachableFetchClinicalDataBinCounts(
        DataBinMethod dataBinMethod,
        ClinicalDataBinCountFilter interceptedClinicalDataBinCountFilter,
        boolean singleStudyUnfiltered
    ) {
        return clinicalDataBinUtil.fetchClinicalDataBinCounts(
            dataBinMethod,
            interceptedClinicalDataBinCountFilter,
            // we don't need to remove filter again since we already did it in the previous step 
            false 
        );
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/custom-data-bin-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch custom data bin counts by study view filter")
    public ResponseEntity<List<ClinicalDataBin>> fetchCustomDataBinCounts(
        @ApiParam("Method for data binning")
        @RequestParam(defaultValue = "DYNAMIC") DataBinMethod dataBinMethod,
        @ApiParam(required = true, value = "Clinical data bin count filter")
        @Valid @RequestBody(required = false) ClinicalDataBinCountFilter clinicalDataBinCountFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedClinicalDataBinCountFilter") ClinicalDataBinCountFilter interceptedClinicalDataBinCountFilter
    ) {
        // TODO code shared with ClinicalDataController.fetchCustomDataCounts
        List<ClinicalDataBinFilter> attributes = interceptedClinicalDataBinCountFilter.getAttributes();
        StudyViewFilter studyViewFilter = interceptedClinicalDataBinCountFilter.getStudyViewFilter();
        if (attributes.size() == 1) {
            studyViewFilterUtil.removeSelfCustomDataFromFilter(attributes.get(0).getAttributeId(), studyViewFilter);
        }
        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(studyViewFilter);

        if (filteredSampleIdentifiers.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }

        final List<ClinicalDataBin> clinicalDataBins = clinicalDataBinUtil.fetchCustomDataBinCounts(dataBinMethod, interceptedClinicalDataBinCountFilter, false);

        return new ResponseEntity<>(clinicalDataBins, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>',T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/mutated-genes/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch mutated genes by study view filter")
    public ResponseEntity<List<AlterationCountByGene>> fetchMutatedGenes(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter
    ) throws StudyNotFoundException {
        boolean singleStudyUnfiltered = studyViewFilterUtil.isSingleStudyUnfiltered(interceptedStudyViewFilter);
        List<AlterationCountByGene> alterationCountByGenes = instance.cachedFetchMutatedGenes(interceptedStudyViewFilter, singleStudyUnfiltered);
        return new ResponseEntity<>(alterationCountByGenes, HttpStatus.OK);
    }
    
    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabled() && #singleStudyUnfiltered"
    )
    public List<AlterationCountByGene> cachedFetchMutatedGenes(
        StudyViewFilter interceptedStudyViewFilter, boolean singleStudyUnfiltered
    ) throws StudyNotFoundException {
        AlterationFilter annotationFilters = interceptedStudyViewFilter.getAlterationFilter();

        List<SampleIdentifier> sampleIdentifiers = studyViewFilterApplier.apply(interceptedStudyViewFilter);
        List<AlterationCountByGene> alterationCountByGenes = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(sampleIdentifiers)) {
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);
            alterationCountByGenes = studyViewService.getMutationAlterationCountByGenes(studyIds, sampleIds, annotationFilters);
        }
        return alterationCountByGenes;
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/structuralvariant-genes/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch structural variant genes by study view filter")
    public ResponseEntity<List<AlterationCountByGene>> fetchStructuralVariantGenes(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. This attribute is needed for the @PreAuthorize tag above.
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter
    ) throws StudyNotFoundException {

        boolean singleStudyUnfiltered = studyViewFilterUtil.isSingleStudyUnfiltered(interceptedStudyViewFilter);
        List<AlterationCountByGene> alterationCountByGenes = 
            instance.cacheableFetchStructuralVariantGenes(interceptedStudyViewFilter, singleStudyUnfiltered);
        return new ResponseEntity<>(alterationCountByGenes, HttpStatus.OK);
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabled() && #singleStudyUnfiltered"
    )
    public List<AlterationCountByGene> cacheableFetchStructuralVariantGenes(
        StudyViewFilter interceptedStudyViewFilter, boolean singleStudyUnfiltered
    ) throws StudyNotFoundException {
        AlterationFilter annotationFilters = interceptedStudyViewFilter.getAlterationFilter();

        List<SampleIdentifier> sampleIdentifiers = studyViewFilterApplier.apply(interceptedStudyViewFilter);
        List<AlterationCountByGene> alterationCountByGenes = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(sampleIdentifiers)) {
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);
            alterationCountByGenes = studyViewService.getStructuralVariantAlterationCountByGenes(studyIds, sampleIds, annotationFilters);
        }
        return alterationCountByGenes;
    }
    
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/structuralvariant-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch structural variant genes by study view filter")
    public ResponseEntity<List<AlterationCountByStructuralVariant>> fetchStructuralVariantCounts(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. This attribute is needed for the @PreAuthorize tag above.
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter
    ) throws StudyNotFoundException {

        boolean singleStudyUnfiltered = studyViewFilterUtil.isSingleStudyUnfiltered(interceptedStudyViewFilter);
        List<AlterationCountByStructuralVariant> alterationCountByStructuralVariants = 
            instance.cacheableFetchStructuralVariantCounts(interceptedStudyViewFilter, singleStudyUnfiltered);
        return new ResponseEntity<>(alterationCountByStructuralVariants, HttpStatus.OK);
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabled() && #singleStudyUnfiltered"
    )
    public List<AlterationCountByStructuralVariant> cacheableFetchStructuralVariantCounts(
        StudyViewFilter interceptedStudyViewFilter, boolean singleStudyUnfiltered
    ) throws StudyNotFoundException {

        List<SampleIdentifier> sampleIdentifiers = studyViewFilterApplier.apply(interceptedStudyViewFilter);
        if(CollectionUtils.isNotEmpty(sampleIdentifiers)) {
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);
            return studyViewService.getStructuralVariantAlterationCounts(studyIds, sampleIds, interceptedStudyViewFilter.getAlterationFilter());
        }
        return new ArrayList<>();
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/cna-genes/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch CNA genes by study view filter")
    public ResponseEntity<List<CopyNumberCountByGene>> fetchCNAGenes(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter
    ) throws StudyNotFoundException {
        boolean singleStudyUnfiltered = studyViewFilterUtil.isSingleStudyUnfiltered(interceptedStudyViewFilter);
        List<CopyNumberCountByGene> copyNumberCountByGenes = instance.cacheableFetchCNAGenes(interceptedStudyViewFilter, singleStudyUnfiltered);
        return new ResponseEntity<>(copyNumberCountByGenes, HttpStatus.OK);
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabled() && #singleStudyUnfiltered"
    )
    public List<CopyNumberCountByGene> cacheableFetchCNAGenes(StudyViewFilter interceptedStudyViewFilter, boolean singleStudyUnfiltered) throws StudyNotFoundException {
        AlterationFilter alterationFilter = interceptedStudyViewFilter.getAlterationFilter();

        List<SampleIdentifier> sampleIdentifiers = studyViewFilterApplier.apply(interceptedStudyViewFilter);
        List<CopyNumberCountByGene> copyNumberCountByGenes = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(sampleIdentifiers)) {
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);
            copyNumberCountByGenes = studyViewService.getCNAAlterationCountByGenes(studyIds, sampleIds, alterationFilter);
        }
        return copyNumberCountByGenes;
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/filtered-samples/fetch", method = RequestMethod.POST,
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

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();

        studyViewFilterUtil.extractStudyAndSampleIds(
            studyViewFilterApplier.apply(interceptedStudyViewFilter, negateFilters), studyIds, sampleIds);

        List<Sample> result = new ArrayList<>();
        if (!sampleIds.isEmpty()) {
            result = sampleService.fetchSamples(studyIds, sampleIds, Projection.ID.name());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/molecular-profile-sample-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch sample counts by study view filter")
    public ResponseEntity<List<GenomicDataCount>> fetchMolecularProfileSampleCounts(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter
    )
    {
        boolean singleStudyUnfiltered = studyViewFilterUtil.isSingleStudyUnfiltered(interceptedStudyViewFilter);
        List<GenomicDataCount> sampleCounts = instance.cacheableFetchMolecularProfileSampleCounts(interceptedStudyViewFilter, singleStudyUnfiltered);
        return new ResponseEntity<>(sampleCounts, HttpStatus.OK);
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabled() && #singleStudyUnfiltered"
    )
    public List<GenomicDataCount> cacheableFetchMolecularProfileSampleCounts(
        StudyViewFilter interceptedStudyViewFilter, boolean singleStudyUnfiltered
    ) {
        List<SampleIdentifier> sampleIdentifiers = studyViewFilterApplier.apply(interceptedStudyViewFilter);
        List<GenomicDataCount> genomicDataCounts = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(sampleIdentifiers)) {
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);
            genomicDataCounts = studyViewService.getGenomicDataCounts(studyIds, sampleIds);
        }
        return genomicDataCounts;
    }

    private static boolean isLogScalePossibleForAttribute(String clinicalAttributeId) {
        return clinicalAttributeId.equals("MUTATION_COUNT");
    }

    private static double logScale(double val) {
        return Math.log(1+val);
    }
    
    private static double parseValueLog(ClinicalData c) {
        return StudyViewController.logScale(Double.parseDouble(c.getAttrValue()));
    }
    
    private static double parseValueLinear(ClinicalData c) {
        return Double.parseDouble(c.getAttrValue());
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/clinical-data-density-plot/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch clinical data density plot bins by study view filter")
    @Validated
    public ResponseEntity<DensityPlotData> fetchClinicalDataDensityPlot(
        @ApiParam(required = true, value = "Clinical Attribute ID of the X axis")
        @RequestParam String xAxisAttributeId,
        @ApiParam("Number of the bins in X axis")
        @RequestParam(defaultValue = "50") Integer xAxisBinCount,
        @ApiParam("Starting point of the X axis, if different than smallest value")
        @RequestParam(required = false) BigDecimal xAxisStart,
        @ApiParam("Starting point of the X axis, if different than largest value")
        @RequestParam(required = false) BigDecimal xAxisEnd,
        @ApiParam(required = true, value = "Clinical Attribute ID of the Y axis")
        @RequestParam String yAxisAttributeId,
        @ApiParam("Number of the bins in Y axis")
        @RequestParam(defaultValue = "50") Integer yAxisBinCount,
        @ApiParam("Starting point of the Y axis, if different than smallest value")
        @RequestParam(required = false) BigDecimal yAxisStart,
        @ApiParam("Starting point of the Y axis, if different than largest value")
        @RequestParam(required = false) BigDecimal yAxisEnd,
        @ApiParam(value="Use log scale for X axis")
        @RequestParam(required = false, defaultValue = "false") Boolean xAxisLogScale,
        @ApiParam(value="Use log scale for Y axis", defaultValue = "false")
        @RequestParam(required = false, defaultValue = "false") Boolean yAxisLogScale,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter,
        @ApiParam(required = true, value = "Study view filter")
        @RequestBody(required = false) StudyViewFilter studyViewFilter) {

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(studyViewFilterApplier.apply(interceptedStudyViewFilter), studyIds, sampleIds);
        DensityPlotData result = new DensityPlotData();
        result.setBins(new ArrayList<>());
        if (sampleIds.isEmpty()) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        List<String> sampleAttributeIds = new ArrayList<>();
        List<String> patientAttributeIds = new ArrayList<>();
        
        List<ClinicalAttribute> clinicalAttributes = clinicalAttributeService
                .getClinicalAttributesByStudyIdsAndAttributeIds(studyIds,
                        Arrays.asList(xAxisAttributeId, yAxisAttributeId));

        clinicalAttributeUtil.extractCategorizedClinicalAttributes(clinicalAttributes, sampleAttributeIds, patientAttributeIds, patientAttributeIds);
        
        List<String> patientIds = new ArrayList<>();
        List<String> studyIdsOfPatients = new ArrayList<>();
        Map<String, Map<String, List<Sample>>> patientToSamples = null;
        

        if (CollectionUtils.isNotEmpty(patientAttributeIds)) {
            List<Sample> samples = sampleService.fetchSamples(studyIds, sampleIds, Projection.DETAILED.name());
            List<Patient> patients = patientService.getPatientsOfSamples(studyIds, sampleIds);
            patientIds = patients.stream().map(Patient::getStableId).collect(Collectors.toList());
            studyIdsOfPatients = patients.stream().map(Patient::getCancerStudyIdentifier).collect(Collectors.toList());
            patientToSamples = samples.stream().collect(
                Collectors.groupingBy(Sample::getPatientStableId, Collectors.groupingBy(Sample::getCancerStudyIdentifier))
            );
        }

        List<ClinicalData> clinicalDataList = clinicalDataFetcher.fetchClinicalData(
            studyIds, sampleIds, patientIds, studyIdsOfPatients, sampleAttributeIds, patientAttributeIds, null
        );
        
        List<ClinicalData> sampleClinicalDataList;
        // put all clinical data into sample form
        if (CollectionUtils.isNotEmpty(patientAttributeIds)) {
            sampleClinicalDataList = new ArrayList<>();
            for (ClinicalData d: clinicalDataList) {
                if (d.getSampleId() == null) {
                    // null sample id means its a patient data, 
                    //  we need to distribute the value to samples
                    List<Sample> samplesForPatient = patientToSamples.get(
                        d.getPatientId()
                    ).get(d.getStudyId());
                    if (samplesForPatient != null) {
                        for (Sample s: samplesForPatient) {
                            ClinicalData newData = new ClinicalData();
                            newData.setAttrId(d.getAttrId());
                            newData.setPatientId(d.getPatientId());
                            newData.setStudyId(d.getStudyId());
                            newData.setAttrValue(d.getAttrValue());
                            newData.setSampleId(s.getStableId());
                            sampleClinicalDataList.add(newData);
                        }
                    } else {
                        // patient has no samples - this shouldn't happen and could affect the integrity
                        //  of the data analysis
                        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                } else {
                    // if its a sample data, just add it to the list
                    sampleClinicalDataList.add(d);
                }
            }
        } else {
            sampleClinicalDataList = clinicalDataList;
        }

        // clinicalDataMap is a map sampleId->studyId->data
        Map<String, Map<String, List<ClinicalData>>> clinicalDataMap = sampleClinicalDataList.stream().collect(Collectors.groupingBy(ClinicalData::getSampleId, 
                Collectors.groupingBy(ClinicalData::getStudyId)));
        
        List<ClinicalData> filteredClinicalDataList = new ArrayList<>();
        clinicalDataMap.forEach((k, v) -> v.forEach((m, n) -> {
            if (
                // n.size() == 2 means we have clinical data for the sample for both of the queried attributes
                n.size() == 2 && 
                    // check if both of the sample data are numerical
                    NumberUtils.isCreatable(n.get(0).getAttrValue()) && NumberUtils.isCreatable(n.get(1).getAttrValue())) {
                filteredClinicalDataList.addAll(n);
            }
        }));
        if (filteredClinicalDataList.isEmpty()) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        
        Map<Boolean, List<ClinicalData>> partition = filteredClinicalDataList.stream().collect(
            Collectors.partitioningBy(c -> c.getAttrId().equals(xAxisAttributeId)));

        boolean useXLogScale = xAxisLogScale && StudyViewController.isLogScalePossibleForAttribute(xAxisAttributeId);
        boolean useYLogScale = yAxisLogScale && StudyViewController.isLogScalePossibleForAttribute(yAxisAttributeId);
        
        double[] xValues = partition.get(true).stream().mapToDouble(
            useXLogScale ? StudyViewController::parseValueLog : StudyViewController::parseValueLinear
        ).toArray();
        double[] yValues = partition.get(false).stream().mapToDouble(
            useYLogScale ? StudyViewController::parseValueLog : StudyViewController::parseValueLinear
        ).toArray();
        double[] xValuesCopy = Arrays.copyOf(xValues, xValues.length);
        double[] yValuesCopy = Arrays.copyOf(yValues, yValues.length);
        Arrays.sort(xValuesCopy);
        Arrays.sort(yValuesCopy);

        double xAxisStartValue = xAxisStart == null ? xValuesCopy[0] :
            (useXLogScale ? StudyViewController.logScale(xAxisStart.doubleValue()) : xAxisStart.doubleValue());
        double xAxisEndValue = xAxisEnd == null ? xValuesCopy[xValuesCopy.length - 1] :
            (useXLogScale ? StudyViewController.logScale(xAxisEnd.doubleValue()) : xAxisEnd.doubleValue());
        double yAxisStartValue = yAxisStart == null ? yValuesCopy[0] :
            (useYLogScale ? StudyViewController.logScale(yAxisStart.doubleValue()) : yAxisStart.doubleValue());
        double yAxisEndValue = yAxisEnd == null ? yValuesCopy[yValuesCopy.length - 1] :
            (useYLogScale ? StudyViewController.logScale(yAxisEnd.doubleValue()) : yAxisEnd.doubleValue());
        double xAxisBinInterval = (xAxisEndValue - xAxisStartValue) / xAxisBinCount;
        double yAxisBinInterval = (yAxisEndValue - yAxisStartValue) / yAxisBinCount;
        List<DensityPlotBin> bins = result.getBins();
        for (int i = 0; i < xAxisBinCount; i++) {
            for (int j = 0; j < yAxisBinCount; j++) {
                DensityPlotBin densityPlotBin = new DensityPlotBin();
                densityPlotBin.setBinX(new BigDecimal(xAxisStartValue + (i * xAxisBinInterval)));
                densityPlotBin.setBinY(new BigDecimal(yAxisStartValue + (j * yAxisBinInterval)));
                densityPlotBin.setCount(0);
                bins.add(densityPlotBin);
            }
        }

        for (int i = 0; i < xValues.length; i++) {
            double xValue = xValues[i];
            double yValue = yValues[i];
            int xBinIndex = (int) ((xValue - xAxisStartValue) / xAxisBinInterval);
            int yBinIndex = (int) ((yValue - yAxisStartValue) / yAxisBinInterval);
            int index = (int) (((xBinIndex - (xBinIndex == xAxisBinCount ? 1 : 0)) * yAxisBinCount) +
                (yBinIndex - (yBinIndex == yAxisBinCount ? 1 : 0)));
            DensityPlotBin densityPlotBin = bins.get(index);
            densityPlotBin.setCount(densityPlotBin.getCount() + 1);
            BigDecimal xValueBigDecimal = new BigDecimal(xValue);
            BigDecimal yValueBigDecimal = new BigDecimal(yValue);
            if (densityPlotBin.getMinX() != null) {
                if (densityPlotBin.getMinX().compareTo(xValueBigDecimal) > 0) {
                    densityPlotBin.setMinX(xValueBigDecimal);
                }
            } else {
                densityPlotBin.setMinX(xValueBigDecimal);
            }
            if (densityPlotBin.getMaxX() != null) {
                if (densityPlotBin.getMaxX().compareTo(xValueBigDecimal) < 0) {
                    densityPlotBin.setMaxX(xValueBigDecimal);
                }
            } else {
                densityPlotBin.setMaxX(xValueBigDecimal);
            }
            if (densityPlotBin.getMinY() != null) {
                if (densityPlotBin.getMinY().compareTo(yValueBigDecimal) > 0) {
                    densityPlotBin.setMinY(yValueBigDecimal);
                }
            } else {
                densityPlotBin.setMinY(yValueBigDecimal);
            }
            if (densityPlotBin.getMaxY() != null) {
                if (densityPlotBin.getMaxY().compareTo(yValueBigDecimal) < 0) {
                    densityPlotBin.setMaxY(yValueBigDecimal);
                }
            } else {
                densityPlotBin.setMaxY(yValueBigDecimal);
            }
        }
        
        if (xValues.length > 1) {
            // need at least 2 entries in each to compute correlation
            result.setPearsonCorr(new PearsonsCorrelation().correlation(xValues, yValues));
            result.setSpearmanCorr(new SpearmansCorrelation().correlation(xValues, yValues));
        } else {
            // if less than 1 entry, just set 0 correlation
            result.setSpearmanCorr(0.0);
            result.setPearsonCorr(0.0);
        }
        
        // filter out empty bins
        result.setBins(result.getBins().stream().filter((bin)->(bin.getCount() > 0)).collect(Collectors.toList()));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/clinical-data-violin-plots/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch violin plot curves per categorical clinical data value, filtered by study view filter")
    public ResponseEntity<ClinicalViolinPlotData> fetchClinicalDataViolinPlots(
        @ApiParam(required = true, value = "Clinical Attribute ID of the categorical attribute")
        @RequestParam String categoricalAttributeId,
        @ApiParam(required = true, value = "Clinical Attribute ID of the numerical attribute")
        @RequestParam String numericalAttributeId,
        @ApiParam("Starting point of the violin plot axis, if different than smallest value")
        @RequestParam(required = false) BigDecimal axisStart,
        @ApiParam("Ending point  of the violin plot axis, if different than largest value")
        @RequestParam(required = false) BigDecimal axisEnd,
        @ApiParam("Number of points in the curve")
        @RequestParam(required = false, defaultValue = "100") BigDecimal numCurvePoints,
        @ApiParam(value="Use log scale for the numerical attribute")
        @RequestParam(required = false, defaultValue = "false") Boolean logScale,
        @ApiParam(value="Sigma stepsize multiplier")
        @RequestParam(required = false, defaultValue = "1") BigDecimal sigmaMultiplier,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter,
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter) {
        
        ClinicalViolinPlotData result = new ClinicalViolinPlotData();
        
        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        // first get samples that are filtered by all current filters - this will give us
        //  the by-row sample counts
        studyViewFilterUtil.extractStudyAndSampleIds(studyViewFilterApplier.apply(interceptedStudyViewFilter), studyIds, sampleIds);
        List<Sample> filteredSamples = sampleService.fetchSamples(studyIds, sampleIds, Projection.DETAILED.name());

        List<String> studyIdsWithoutNumericalFilter = new ArrayList<>();
        List<String> sampleIdsWithoutNumericalFilter = new ArrayList<>();
        // next, get samples that are filtered without the numerical filter - this will
        //  give us the violin plot data
        if (interceptedStudyViewFilter.getClinicalDataFilters() != null) {
            // Remove numerical clinical data filter, if there is one
            interceptedStudyViewFilter.getClinicalDataFilters().stream()
                .filter(f->f.getAttributeId().equals(numericalAttributeId))
                .findAny()
                .ifPresent(f->interceptedStudyViewFilter.getClinicalDataFilters().remove(f));
        }
        studyViewFilterUtil.extractStudyAndSampleIds(
            studyViewFilterApplier.apply(interceptedStudyViewFilter), 
            studyIdsWithoutNumericalFilter, 
            sampleIdsWithoutNumericalFilter
        );

        if (sampleIds.isEmpty()) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        
        List<String> sampleAttributeIds = new ArrayList<>();
        List<String> patientAttributeIds = new ArrayList<>();

        List<ClinicalAttribute> clinicalAttributes = clinicalAttributeService
            .getClinicalAttributesByStudyIdsAndAttributeIds(studyIds,
                Arrays.asList(categoricalAttributeId, numericalAttributeId));

        clinicalAttributeUtil.extractCategorizedClinicalAttributes(clinicalAttributes, sampleAttributeIds, patientAttributeIds, patientAttributeIds);

        List<String> patientIds = new ArrayList<>();
        List<String> studyIdsOfPatients = new ArrayList<>();
        Map<String, Map<String, List<Sample>>> patientToSamples = null;

        if (CollectionUtils.isNotEmpty(patientAttributeIds)) {
            List<Sample> samplesWithoutNumericalFilter = sampleService.fetchSamples(studyIdsWithoutNumericalFilter, sampleIdsWithoutNumericalFilter, Projection.DETAILED.name());
            List<Patient> patients = patientService.getPatientsOfSamples(studyIdsWithoutNumericalFilter, sampleIdsWithoutNumericalFilter);
            patientIds = patients.stream().map(Patient::getStableId).collect(Collectors.toList());
            studyIdsOfPatients = patients.stream().map(Patient::getCancerStudyIdentifier).collect(Collectors.toList());
            patientToSamples = samplesWithoutNumericalFilter.stream().collect(
                Collectors.groupingBy(Sample::getPatientStableId, Collectors.groupingBy(Sample::getCancerStudyIdentifier))
            );
        }

        List<ClinicalData> clinicalDataList = clinicalDataFetcher.fetchClinicalData(
            studyIdsWithoutNumericalFilter, sampleIdsWithoutNumericalFilter, patientIds, studyIdsOfPatients, sampleAttributeIds, patientAttributeIds, null
        );

        List<ClinicalData> sampleClinicalDataList;
        // put all clinical data into sample form
        if (CollectionUtils.isNotEmpty(patientAttributeIds)) {
            sampleClinicalDataList = new ArrayList<>();
            for (ClinicalData d: clinicalDataList) {
                if (d.getSampleId() == null) {
                    // null sample id means its a patient data, 
                    //  we need to distribute the value to samples
                    List<Sample> samplesForPatient = patientToSamples.get(d.getPatientId()).get(d.getStudyId());
                    if (samplesForPatient != null) {
                        for (Sample s: samplesForPatient) {
                            ClinicalData newData = new ClinicalData();
                            newData.setInternalId(s.getInternalId());
                            newData.setAttrId(d.getAttrId());
                            newData.setPatientId(d.getPatientId());
                            newData.setStudyId(d.getStudyId());
                            newData.setAttrValue(d.getAttrValue());
                            newData.setSampleId(s.getStableId());
                            sampleClinicalDataList.add(newData);
                        }
                    } else {
                        // patient has no samples - this shouldn't happen and could affect the integrity
                        //  of the data analysis
                        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                } else {
                    // if its a sample data, just add it to the list
                    sampleClinicalDataList.add(d);
                }
            }
        } else {
            sampleClinicalDataList = clinicalDataList;
        }

        boolean useLogScale = logScale && StudyViewController.isLogScalePossibleForAttribute(numericalAttributeId);

        
        result = violinPlotService.getClinicalViolinPlotData(
            sampleClinicalDataList,
            filteredSamples,
            axisStart,
            axisEnd,
            numCurvePoints,
            useLogScale,
            sigmaMultiplier
        );

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/sample-lists-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch case list sample counts by study view filter")
    public List<CaseListDataCount> fetchCaseListCounts(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") StudyViewFilter interceptedStudyViewFilter) {

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(interceptedStudyViewFilter);
        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);
        List<SampleList> sampleLists = sampleListService.getAllSampleListsInStudies(studyIds,
                Projection.DETAILED.name());

        HashMap<String, Integer> sampleCountBySampleListId = new HashMap<String, Integer>();

        Map<String, SampleIdentifier> filteredSampleSet = filteredSampleIdentifiers.stream()
                .collect(Collectors.toMap(sampleidentifier -> studyViewFilterUtil
                        .getCaseUniqueKey(sampleidentifier.getStudyId(), sampleidentifier.getSampleId()),
                        Function.identity()));

        for (SampleList sampleList : sampleLists) {
            for (String sampleId : sampleList.getSampleIds()) {
                if (filteredSampleSet.containsKey(
                        studyViewFilterUtil.getCaseUniqueKey(sampleList.getCancerStudyIdentifier(), sampleId))) {
                    Integer count = sampleCountBySampleListId.getOrDefault(sampleList.getStableId(), 0);
                    sampleCountBySampleListId.put(sampleList.getStableId(), count + 1);
                }
            }
        }

        return studyViewFilterUtil
                .categorizeSampleLists(sampleLists)
                .entrySet()
                .stream()
                .map(entry -> {
                    CaseListDataCount dataCount = new CaseListDataCount();
                    dataCount.setValue(entry.getKey());
        
                    Integer count = entry.getValue().stream().mapToInt(sampleList -> {
                        return sampleCountBySampleListId.getOrDefault(sampleList.getStableId(), 0);
                    }).sum();
        
                    dataCount.setCount(count);
                    dataCount.setLabel(entry.getValue().get(0).getName());
        
                    return dataCount;
                })
                .filter(dataCount -> dataCount.getCount() > 0)
                .collect(Collectors.toList());

    }
    
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/genomic-data-bin-counts/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch genomic data bin counts by study view filter")
    public ResponseEntity<List<GenomicDataBin>> fetchGenomicDataBinCounts(
            @ApiParam("Method for data binning") @RequestParam(defaultValue = "DYNAMIC") DataBinMethod dataBinMethod,
            @ApiParam(required = true, value = "Genomic data bin count filter") @Valid @RequestBody(required = false) GenomicDataBinCountFilter genomicDataBinCountFilter,
            @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
            @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
            @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this
                       // attribute is needed for the @PreAuthorize tag above.
            @Valid @RequestAttribute(required = false, value = "interceptedGenomicDataBinCountFilter") GenomicDataBinCountFilter interceptedGenomicDataBinCountFilter) {

        return new ResponseEntity<>(studyViewFilterApplier.getDataBins(dataBinMethod, interceptedGenomicDataBinCountFilter), HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/genomic-data-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch genomic data counts by GenomicDataCountFilter")
    public ResponseEntity<List<GenomicDataCountItem>> fetchGenomicDataCounts(
        @ApiParam(required = true, value = "Genomic data count filter") @Valid @RequestBody(required = false) GenomicDataCountFilter genomicDataCountFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiParam(required = true, value = "Intercepted Genomic Data Count Filter")
        @ApiIgnore
        @Valid @RequestAttribute(required = false, value = "interceptedGenomicDataCountFilter") GenomicDataCountFilter interceptedGenomicDataCountFilter
    ) throws StudyNotFoundException {
        List<GenomicDataFilter> gdFilters = interceptedGenomicDataCountFilter.getGenomicDataFilters();
        StudyViewFilter studyViewFilter = interceptedGenomicDataCountFilter.getStudyViewFilter();
        // when there is only one filter, it means study view is doing a single chart filter operation
        // remove filter from studyViewFilter to return all data counts
        // the reason we do this is to make sure after chart get filtered, user can still see unselected portion of the chart
        if (gdFilters.size() == 1) {
            studyViewFilterUtil.removeSelfFromGenomicDataFilter(
                gdFilters.get(0).getHugoGeneSymbol(), 
                gdFilters.get(0).getProfileType(), 
                studyViewFilter);
        }
        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(studyViewFilter);

        if (filteredSampleIdentifiers.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);
        
        List<GenomicDataCountItem> result = studyViewService.getCNAAlterationCountsByGeneSpecific(
            studyIds,
            sampleIds,
            gdFilters.stream().map(gdFilter -> new Pair<>(gdFilter.getHugoGeneSymbol(), gdFilter.getProfileType())).collect(Collectors.toList()));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/generic-assay-data-counts/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch generic assay data counts by study view filter")
    public ResponseEntity<List<GenericAssayDataCountItem>> fetchGenericAssayDataCounts(
        @ApiParam(required = true, value = "Generic assay data count filter") @Valid @RequestBody(required = false) GenericAssayDataCountFilter genericAssayDataCountFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this
        // attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedGenericAssayDataCountFilter") GenericAssayDataCountFilter interceptedGenericAssayDataCountFilter) {

        List<GenericAssayDataFilter> gaFilters = interceptedGenericAssayDataCountFilter.getGenericAssayDataFilters();
        StudyViewFilter studyViewFilter = interceptedGenericAssayDataCountFilter.getStudyViewFilter();
        // when there is only one filter, it means study view is doing a single chart filter operation
        // remove filter from studyViewFilter to return all data counts
        // the reason we do this is to make sure after chart get filtered, user can still see unselected portion of the chart
        if (gaFilters.size() == 1) {
            studyViewFilterUtil.removeSelfFromGenericAssayFilter(gaFilters.get(0).getStableId(), studyViewFilter);
        }
        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(studyViewFilter);

        if (filteredSampleIdentifiers.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }
        
        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);
        
        List<GenericAssayDataCountItem> result = studyViewService.fetchGenericAssayDataCounts(
            sampleIds,
            studyIds,
            gaFilters.stream().map(GenericAssayDataFilter::getStableId).collect(Collectors.toList()),
            gaFilters.stream().map(GenericAssayDataFilter::getProfileType).collect(Collectors.toList()));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/generic-assay-data-bin-counts/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch generic assay data bin counts by study view filter")
    public ResponseEntity<List<GenericAssayDataBin>> fetchGenericAssayDataBinCounts(
            @ApiParam("Method for data binning") @RequestParam(defaultValue = "DYNAMIC") DataBinMethod dataBinMethod,
            @ApiParam(required = true, value = "Generic assay data bin count filter") @Valid @RequestBody(required = false) GenericAssayDataBinCountFilter genericAssayDataBinCountFilter,
            @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
            @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
            @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this
                        // attribute is needed for the @PreAuthorize tag above.
            @Valid @RequestAttribute(required = false, value = "interceptedGenericAssayDataBinCountFilter") GenericAssayDataBinCountFilter interceptedGenericAssayDataBinCountFilter) {

        return new ResponseEntity<>(studyViewFilterApplier.getDataBins(dataBinMethod, interceptedGenericAssayDataBinCountFilter), HttpStatus.OK);
    }
    
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/clinical-data-table/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch clinical data for the Clinical Tab of Study View")
    public ResponseEntity<ClinicalDataCollection> fetchClinicalDataClinicalTable(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody(required = false) 
            StudyViewFilter studyViewFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") 
            Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedStudyViewFilter") 
            StudyViewFilter interceptedStudyViewFilter,
        @ApiParam("Page size of the result list")
        @Max(CLINICAL_TAB_MAX_PAGE_SIZE)
        @Min(PagingConstants.NO_PAGING_PAGE_SIZE)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_NO_PAGING_PAGE_SIZE) 
            Integer pageSize,
        @ApiParam("Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) 
            Integer pageNumber,
        @ApiParam("Search term to filter sample rows. Samples are returned " +
            "with a partial match to the search term for any sample clinical attribute.")
        @RequestParam(defaultValue = "") 
            String searchTerm,
        @ApiParam(value = "sampleId, patientId, or the ATTR_ID to sorted by")
        @RequestParam(required = false) 
            // TODO: Can we narrow down this string to a specific enum? 
            String sortBy,
        @ApiParam("Direction of the sort")
        @RequestParam(defaultValue = "ASC") 
            Direction direction
    ) {
        List<String> sampleStudyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(interceptedStudyViewFilter);
        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, sampleStudyIds, sampleIds);
        
        List<ClinicalData> sampleClinicalData = clinicalDataService.fetchSampleClinicalTable(
            sampleStudyIds,
            sampleIds,
            pageSize,
            pageNumber,
            searchTerm,
            sortBy,
            direction.name()
        );
        Integer total = clinicalDataService.fetchSampleClinicalTableCount(
            sampleStudyIds,
            sampleIds,
            searchTerm,
            sortBy,
            direction.name()
        );
            
        // Return empty when possible.
        if (sampleClinicalData.isEmpty()) {
            return new ResponseEntity<>(new ClinicalDataCollection(), HttpStatus.OK);
        }

        // Resolve for which patient clinical data should be included.
        final List<ImmutablePair<String, String>> patientIdentifiers = sampleClinicalData.stream()
            .map(d -> new ImmutablePair<>(d.getStudyId(), d.getPatientId()))
            .distinct()
            .collect(Collectors.toList());
        List<String> patientStudyIds = patientIdentifiers.stream().map(p -> p.getLeft()).collect(Collectors.toList());
        List<String> patientIds = patientIdentifiers.stream().map(p -> p.getRight()).collect(Collectors.toList());
        
        
        List<String> searchAllAttributes = null;
        final List<ClinicalData> patientClinicalData = clinicalDataService.fetchClinicalData(patientStudyIds, patientIds,
            searchAllAttributes, ClinicalDataType.PATIENT.name(), Projection.SUMMARY.name());

        final ClinicalDataCollection clinicalDataCollection = new ClinicalDataCollection();
        clinicalDataCollection.setSampleClinicalData(sampleClinicalData);
        clinicalDataCollection.setPatientClinicalData(patientClinicalData);
        
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, total.toString());
        return new ResponseEntity<>(clinicalDataCollection, responseHeaders, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @PostMapping(value = "/clinical-event-type-counts/fetch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get Counts of Clinical Event Types by Study View Filter")
    public ResponseEntity<List<ClinicalEventTypeCount>> getClinicalEventTypeCounts(
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
        List<ClinicalEventTypeCount> eventTypeCounts = instance.cachedClinicalEventTypeCounts(interceptedStudyViewFilter, singleStudyUnfiltered); 
        return new ResponseEntity<>(eventTypeCounts, HttpStatus.OK);
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabled() && #singleStudyUnfiltered"
    )
    public List<ClinicalEventTypeCount> cachedClinicalEventTypeCounts(
        StudyViewFilter interceptedStudyViewFilter, boolean singleStudyUnfiltered
    ){
        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(interceptedStudyViewFilter);
        List<String> sampleIds = new ArrayList<>();
        List<String> studyIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);
        return clinicalEventService.getClinicalEventTypeCounts(studyIds, sampleIds);
    }
}
