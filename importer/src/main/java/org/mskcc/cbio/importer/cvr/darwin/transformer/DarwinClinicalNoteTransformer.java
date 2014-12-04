package org.mskcc.cbio.importer.cvr.darwin.transformer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.darwin.dao.dvcbio.ClinicalNoteMapper;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.ClinicalNote;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.ClinicalNoteExample;
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
 * Created by criscuof on 11/22/14.
 */
public class DarwinClinicalNoteTransformer extends DarwinTransformer {

    private static final Logger logger = Logger.getLogger(DarwinClinicalNoteTransformer.class);
    private final ClinicalNoteMapper clinicalNoteMapper;
    private final ClinicalNoteExample clinicalNoteExample;
    private static final String clinicalNoteFile = "data_clinical_clinicalnote.txt";

    public DarwinClinicalNoteTransformer(Path aPath) {
        super(aPath.resolve(clinicalNoteFile));
        this.clinicalNoteExample = new ClinicalNoteExample();
        this.clinicalNoteMapper = DarwinSessionManager.INSTANCE.getDarwinSession()
                .getMapper(ClinicalNoteMapper.class);
    }

    /*
    private method to generate a List of tsv Strings. The first String in the list
    contains the attribute names; subsequent Strings contain item values
    This method queries the ClinicalNotes view based on the criteria specified
    in the ClinicalNoteExample object
     */
    private List<String> generateClinicalNoteReport() {
        List<String> clinicalNoteDataList = Lists.newArrayList(this.generateColumnHeaders(ClinicalNote.class));
        List<Object> clinicalNoteObjectList = new ArrayList<Object>
                (this.clinicalNoteMapper.selectByExampleWithBLOBs(clinicalNoteExample));
        clinicalNoteDataList.addAll(this.generateStagingFileRecords(clinicalNoteObjectList));
        return clinicalNoteDataList;
    }

    /*
    transforms all the data in the ClinicNotes view into a tsv Strings and exports
    them to a staging file. The exported report includes column headings
     */
    public void transform() {
        this.clinicalNoteExample.clear();
        this.clinicalNoteExample.createCriteria()
                .andCLNT_PT_DEIDENTIFICATION_IDIn(IdMapService.INSTANCE.getDarwinIdList());
        this.writeStagingFile(this.generateClinicalNoteReport());
        return ;
    }

    /*
    public method to generate a report contain the clinical note data for a specified
    patient as a List of Strings. The first item in the list is the column headings
     */
    @Override
    public List<String> generateReportByPatientId(Integer patientId) {
        Preconditions.checkArgument(null != patientId && patientId > 0,
                "A valid patient id is required");
        this.clinicalNoteExample.clear();
        this.clinicalNoteExample.createCriteria().andCLNT_PT_DEIDENTIFICATION_IDEqualTo(patientId);
        return this.generateClinicalNoteReport();
    }

    @Override
    public List<String> generateReportByPatientIdList(List<Integer> patientIdList) {
        Preconditions.checkArgument(null != patientIdList, "A List of patient ids is required");
        this.clinicalNoteExample.clear();
        this.clinicalNoteExample.createCriteria()
                .andCLNT_PT_DEIDENTIFICATION_IDIn(patientIdList);
        return this.generateClinicalNoteReport();
    }

    // main class for testing
    public static void main (String...args){
        Path filePath = Paths.get("/tmp/cvr");
        DarwinClinicalNoteTransformer transformer = new DarwinClinicalNoteTransformer(filePath);
        transformer.transform();
        // test for an individual patient
        for (String s : transformer.generateReportByPatientId(1519355)) {
            System.out.println(s);
        }
        // terminate the SQL session
        DarwinSessionManager.INSTANCE.closeSession();
    }

}
