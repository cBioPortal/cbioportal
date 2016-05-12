package org.cbioportal.web;

import org.cbioportal.model.Mutation;
import org.cbioportal.persistence.dto.SampleMutationCount;
import org.cbioportal.service.MutationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MutationController {

    @Autowired
    private MutationService mutationService;

    @RequestMapping(method = RequestMethod.GET, value = "/mutations")
    public List<Mutation> getMutations(@RequestParam List<String> geneticProfileStableIds,
                                       @RequestParam List<String> hugoGeneSymbols,
                                       @RequestParam(required = false) List<String> sampleStableIds,
                                       @RequestParam(required = false) String sampleListStableId) {

        return mutationService.getMutations(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds,
                sampleListStableId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/mutationCounts")
    public List<SampleMutationCount> getMutationCounts(@RequestParam String geneticProfileStableId,
                                                       @RequestParam List<String> sampleStableIds) {

        return mutationService.getMutationCounts(geneticProfileStableId, sampleStableIds);
    }
}