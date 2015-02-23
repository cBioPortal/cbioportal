package org.mskcc.cbio.importer.cvr.crdb.transformer;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.crdb.rdbms.dao.cbioint.CrdbDatasetMapper;
import org.mskcc.cbio.importer.cvr.crdb.rdbms.model.cbioint.CrdbDataset;
import org.mskcc.cbio.importer.cvr.crdb.rdbms.model.cbioint.CrdbDatasetExample;
import org.mskcc.cbio.importer.cvr.crdb.util.CrdbSessionManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
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
 * Created by criscuof on 2/22/15.
 */
public class CrdbDatasetTransformer extends CrdbTransformer {

    private static final Logger logger = Logger.getLogger(CrdbTransformer.class);
    private static final String patientIdColumnName = "DMP_ID";
   private final CrdbDatasetMapper crdbDatasetMapper;
    private final CrdbDatasetExample crdbDatasetExample;
    //TODO: move filename to Google worksheet
    private static final String crdbDataSetClinicalFilename = "data_clinical_crdbdataset.txt";

    public CrdbDatasetTransformer(Path filePath) {
        super(filePath.resolve(crdbDataSetClinicalFilename));
        this.crdbDatasetExample = new CrdbDatasetExample();
        this.crdbDatasetMapper = CrdbSessionManager.INSTANCE.getCrdbSession()
                .getMapper(CrdbDatasetMapper.class);
    }


    @Override
    public void transform() {
        this.crdbDatasetExample.clear();
        this.crdbDatasetExample.createCriteria().andDMP_IDIsNotNull();
        this.writeStagingFile(this.generateCrdbDatasetReport(),CrdbDataset.class);
    }

    @Override
    public List<String> generateReportByPatientId(Integer patientId) {
        return null;
    }

    @Override
    public List<String> generateReportByPatientIdList(List<Integer> patientIdList) {
        return null;
    }


   private List<String> generateCrdbDatasetReport() {
       List<Object> crdbDatsetObjectList = new ArrayList<Object>
               (this.crdbDatasetMapper.selectByExample(this.crdbDatasetExample));
       logger.info("Selected " +crdbDatsetObjectList.size() +" CRDB dataset records");
       return this.generateStagingFileRecords(crdbDatsetObjectList);
   }

    // main class for standalone testing
    public static void main (String...args) {
        Path filePath = Paths.get("/tmp/cvr");
        CrdbDatasetTransformer transformer = new CrdbDatasetTransformer(filePath);
        transformer.transform();
        CrdbSessionManager.INSTANCE.closeSession();
    }

}
