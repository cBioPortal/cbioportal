package org.mskcc.cbio.importer.cvr.darwin.transformer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.darwin.dao.dvcbio.ClinicalTrialMapper;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.ClinicalTrial;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.ClinicalTrialExample;
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
 * Created by criscuof on 11/23/14.
 */
public class DarwinClinicalTrialTransformer extends DarwinTransformer {
    private static final Logger logger = Logger.getLogger(DarwinClinicalTrialTransformer.class);
    private final ClinicalTrialMapper clinicalTrialMapper;
    private final ClinicalTrialExample clinicalTrialExample;
    private static final String clinicalTrialFile = "data_clinical_clinicaltrial.txt";

    public DarwinClinicalTrialTransformer(Path aFilePath) {
        super(aFilePath.resolve(clinicalTrialFile));
        this.clinicalTrialMapper =  DarwinSessionManager.INSTANCE.getDarwinSession()
                .getMapper(ClinicalTrialMapper.class);
        this.clinicalTrialExample = new ClinicalTrialExample();
    }

    private List<String> generateClinicalTrialReport() {
        List<String> clinicalTrialList = Lists.newArrayList(this.generateColumnHeaders(ClinicalTrial.class));
        List<Object> clinicalTrialObjectList = new ArrayList<Object>(this.clinicalTrialMapper.selectByExample(this.clinicalTrialExample));
        clinicalTrialList.addAll(this.generateStagingFileRecords(clinicalTrialObjectList));
        return clinicalTrialList;
    }

    @Override
    public void transform() {
        this.clinicalTrialExample.clear();
        this.clinicalTrialExample.createCriteria().andCLIN_TRIAL_PT_DEIDENTIFICATION_IDIn(IdMapService.INSTANCE.getDarwinIdList());
        this.writeStagingFile(this.generateClinicalTrialReport());
        return;
    }
    @Override
    public List<String> generateReportByPatientId(Integer patientId) {
        Preconditions.checkArgument(null != patientId && patientId > 0,
                "A valid patient id is required");
        this.clinicalTrialExample.clear();
        this.clinicalTrialExample.createCriteria().andCLIN_TRIAL_PT_DEIDENTIFICATION_IDEqualTo(patientId);
        return this.generateClinicalTrialReport();
    }

    @Override
    public List<String> generateReportByPatientIdList(List<Integer> patientIdList) {
        Preconditions.checkArgument(null != patientIdList, "A List of patient ids is required");
        this.clinicalTrialExample.clear();
        this.clinicalTrialExample.createCriteria().andCLIN_TRIAL_PT_DEIDENTIFICATION_IDIn(patientIdList);
        return this.generateClinicalTrialReport();
    }

    // main method for stand alone testing
    public static void main (String...args){
        DarwinClinicalTrialTransformer transformer = new DarwinClinicalTrialTransformer
                (Paths.get("/tmp/cvr"));
        transformer.transform();
        // test report for individual patient
        for(String line: transformer.generateReportByPatientId(1339055)) {
            System.out.println(line);
        }
        // terminate the SQL session
        DarwinSessionManager.INSTANCE.closeSession();
    }
}
