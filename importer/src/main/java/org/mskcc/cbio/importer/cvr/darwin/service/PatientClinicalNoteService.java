package org.mskcc.cbio.importer.cvr.darwin.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.darwin.dao.dvcbio.ClinicalNoteMapper;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.ClinicalNote;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.ClinicalNoteExample;
import org.mskcc.cbio.importer.cvr.darwin.transformer.DarwinClinicalNoteTransformer;
import org.mskcc.cbio.importer.cvr.darwin.util.DarwinSessionManager;
import org.mskcc.cbio.importer.cvr.darwin.util.IdMapService;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
 * Created by criscuof on 11/30/14.
 */
public class PatientClinicalNoteService {
    private static final Logger logger = Logger.getLogger(PatientClinicalNoteService.class);
    private final ClinicalNoteMapper clinicalNoteMapper;
    private final ClinicalNoteExample clinicalNoteExample;
    private final Path clinicalDetailsPath;
    private Integer seq  = 0;
    private static final String patientIdColumn = "Patient ID";

    public PatientClinicalNoteService(Path clinPath){
        Preconditions.checkArgument(null != clinPath, "A Path for clinical reports is required");
        this.clinicalDetailsPath = clinPath.resolve("clinical_notes.dtails.txt");
        this.clinicalNoteExample = new ClinicalNoteExample();
        this.clinicalNoteMapper = DarwinSessionManager.INSTANCE.getDarwinSession()
                .getMapper(ClinicalNoteMapper.class);
    }



    private void processClinicalNotesForPatient(Integer patientId){
        this.clinicalNoteExample.clear();
        this.clinicalNoteExample.createCriteria().andCLNT_PT_DEIDENTIFICATION_IDEqualTo(patientId);
        Table<Integer, String,String> clinTable = HashBasedTable.create();
        for(ClinicalNote cn : this.clinicalNoteMapper.selectByExampleWithBLOBs(this.clinicalNoteExample)){
            String clinicalDoc = cn.getCLNT_DEID_MEDICAL_DOCUMENT_TXT();
            if (!Strings.isNullOrEmpty(clinicalDoc))
                this.parseClinicalDoc(patientId, clinicalDoc, clinTable);
        }
        this.exportClinicalNotes(clinTable);
    }

    private void exportClinicalNotes(Table<Integer,String,String> clinTable ) {
        OpenOption[] options = new OpenOption[]{ CREATE, DSYNC};
        List<String> reportList = Lists.newArrayList();
        reportList.add( StagingCommonNames.tabJoiner.join(clinTable.columnKeySet()));
        Set<String> colSet = clinTable.columnKeySet();
        for (Integer row : clinTable.rowKeySet()){
            List<String> valueList = Lists.newArrayList();
            for (String column : colSet){
                valueList.add(clinTable.get(row,column));

            }
            reportList.add( StagingCommonNames.tabJoiner.join(valueList));

        }
        try {
            Files.deleteIfExists(this.clinicalDetailsPath);
            Files.write(this.clinicalDetailsPath, reportList, Charset.defaultCharset(),options);
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

    }

 /*
 private method to filter and parse clinical attributes from a String encapsulating the
 entire clinical note
  */
   private void parseClinicalDoc (Integer patientId, String clinDoc, Table<Integer, String,String> clinTable){

        List<String> lines = Lists.newArrayList(StagingCommonNames.lineSplitter.split(clinDoc));
        logger.info("Clinical notes has " +lines.size() + " lines");
        List<String> filteredLines = FluentIterable.from(lines)
                .filter(new Predicate<String>() {
                    @Override
                    public boolean apply(String line) {
                        return StringUtils.isNotBlank(line);
                    }
                })
                .filter(new Predicate<String>() {
                    @Override
                    public boolean apply(String line) {
                        for (String skipWord : ClinicalNoteNames.FILTER_LIST) {
                            if (line.startsWith(skipWord)) {
                                return false;
                            }
                        }
                        return true;
                    }
                }).toList();

        String attributeName = patientIdColumn;
        StringBuilder sb = new StringBuilder(patientId.toString());
        seq++;

       /*
       process each report line
       determine in a line represents the start of a new clinical attribute or the
       continuation of the current clinical attribute.
        */
        for ( String line : filteredLines){
            boolean processed = false;
            for (String attribute : ClinicalNoteNames.CN_ATTIBUTE_LIST){
                if (line.startsWith(attribute)) {
                    if(null != attributeName && sb.length() > 0) {
                        clinTable.put(seq, attributeName,sb.toString());
                       // logger.info("row = " + seq.toString() +" col = " +attributeName  +" value = " +sb.toString());
                        sb.setLength(0);

                    }
                    attributeName = attribute;
                    sb.append(line.replace(attribute,""));
                    processed = true;
                }
            }
            // if the line is a continuation, add its value to the buffer
            if(!processed){
                sb.append(" ");
                sb.append(line);
            }
        }
        return ;
    }


    public static void main (String...args){
        OpenOption[] options = new OpenOption[]{ CREATE, APPEND, DSYNC};
        Path clinicalPath = Paths.get("/tmp/cvr/patient/clinical");
        PatientClinicalNoteService service = new PatientClinicalNoteService(clinicalPath);
        service.processClinicalNotesForPatient(1519355);

    }

}
