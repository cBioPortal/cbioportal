package org.cbioportal.web.columnar;

import com.clickhouse.jdbc.ClickHouseDataSource;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.CaseListDataCount;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.ClinicalEventKeyCode;
import org.cbioportal.model.ClinicalEventTypeCount;
import org.cbioportal.model.ClinicalViolinPlotData;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DensityPlotData;
import org.cbioportal.model.EnrichmentType;
import org.cbioportal.model.GenericAssayDataBin;
import org.cbioportal.model.GenericAssayDataCountItem;
import org.cbioportal.model.GenomicDataBin;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.PatientTreatmentReport;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleTreatmentReport;
import org.cbioportal.persistence.mybatisclickhouse.StudyViewMapper;
import org.cbioportal.properties.CustomDataSourceConfiguration;
import org.cbioportal.service.ClinicalDataDensityPlotService;
import org.cbioportal.service.CustomDataService;
import org.cbioportal.service.StudyViewColumnarService;
import org.cbioportal.service.ViolinPlotService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.service.util.CustomDataSession;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.cbioportal.web.columnar.util.CustomDataFilterUtil;
import org.cbioportal.web.columnar.util.NewStudyViewFilterUtil;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.ClinicalDataBinCountFilter;
import org.cbioportal.web.parameter.ClinicalDataCountFilter;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.DataBinMethod;
import org.cbioportal.web.parameter.GenericAssayDataBinCountFilter;
import org.cbioportal.web.parameter.GenericAssayDataCountFilter;
import org.cbioportal.web.parameter.GenericAssayDataFilter;
import org.cbioportal.web.parameter.GenomicDataBinCountFilter;
import org.cbioportal.web.parameter.GenomicDataCountFilter;
import org.cbioportal.web.parameter.GenomicDataFilter;
import org.cbioportal.web.parameter.MolecularProfileCasesGroupAndAlterationTypeFilter;
import org.cbioportal.web.parameter.MolecularProfileCasesGroupFilter;
import org.cbioportal.web.parameter.MutationOption;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.util.DensityPlotParameters;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.transform.Result;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.cbioportal.web.columnar.util.ClinicalDataXyPlotUtil.fetchClinicalDataForXyPlot;



@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
@ConditionalOnProperty(name = "clickhouse_mode", havingValue = "true")
public class EnrichmentsColumnStoreController {
    
    private final StudyViewColumnarService studyViewColumnarService;
    private final ClinicalDataBinner clinicalDataBinner;

    private final BasicDataBinner basicDataBinner;
    private final ClinicalDataDensityPlotService clinicalDataDensityPlotService;
    private final ViolinPlotService violinPlotService;
    private final CustomDataService customDataService;
    private final StudyViewFilterUtil studyViewFilterUtil;
    private final CustomDataFilterUtil customDataFilterUtil;
    
    @Autowired
    public EnrichmentsColumnStoreController(StudyViewColumnarService studyViewColumnarService,
                                            ClinicalDataBinner clinicalDataBinner,
                                            BasicDataBinner basicDataBinner,
                                            ClinicalDataDensityPlotService clinicalDataDensityPlotService,
                                            ViolinPlotService violinPlotService,
                                            CustomDataService customDataService,
                                            StudyViewFilterUtil studyViewFilterUtil,
                                            CustomDataFilterUtil customDataFilterUtil
                                          ) {
        this.studyViewColumnarService = studyViewColumnarService;
        this.clinicalDataBinner = clinicalDataBinner;
        this.basicDataBinner = basicDataBinner;
        this.clinicalDataDensityPlotService = clinicalDataDensityPlotService;
        this.violinPlotService = violinPlotService;
        this.customDataService = customDataService;
        this.studyViewFilterUtil = studyViewFilterUtil;
        this.customDataFilterUtil = customDataFilterUtil;
    }


    @Hidden // should unhide when we remove legacy controller
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/column-store/goobers/fetch",
        consumes = MediaType.APPLICATION_JSON_VALUE, method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AlterationCountByGene>> fetchFilteredSamples(
        @RequestParam(defaultValue = "false") Boolean negateFilters,
        @RequestBody(required = false) StudyViewFilter studyViewFilter) throws SQLException {

        List<String> stringList = new ArrayList<>();
        
        stringList.add("genie_public_GENIE-PROV-5b959d8cdd-4794af60ad");
        stringList.add("genie_public_GENIE-PROV-8e85d246c9-ec39adbd80");
        
        var counts = studyViewColumnarService.getAlterationEnrichmentCounts(stringList);
        
        return new ResponseEntity<>(counts,
            HttpStatus.OK
        );
    }


    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/alteration-enrichmentss/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary ="Fetch alteration enrichments in molecular profiles")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = AlterationEnrichment.class))))
    public ResponseEntity<Map<String, List<AlterationCountByGene>>> fetchAlterationEnrichmentsNew(
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true)
        // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedMolecularProfileCasesGroupFilters") List<MolecularProfileCasesGroupFilter> interceptedMolecularProfileCasesGroupFilters,
        @Parameter(hidden = true)
        @Valid @RequestAttribute(required = false, value = "alterationEventTypes") AlterationFilter alterationEventTypes,
        @Parameter(description = "Type of the enrichment e.g. SAMPLE or PATIENT")
        @RequestParam(defaultValue = "SAMPLE") EnrichmentType enrichmentType,
        @Parameter(required = true, description = "List of groups containing sample identifiers and list of Alteration Types")
        @Valid @RequestBody(required = false) MolecularProfileCasesGroupAndAlterationTypeFilter groupsAndAlterationTypes) throws MolecularProfileNotFoundException {


        Map<String, List<AlterationCountByGene>> ret = new HashMap();
        
        groupsAndAlterationTypes.getMolecularProfileCasesGroupFilter()
            .forEach(group ->{
                    List<String> stringList = new ArrayList<>();
                    
                    stringList.addAll(
                        group.getMolecularProfileCaseIdentifiers().stream()
                            .map(n->"genie_public_"+n.getCaseId()).toList());

                var counts = studyViewColumnarService.getAlterationEnrichmentCounts(stringList);
                
                ret.put(group.getName(),counts);

            });
        
//        List<AlterationEnrichment> alterationEnrichments = alterationEnrichmentService.getAlterationEnrichments(
//            groupCaseIdentifierSet,
//            enrichmentType,
//            alterationEventTypes);

        return new ResponseEntity<>(ret, HttpStatus.OK);
    }
    
    
    

}
