/*
 *  Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
  * 
  *  This library is distributed in the hope that it will be useful, but
  *  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  *  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  *  documentation provided hereunder is on an "as is" basis, and
  *  Memorial Sloan-Kettering Cancer Center 
  *  has no obligations to provide maintenance, support,
  *  updates, enhancements or modifications.  In no event shall
  *  Memorial Sloan-Kettering Cancer Center
  *  be liable to any party for direct, indirect, special,
  *  incidental or consequential damages, including lost profits, arising
  *  out of the use of this software and its documentation, even if
  *  Memorial Sloan-Kettering Cancer Center 
  *  has been advised of the possibility of such damage.
 */

package org.mskcc.cbio.importer.persistence.staging;

import com.google.common.collect.Lists;
import com.google.inject.internal.Preconditions;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.model.CaseListMetadata;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.DSYNC;

/*
responsible for CRUD operations against a cbio staging file specified
via a CaseStudyMetadata object
*/
public class CaseListFileHandler {

    Path caseFilePath;
    String study;
    private final static Logger logger = Logger.getLogger(CaseListFileHandler.class);
    OpenOption[] options = new OpenOption[]{CREATE, APPEND, DSYNC};
    
    public CaseListFileHandler(String cancerStudy, Path stagingFileDirectory) {
        this.caseFilePath = stagingFileDirectory.resolve(stagingFileDirectory.toString()+"/case_lists");
        this.study = cancerStudy;

    }
    
    public void generateCaseListFiles(Set<String> caseSet){
        String caseListIds = "case_list_ids:" + StagingCommonNames.tabJoiner.join(caseSet);
        Integer numCases = caseSet.size();
        this.generateCaseListAll(caseListIds,numCases);
        this.generateCaseListCna(caseListIds,numCases);
        this.generateCaseListCnaSeq(caseListIds,numCases);
        this.generateCaseListSequenced(caseListIds,numCases);
        
    }
    private void generateCaseListAll(String caseListIds,Integer nCases){
        Path filePath = this.caseFilePath.resolve("cases_all.txt");
        logger.info("Writing cases_all.txt to " +filePath.toString());
        List<String> lines = Lists.newArrayList();
        lines.add("cancer_study_identifier:" +this.study);
        lines.add("stable_id: " +this.study +"_all");
        lines.add("case_list_category: all_cases_in_study");
        lines.add("case_list_name: All Tumors");
        lines.add("case_list_description: All tumor samples (" + nCases +" samples)");
        lines.add(caseListIds);
        this.outputCaseData(filePath, lines);

    }

    private void generateCaseListCna(String caseListIds,Integer nCases){
        Path filePath = this.caseFilePath.resolve("cases_cna.txt");
        logger.info("Writing cases_cna.txt to " +filePath.toString());
        List<String> lines = Lists.newArrayList();
        lines.add("cancer_study_identifier:" +this.study);
        lines.add("stable_id: " +this.study +"_cna");
        lines.add("case_list_category: all_cases_with_cna_data");
        lines.add("case_list_name: Tumors CNA");
        lines.add("case_list_description: All tumors with copy-number data (" + nCases +" samples)");
        lines.add(caseListIds);
        this.outputCaseData(filePath, lines);

    }

    private void generateCaseListCnaSeq(String caseListIds, Integer nCases){
        Path filePath = this.caseFilePath.resolve("cases_cnaseq.txt");
        logger.info("Writing cases_cna.txt to " +filePath.toString());
        List<String> lines = Lists.newArrayList();
        lines.add("cancer_study_identifier:" +this.study);
        lines.add("stable_id: " +this.study +"_cna_seq");
        lines.add("case_list_category: all_cases_with_mutation_and_cna_data");
        lines.add("case_list_name: Tumors with mutation and copy-number data");
        lines.add("case_list_description: All tumors with CNA and mutation data (" + nCases +" samples)");
        lines.add(caseListIds);
        this.outputCaseData(filePath, lines);

    }

    private void generateCaseListSequenced(String caseListIds, Integer nCases){
        Path filePath = this.caseFilePath.resolve("cases_sequenced.txt");
        logger.info("Writing cases_cna.txt to " +filePath.toString());
        List<String> lines = Lists.newArrayList();
        lines.add("cancer_study_identifier:" +this.study);
        lines.add("stable_id: " +this.study +"_sequenced");
        lines.add("case_list_category: all_cases_with_mutation data");
        lines.add("case_list_name: Sequenced Tumors");
        lines.add("case_list_description: All sequenced samples (" + nCases +" samples)");
        lines.add(caseListIds);
        this.outputCaseData(filePath, lines);

    }
    private void outputCaseData(Path filePath, List<String> caseData){
        try {
            Files.deleteIfExists(filePath);
            Files.write(filePath, caseData, Charset.defaultCharset(),options);
            logger.info("Case data written to " + filePath.toString());

        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
