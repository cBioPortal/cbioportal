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

    public DarwinClinicalNoteTransformer(Path aPath) {
        super(aPath);
        this.clinicalNoteExample = new ClinicalNoteExample();
        this.clinicalNoteMapper = DarwinSessionManager.INSTANCE.getDarwinSession()
                .getMapper(ClinicalNoteMapper.class);
    }

    private List<String> generateClinicalNoteReport() {
        List<String> clinicalNoteDataList = Lists.newArrayList(this.generateColumnHeaders(ClinicalNote.class));
        List<Object> clinicalNoteObjectList = new ArrayList<Object>
                (this.clinicalNoteMapper.selectByExampleWithBLOBs(clinicalNoteExample));
        clinicalNoteDataList.addAll(this.generateStagingFileRecords(clinicalNoteObjectList));
        return clinicalNoteDataList;
    }

    public void transform() {
        this.clinicalNoteExample.clear();
        this.clinicalNoteExample.createCriteria()
                .andCLNT_PT_DEIDENTIFICATION_IDIn(IdMapService.INSTANCE.getDarwinIdList());
        this.writeStagingFile(this.generateClinicalNoteReport());
        return ;
    }

    @Override
    public List<String> generateReportByPatientId(Integer patientId) {
        Preconditions.checkArgument(null != patientId && patientId > 0,
                "A valid patient id is required");
        this.clinicalNoteExample.clear();
        this.clinicalNoteExample.createCriteria().andCLNT_PT_DEIDENTIFICATION_IDEqualTo(patientId);
        return this.generateClinicalNoteReport();
    }
    // main class for testing
    public static void main (String...args){
        Path filePath = Paths.get("/tmp/cvr/data_clinical_clinical_note.txt");
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
