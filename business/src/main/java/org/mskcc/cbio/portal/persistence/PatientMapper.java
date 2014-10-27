package org.mskcc.cbio.portal.persistence;


import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.mskcc.cbio.portal.model.DBPatient;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author abeshoua
 */
public interface PatientMapper {
    List<DBPatient> byInternalStudyId(@Param("ids") List<Integer> ids);
    List<DBPatient> byStableStudyId(@Param("ids") List<String> ids);
    List<DBPatient> byInternalPatientId(@Param("ids") List<Integer> ids);
    List<DBPatient> byStablePatientIdInternalStudyId(@Param("study") Integer study, @Param("ids") List<String> ids);
    List<DBPatient> byStablePatientIdStableStudyId(@Param("study") String study, @Param("ids") List<String> ids);
}
