/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.service;

import java.util.List;
import org.mskcc.cbio.portal.model.DBSample;
import org.mskcc.cbio.portal.persistence.SampleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author abeshoua
 */
@Service
public class SampleService {
    @Autowired
    private SampleMapper sampleMapper;
    
    public List<DBSample> byInternalStudyId(List<Integer> ids) {
        return sampleMapper.byInternalStudyId(ids);
    }
    public List<DBSample> byStableStudyId(List<String> ids) {
        return sampleMapper.byStableStudyId(ids);
    }
    public List<DBSample> byInternalSampleId(List<Integer> ids) {
        return sampleMapper.byInternalSampleId(ids);
    }
    public List<DBSample> byStableSampleId(Integer study, List<String> ids) {
        return sampleMapper.byStableSampleIdInternalStudyId(study, ids);
    }
    public List<DBSample> byStableSampleId(String study, List<String> ids) {
        return sampleMapper.byStableSampleIdStableStudyId(study, ids);
    }
}
