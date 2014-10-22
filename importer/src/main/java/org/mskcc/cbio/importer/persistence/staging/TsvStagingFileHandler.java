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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.internal.Preconditions;
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
import org.apache.log4j.Logger;


public abstract class  TsvStagingFileHandler  {
     protected  Path stagingFilePath;
    
    private final static Logger logger = Logger.getLogger(TsvStagingFileHandler.class);
     
    protected void registeStagingFile(Path stagingFilePath, List<String> columnHeadings) {
         
       if (!Files.exists(stagingFilePath, LinkOption.NOFOLLOW_LINKS)) {
           Preconditions.checkArgument(null != columnHeadings && !columnHeadings.isEmpty(),
                   "Column headings are required for the new staging file: " 
                           +stagingFilePath.toString());
            try {
                OpenOption[] options = new OpenOption[]{CREATE, APPEND, DSYNC};
                String line = StagingCommonNames.tabJoiner.join(columnHeadings) +"\n";
                Files.write(stagingFilePath,
                        line.getBytes(),
                        options);
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }       
       }
      this.stagingFilePath = stagingFilePath;
    }

    
    protected void transformImportDataToStagingFile(List aList, 
            Function transformationFunction) {
        
        OpenOption[] options = new OpenOption[]{ APPEND, DSYNC};      
        try {
            Files.write(this.stagingFilePath, Lists.transform(aList, 
                    transformationFunction), 
                    Charset.defaultCharset(), options);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

}
