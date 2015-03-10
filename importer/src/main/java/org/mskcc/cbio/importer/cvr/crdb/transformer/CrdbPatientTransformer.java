package org.mskcc.cbio.importer.cvr.crdb.transformer;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.crdb.rdbms.dao.cbioint.CrdbDatasetMapper;
import org.mskcc.cbio.importer.cvr.crdb.rdbms.dao.cbioint.CrdbImpactSurveyMapper;
import org.mskcc.cbio.importer.cvr.crdb.rdbms.model.cbioint.CrdbDataset;
import org.mskcc.cbio.importer.cvr.crdb.rdbms.model.cbioint.CrdbDatasetExample;
import org.mskcc.cbio.importer.cvr.crdb.rdbms.model.cbioint.CrdbImpactSurvey;
import org.mskcc.cbio.importer.cvr.crdb.rdbms.model.cbioint.CrdbImpactSurveyExample;
import org.mskcc.cbio.importer.cvr.crdb.util.CrdbSessionManager;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Date;
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
 * Created by Fred Criscuolo on 3/9/15.
 * criscuof@mskcc.org
 */
public class CrdbPatientTransformer extends CrdbTransformer {

    /*
    Represents a transformer that will combine data form both the CrdbImpactSurvey and CrdbDataset views
     */
    private static final Logger logger = Logger.getLogger(CrdbPatientTransformer.class);
    //TODO: move filename to Google worksheet
    private static final String crdbSurveyClinicalFilename = "data_clinical_supp_crdbsurvey.txt";
    private final CrdbImpactSurveyMapper crdbSurveyMapper;
    private final CrdbImpactSurveyExample crdbImpactSurveyExample;
    private final CrdbDatasetMapper crdbDatasetMapper;
    private final CrdbDatasetExample crdbDatasetExample;
    private static final String patientIdColumnName = "DMP_ID";

    public CrdbPatientTransformer(Path filePath){
        super(filePath.resolve(crdbSurveyClinicalFilename));
        this.crdbImpactSurveyExample = new CrdbImpactSurveyExample();
        this.crdbSurveyMapper = CrdbSessionManager.INSTANCE.getCrdbSession()
                .getMapper(CrdbImpactSurveyMapper.class);
        this.crdbDatasetExample = new CrdbDatasetExample();
        this.crdbDatasetMapper = CrdbSessionManager.INSTANCE.getCrdbSession()
                .getMapper(CrdbDatasetMapper.class);
    }

    @Override
    public void transform() {
    }

    @Override
    public List<String> generateReportByPatientId(String patientId) {
        return null;
    }

    @Override
    public List<String> generateReportByPatientIdList(List<String> patientIdList) {
        Preconditions.checkArgument(null != patientIdList && !patientIdList.isEmpty(),
                "A valid List of CVR patient ids is required");
        return this.generateStagingFileRecords(this.findCrdbPatientsByPatientIds(patientIdList));
    }

    /*
    Private method to create a List of CrdbPatient objects corresponding to a List of patient ids
    A CrdbPatient object consists of a join of attributes from CrdbDataset and CrdbImpactSurvey objects
     */
    private List<CrdbPatient>  findCrdbPatientsByPatientIds(List<String> patientIdList){
        return FluentIterable.from(patientIdList).transform(new Function<String, CrdbPatient>() {
            @Nullable
            @Override
            public CrdbPatient apply(String patientId) {
                Optional<CrdbDataset> dsOpt = findCrdbDatsetByPatientId(patientId);
                Optional<CrdbImpactSurvey> surveyOpt = findCrdbImpactSurveyByPatientId(patientId);
                if (dsOpt.isPresent() && surveyOpt.isPresent()) {
                    return new CrdbPatient(surveyOpt.get(), dsOpt.get());
                }
                // if one or both database records are missing, return a default instance of CrdbPatient
                return new CrdbPatient();
            }
        }).filter(new Predicate<CrdbPatient>() {
            // filter out incomplete records identified by an invalid id
            @Override
            public boolean apply(@Nullable CrdbPatient input) {
                return !input.getPatientId().equals("XXXX");
            }
        }).toList();
    }

    /**
     * find a CrdbDataset entry based on a patient id
     * use Optional return type to handle missing patient entries in CRDB
     * @param patientId
     * @return
     */
    Optional<CrdbDataset> findCrdbDatsetByPatientId(String patientId){
       this.crdbDatasetExample.clear();
        this.crdbDatasetExample.createCriteria().andDMP_IDEqualTo(patientId);
       List<CrdbDataset> crdbDatasets = this.crdbDatasetMapper.selectByExample(this.crdbDatasetExample);
        if( crdbDatasets.isEmpty()) {
            logger.info("Unable to find a CrdbDataset entry for patient id " + patientId);
            return Optional.absent();
        }
        return Optional.of(crdbDatasets.get(0));  // there should only be one
    }

