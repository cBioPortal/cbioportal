package org.cbioportal.web;

import org.cbioportal.model.Mutation;
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

    @RequestMapping(method = RequestMethod.GET, value = "/mutationsdetailed")
    public List<Mutation> getMutation(@RequestParam List<String> geneticProfileStableIds,
                                      @RequestParam List<String> hugoGeneSymbols,
                                      @RequestParam(required = false) List<String> sampleStableIds,
                                      @RequestParam(required = false) String sampleListStableId) {

        return mutationService.getMutationsDetailed(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds,
                sampleListStableId);
    }
}