package org.mskcc.cbio.importer.cvr.darwin.transformer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.darwin.dao.dvcbio.LabResultMapper;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.LabResult;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.LabResultExample;
import org.mskcc.cbio.importer.cvr.darwin.util.DarwinSessionManager;
import org.mskcc.cbio.importer.cvr.darwin.util.IdMapService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * <p/>
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
 * <p/>
 * Created by criscuof on 11/24/14.
 */
public class DarwinLabResultTransformer extends DarwinTransformer {
    private static final Logger logger = Logger.getLogger(DarwinLabResultTransformer.class);
    private final LabResultMapper labResultMapper;
    private final LabResultExample labResultExample;

    public DarwinLabResultTransformer(Path filePath) {
        super(filePath);
        this.labResultMapper = DarwinSessionManager.INSTANCE.getDarwinSession()
                .getMapper(LabResultMapper.class);
        this.labResultExample = new LabResultExample();
    }

    private List<String> generateLabResultReport() {
        List<String> labDataList = Lists.newArrayList(this.generateColumnHeaders(LabResult.class));
        List<Object> labObjectList =  new ArrayList<Object>(this.labResultMapper.selectByExample(this.labResultExample));
        labDataList.addAll(this.generateStagingFileRecords(labObjectList));
        return labDataList;
    }

    @Override
    public void transform() {
        this.labResultExample.clear();
        this.labResultExample.createCriteria().andLAB_PT_DEIDENTIFICATION_IDIn(IdMapService.INSTANCE.getDarwinIdList());
        this.writeStagingFile(this.generateLabResultReport());
        return;
    }

    @Override
    public List<String> generateReportByPatientId(Integer patientId) {
        Preconditions.checkArgument(null != patientId && patientId > 0,
                "A valid patient id is required");
        this.labResultExample.clear();
        this.labResultExample.createCriteria().andLAB_PT_DEIDENTIFICATION_IDEqualTo(patientId);
        return this.generateLabResultReport();

    }

    // main class for testing
    public static void main (String...args){
        Path labPath = Paths.get("/tmp/cvr/data_clinical_lab_result.txt");
        DarwinLabResultTransformer transformer = new  DarwinLabResultTransformer(labPath);
        transformer.transform();
        // test report for individual patient
        for(String line: transformer.generateReportByPatientId(1339055)) {
            System.out.println(line);
        }
        // terminate the SQL session
        DarwinSessionManager.INSTANCE.closeSession();
    }
}
