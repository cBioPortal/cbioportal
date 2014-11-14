package org.mskcc.cbio.importer.persistence.staging;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

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
 * Created by criscuof on 11/11/14.
 */
public enum MetadataFileHandler {

    /*
    Singleton class implemented as a enum
    responsible for writing out a importer metadata file
    assumes metadata attributes have a 3 digit numeric prefix to support mapp order
    an existing metadata file with the same absolute Path will be overwritten
     */
    INSTANCE;

    private static final Logger logger = Logger.getLogger(MetadataFileHandler.class);
    private static final OpenOption[] options = new OpenOption[]{CREATE, DSYNC};

    public void generateMetadataFile(Map<String,String> metaMap, Path metadataFilePath){
        Preconditions.checkArgument(null != metaMap,"A Map of metadata attributes and values is required");
        Preconditions.checkArgument(null != metadataFilePath,"A Path to the metadata file is required");
        List<String> lines = Lists.newArrayList();
        for (Map.Entry<String,String> entry : metaMap.entrySet()){
            String key = entry.getKey().substring(3);
            lines.add(StagingCommonNames.blankJoiner.join(key,entry.getValue()));
        }
        try {
            Files.write(metadataFilePath, lines, Charset.defaultCharset(),options);
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

    }
}
