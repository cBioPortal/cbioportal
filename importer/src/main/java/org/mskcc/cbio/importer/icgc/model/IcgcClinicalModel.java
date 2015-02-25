package org.mskcc.cbio.importer.icgc.model;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.util.StringUtils;
import org.mskcc.cbio.importer.icgc.support.IcgcFunctionLibrary;
import org.mskcc.cbio.importer.icgc.support.IcgcUtil;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.util.StagingUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
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
 * Created by criscuof on 12/18/14.
 */
public class IcgcClinicalModel extends IcgcModel{

    private String icgc_donor_id;
    private String project_code;
    private String submitted_donor_id;
    private String donor_sex;
    private String donor_vital_status;
    private String disease_status_last_followup;
    private String donor_relapse_type;
    private String donor_age_at_diagnosis;
    private String donor_age_at_enrollment;
    private String donor_age_at_last_followup;
    private String donor_relapse_interval;
    private String donor_diagnosis_icd10;
    private String donor_tumour_staging_system_at_diagnosis;
    private String donor_tumour_stage_at_diagnosis;
    private String donor_tumour_stage_at_diagnosis_supplemental;
    private String donor_survival_time;
    private String donor_interval_of_last_followup;
    private String icgc_specimen_id;
    private String submitted_specimen_id;
    private String specimen_type;
    private String specimen_type_other;
    private String specimen_interval;
    private String specimen_donor_treatment_type;
    private String specimen_donor_treatment_type_other;
    private String specimen_processing;
    private String specimen_processing_other;
    private String specimen_storage;
    private String specimen_storage_other;
    private String tumour_confirmed;
    private String specimen_biobank;
    private String specimen_biobank_id;
    private String specimen_available;
    private String tumour_histological_type;
    private String tumour_grading_system;
    private String tumour_grade;
    private String tumour_grade_supplemental;
    private String tumour_stage_system;
    private String tumour_stage;
    private String tumour_stage_supplemental;
    private String digital_image_of_stained_section;

    public static final Map<String,String> transformationMap = Maps.newTreeMap();
    static {
        transformationMap.put("001STUDY_ID", "getProject_code");
        transformationMap.put("002SAMPLE_ID", "getSubmitted_specimen_id");
        transformationMap.put("003DONOR_ID", "getSubmitted_donor_id");
        transformationMap.put("004GENDER", "getDonor_sex");
        transformationMap.put("005VITAL_STATUS", "getDonor_vital_status");
        transformationMap.put("006DONOR_STATUS_LAST_FOLLOwUP","getDisease_status_last_followup");
        transformationMap.put("007DONOR_RELAPSE_TYPE","getDonor_relapse_type");
        transformationMap.put("008DONOR_DIAGNOSIS_ICD10","getDonor_diagnosis_icd10");
        transformationMap.put("009DONOR_SURVIVAL_TIME","getDonor_survival_time");
        transformationMap.put("010SPECIMEN_TYPE", "getSpecimen_type");
        transformationMap.put("011TUMOR_CONFIRMED","getTumour_confirmed");
        transformationMap.put("012TUMOR_HISTOLOGICAL_TYPE","getTumour_histological_type");
        transformationMap.put("013TUMOR_GRADE","getTumour_grade");
        transformationMap.put("014TUMOR_STAGE","getTumour_stage");
        transformationMap.put("015TUMOR_CONFIRMED","getTumour_confirmed");
    }


     public static String[] getFieldNames() {
         return StagingUtils.resolveFieldNames(IcgcClinicalModel.class);
     }

    final public static Function<IcgcClinicalModel, String> transformationFunction = new Function<IcgcClinicalModel, String>() {
        @Override
        public String apply(final IcgcClinicalModel mm) {
            Set<String> attributeList = transformationMap.keySet();
            List<String> mafAttributes = FluentIterable.from(attributeList)
                    .transform(new Function<String, String>() {
                        @Override
                        public String apply(String attribute) {
                            String getterName = transformationMap.get(attribute);
                            return StagingUtils.pojoStringGetter(getterName, mm);

                        }
                    }).toList();
            String retRecord = StagingCommonNames.tabJoiner.join(mafAttributes);

            return retRecord;
        }

    };

    public String getIcgc_donor_id() {
        return icgc_donor_id;
    }

    public void setIcgc_donor_id(String icgc_donor_id) {
        this.icgc_donor_id = icgc_donor_id;
    }

    public String getProject_code() {
        return project_code;
    }

    public void setProject_code(String project_code) {
        this.project_code = project_code;
    }

    public String getSubmitted_donor_id() {
        return submitted_donor_id;
    }

    public void setSubmitted_donor_id(String submitted_donor_id) {
        this.submitted_donor_id = submitted_donor_id;
    }

    public String getDonor_sex() {
        return donor_sex;
    }

    public void setDonor_sex(String donor_sex) {
        this.donor_sex = donor_sex;
    }

    public String getDonor_vital_status() {
        return donor_vital_status;
    }

    public void setDonor_vital_status(String donor_vital_status) {
        this.donor_vital_status = donor_vital_status;
    }

