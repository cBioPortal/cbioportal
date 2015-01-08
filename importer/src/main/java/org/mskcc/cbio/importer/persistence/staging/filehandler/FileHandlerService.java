package org.mskcc.cbio.importer.persistence.staging.filehandler;

import com.google.common.base.Preconditions;
import org.mskcc.cbio.importer.persistence.staging.util.StagingUtils;

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
public enum FileHandlerService {
    INSTANCE;

    public TsvFileHandler obtainFileHandlerForNewStagingFile(Path aPath, List<String> columnHeadings) {
        Preconditions.checkArgument(null != aPath,
                "A Path to a staging file is required");
        Preconditions.checkArgument(null != columnHeadings && !columnHeadings.isEmpty(),
                "A List of column headings is required");
       return new TsvFileHandlerImpl(aPath, columnHeadings, true);
    }

    public TsvFileHandler obtainFileHandlerForAppendingToStagingFile(Path aPath, List<String> columnHeadings) {
        Preconditions.checkArgument(null != aPath,
                "A Path to a staging file is required");
        Preconditions.checkArgument(null != columnHeadings && !columnHeadings.isEmpty(),
                "A List of column headings is required");
        return  new TsvFileHandlerImpl(aPath, columnHeadings,false);

    }

}
