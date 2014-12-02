package org.mskcc.cbio.importer.cvr.darwin.transformer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.darwin.dao.dvcbio.PathologyDataMapper;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.PathologyData;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.PathologyDataExample;
import org.mskcc.cbio.importer.cvr.darwin.util.DarwinSessionManager;
import org.mskcc.cbio.importer.cvr.darwin.util.IdMapService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
 * Created by criscuof on 11/21/14.
 */
public class DarwinPathologyDataTransformer extends DarwinTransformer {

    private static final Logger logger = Logger.getLogger(DarwinPathologyDataTransformer.class);
    private final PathologyDataMapper pathMapper;
    private final PathologyDataExample pathEx;
    private static final String pathDataFile = "data_clinical_pathology.txt";

    public DarwinPathologyDataTransformer(Path aPath){
        super(aPath.resolve(pathDataFile));
        this.pathMapper = DarwinSessionManager.INSTANCE.getDarwinSession()
                .getMapper(PathologyDataMapper.class);
        this.pathEx = new PathologyDataExample();
    }

    private List<String> generatePathReport() {
        List<String> pathDataList = Lists.newArrayList(this.generateColumnHeaders(PathologyData.class));
        List<Object> pathObjectList = new ArrayList<Object>(this.pathMapper.selectByExampleWithBLOBs(this.pathEx));
        pathDataList.addAll(this.generateStagingFileRecords(pathObjectList));
        return pathDataList;
    }

    @Override
    public void transform() {
        this.pathEx.clear();
        this.pathEx.createCriteria().andPATH_PT_DEIDENTIFICATION_IDIn(IdMapService.INSTANCE.getDarwinIdList());
        this.writeStagingFile(this.generatePathReport());
        return;
    }

    @Override
    public List<String> generateReportByPatientId(Integer patientId) {
        Preconditions.checkArgument(null != patientId && patientId > 0,
                "A valid patient id is required");
        this.pathEx.clear();
        this.pathEx.createCriteria().andPATH_PT_DEIDENTIFICATION_IDEqualTo(patientId);
        return this.generatePathReport();
    }

    @Override
    public List<String> generateReportByPatientIdList(List<Integer> patientIdList) {
        Preconditions.checkArgument(null!= patientIdList,"A List of patient ids is required");
        this.pathEx.clear();
        this.pathEx.createCriteria().andPATH_PT_DEIDENTIFICATION_IDIn(patientIdList);
        return this.generatePathReport();
    }

    // main class for testing
    public static void main (String...args){
        Path pathPath = Paths.get("/tmp/cvr");
        DarwinPathologyDataTransformer transformer = new DarwinPathologyDataTransformer(pathPath);
        transformer.transform();
        // terminate the SQL session
        DarwinSessionManager.INSTANCE.closeSession();
    }

}