    public String getDisease_status_last_followup() {
        return disease_status_last_followup;
    }

    public void setDisease_status_last_followup(String disease_status_last_followup) {
        this.disease_status_last_followup = disease_status_last_followup;
    }

    public String getDonor_relapse_type() {
        return donor_relapse_type;
    }

    public void setDonor_relapse_type(String donor_relapse_type) {
        this.donor_relapse_type = donor_relapse_type;
    }

    public String getDonor_age_at_diagnosis() {
        return donor_age_at_diagnosis;
    }

    public void setDonor_age_at_diagnosis(String donor_age_at_diagnosis) {
        this.donor_age_at_diagnosis = donor_age_at_diagnosis;
    }

    public String getDonor_age_at_enrollment() {
        return donor_age_at_enrollment;
    }

    public void setDonor_age_at_enrollment(String donor_age_at_enrollment) {
        this.donor_age_at_enrollment = donor_age_at_enrollment;
    }

    public String getDonor_age_at_last_followup() {
        return donor_age_at_last_followup;
    }

    public void setDonor_age_at_last_followup(String donor_age_at_last_followup) {
        this.donor_age_at_last_followup = donor_age_at_last_followup;
    }

    public String getDonor_relapse_interval() {
        return donor_relapse_interval;
    }

    public void setDonor_relapse_interval(String donor_relapse_interval) {
        this.donor_relapse_interval = donor_relapse_interval;
    }

    public String getDonor_diagnosis_icd10() {
        return donor_diagnosis_icd10;
    }

    public void setDonor_diagnosis_icd10(String donor_diagnosis_icd10) {
        this.donor_diagnosis_icd10 = donor_diagnosis_icd10;
    }

    public String getDonor_tumour_staging_system_at_diagnosis() {
        return donor_tumour_staging_system_at_diagnosis;
    }

    public void setDonor_tumour_staging_system_at_diagnosis(String donor_tumour_staging_system_at_diagnosis) {
        this.donor_tumour_staging_system_at_diagnosis = donor_tumour_staging_system_at_diagnosis;
    }

    public String getDonor_tumour_stage_at_diagnosis() {
        return donor_tumour_stage_at_diagnosis;
    }

    public void setDonor_tumour_stage_at_diagnosis(String donor_tumour_stage_at_diagnosis) {
        this.donor_tumour_stage_at_diagnosis = donor_tumour_stage_at_diagnosis;
    }

    public String getDonor_tumour_stage_at_diagnosis_supplemental() {
        return donor_tumour_stage_at_diagnosis_supplemental;
    }

    public void setDonor_tumour_stage_at_diagnosis_supplemental(String donor_tumour_stage_at_diagnosis_supplemental) {
        this.donor_tumour_stage_at_diagnosis_supplemental = donor_tumour_stage_at_diagnosis_supplemental;
    }

    public String getDonor_survival_time() {
        return donor_survival_time;
    }

    public void setDonor_survival_time(String donor_survival_time) {
        this.donor_survival_time = donor_survival_time;
    }

    public String getDonor_interval_of_last_followup() {
        return donor_interval_of_last_followup;
    }

    public void setDonor_interval_of_last_followup(String donor_interval_of_last_followup) {
        this.donor_interval_of_last_followup = donor_interval_of_last_followup;
    }

    public String getIcgc_specimen_id() {
        return icgc_specimen_id;
    }

    public void setIcgc_specimen_id(String icgc_specimen_id) {
        this.icgc_specimen_id = icgc_specimen_id;
    }

    public String getSubmitted_specimen_id() {
        return submitted_specimen_id;
    }

    public void setSubmitted_specimen_id(String submitted_specimen_id) {
        this.submitted_specimen_id = submitted_specimen_id;
    }

    public String getSpecimen_type() {
        return specimen_type;
    }

    public void setSpecimen_type(String specimen_type) {
        this.specimen_type = specimen_type;
    }

    public String getSpecimen_type_other() {
        return specimen_type_other;
    }

    public void setSpecimen_type_other(String specimen_type_other) {
        this.specimen_type_other = specimen_type_other;
    }

    public String getSpecimen_interval() {
        return specimen_interval;
    }

    public void setSpecimen_interval(String specimen_interval) {
        this.specimen_interval = specimen_interval;
    }

    public String getSpecimen_donor_treatment_type() {
        return specimen_donor_treatment_type;
    }

    public void setSpecimen_donor_treatment_type(String specimen_donor_treatment_type) {
        this.specimen_donor_treatment_type = specimen_donor_treatment_type;
    }

    public String getSpecimen_donor_treatment_type_other() {
        return specimen_donor_treatment_type_other;
    }

    public void setSpecimen_donor_treatment_type_other(String specimen_donor_treatment_type_other) {
        this.specimen_donor_treatment_type_other = specimen_donor_treatment_type_other;
    }

    public String getSpecimen_processing() {
        return specimen_processing;
    }

    public void setSpecimen_processing(String specimen_processing) {
        this.specimen_processing = specimen_processing;
    }

