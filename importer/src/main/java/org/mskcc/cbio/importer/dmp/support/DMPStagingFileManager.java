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
package org.mskcc.cbio.importer.dmp.support;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.internal.Preconditions;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.DSYNC;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.dmp.model.Result;
import org.mskcc.cbio.importer.dmp.util.DmpUtils;

/*
 Resonsible for writing DMP data to MAF files
 */
public class DMPStagingFileManager {

    private final static Logger logger = Logger.getLogger(DMPStagingFileManager.class);
    private final Path stagingFilePath;
    private final Map<String, Path> filePathMap = Maps.newHashMap();

    public DMPStagingFileManager(Path aBasePath) {
        Preconditions.checkArgument(null != aBasePath,
                "A Path to the DMP staging file directory is required");
        Preconditions.checkArgument(Files.isDirectory(aBasePath, LinkOption.NOFOLLOW_LINKS),
                "The specified Path: " + aBasePath + " is not a directory");
        Preconditions.checkArgument(Files.isWritable(aBasePath),
                "The specified Path: " + aBasePath + " is not writable");
        this.stagingFilePath = aBasePath;
        this.initFileMap();
    }

    private void initFileMap() {
        this.filePathMap.put(DMPCommonNames.REPORT_TYPE_CNV, this.stagingFilePath.resolve("data_CNA.txt"));
        this.filePathMap.put(DMPCommonNames.REPORT_TYPE_CNV_INTRAGENIC, this.stagingFilePath.resolve("data_CNA_intragenic.txt"));
        this.filePathMap.put(DMPCommonNames.REPORT_TYPE_SNP_EXONIC, this.stagingFilePath.resolve("data_mutations_extended.txt"));
        this.filePathMap.put(DMPCommonNames.REPORT_TYPE_SNP_SILENT, this.stagingFilePath.resolve("data_mutations_silent.txt"));
        //create the staging files and write out column headings
        for (Map.Entry<String, Path> entry : this.filePathMap.entrySet()) {
            try {
                Path path = entry.getValue();
                Files.deleteIfExists(path);
                Files.createFile(path); // accept default file attributes
                this.writeColumnHeaders(entry.getKey(), path);
                logger.info("Staging file " + path.toString() + " created");
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
    }

    private void writeColumnHeaders(String reportType, Path path) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(path, Charset.defaultCharset());
        writer.append(DmpUtils.getColumnNamesByReportType(reportType));
        writer.newLine();
        writer.flush();
    }

    /*
    public method to transform a List of DMP sequence data to a List of Strings and
    output that List to the appropriate staging file based on the report type
    */
    public void appendDMPDataToStagingFile(String reportType, List aList, Function transformationFunction) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(reportType), "A DMP report type is required");
        Preconditions.checkArgument(this.filePathMap.containsKey(reportType),
                "Report type: " + reportType + " is not supported");
        Preconditions.checkArgument(null != aList && !aList.isEmpty(),
                "A valid List of SMP data is required");
        Preconditions.checkArgument(null != transformationFunction,
                "A transformation function is required");
        Path outPath = this.filePathMap.get(reportType);
        OpenOption[] options = new OpenOption[]{CREATE, APPEND, DSYNC};
        // create the file if it doesn't exist, append to it if it does
        try {
            Files.write(outPath, Lists.transform(aList, transformationFunction), Charset.defaultCharset(), options);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }

    }
    
    
    

}
