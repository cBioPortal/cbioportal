package org.mskcc.cbio.portal.persistence;


import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.mskcc.cbio.portal.model.DBSample;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author abeshoua
 */
public interface SampleMapper {
    List<DBSample> byInternalStudyId(@Param("ids") List<Integer> ids);
    List<DBSample> byStableStudyId(@Param("ids") List<String> ids);
    List<DBSample> byInternalSampleId(@Param("ids") List<Integer> ids);
    List<DBSample> byStableSampleIdInternalStudyId(@Param("study") Integer study, @Param("ids") List<String> ids);
    List<DBSample> byStableSampleIdStableStudyId(@Param("study") String study, @Param("ids") List<String> ids);
}
