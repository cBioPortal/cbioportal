package org.mskcc.cbio.importer.persistence.staging.structvariant;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.persistence.staging.filehandler.FileHandlerService;
import org.mskcc.cbio.importer.persistence.staging.filehandler.TsvFileHandler;

import java.nio.file.Path;

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
 * Created by criscuof on 1/29/15.
 */
public class StructVariantTransformer {

    protected static final String TRANSFORMER_DATA_TYPE = "structural-variation";
    protected static final DatatypeMetadata dtMeta;
    private final static Logger logger = Logger.getLogger(StructVariantTransformer.class);
    protected TsvFileHandler tsvFileHandler;
    protected CancerStudyMetadata csMeta;


    static {
        Optional<DatatypeMetadata> dtMetaOpt = DatatypeMetadata.findDatatypeMetadatByDataType(TRANSFORMER_DATA_TYPE);
        if (dtMetaOpt.isPresent()){
            dtMeta = dtMetaOpt.get();
        } else {
            logger.error("Unable to resolve DatatypeMetaData object for " +TRANSFORMER_DATA_TYPE);
            dtMeta = null;
        }
    }

    public StructVariantTransformer(Path aPath, Boolean deleteFlag, CancerStudyMetadata csMeta){
        Preconditions.checkArgument(null != aPath, "A Path to a staging file is required");
        Preconditions.checkArgument(null != csMeta, "A CancerStudyMetadata object is required");
        this.csMeta = csMeta;
        if (deleteFlag) {
            this.tsvFileHandler = FileHandlerService.INSTANCE.obtainFileHandlerForNewStagingFile(aPath,
                    StructVariantModel.resolveColumnNames());
        } else {
            this.tsvFileHandler = FileHandlerService.INSTANCE
                    .obtainFileHandlerForAppendingToStagingFile(aPath, StructVariantModel.resolveColumnNames());
        }
    }


}