    /**
     * find a CrdbImpactSurvey entry based on a patient id
     * use an Optional return type to handle missing patient entries in CRDB
     * @param patientId
     * @return
     */
    Optional<CrdbImpactSurvey> findCrdbImpactSurveyByPatientId(String patientId){
        this.crdbImpactSurveyExample.clear();
        this.crdbImpactSurveyExample.createCriteria().andDMP_IDEqualTo(patientId);
        List<CrdbImpactSurvey> crdbSurveyList = this.crdbSurveyMapper.selectByExample(this.crdbImpactSurveyExample);
        if(crdbSurveyList.isEmpty()){
            logger.info("Unable to find a CrdbImpactSurvey entry for patient id " + patientId);
            return Optional.absent();
        }
        return Optional.of(crdbSurveyList.get(0));
    }

    /**
     * Inner class representing the combined attributes of a CrdbImpactSurvey record and
     * a CrdbDataset record
     */
    protected class CrdbPatient {
        private CrdbImpactSurvey crdbImpactSurvey;
        private CrdbDataset crdbDataset;
        private final String patientId;

        CrdbPatient() { this.patientId = "XXXX";}

        CrdbPatient(CrdbImpactSurvey survey, CrdbDataset ds){
            this.patientId = survey.getDMP_ID();
            this.crdbDataset = ds;
            this.crdbImpactSurvey = survey;
        }

        /*
        Public getters to support report generation via reflection
         */
        public String getPatientId(){ return this.patientId;}

        public String getDMP_ID() {
            return this.crdbDataset.getDMP_ID();
        }

        public String getPRIM_DISEASE_12245() {
            return this.crdbDataset.getPRIM_DISEASE_12245();
        }

        public String getINITIAL_SX_YEAR() {
            return this.crdbDataset.getINITIAL_SX_YEAR();
        }

        public String getINITIAL_DX_YEAR() {
            return this.crdbDataset.getINITIAL_DX_YEAR();
        }

        public String getFIRST_METASTASIS_YEAR() {
            return this.crdbDataset.getFIRST_METASTASIS_YEAR();
        }

        public Short getINIT_DX_STATUS_ID() {
            return this.crdbDataset.getINIT_DX_STATUS_ID();
        }

        public String getINIT_DX_STATUS() {
            return this.crdbDataset.getINIT_DX_STATUS();
        }

        public String getINIT_DX_STATUS_YEAR() {
            return this.crdbDataset.getINIT_DX_STATUS_YEAR();
        }

        public String getINIT_DX_STAGING_DSCRP() {
            return this.crdbDataset.getINIT_DX_STAGING_DSCRP();
        }

        public String getINIT_DX_STAGE() {
            return this.crdbDataset.getINIT_DX_STAGE();
        }

        public String getINIT_DX_STAGE_DSCRP() {
            return this.crdbDataset.getINIT_DX_STAGE_DSCRP();
        }

        public String getINIT_DX_GRADE() {
            return this.crdbDataset.getINIT_DX_GRADE();
        }

        public String getINIT_DX_GRADE_DSCRP() {
            return this.crdbDataset.getINIT_DX_GRADE_DSCRP();
        }

        public String getINIT_DX_T_STAGE() {
            return this.crdbDataset.getINIT_DX_T_STAGE();
        }

        public String getINIT_DX_T_STAGE_DSCRP() {
            return this.crdbDataset.getINIT_DX_T_STAGE_DSCRP();
        }

        public String getINIT_DX_N_STAGE() {
            return this.crdbDataset.getINIT_DX_N_STAGE();
        }

        public String getINIT_DX_N_STAGE_DSCRP() {
            return this.getENROLL_DX_N_STAGE_DSCRP();
        }

        public String getINIT_DX_M_STAGE() {
            return this.crdbDataset.getINIT_DX_M_STAGE();
        }

        public String getINIT_DX_M_STAGE_DSCRP() {
            return this.crdbDataset.getINIT_DX_M_STAGE_DSCRP();
        }

        public String getINIT_DX_HIST() {
            return this.crdbDataset.getINIT_DX_HIST();
        }

        public String getINIT_DX_SUB_HIST() {
            return this.crdbDataset.getINIT_DX_SUB_HIST();
        }

        public String getINIT_DX_SUB_SUB_HIST() {
            return this.crdbDataset.getINIT_DX_SUB_SUB_HIST();
        }

