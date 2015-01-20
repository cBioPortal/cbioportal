package org.mskcc.cbio.importer.cvr.darwin.transformer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.darwin.dao.dvcbio.TumorMapper;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.Tumor;
import org.mskcc.cbio.importer.cvr.darwin.model.dvcbio.TumorExample;
import org.mskcc.cbio.importer.cvr.darwin.util.DarwinSessionManager;
import org.mskcc.cbio.importer.cvr.darwin.util.IdMapService;

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
 * Created by criscuof on 11/19/14.
 */
public class DarwinTumorTransformer extends DarwinTransformer {
    /*
    responsible for transforming tumor data associated with DMP samples
    to tab-delimited text for output
     */
    private static final Logger logger = Logger.getLogger(DarwinTumorTransformer.class);
    private final TumorMapper tumorMapper;
    private final TumorExample tumorEx;
    private static final String tumorFile = "data_clinical_tumor.txt";

    public DarwinTumorTransformer(Path filePath) {
        super(filePath.resolve(tumorFile));
        this.tumorEx = new TumorExample();
        this.tumorMapper = DarwinSessionManager.INSTANCE.getDarwinSession()
                .getMapper(TumorMapper.class);
    }

    private List<String> generateTumorReport(){
        List<String> tumorDataList = Lists.newArrayList(this.generateColumnHeaders(Tumor.class));
        List<Object> tumorObjectList = new ArrayList<Object>(this.tumorMapper.selectByExample(tumorEx));
        tumorDataList.addAll(this.generateStagingFileRecords(tumorObjectList));
        return tumorDataList;
    }

    public void transform() {
        this.tumorEx.clear();
        this.tumorEx.createCriteria().andTUMOR_PT_DEIDENTIFICATION_IDIn(IdMapService.INSTANCE.getDarwinIdList());
        this.writeStagingFile(this.generateTumorReport());
        return;
    }

    @Override
    public List<String> generateReportByPatientId(Integer patientId) {
        Preconditions.checkArgument(null != patientId && patientId > 0,
                "A valid patient id is required");
        this.tumorEx.clear();
        this.tumorEx.createCriteria().andTUMOR_PT_DEIDENTIFICATION_IDEqualTo(patientId);
        return this.generateTumorReport();
    }

    @Override
    public List<String> generateReportByPatientIdList(List<Integer> patientIdList) {
        Preconditions.checkArgument(null != patientIdList,
                "A List of patient ids is required.");
        this.tumorEx.clear();
        this.tumorEx.createCriteria().andTUMOR_PT_DEIDENTIFICATION_IDIn(IdMapService.INSTANCE.getDarwinIdList());
        return this.generateTumorReport();
    }

    // main class for testing
    public static void main (String...args){
        Path tumorPath = Paths.get("/tmp/cvr");
        DarwinTumorTransformer transformer = new DarwinTumorTransformer(tumorPath);
        transformer.transform();
        // test report for individual patient
        for(String line: transformer.generateReportByPatientId(1339055)) {
            System.out.println(line);
        }
        // terminate the SQL session
        DarwinSessionManager.INSTANCE.closeSession();
    }

    }
