package org.mskcc.cbio.importer.cvr.darwin.service;

import com.google.common.base.*;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.darwin.dao.dvcbio.ClinicalNoteMapper;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.ClinicalNote;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.ClinicalNoteExample;
import org.mskcc.cbio.importer.cvr.darwin.transformer.DarwinTransformer;
import org.mskcc.cbio.importer.cvr.darwin.util.DarwinSessionManager;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

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
public class DarwinClinicalNoteDetailsService  {
    private static final Logger logger = Logger.getLogger(DarwinClinicalNoteDetailsService.class);
    private final ClinicalNoteMapper clinicalNoteMapper;
    private final ClinicalNoteExample clinicalNoteExample;
    private final Path clinicalDetailsPath;
    private Integer seq  = 0;
    private static final String patientIdColumn = "Patient ID";
    private static final  String clinicalNoteDetailsFile = "data_clinical_clinicalnotedetails.txt";

    public DarwinClinicalNoteDetailsService(Path clinPath){
        Preconditions.checkArgument(null != clinPath, "A Path for clinical reports is required");
        this.clinicalDetailsPath = clinPath.resolve(clinicalNoteDetailsFile);
        this.clinicalNoteExample = new ClinicalNoteExample();
        this.clinicalNoteMapper = DarwinSessionManager.INSTANCE.getDarwinSession()
                .getMapper(ClinicalNoteMapper.class);
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
                this.parseClinicalDoc(patientId, clinicalDoc, clinTable);
        }
        return clinTable;
    }

    /*
    private method to generate a clinical note details report for a single patient id
     */
    private void processClinicalNotesForPatient(Integer patientId){

        this.exportClinicalNotes(this.processClinicalNoteDetails(
              Lists.newArrayList(patientId) ));

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
                        for (String skipWord : ClinicalNoteNames.CN_FILTER_LIST) {
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
                        if (attributeName.equals(ClinicalNoteNames.CN_REVIEW_OF_SYSTEMS)){
                            this.processROS(seq, sb.toString(), clinTable);
                        } else {
                            clinTable.put(seq, attributeName, sb.toString());

                        }
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

    /*
    Review of Systems must be parsed by keyword substrings
     */
    private void processROS(Integer seq, String rosString, Table<Integer, String,String> clinTable){
        Splitter wsSplitter = Splitter.on(CharMatcher.BREAKING_WHITESPACE).trimResults().omitEmptyStrings();
        String s1 = CharMatcher.JAVA_ISO_CONTROL.removeFrom(rosString);
        String s2 = CharMatcher.WHITESPACE.trimAndCollapseFrom(s1,' ');
        List<String> words = wsSplitter.splitToList(s2);
        String keyword = null;
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            //logger.info(word);
            if (ClinicalNoteNames.ROS_KEYWORD_LIST.contains(word)){
                if(!Strings.isNullOrEmpty(keyword) && !Strings.isNullOrEmpty(sb.toString())){
                    String columnName = "ROS_" +keyword;
                    clinTable.put(seq, columnName, sb.toString());

                }
                keyword = word;
                sb.setLength(0);
            } else if (!Strings.isNullOrEmpty(keyword)){
                sb.append(word);
                sb.append(" ");
            }

        }

    }

    public static void main (String...args){

        Path clinicalPath = Paths.get("/tmp/cvr/patient/clinical");
        DarwinClinicalNoteDetailsService service = new DarwinClinicalNoteDetailsService(clinicalPath);
        service.processClinicalNotesForPatient(1519355);

    }

}
