package org.mskcc.cbio.importer.cvr.darwin.transformer;

import com.google.common.base.Preconditions;
import com.google.inject.internal.Lists;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.darwin.dao.dvcbio.PatientMapper;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.Patient;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.PatientExample;
import org.mskcc.cbio.importer.cvr.darwin.util.DarwinSessionManager;
import org.mskcc.cbio.importer.cvr.darwin.util.IdMapService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
 * Created by criscuof on 11/21/14.
 */
public class DarwinPatientTransformer extends DarwinTransformer {
    private static final Logger logger = Logger.getLogger(DarwinPatientTransformer.class);
    private final PatientMapper patientMapper;
    private final PatientExample patientExample;
    private static final String patientFile = "data_clinical_patient.txt";

    public DarwinPatientTransformer(Path aPath){
        super(aPath.resolve(patientFile));
        this.patientExample = new PatientExample();
        this.patientMapper = DarwinSessionManager.INSTANCE.getDarwinSession()
                .getMapper(PatientMapper.class);
    }

    private List<String> generatePatientReport() {
        List<String> patientDataList = Lists.newArrayList(this.generateColumnHeaders(Patient.class));
        List<Object>patientObjectList = new ArrayList<Object>(this.patientMapper.selectByExample(this.patientExample));
        patientDataList.addAll(this.generateStagingFileRecords(patientObjectList));
        return patientDataList;
    }

    @Override
    public  void transform() {
        this.patientExample.clear();
        this.patientExample.createCriteria().andPT_PT_DEIDENTIFICATION_IDIn(IdMapService.INSTANCE.getDarwinIdList());
        this.writeStagingFile(this.generatePatientReport());
        return;
    }

    @Override
    public List<String> generateReportByPatientId(Integer patientId) {
        Preconditions.checkArgument(null != patientId && patientId > 0,
                "A valid patient id is required");
        this.patientExample.clear();
        this.patientExample.createCriteria().andPT_PT_DEIDENTIFICATION_IDEqualTo(patientId);
        return this.generatePatientReport();
    }

    @Override
    public List<String> generateReportByPatientIdList(List<Integer> patientIdList) {
        Preconditions.checkArgument(null != patientIdList,
                "A List of patient ids is required.");
        this.patientExample.clear();
        this.patientExample.createCriteria().andPT_PT_DEIDENTIFICATION_IDIn(patientIdList);
        return this.generatePatientReport();
    }

    // main class for testing
    public static void main (String...args){
        Path patientPath = Paths.get("/tmp/cvr");
        DarwinPatientTransformer transformer = new DarwinPatientTransformer(patientPath);
        transformer.transform();
        // test report for individual patient
        for(String line: transformer.generateReportByPatientId(1339055)) {
            System.out.println(line);
        }
        // terminate the SQL session
        DarwinSessionManager.INSTANCE.closeSession();
    }
}
