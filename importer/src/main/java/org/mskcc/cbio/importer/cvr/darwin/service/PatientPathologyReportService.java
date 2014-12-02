package org.mskcc.cbio.importer.cvr.darwin.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.darwin.dao.dvcbio.PathologyDataMapper;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.PathologyData;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.PathologyDataExample;
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
 * Created by criscuof on 12/1/14.
 */
public class PatientPathologyReportService {
    /*
    responsible for transforming attributes the Darwin Pathology Report column
    to a tsv report file
     */

    private static final Logger logger = Logger.getLogger(PatientPathologyReportService.class);
    private final PathologyDataMapper pathologyDataMapper;
    private final PathologyDataExample pathologyDataExample;
    private final Path pathologyReportPath;
    private Integer seq  = 0;
    private static final String patientIdColumn = "Patient ID";
    private static final String pathologyReportFile = "data_cinical_pathology_report.txt";

    public PatientPathologyReportService (Path stagingFilePath){
        Preconditions.checkArgument(null != stagingFilePath,
                "A Path for the pathology report is required");
        this.pathologyReportPath = stagingFilePath.resolve(pathologyReportFile);
        this.pathologyDataExample = new PathologyDataExample();
        this.pathologyDataMapper = DarwinSessionManager.INSTANCE.getDarwinSession()
                .getMapper(PathologyDataMapper.class);
    }

    private void processPathologyReportForPatient(Integer patientId){
        this.pathologyDataExample.clear();
        this.pathologyDataExample.createCriteria().andPATH_PT_DEIDENTIFICATION_IDEqualTo(patientId);
        Table<Integer, String,String> pathTable = HashBasedTable.create();
        for (PathologyData pd : this.pathologyDataMapper.selectByExampleWithBLOBs(this.pathologyDataExample)){
            String pathologyReport = pd.getPATH_RPT_YEAR();
            if (!Strings.isNullOrEmpty(pathologyReport)){
                this.parsePathologyReport(patientId, pathologyReport, pathTable);
            }
        }
        this.exportPathologyReport(pathTable);

    }

    private void exportPathologyReport(Table<Integer,String,String> pathTable ){
        OpenOption[] options = new OpenOption[]{ CREATE, DSYNC};
        List<String> reportList = Lists.newArrayList();
        reportList.add( StagingCommonNames.tabJoiner.join(pathTable.columnKeySet()));
        Set<String> colSet = pathTable.columnKeySet();
        for (Integer row : pathTable.rowKeySet()){
            List<String> valueList = Lists.newArrayList();
            for (String column : colSet){
                valueList.add(pathTable.get(row,column));

            }
            reportList.add( StagingCommonNames.tabJoiner.join(valueList));

        }
        try {
            Files.deleteIfExists(this.pathologyReportPath);
            Files.write(this.pathologyReportPath, reportList, Charset.defaultCharset(), options);
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

    }

    private void parsePathologyReport(Integer patientId, String pathologyReport,
                                      Table<Integer,String,String> pathTable){
        List<String> lines = Lists.newArrayList(StagingCommonNames.lineSplitter.split(pathologyReport));
        List<String> filteredLines = FluentIterable.from(lines)
                .filter(new Predicate<String>() {
                    @Override
                    public boolean apply(String line) {
                        for (String skipWord : ClinicalNoteNames.PATH_FILTER_LIST) {
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
            if(line.startsWith(ClinicalNoteNames.PATH_STOP_SIGNAL)){
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
                if (ClinicalNoteNames.PATH_ATTRIBUTE_LIST.contains(line.trim())){
                    attributeName = line.trim();
                } else if(!Strings.isNullOrEmpty(attributeName)){
                    sb.append(line);
                    sb.append(" ");
                }
            }
        }
        return;
    }

    public static void main (String...args){
        Path pathologyPath = Paths.get("/tmp/cvr/patient/pathology");
        PatientPathologyReportService service = new PatientPathologyReportService(pathologyPath);
        service.processPathologyReportForPatient(1519355);

    }
}