    public String getSpecimen_processing_other() {
        return specimen_processing_other;
    }

    public void setSpecimen_processing_other(String specimen_processing_other) {
        this.specimen_processing_other = specimen_processing_other;
    }

    public String getSpecimen_storage() {
        return specimen_storage;
    }

    public void setSpecimen_storage(String specimen_storage) {
        this.specimen_storage = specimen_storage;
    }

    public String getSpecimen_storage_other() {
        return specimen_storage_other;
    }

    public void setSpecimen_storage_other(String specimen_storage_other) {
        this.specimen_storage_other = specimen_storage_other;
    }

    public String getTumour_confirmed() {
        return tumour_confirmed;
    }

    public void setTumour_confirmed(String tumour_confirmed) {
        this.tumour_confirmed = tumour_confirmed;
    }

    public String getSpecimen_biobank() {
        return specimen_biobank;
    }

    public void setSpecimen_biobank(String specimen_biobank) {
        this.specimen_biobank = specimen_biobank;
    }

    public String getSpecimen_biobank_id() {
        return specimen_biobank_id;
    }

    public void setSpecimen_biobank_id(String specimen_biobank_id) {
        this.specimen_biobank_id = specimen_biobank_id;
    }

    public String getSpecimen_available() {
        return specimen_available;
    }

    public void setSpecimen_available(String specimen_available) {
        this.specimen_available = specimen_available;
    }

    public String getTumour_histological_type() {
        return tumour_histological_type;
    }

    public void setTumour_histological_type(String tumour_histological_type) {
        this.tumour_histological_type = tumour_histological_type;
    }

    public String getTumour_grading_system() {
        return tumour_grading_system;
    }

    public void setTumour_grading_system(String tumour_grading_system) {
        this.tumour_grading_system = tumour_grading_system;
    }

    public String getTumour_grade() {
        return tumour_grade;
    }

    public void setTumour_grade(String tumour_grade) {
        this.tumour_grade = tumour_grade;
    }

    public String getTumour_grade_supplemental() {
        return tumour_grade_supplemental;
    }

    public void setTumour_grade_supplemental(String tumour_grade_supplemental) {
        this.tumour_grade_supplemental = tumour_grade_supplemental;
    }

    public String getTumour_stage_system() {
        return tumour_stage_system;
    }

    public void setTumour_stage_system(String tumour_stage_system) {
        this.tumour_stage_system = tumour_stage_system;
    }

    public String getTumour_stage() {
        return tumour_stage;
    }

    public void setTumour_stage(String tumour_stage) {
        this.tumour_stage = tumour_stage;
    }

    public String getTumour_stage_supplemental() {
        return tumour_stage_supplemental;
    }

    public void setTumour_stage_supplemental(String tumour_stage_supplemental) {
        this.tumour_stage_supplemental = tumour_stage_supplemental;
    }

    public String getDigital_image_of_stained_section() {
        return digital_image_of_stained_section;
    }

    public void setDigital_image_of_stained_section(String digital_image_of_stained_section) {
        this.digital_image_of_stained_section = digital_image_of_stained_section;
    }

    @Override
    public Map<String, String> getTransformationMap() {
        return transformationMap;
    }

    public static void main (String...args){
        // read in a small ICGC clinical data from URL or local file

        String dataSourceUrl = "https://dcc.icgc.org/api/v1/download?fn=/current/Projects/BOCA-UK/clinical.BOCA-UK.tsv.gz";
        if(!IcgcUtil.isIcgcConnectionWorking()) {
            dataSourceUrl = "///Users/criscuof/Downloads/clinical.BOCA-UK.tsv.gz";
        }
        List<String> lines = Lists.newArrayList();
        List<IcgcSimpleSomaticMutationModel> modelList = Lists.newArrayList();
        Path stagingFilePath = Paths.get("/tmp/icgctest/data_clinical.txt");
        MutationFileHandlerImpl fileHandler = new MutationFileHandlerImpl();
        fileHandler.registerTsvStagingFile(stagingFilePath,Lists.newArrayList(
                IcgcFunctionLibrary.resolveColumnNames(IcgcClinicalModel.transformationMap)),true);
        System.out.println("Processing data from: " +dataSourceUrl);
        try {
            BufferedReader rdr = new BufferedReader(new InputStreamReader(IOUtils.getInputStreamFromURLOrClasspathOrFileSystem(dataSourceUrl)));
            String line = "";
            int lineCount = 0;
            while ((line = rdr.readLine()) != null) {
                if (lineCount++ > 0){
                    IcgcClinicalModel model = StringUtils.columnStringToObject(IcgcClinicalModel.class,
                            line, StagingCommonNames.tabPattern, IcgcFunctionLibrary.resolveFieldNames( IcgcClinicalModel.class));
                    fileHandler.transformImportDataToTsvStagingFile(Lists.newArrayList(model),model.transformationFunction  );
                }
            }
            System.out.println(lineCount +" records processed");
            System.out.println("FINIS...");
        } catch (IOException | InvocationTargetException | NoSuchMethodException |  NoSuchFieldException
                | InstantiationException  | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


}
