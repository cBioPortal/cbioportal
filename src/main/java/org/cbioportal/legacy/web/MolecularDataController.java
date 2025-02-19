package org.cbioportal.legacy.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.lang3.math.NumberUtils;
import org.cbioportal.legacy.model.GeneMolecularData;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.NumericGeneMolecularData;
import org.cbioportal.legacy.service.MolecularDataService;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.utils.BigDecimalStats;
import org.cbioportal.legacy.web.config.PublicApiTags;
import org.cbioportal.legacy.web.config.annotation.PublicApi;
import org.cbioportal.legacy.web.parameter.HeaderKeyConstants;
import org.cbioportal.legacy.web.parameter.MolecularDataFilter;
import org.cbioportal.legacy.web.parameter.MolecularDataMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@PublicApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = PublicApiTags.MOLECULAR_DATA, description = " ")
public class MolecularDataController {

    private static final Logger log = LoggerFactory.getLogger(MolecularDataController.class);
    @Autowired
    private MolecularDataService molecularDataService;

    @Autowired
    private MolecularProfileService molecularProfileService;

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/molecular-data", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all molecular data in a molecular profile")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = NumericGeneMolecularData.class))))
    public ResponseEntity<List<NumericGeneMolecularData>> getAllMolecularDataInMolecularProfile(
        @Parameter(required = true, description = "Molecular Profile ID e.g. acc_tcga_rna_seq_v2_mrna")
        @PathVariable String molecularProfileId,
        @Parameter(required = true, description = "Sample List ID e.g. acc_tcga_all")
        @RequestParam String sampleListId,
        @Parameter(required = true, description = "Entrez Gene ID e.g. 1")
        @RequestParam Integer entrezGeneId,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws MolecularProfileNotFoundException {

        List<NumericGeneMolecularData> result = filterNonNumberMolecularData(molecularDataService.getMolecularData(
            molecularProfileId, sampleListId, Arrays.asList(entrezGeneId), projection.name()));

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, String.valueOf(result.size()));
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/molecular-data/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch molecular data in a molecular profile")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = NumericGeneMolecularData.class))))
    public ResponseEntity<List<NumericGeneMolecularData>> fetchAllMolecularDataInMolecularProfile(
        @Parameter(required = true, description = "Molecular Profile ID e.g. acc_tcga_rna_seq_v2_mrna")
        @PathVariable String molecularProfileId,
        @Parameter(required = true, description = "List of Sample IDs/Sample List ID and Entrez Gene IDs")
        @Valid @RequestBody MolecularDataFilter molecularDataFilter,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @RequestParam(required = false, defaultValue = "false") boolean calculateSampleZScores) throws MolecularProfileNotFoundException {

        List<NumericGeneMolecularData> result;
        if (molecularDataFilter.getSampleListId() != null) {
            result = filterNonNumberMolecularData(molecularDataService.getMolecularData(molecularProfileId,
                molecularDataFilter.getSampleListId(), molecularDataFilter.getEntrezGeneIds(), projection.name()));
        } else {
            result = filterNonNumberMolecularData(molecularDataService.fetchMolecularData(molecularProfileId,
                molecularDataFilter.getSampleIds(), molecularDataFilter.getEntrezGeneIds(), projection.name()));
        }

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, String.valueOf(result.size()));
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            if (calculateSampleZScores) {
                // Calculate Z-scores
                doCalculateSampleZScores(result);
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/molecular-data/fetch", method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch molecular data")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = NumericGeneMolecularData.class))))
    public ResponseEntity<List<NumericGeneMolecularData>> fetchMolecularDataInMultipleMolecularProfiles(
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedMolecularDataMultipleStudyFilter") MolecularDataMultipleStudyFilter interceptedMolecularDataMultipleStudyFilter,
        @Parameter(required = true, description = "List of Molecular Profile ID and Sample ID pairs or List of Molecular" +
            "Profile IDs and Entrez Gene IDs")
        @Valid @RequestBody(required = false) MolecularDataMultipleStudyFilter molecularDataMultipleStudyFilter,
        @RequestParam(required = false, defaultValue = "false") boolean calculateSampleZScores,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws MolecularProfileNotFoundException {

        List<NumericGeneMolecularData> result;
        if (interceptedMolecularDataMultipleStudyFilter.getMolecularProfileIds() != null) {
            result = filterNonNumberMolecularData(molecularDataService.getMolecularDataInMultipleMolecularProfiles(
                interceptedMolecularDataMultipleStudyFilter.getMolecularProfileIds(), null,
                interceptedMolecularDataMultipleStudyFilter.getEntrezGeneIds(), projection.name()));
        } else {

            List<String> molecularProfileIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            extractMolecularProfileAndSampleIds(interceptedMolecularDataMultipleStudyFilter, molecularProfileIds, sampleIds);
            result = filterNonNumberMolecularData(molecularDataService.getMolecularDataInMultipleMolecularProfiles(molecularProfileIds,
                sampleIds, interceptedMolecularDataMultipleStudyFilter.getEntrezGeneIds(), projection.name()));
        }

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, String.valueOf(result.size()));
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            if (calculateSampleZScores) {
                // Calculate Z-scores
                doCalculateSampleZScores(result);
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
    }

    //TODO move me to the service layer
    private void doCalculateSampleZScores(List<NumericGeneMolecularData> values) throws MolecularProfileNotFoundException {
        for (Map.Entry<String, Map<Integer, List<NumericGeneMolecularData>>> entry : values.stream().collect(Collectors.groupingBy(NumericGeneMolecularData::getMolecularProfileId, Collectors.groupingBy(NumericGeneMolecularData::getEntrezGeneId))).entrySet()) {
            String molecularProfileId = entry.getKey();
            Map<Integer, List<NumericGeneMolecularData>> geneValuesPerEntrezGeneId = entry.getValue();
            MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);
            if (!Set.of("CONTINUOUS", "LOG2-VALUE").contains(molecularProfile.getDatatype())) {
                log.debug("Z-score calculation is only supported for continuous and log2-value molecular profiles. Skipping Z-score calculation for molecular profile {}", molecularProfileId);
                return;
            }
            geneValuesPerEntrezGeneId.forEach((entrezGeneId, geneValues) -> {
                List<BigDecimal> data = geneValues.stream().map(NumericGeneMolecularData::getValue).collect(Collectors.toList());
                if (data.size() < 2) {
                    log.debug("Sample size must be at least 2. Skipping Z-score calculation for molecular profile {} and entrez gene {}",
                        molecularProfileId, entrezGeneId);
                    return;
                }
                List<BigDecimal> zScores = BigDecimalStats.calculateZScores(data);
                for (int i = 0; i < data.size(); i++) {
                    geneValues.get(i).setValue(zScores.get(i));
                    geneValues.get(i).setCalculatedSampleZScore(true);
                }
            });
        }
    }

    private void extractMolecularProfileAndSampleIds(MolecularDataMultipleStudyFilter molecularDataMultipleStudyFilter,
                                                     List<String> molecularProfileIds, List<String> sampleIds) {

        for (SampleMolecularIdentifier sampleMolecularIdentifier :
            molecularDataMultipleStudyFilter.getSampleMolecularIdentifiers()) {

            molecularProfileIds.add(sampleMolecularIdentifier.getMolecularProfileId());
            sampleIds.add(sampleMolecularIdentifier.getSampleId());
        }
    }

    private List<NumericGeneMolecularData> filterNonNumberMolecularData(List<GeneMolecularData> geneMolecularDataList) {

        List<NumericGeneMolecularData> result = new ArrayList<>();
        geneMolecularDataList.forEach(g -> {
            String value = g.getValue();
            if (NumberUtils.isNumber(value)) {
                NumericGeneMolecularData data = new NumericGeneMolecularData();
                data.setEntrezGeneId(g.getEntrezGeneId());
                data.setGene(g.getGene());
                data.setMolecularProfileId(g.getMolecularProfileId());
                data.setPatientId(g.getPatientId());
                data.setSampleId(g.getSampleId());
                data.setStudyId(g.getStudyId());
                data.setValue(new BigDecimal(g.getValue()));
                result.add(data);
            }
        });

        return result;
    }
}
