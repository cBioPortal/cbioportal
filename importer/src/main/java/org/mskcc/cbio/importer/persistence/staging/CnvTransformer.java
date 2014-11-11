package org.mskcc.cbio.importer.persistence.staging;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Set;

/**
 * <p>
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
 * Created by fcriscuo on 11/9/14.
 */
public abstract class CnvTransformer {

    protected Table<String, String, Double> cnaTable;
    private final static Logger logger = Logger.getLogger(CnvTransformer.class);
    protected final CnvFileHandler fileHandler;

    protected CnvTransformer(CnvFileHandler aHandler){
        Preconditions.checkArgument(null!=aHandler,"A CnvFileHandler Implementation is required");
        this.fileHandler = aHandler;
        this.cnaTable = HashBasedTable.create();

    }

    protected void registerCnv(String geneName, String sampleId, Double cnv){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(geneName),"A gene name is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(sampleId),"A sample id is required");
        Double cnvValue = (null != cnv )?cnv:0.0d;
        this.cnaTable.put(geneName, sampleId,cnvValue);
    }

    protected void resetDeprecatedSamples(List<String> deprecatedSamples){
        Set<String> geneNameSet = this.cnaTable.rowKeySet();
        for (String sampleId : deprecatedSamples){
            for (String geneName : geneNameSet) {
                this.cnaTable.put(geneName, sampleId, 0.0d);
                logger.info("Removed gene " + geneName +" from cnvtable for sample "
                        +sampleId);
            }

        }
    }
     protected void persistCnvData() {
        this.fileHandler.persistCnvTable(this.cnaTable);
    }
}
