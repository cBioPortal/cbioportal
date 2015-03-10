package org.mskcc.cbio.importer.cvr.crdb.transformer;

import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.crdb.rdbms.dao.cbioint.CrdbImpactSurveyMapper;
import org.mskcc.cbio.importer.cvr.crdb.rdbms.model.cbioint.CrdbImpactSurvey;
import org.mskcc.cbio.importer.cvr.crdb.rdbms.model.cbioint.CrdbImpactSurveyExample;
import org.mskcc.cbio.importer.cvr.crdb.util.CrdbSessionManager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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
 * Created by Fred Criscuolo on 3/4/15.
 * criscuof@mskcc.org
 */
public class CrdbSurveyTransformer extends CrdbTransformer {
    private static final Logger logger = Logger.getLogger(CrdbSurveyTransformer.class);
    //TODO: move filename to Google worksheet
    private static final String crdbSurveyClinicalFilename = "data_clinical_supp_crdbsurvey.txt";
    private final CrdbImpactSurveyMapper crdbSurveyMapper;
    private final CrdbImpactSurveyExample crdbImpactSurveyExample;
    private static final String patientIdColumnName = "DMP_ID";

    public CrdbSurveyTransformer(Path filePath){
        super(filePath.resolve(crdbSurveyClinicalFilename));
        this.crdbImpactSurveyExample = new CrdbImpactSurveyExample();
        this.crdbSurveyMapper = CrdbSessionManager.INSTANCE.getCrdbSession()
                .getMapper(CrdbImpactSurveyMapper.class);
    }

    @Override
    public void transform() {
        this.crdbImpactSurveyExample.clear();
        this.crdbImpactSurveyExample.createCriteria().andDMP_IDIsNotNull();
        this.writeStagingFile(this.generateCrdbSurveyReport(), CrdbImpactSurvey.class);
    }

    @Override
    public List<String> generateReportByPatientId(String patientId) {
        return new ArrayList<String>();
    }

    @Override
    public List<String> generateReportByPatientIdList(List<String> patientIdList) {
        return new ArrayList<String>();
    }

    private List<String> generateCrdbSurveyReport() {
        List<Object> crdbSurveyObjectList = new ArrayList<Object>(
                (this.crdbSurveyMapper.selectByExample(this.crdbImpactSurveyExample)));
        logger.info("Selected " +crdbSurveyObjectList.size() + " CRDB survey records");
        return this.generateStagingFileRecords(crdbSurveyObjectList);

    }
    // main class for standalone testing
    public static void main (String...args) {
        Path filePath = Paths.get("/tmp/cvr");
        CrdbSurveyTransformer transformer = new CrdbSurveyTransformer(filePath);
        transformer.transform();
        CrdbSessionManager.INSTANCE.closeSession();
    }

}
