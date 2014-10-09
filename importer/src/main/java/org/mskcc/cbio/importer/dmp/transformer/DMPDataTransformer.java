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

package org.mskcc.cbio.importer.dmp.transformer;

import com.google.common.base.Function;
import com.google.inject.internal.Preconditions;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.dmp.model.DmpData;
import org.mskcc.cbio.importer.dmp.model.Result;
import scala.Tuple2;


public class DMPDataTransformer implements DMPTransformable {
    private final static Logger logger = Logger.getLogger(DMPDataTransformer.class);
    private final DmpData dmpData;
    private final Path dmpBasePath;
    
    public DMPDataTransformer(DmpData data, Path aBasePath) {
        Preconditions.checkArgument(null != data, "A DmpData object is required");
        Preconditions.checkArgument(null != aBasePath, 
                "A Path to the DMP staging file directory is required");
        Preconditions.checkArgument(Files.isDirectory(aBasePath, LinkOption.NOFOLLOW_LINKS),
                "The specified Path: " +aBasePath +" is not a directory");
        this.dmpData = data;
        this.dmpBasePath = aBasePath;
    }
    

    @Override
    public Tuple2<String, Function> getTransformationFunction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
