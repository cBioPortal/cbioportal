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
package org.mskcc.cbio.importer.dmp.persistence.file;

import org.mskcc.cbio.importer.dmp.util.DMPCommonNames;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Table;
import com.google.inject.internal.Maps;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.persistence.staging.CaseListFileManager;
;


/*
 Resonsible for writing DMP data to MAF files
 */
public class DMPStagingFileManager {

    private final static Logger logger = Logger.getLogger(DMPStagingFileManager.class);
    
    
    private final CaseListFileManager caseListManager;

    public DMPStagingFileManager(Path aBasePath) {
        Preconditions.checkArgument(null != aBasePath,
                "A Path to the DMP staging file directory is required");
        Preconditions.checkArgument(Files.isDirectory(aBasePath, LinkOption.NOFOLLOW_LINKS),
                "The specified Path: " + aBasePath + " is not a directory");
        Preconditions.checkArgument(Files.isWritable(aBasePath),
                "The specified Path: " + aBasePath + " is not writable");
      
        this.caseListManager = new CaseListFileManager(aBasePath);
    }

    List<String> readDMPCaseListData() {
        return this.caseListManager.readCaseListData();
    }

    /*
     output of tumor type data
     tumor type data is handled in a read - delete - write mode
     */
    void persistDMPCaseListData(List<String> lines) {
        this.caseListManager.writeCaseListData(lines);
    }   
}
