package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.web.config.InternalApiTags;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.Direction;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.MutationFilter;
import org.cbioportal.web.parameter.MutationMultipleStudyFilter;
import org.cbioportal.web.parameter.MutationPositionIdentifier;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.SampleMolecularIdentifier;
import org.cbioportal.web.parameter.sort.MutationSortBy;
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

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid; 
import java.util.*;

@InternalApi
@RestController
@Validated
@Tag(name = InternalApiTags.MUTATIONS_COUNT, description = " ")
public class MutationCountController {
    
    public static final int MUTATION_MAX_PAGE_SIZE = 10000000;

    @Autowired
    private MutationService mutationService;

    @RequestMapping(value = "/api/mutation-counts-by-position/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch mutation counts in all studies by gene and position")
    public ResponseEntity<List<MutationCountByPosition>> fetchMutationCountsByPosition(
        @Parameter(required = true, description = "List of gene and positions")
        @Size(min = 1, max = MUTATION_MAX_PAGE_SIZE)
        @RequestBody List<MutationPositionIdentifier> mutationPositionIdentifiers) {

        List<Integer> entrezGeneIds = new ArrayList<>();
        List<Integer> proteinPosStarts = new ArrayList<>();
        List<Integer> proteinPosEnds = new ArrayList<>();
        for (MutationPositionIdentifier mutationPositionIdentifier : mutationPositionIdentifiers) {

            entrezGeneIds.add(mutationPositionIdentifier.getEntrezGeneId());
            proteinPosStarts.add(mutationPositionIdentifier.getProteinPosStart());
            proteinPosEnds.add(mutationPositionIdentifier.getProteinPosEnd());
        }

        return new ResponseEntity<>(mutationService.fetchMutationCountsByPosition(entrezGeneIds, proteinPosStarts,
            proteinPosEnds), HttpStatus.OK);
    }


}