        public String getINIT_DX_SUB_SUB_SUB_HIST() {
            return this.crdbDataset.getINIT_DX_SUB_SUB_SUB_HIST();
        }

        public String getINIT_DX_SITE() {
            return this.crdbDataset.getINIT_DX_SITE();
        }

        public String getINIT_DX_SUB_SITE() {
                return this.crdbDataset.getINIT_DX_SUB_SITE();
        }

        public String getINIT_DX_SUB_SUB_SITE() {
            return this.crdbDataset.getINIT_DX_SUB_SUB_SITE();
        }

        public Short getENROLL_DX_STATUS_ID() {
            return this.crdbDataset.getENROLL_DX_STATUS_ID();
        }

        public String getENROLL_DX_STATUS() {
            return this.crdbDataset.getENROLL_DX_STATUS();
        }

        public String getENROLL_DX_STATUS_YEAR() {
            return this.crdbDataset.getENROLL_DX_STATUS_YEAR();
        }

        public String getENROLL_DX_STAGING_DSCRP() {
            return this.crdbDataset.getENROLL_DX_STAGING_DSCRP();
        }

        public String getENROLL_DX_STAGE() {
            return this.crdbDataset.getENROLL_DX_STAGE();
        }


        public String getENROLL_DX_STAGE_DSCRP() {
            return this.crdbDataset.getENROLL_DX_STAGE_DSCRP();
        }

        public String getENROLL_DX_GRADE() {
            return this.crdbDataset.getENROLL_DX_GRADE();
        }

        public String getENROLL_DX_GRADE_DSCRP() {
            return this.crdbDataset.getENROLL_DX_GRADE_DSCRP();
        }


        public String getENROLL_DX_T_STAGE() {
            return this.crdbDataset.getENROLL_DX_T_STAGE();
        }

        public String getENROLL_DX_T_STAGE_DSCRP() {
            return this.crdbDataset.getENROLL_DX_T_STAGE_DSCRP();
        }

        public String getENROLL_DX_N_STAGE() {
            return this.crdbDataset.getENROLL_DX_N_STAGE();
        }

        public String getENROLL_DX_N_STAGE_DSCRP() {
            return this.crdbDataset.getENROLL_DX_N_STAGE_DSCRP();
        }

        public String getENROLL_DX_M_STAGE() {
            return this.crdbDataset.getENROLL_DX_M_STAGE();
        }

        public String getENROLL_DX_M_STAGE_DSCRP() {
            return this.crdbDataset.getENROLL_DX_M_STAGE_DSCRP();
        }

        public String getENROLL_DX_HIST() {
            return this.crdbDataset.getENROLL_DX_HIST();
        }

        public String getENROLL_DX_SUB_HIST() {
            return this.crdbDataset.getENROLL_DX_SUB_HIST();
        }

        public String getENROLL_DX_SUB_SUB_HIST() {
            return this.crdbDataset.getENROLL_DX_SUB_SUB_HIST();
        }

        public String getENROLL_DX_SUB_SUB_SUB_HIST() {
            return this.crdbDataset.getENROLL_DX_SUB_SUB_SUB_HIST();
        }

        public String getENROLL_DX_SITE() {
            return this.crdbDataset.getENROLL_DX_SITE();
        }

        public String getENROLL_DX_SUB_SITE() {
            return this.crdbDataset.getENROLL_DX_SUB_SITE();
        }

        public String getENROLL_DX_SUB_SUB_SITE() {
            return this.crdbDataset.getENROLL_DX_SUB_SUB_SITE();
        }

        public String getSURVIVAL_STATUS() {
            return this.crdbDataset.getSURVIVAL_STATUS();
        }

        public String getTREATMENT_END_YEAR() {
            return this.crdbDataset.getTREATMENT_END_YEAR();
        }

        public String getOFF_STUDY_YEAR() {
            return this.crdbDataset.getOFF_STUDY_YEAR();
        }

        public Date getQS_DATE() {
            return this.crdbImpactSurvey.getQS_DATE();
        }

        public String getADJ_TXT() {
            return this.crdbImpactSurvey.getADJ_TXT();
        }

        public String getNOSYSTXT() {
            return this.crdbImpactSurvey.getNOSYSTXT();
        }

        public String getPRIOR_RX() {
            return this.crdbImpactSurvey.getPRIOR_RX();
        }

        public String getBRAINMET() {
            return this.crdbImpactSurvey.getBRAINMET();
        }

        public String getECOG() {
            return this.crdbImpactSurvey.getECOG();
        }
    }
}
