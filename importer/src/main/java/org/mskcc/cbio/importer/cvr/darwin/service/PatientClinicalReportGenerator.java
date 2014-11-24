package org.mskcc.cbio.importer.cvr.darwin.service;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.darwin.transformer.*;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.DSYNC;

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
public class PatientClinicalReportGenerator {
    /*
    responsible for generating a tsv file containing all persisted clinical findings
    for a specified patient within the Darwin data repository
     */

    private static final Logger logger = Logger.getLogger(PatientClinicalReportGenerator.class);
    private final List<DarwinTransformer> darwinTransformerList = Lists.newArrayList();
    private final Path stagingFilePath;


    public PatientClinicalReportGenerator(Path filePath){
        Preconditions.checkArgument(null != filePath, " A file path is required");
        this.stagingFilePath = filePath;
        this.completeTransformerList();

    }

    private void completeTransformerList(){
        this.darwinTransformerList.add(new DarwinPatientTransformer(this.stagingFilePath.resolve("data_clinical_patient.txt")));
        this.darwinTransformerList.add( new DarwinClinicalNoteTransformer(this.stagingFilePath.resolve("data_clinical_clinical_note.txt")));
        this.darwinTransformerList.add(new DarwinLabResultTransformer(this.stagingFilePath.resolve("data_clinical_lab_result.txt")));
        this.darwinTransformerList.add( new DarwinPathologyDataTransformer(this.stagingFilePath.resolve("data_clinical_pathology_result.txt")));
        this.darwinTransformerList.add(new DarwinTumorTransformer(this.stagingFilePath.resolve("data_clinical_tumor.txt")));
    }


    public List<String> generatePatientReport(final Integer patientId) {
        Preconditions.checkArgument(null != patientId && patientId > 0 ,
                "A valid patient id is required");
        List<String>patientReport = Lists.newArrayList();
        for (DarwinTransformer transformer : this.darwinTransformerList){
            patientReport.addAll(transformer.generateReportByPatientId(patientId));
        }
        return patientReport;

    }

    public static void main (String...args){
        OpenOption[] options = new OpenOption[]{ CREATE, APPEND, DSYNC};
        Path patientPath = Paths.get("/tmp/cvr/patient");
        PatientClinicalReportGenerator generator = new PatientClinicalReportGenerator(patientPath);
        List<String> report = generator.generatePatientReport(1339055);
        Path reportPath = patientPath.resolve("patient_1339055.txt");
        try {
            Files.deleteIfExists(reportPath);
            Files.write(reportPath, report, Charset.defaultCharset(),options);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    

}
