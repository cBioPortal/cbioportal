package org.mskcc.cbio.importer.persistence.staging;

import com.google.inject.internal.Preconditions;
import org.apache.log4j.Logger;

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
 * Created by criscuof on 11/6/14.
 */
public class FusionFileHandlerImpl extends TsvStagingFileHandler {
    /*
    responsible for writing out fusion variant data to a tsv file
     */
    private final static Logger logger = Logger.getLogger(FusionFileHandlerImpl.class);

    public FusionFileHandlerImpl(){
        super();
    }


    /*
    public interface method to register a Path to a fusion file for subsequent
    staging file operations
    if the file does not exist, it will be created and a tab-delimited list of column headings
    will be written as the first line
    */

    public void registerFusionStagingFile(Path fusionFilePath, List<String> columnHeadings) {
        Preconditions.checkArgument(null != fusionFilePath, "A Path object referencing the fusion data file is required");
        if (!Files.exists(fusionFilePath, LinkOption.NOFOLLOW_LINKS)) {
            Preconditions.checkArgument(null != columnHeadings && !columnHeadings.isEmpty(),
                    "Column headings are required for the new fusion data file: " +fusionFilePath.toString());

        }
        super.registerStagingFile(fusionFilePath, columnHeadings);
    }


}
