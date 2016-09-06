/**
 *
 * @author jiaojiao
 */

package org.cbioportal.web;

import org.cbioportal.model.CNASegmentData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.cbioportal.service.CNASegmentService;

@RestController
public class CNASegmentController {

    @Autowired
    private CNASegmentService cnaSegmentService;
    
    @RequestMapping(method = RequestMethod.GET, value = "/cnaSegmentService")
    public List<CNASegmentData> getCNASegment(@RequestParam String cancerStudyId, 
                                      @RequestParam(required = false) List<String> hugoSymbols,
                                      @RequestParam(required = false) List<String> sampleIds) {
        return cnaSegmentService.getCNASegmentData(cancerStudyId, hugoSymbols, sampleIds);
    }
}