package org.mskcc.cbio.importer.cvr.darwin.transformer;

import com.google.common.base.*;
import com.google.common.collect.*;

import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.darwin.dao.dvcbio.ClinicalNoteMapper;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.ClinicalNote;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.ClinicalNoteExample;
import org.mskcc.cbio.importer.cvr.darwin.service.DarwinDocumentParsers;
import org.mskcc.cbio.importer.cvr.darwin.util.DarwinSessionManager;
import org.mskcc.cbio.importer.cvr.darwin.util.IdMapService;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;

import java.nio.file.Path;
import java.nio.file.Paths;
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
 * Created by criscuof on 11/30/14.
 */
public class DarwinClinicalNoteDetailsTransformer  extends DarwinTransformer {
    private static final Logger logger = Logger.getLogger(DarwinClinicalNoteDetailsTransformer.class);
    private final ClinicalNoteMapper clinicalNoteMapper;
    private final ClinicalNoteExample clinicalNoteExample;
    private Integer seq  = 0;
    private static final String patientIdColumn = "Patient ID";
    private static final  String clinicalNoteDetailsFile = "data_clinical_clinicalnotedetails.txt";

    public DarwinClinicalNoteDetailsTransformer(Path clinPath){
        super(clinPath.resolve(clinicalNoteDetailsFile));
        this.clinicalNoteExample = new ClinicalNoteExample();
        this.clinicalNoteMapper = DarwinSessionManager.INSTANCE.getDarwinSession()
                .getMapper(ClinicalNoteMapper.class);
    }

    @Override
    public void transform() {
        //this.writeStagingFile(this.generateReportByPatientIdList(IdMapService.INSTANCE.getDarwinIdList()));
        this.writeStagingFile(this.generateReportByPatientIdList(Lists.newArrayList(1519355)));
        return ;
    }

    @Override
    public List<String> generateReportByPatientId(Integer patientId) {
        Preconditions.checkArgument(null != patientId && patientId >0,
                "A valid patient Id is required");
        return this.generateReportByPatientIdList(Lists.newArrayList(patientId));

    }

    @Override
    public List<String> generateReportByPatientIdList(List<Integer> patientIdList) {
        Preconditions.checkArgument(null != patientIdList && patientIdList.size() > 0,
                "A valid list of patient ids is required.");

        Table<Integer, String,String> clinTable = processClinicalNoteDetails(patientIdList);
        List<String> reportList = Lists.newArrayList();
        reportList.add( StagingCommonNames.tabJoiner.join(clinTable.columnKeySet()));
        Set<String> colSet = clinTable.columnKeySet();
        for (Integer row : clinTable.rowKeySet()){
            List<String> valueList = Lists.newArrayList();
            for (String column : colSet){
                if (!Strings.isNullOrEmpty(clinTable.get(row,column))) {
                    valueList.add(clinTable.get(row,column).replace("\n"," "));
                } else {
                    valueList.add(" ");
                }
            }
            reportList.add( StagingCommonNames.tabJoiner.join(valueList));
        }
        return reportList;
    }

    /*
    private method to generate a Table of clinical note details for a List of patient ids
     */
    private Table<Integer, String,String> processClinicalNoteDetails (List<Integer> patientIdList){
        this.clinicalNoteExample.clear();
        this.clinicalNoteExample.createCriteria().andCLNT_PT_DEIDENTIFICATION_IDIn(patientIdList);
        Table<Integer, String,String> clinTable = HashBasedTable.create();
        for(ClinicalNote cn : this.clinicalNoteMapper.selectByExampleWithBLOBs(this.clinicalNoteExample)){
            String clinicalDoc = cn.getCLNT_DEID_MEDICAL_DOCUMENT_TXT();
            Integer patientId = cn.getCLNT_PT_DEIDENTIFICATION_ID();
            if (!Strings.isNullOrEmpty(clinicalDoc))
                DarwinDocumentParsers.parseDarwinClinicalNoteDocument(patientId, clinicalDoc, clinTable);
        }
        return clinTable;
    }

    public static void main (String...args){
        logger.info("Start....");

        Path clinicalPath = Paths.get("/tmp/cvr/patient/clinical");
        DarwinClinicalNoteDetailsTransformer transformer = new DarwinClinicalNoteDetailsTransformer(clinicalPath);
        transformer.transform();
       // for (String s : transformer.generateReportByPatientId(1519355)) {
        //    System.out.println(s);
       // }
        // terminate the SQL session
        DarwinSessionManager.INSTANCE.closeSession();
        logger.info("FINIS...");

    }

}
