package org.mskcc.cbio.importer.cvr.darwin.service;

import com.google.common.base.*;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.darwin.dao.dvcbio.ClinicalNoteMapper;
import org.mskcc.cbio.importer.cvr.darwin.dao.dvcbio.PathologyDataMapper;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.ClinicalNote;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.ClinicalNoteExample;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.PathologyData;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.PathologyDataExample;
import org.mskcc.cbio.importer.cvr.darwin.util.DarwinSessionManager;
import org.mskcc.cbio.importer.cvr.darwin.util.IdMapService;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import javax.annotation.Nullable;
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
 * Created by criscuof on 12/3/14.
 */
public class DarwinDocumentParsers {
    /*
    represents a service class that provides static utility methods to facilitate
    transforming data extracted from Darwin database BLOB columns
     */

    private static final String patientIdColumn = "Patient ID: ";
    private static final StanfordCoreNLP pipeline =  Suppliers.memoize(new NLPSupplier()).get();
    private static final Logger logger = Logger.getLogger(DarwinDocumentParsers.class);

    /*
    public static utility method that parses the contents of the clinical note medical document to a supplied
    Gooogle Guava Table object
     */

    public static void parseDarwinClinicalNoteDocument (Integer patientId, String clinDoc, Table<Integer, String,String> clinTable) {
        Preconditions.checkArgument(null != patientId && patientId > 0, "A valid patient id is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(clinDoc), " A clinical note document is required");
        Preconditions.checkArgument(null != clinTable, " A Google Guava Table is required");

        Integer rowNum = clinTable.rowKeySet().size();
        List<String> lines = Lists.newArrayList(StagingCommonNames.lineSplitter.split(clinDoc));

        StringBuilder sb = new StringBuilder(patientIdColumn);
        sb.append(patientId.toString());
        sb.append(".\n");
        sb.append("SAMPLE ID(s): ");
        sb.append(IdMapService.INSTANCE.displayDmpIdsByDarwinId(patientId));
        sb.append(".\n");
        // filter the original text
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
                        for (String skipWord : DarwinParserNames.CN_FILTER_LIST) {
                            if (line.startsWith(skipWord)) {
                                return false;
                            }
                        }
                        return true;
                    }
                })
                        //get rid of multiple blank spaces
                .transform(new Function<String, String>() {
                               @Nullable
                               @Override
                               public String apply(String input) {
                                   return input.replaceAll(" +", " ");
                               }
                           }
                )
                .toList();
        for (String line : filteredLines){
            sb.append(line);
            sb.append("\n");
        }
        Annotation annotation = new Annotation(sb.toString());
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        processSentences(sentences, clinTable.row(rowNum));
    }

    /*
    private method that processes sentences into cells within a table row
    a cell may contain one or more sentences
    if a sentence starts with an uppercase word followed by a colon (e.g. ABCD:), it is
    consider to be start of a new cell. The cell name is the uppercase word
    the exception is sentences belonging to the review of systems group; these are processed as a
    group.
     */
    private static void processSentences(List<CoreMap> sentences, Map<String,String> rowMap){
        String columnName = null;
        boolean rosFlag = false;
        String value = null;
        for (CoreMap sentence : sentences) {
            String s = sentence.toString();
            if(s.startsWith(DarwinParserNames.CN_REVIEW_OF_SYSTEMS)) {
                rosFlag = true;
                rowMap.put(DarwinParserNames.CN_REVIEW_OF_SYSTEMS, "");
                s.replace(DarwinParserNames.CN_REVIEW_OF_SYSTEMS,"");
                columnName = DarwinParserNames.CN_REVIEW_OF_SYSTEMS;
            } else if (s.startsWith("GENERAL:")) {
                rosFlag = false;
            }
                if (rosFlag) {
                    StringBuilder sb = new StringBuilder(rowMap.get(DarwinParserNames.CN_REVIEW_OF_SYSTEMS))
                            .append(" ").append(s);
                    rowMap.put(DarwinParserNames.CN_REVIEW_OF_SYSTEMS,sb.toString());
                } else {
                    List<String> parts = edu.stanford.nlp.util.StringUtils.split(s, "\\:");
                    if(parts.size() > 1){
                        columnName = parts.get(0);
                        rowMap.put(columnName, parts.get(1));
                    } else {
                        if (!Strings.isNullOrEmpty(columnName)){
                            StringBuilder sb = new StringBuilder(rowMap.get(columnName))
                                    .append(" ").append(s);
                            rowMap.put(columnName,sb.toString());
                        }
                    }
                }
        }
    }

    public static  void parsePathologyReport(final Integer patientId, final String pathologyReport,
                                      Table<Integer,String,String> pathTable){
        Integer seq = pathTable.rowKeySet().size();
        List<String> lines = Lists.newArrayList(StagingCommonNames.lineSplitter.split(pathologyReport));
        List<String> filteredLines = FluentIterable.from(lines)
                .filter(new Predicate<String>() {
                    @Override
                    public boolean apply(String line) {
                        for (String skipWord : DarwinParserNames.PATH_FILTER_LIST) {
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
        for(String line : filteredLines) {
            if(line.startsWith(DarwinParserNames.PATH_STOP_SIGNAL)){
                break;
            }
            // a blank line may signal the end of any previous attribute
            if(StringUtils.isBlank(line)){
                if(!Strings.isNullOrEmpty(attributeName) && sb.length()>0){
                    pathTable.put(seq,attributeName, sb.toString().trim());
                    sb.setLength(0);
                    attributeName = null;
                }
            } else {
                if (DarwinParserNames.PATH_ATTRIBUTE_LIST.contains(line.trim())){
                    attributeName = line.trim();
                } else if(!Strings.isNullOrEmpty(attributeName)){
                    sb.append(line);
                    sb.append(" ");
                }
            }
        }
        return;
    }
    /*
    main method for stand alone testing
     */
    public static void main (String...args){
        testClinicalNoteParser();
        testPathologyReportParser();
    }

    static void testPathologyReportParser() {
        PathologyDataMapper pathologyDataMapper = DarwinSessionManager.INSTANCE.getDarwinSession()
                .getMapper(PathologyDataMapper.class);
        PathologyDataExample pathologyDataExample = new PathologyDataExample();
        pathologyDataExample.clear();
        pathologyDataExample.createCriteria().andPATH_PT_DEIDENTIFICATION_IDIn(Lists.newArrayList(1519355));
        Table<Integer, String,String> pathTable = HashBasedTable.create();
        for (PathologyData pd : pathologyDataMapper.selectByExampleWithBLOBs(pathologyDataExample)){
            Integer patientId = pd.getPATH_PT_DEIDENTIFICATION_ID();
            // TODO: change to correct report column when database column name is corrected
            String pathReport = pd.getPATH_RPT_YEAR();
            if(!Strings.isNullOrEmpty(pathReport))
            {
                parsePathologyReport(patientId, pathReport, pathTable);
            }
            // display table contents
            logger.info("+++++++++++++++++++PATHOLOGY DATA ++++++++++++++++++++++++++++++++");
            Set<String> colSet = pathTable.columnKeySet();
            for (Integer row : pathTable.rowKeySet()){
                for (String column : colSet){
                    logger.info("ROW " +row.toString() + " COL: " +column +" VALUE " +pathTable.get(row,column));
                }
            }
        }
    }

    static void testClinicalNoteParser() {
        ClinicalNoteMapper clinicalNoteMapper = DarwinSessionManager.INSTANCE.getDarwinSession()
                .getMapper(ClinicalNoteMapper.class);
        ClinicalNoteExample clinicalNoteExample = new ClinicalNoteExample();
        clinicalNoteExample.clear();
        clinicalNoteExample.createCriteria().andCLNT_PT_DEIDENTIFICATION_IDIn(Lists.newArrayList(1519355));
        Table<Integer, String,String> clinTable = HashBasedTable.create();
        for(ClinicalNote cn : clinicalNoteMapper.selectByExampleWithBLOBs(clinicalNoteExample)){
            String clinicalDoc = cn.getCLNT_DEID_MEDICAL_DOCUMENT_TXT();
            Integer patientId = cn.getCLNT_PT_DEIDENTIFICATION_ID();
            if (!Strings.isNullOrEmpty(clinicalDoc))
                parseDarwinClinicalNoteDocument(patientId, clinicalDoc, clinTable);
        }
        logger.info("+++++++++++++++++++CLINICAL NOTES ++++++++++++++++++++++++++++++++");
        Set<String> colSet = clinTable.columnKeySet();
        for (Integer row : clinTable.rowKeySet()){

            for (String column : colSet){

                logger.info("ROW " +row.toString() + " COL: " +column +" VALUE " +clinTable.get(row,column));
            }

        }
    }

}
