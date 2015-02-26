package org.mskcc.cbio.importer.cvr.darwin.service;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mskcc.cbio.importer.cvr.darwin.transformer.*;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;


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

    private final Map<String,DarwinTransformer> transformerMap = Maps.newHashMap();
    private final Path stagingFilePath;
    private final XSSFWorkbook  workbook;

    public PatientClinicalReportGenerator(Path filePath){
        Preconditions.checkArgument(null != filePath, " A file path is required");
        this.stagingFilePath = filePath;
        this.completeTransformerMap();
        this.workbook = new XSSFWorkbook();
    }

    private void completeTransformerMap(){
        this.transformerMap.put("Patient", new DarwinPatientTransformer(this.stagingFilePath));
        this.transformerMap.put("ClinicalNotes",new DarwinClinicalNoteTransformer(this.stagingFilePath) );
        this.transformerMap.put("ClinicalNoteDetails", new DarwinClinicalNoteDetailsTransformer(this.stagingFilePath));
        this.transformerMap.put("Lab Results", new DarwinLabResultTransformer(this.stagingFilePath));
        this.transformerMap.put("Pathology", new DarwinPathologyDataTransformer(this.stagingFilePath));
        this.transformerMap.put("PathologyDetails", new DarwinPathologyDataDetailsTransformer(this.stagingFilePath));
        this.transformerMap.put("Tumor",new DarwinTumorTransformer(this.stagingFilePath));
    }

    public void generateWorksheet(final Integer patientId){
        Preconditions.checkArgument(null != patientId && patientId > 0 ,
                "A valid patient id is required");
        logger.info("Processing patient " +patientId);
        for (String name : this.transformerMap.keySet()){
            logger.info("Processing sheet " +name);
            DarwinTransformer transformer = this.transformerMap.get(name);
            this.generateSheet(name, transformer.generateReportByPatientId(patientId) );
        }
        try
        {
            //Write the workbook in file system
            String filename = "/tmp/cvr/patient/patient_" +patientId +".xlsx";
            FileOutputStream out = new FileOutputStream(new File(filename));
            workbook.write(out);
            out.close();
            System.out.println(filename + " written successfully on disk.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void generateSheet(String name, List<String> dataList) {
        logger.info("name = " +name + " lines = " +dataList.size());
        XSSFSheet sheet = workbook.createSheet(name);
        int rownum = 0;
        for (String data : dataList){
            Row row = sheet.createRow(rownum++);
           List<String> rowList = StagingCommonNames.tabSplitter.splitToList(data);
            int cellnum = 0;
            for(String  cellString : rowList){
                Cell cell = row.createCell(cellnum++);
                cell.setCellValue(cellString);
            }
        }
        logger.info("Created worksheet " +name);
        return;
    }

    public static void main (String...args){
        OpenOption[] options = new OpenOption[]{ CREATE, APPEND, DSYNC};
        Path patientPath = Paths.get("/tmp/cvr/patient");
        PatientClinicalReportGenerator generator = new PatientClinicalReportGenerator(patientPath);
        generator.generateWorksheet(1519355);

    }
}
