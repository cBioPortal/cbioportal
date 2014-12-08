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
import scala.Tuple2;

import javax.annotation.Nullable;
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
    public static utility method that parses the contents of the clinical not medical document to a supplied
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
        String columnName = null;
        String value = null;
        for (CoreMap sentence : sentences){
            String s = sentence.toString();
            if(s.startsWith(DarwinParserNames.CN_REVIEW_OF_SYSTEMS)) {
                processReviewOfSystemsAttribute(rowNum, sentence.toString(), clinTable);
            } else {
                List<String> parts = edu.stanford.nlp.util.StringUtils.split(s, "\\:");
                if (parts.size() > 1) {
                    if (!Strings.isNullOrEmpty(columnName) && !Strings.isNullOrEmpty(value)) {
                        clinTable.put(rowNum, columnName, value);
                    }
                    columnName = parts.get(0);
                    value = parts.get(1);

                } else {
                    value = value + " " + s;
                }
            }
        }

    }

    public static void parseDarwinClinicalNoteDocumentOld (Integer patientId, String clinDoc, Table<Integer, String,String> clinTable){
        Preconditions.checkArgument(null!= patientId && patientId >0, "A valid patient id is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(clinDoc)," A clinical note document is required");
        Preconditions.checkArgument(null!=clinTable," A Google Guava Table is required");

        Integer seq = clinTable.rowKeySet().size();
        List<String> lines = Lists.newArrayList(StagingCommonNames.lineSplitter.split(clinDoc));

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
                                   return input.replaceAll(" +"," ");
                               }
                           }
                )
                .toList();

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
            for (String attribute : DarwinParserNames.CN_ATTIBUTE_LIST){
                if (line.startsWith(attribute)) {

                    if(null != attributeName && sb.length() > 0) {
                        if (attributeName.equals(DarwinParserNames.CN_REVIEW_OF_SYSTEMS)){

                            processReviewOfSystemsAttribute(seq, sb.toString(), clinTable);

                        } else {
                            clinTable.put(seq, attributeName, sb.toString());

                        }
                        sb.setLength(0);
                    }
                    attributeName = attribute.replace(":","");
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
    private method to process the attributes within the Review of Systems text block
    updates supplied Table
     */

    private static void processReviewOfSystemsAttribute(final Integer rowNum, final String text, Table<Integer,
            String,String> clinTable){
        // remove the REVIEW OF SYSTEMS: header
        String rosText = text.replace(DarwinParserNames.CN_REVIEW_OF_SYSTEMS,"").trim();
        Annotation annotation = new Annotation(rosText);
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        String columnName = null;
        String value = null;
        for (CoreMap sentence: sentences){
            //String s1 = CharMatcher.JAVA_ISO_CONTROL.removeFrom(sentence.toString());
            String  s1 = sentence.toString().replace("\n","");
            String filtered = s1.replaceAll(" +"," ");
            List<String>parts = edu.stanford.nlp.util.StringUtils.split(filtered,"\\:");
            if(parts.size()> 1){
                if (!Strings.isNullOrEmpty(columnName) && !Strings.isNullOrEmpty(value) ) {
                    clinTable.put(rowNum, "ROS_" +columnName, value);
                }
                columnName = parts.get(0);
                value = parts.get(1);

            }else {
                value = value + " " + filtered;
            }
        }
        return;
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
