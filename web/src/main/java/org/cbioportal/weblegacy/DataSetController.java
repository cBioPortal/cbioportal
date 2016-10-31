/**
 *
 * @author jiaojiao
 */

package org.cbioportal.weblegacy;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.cbioportal.model.DataSet;
import org.cbioportal.service.DataSetService;
import org.springframework.transaction.annotation.Transactional;

@RestController
public class DataSetController {

    @Autowired
    private DataSetService dataSetService;
 
    @ApiOperation(value = "Get list of public datasets, including study name, citation, cancer_study_identifier, stable_id, corresponding sample count and its pmid",
            nickname = "getDataSets",
            notes = "")
    @Transactional
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/datasets")
    public List<DataSet> getDataSets() {
        return dataSetService.getDataSets();
    }
}