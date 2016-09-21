package org.cbioportal.weblegacy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.cbioportal.model.CosmicCount;
import org.cbioportal.service.CosmicCountService;

@RestController
public class CosmicCountController {

    @Autowired
    private CosmicCountService cosmicCountService;

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/cosmiccounts")
    public List<CosmicCount> getCOSMICCountsByKeywords(@RequestParam(required = true) List<String> keywords) {
        return cosmicCountService.getCOSMICCountsByKeywords(keywords);
    }
}