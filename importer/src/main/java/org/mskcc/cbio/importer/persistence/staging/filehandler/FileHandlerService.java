package org.mskcc.cbio.importer.persistence.staging.filehandler;

import com.google.common.base.Preconditions;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
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

    public TsvFileHandler obtainFileHandlerForNewStagingFile(Path aPath, List<String> columnHeadings) {

        if (isValidStagingDirectoryPath(aPath)) {
            Preconditions.checkArgument(null != columnHeadings && !columnHeadings.isEmpty(),
                    "A List of column headings is required");
            return new TsvFileHandlerImpl(aPath, columnHeadings, true);
        }
        return null;
    }

    public TsvFileHandler obtainFileHandlerForAppendingToStagingFile(Path aPath, List<String> columnHeadings) {
        return  new TsvFileHandlerImpl(aPath, columnHeadings,false);
    }

    /*
   common set of criteria for a Path to a directory to be used for
   writing staging files
    */
    private boolean isValidStagingDirectoryPath(final Path aPath) {
        com.google.common.base.Preconditions.checkArgument
                (null != aPath,
                        "A Path to the staging file directory is required");
        Path subPath;
        if (Files.isDirectory(aPath, LinkOption.NOFOLLOW_LINKS)) {
            subPath = aPath;
        } else {
            subPath = aPath.getParent();
        }

          com.google.common.base.Preconditions.checkArgument
                (Files.isWritable(subPath),
                        "The specified Path: " + aPath + " is not writable");
        return true;

    }

}
