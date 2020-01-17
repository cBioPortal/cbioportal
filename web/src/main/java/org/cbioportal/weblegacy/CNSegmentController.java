/**
 *
 * @author jiaojiao
 */

package org.cbioportal.weblegacy;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.mskcc.cbio.portal.model.CNSegmentData;
import org.mskcc.cbio.portal.service.CNSegmentService;
import org.springframework.transaction.annotation.Transactional;

@RestController
public class CNSegmentController {

    @Autowired
    private CNSegmentService cnSegmentService;

    @ApiOperation(value = "Get copy number segment data including sample id, chromosome, start position, end position, numProbes and segment mean value",
            nickname = "getCNSegment",
            notes = "")
    @Transactional
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/copynumbersegments")
    public List<CNSegmentData> getCNSegment(@ApiParam(value = "Return segment data related to the study with this cancer study id eg. acc_tcga") @RequestParam(required = true) String cancerStudyId,
        @ApiParam(value = "Return segment data in these chromosomes. If ommitted, return segment data on all chromosomes")  @RequestParam(required = false) List<String> chromosomes,
        @ApiParam(value = "Return the segment data with this list of sampleIds. If omitted, return segment data on all samples")  @RequestParam(required = false) List<String> sampleIds) {
        return cnSegmentService.getCNSegmentData(cancerStudyId, chromosomes, sampleIds);
    }

    @ApiOperation(value = "Get copy number segment file",
            nickname = "getSegmentFile",
            notes = "")
    @Transactional
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/segmentfile")
    public String getCNSegmentFile(@ApiParam(value = "Return segment data related to the study with this cancer study id eg. acc_tcga") @RequestParam(required = true) String cancerStudyId,
        @ApiParam(value = "Return the segment data with this list of sampleIds. If omitted, return segment data on all samples")  @RequestParam(required = false) List<String> sampleIds) {
        return cnSegmentService.getCNSegmentFile(cancerStudyId, sampleIds);
    }
}
