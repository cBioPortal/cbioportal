package org.cbioportal.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.cbioportal.model.COSMICCount;
import org.cbioportal.service.COSMICCountService;

@RestController
public class COSMICCountController {

    @Autowired
    private COSMICCountService cosmicCountService;

    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = "/cosmiccounts")
    public List<COSMICCount> getCOSMICCountsByKeywords(@RequestParam(required = true) List<String> keywords) {
	    return cosmicCountService.getCOSMICCountsByKeywords(keywords);
    }
}