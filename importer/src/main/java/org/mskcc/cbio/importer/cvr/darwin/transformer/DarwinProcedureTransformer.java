package org.mskcc.cbio.importer.cvr.darwin.transformer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.darwin.dao.dvcbio.ProcedureMapper;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.Procedure;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.ProcedureExample;
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
public class DarwinProcedureTransformer extends DarwinTransformer {

    private static final Logger logger = Logger.getLogger(DarwinProcedureTransformer.class);

    private final ProcedureMapper procedureMapper;
    private final ProcedureExample procedureExample;


    public DarwinProcedureTransformer(Path filePath){
        super(filePath);
        this.procedureMapper =  DarwinSessionManager.INSTANCE.getDarwinSession()
                .getMapper(ProcedureMapper.class);
        this.procedureExample = new ProcedureExample();
    }

    private List<String> generateProcedureReport (){
        List<String> procedureDataList = Lists.newArrayList(this.generateColumnHeaders(Procedure.class));
        List<Object> procedureObjectList = new ArrayList<Object>(this.procedureMapper.selectByExample(this.procedureExample));
        procedureDataList.addAll(this.generateStagingFileRecords(procedureObjectList));
        return procedureDataList;
    }

    @Override
    public void transform() {
        this.procedureExample.clear();
        this.procedureExample.createCriteria().andPROC_PT_DEIDENTIFICATION_IDIn(IdMapService.INSTANCE.getDarwinIdList());
        this.writeStagingFile(this.generateProcedureReport());
        return;
    }

    @Override
    public List<String> generateReportByPatientId(Integer patientId) {
        Preconditions.checkArgument(null != patientId && patientId > 0,
                "A valid patient id is required");
        this.procedureExample.clear();
        this.procedureExample.createCriteria().andPROC_PT_DEIDENTIFICATION_IDEqualTo(patientId);
        return this.generateProcedureReport();
    }

    // main class for testing
    public static void main (String...args){
        Path procedurePath = Paths.get("/tmp/cvr/data_clinical_procedure.txt");
        DarwinProcedureTransformer transformer = new DarwinProcedureTransformer(procedurePath);
        transformer.transform();
        // test report for individual patient
        for(String line: transformer.generateReportByPatientId(1339055)) {
            System.out.println(line);
        }
        // terminate the SQL session
        DarwinSessionManager.INSTANCE.closeSession();
    }
}
