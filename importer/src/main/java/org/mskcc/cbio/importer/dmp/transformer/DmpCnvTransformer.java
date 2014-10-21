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
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.inject.internal.Iterables;
import java.util.List;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.dmp.model.CnvIntragenicVariant;
import org.mskcc.cbio.importer.dmp.model.CnvVariant;
import org.mskcc.cbio.importer.dmp.model.DmpData;
import org.mskcc.cbio.importer.dmp.model.Result;
import org.mskcc.cbio.importer.dmp.util.DMPCommonNames;
import org.mskcc.cbio.importer.dmp.persistence.file.DMPStagingFileManager;
import scala.Tuple3;

/*
 responsible for maintaining the CNV table for DMP data:
 invoked when new DMP CNV data are available
 creates a in-memory table of CNV values by gene (rows) and samples (columns)
 reads in any persisted CNV data into table
 updates table with new CNV data
 persists updated table to designated file
 */
public class DmpCnvTransformer implements DMPDataTransformable {

    private final DMPStagingFileManager fileManager;
    private final static Logger logger = Logger.getLogger(DmpCnvTransformer.class);
    private Table<String, String, Double> cnvTable;

    public DmpCnvTransformer(DMPStagingFileManager aManager) {
        Preconditions.checkArgument(null != aManager, "A DMPStagingFileManager object is required");
        this.fileManager = aManager;
        // initialize the in-memory table
        this.cnvTable = fileManager.initializeCnvTable();
    }

    /*
    Function to transform attributes from the Result object to a list of tuples conating the cnv gene name, the result sample id,
    and the cnv fold change
    */
    Function<Result, List<Tuple3<String, String, Double>>> cnvFunction = new Function<Result, List<Tuple3<String, String, Double>>>() {
        @Override
        public List<Tuple3<String, String, Double>> apply(final Result result) {
            List<Tuple3<String, String, Double>> list1 = Lists.newArrayList();
            list1.addAll(FluentIterable.from(result.getCnvVariants())
                    .transform(new Function<CnvVariant, Tuple3<String, String, Double>>() {
                        @Override
                        public Tuple3<String, String, Double> apply(CnvVariant cnv) {
                            return new Tuple3(cnv.getGeneId(), result.getMetaData().getDmpSampleId().toString(), cnv.getGeneFoldChange());
                        }
                    }).toList());
            list1.addAll(FluentIterable.from(result.getCnvIntragenicVariants())
                    .transform(new Function<CnvIntragenicVariant, Tuple3<String, String, Double>>() {
                        @Override
                        public Tuple3<String, String, Double> apply(CnvIntragenicVariant intra) {
                            return new Tuple3(intra.getGeneId(), result.getMetaData().getDmpSampleId().toString(), intra.getGeneFoldChange());
                        }
                    }).toList());

            return list1;
        }
    };

    @Override
    public void transform(DmpData data) {

        Iterable<Tuple3<String, String, Double>> cnvList = Iterables.concat(Lists.transform(data.getResults(), cnvFunction));
        for (Tuple3<String, String, Double> cnv : cnvList) {
            this.cnvTable.put(cnv._1(), cnv._2(), cnv._3());
        }
        // write out updated table
        this.fileManager.persistCnvTable(cnvTable);

    }
    
}
