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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;
import java.util.List;
import org.apache.log4j.Logger;



public class CaseListFileManager {

    private final Path csPath; // path for tumor type data
    private final static Logger logger = Logger.getLogger(CaseListFileManager.class);
    public CaseListFileManager(Path aBasePath) {
        Preconditions.checkArgument(null != aBasePath,
                "A Path to the staging file directory is required");
        Preconditions.checkArgument(Files.isDirectory(aBasePath, LinkOption.NOFOLLOW_LINKS),
                "The specified Path: " + aBasePath + " is not a directory");
        Preconditions.checkArgument(Files.isWritable(aBasePath),
                "The specified Path: " + aBasePath + " is not writable");
        this.csPath = aBasePath.resolve(StagingCommonNames.CASE_STUDY_FILENAME);

    }
    
     /*
     package level method to read in the current contents of the DMP tumor
     type data.
     returns an empty list if tumor type data does not exist
     */
   public List<String> readCaseListData() {
        try {
            return Files.readAllLines(this.csPath, Charset.defaultCharset());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        return Lists.newArrayList();
    }

    /*
     output of tumor type data
     tumor type data is handled in a read - delete - write mode
     */
   public void writeCaseListData(List<String> lines) {
        try {
            // delete the existing tumor type file if one exists
            Files.deleteIfExists(this.csPath);
            OpenOption[] options = new OpenOption[]{CREATE_NEW, WRITE};
            Files.write(this.csPath, lines, Charset.defaultCharset(), options);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }
}
