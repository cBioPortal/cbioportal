package org.mskcc.cbio.importer.persistence.staging.filehandler;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
 * Created by criscuof on 1/8/15.
 */
/*
A Singleton service implemented as an enum to serve as a factory for instantiating and initializing
a TsvFileHandler implementation
 */

public enum FileHandlerService {
    INSTANCE;
    private final static Logger logger = Logger.getLogger(FileHandlerService.class);


    public TsvFileHandler obtainFileHandlerForNewStagingFile(Path aPath, List<String> columnHeadings) {

        if (validateStagingPath(aPath)) {
            Preconditions.checkArgument(null != columnHeadings && !columnHeadings.isEmpty(),
                    "A List of column headings is required");
            return new TsvFileHandlerImpl(aPath, columnHeadings, true);
        }
        return null;
    }

    public TsvFileHandler obtainFileHandlerByDataType(Path directoryPath, String dataType, List<String> columnHeadings,
                                                      Boolean deleteFile){
        if(validateStagingPath(directoryPath)){
            Preconditions.checkArgument(!Strings.isNullOrEmpty(dataType),
                    "Am importer worksheet data type is required ");
            Preconditions.checkArgument(!columnHeadings.isEmpty(),
                    "A list of column headings is required");
            return new TsvFileHandlerImpl(directoryPath, dataType, columnHeadings, deleteFile);
        }
        return null;
    }



    /*
    public method to provide file handler for a CNV staging file
    the distinction is that the CNV file's column headers are the sample ids and cannot be determined when
    the file is initiated
     */
    public TsvFileHandler obtainFileHandlerForCnvFile(Path aPath, Boolean deleteFlag) {
        // data  type is cnv - file name resolves to data_CNA.txt
        if (validateStagingPath(aPath)) {
            return new TsvFileHandlerImpl(aPath, StagingCommonNames.DATATYPE_CNA,
                    new ArrayList<String>(),deleteFlag);
        }
        return null;
    }


    public TsvFileHandler obtainFileHandlerForAppendingToStagingFile(Path aPath, List<String> columnHeadings) {
        return  new TsvFileHandlerImpl(aPath, columnHeadings,false);
    }

    /*
    create a staging file path if one does not exist already
    */
    private boolean validateStagingPath(final Path aPath) {
       Preconditions.checkArgument
                (null != aPath,
                        "A Path to the staging file directory is required");
        try {
            if(Files.notExists(aPath)) {
                Files.createDirectories(aPath);
                logger.info("Staging file path " +aPath +" created");
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            return false;
        }
        return true;

    }

}
