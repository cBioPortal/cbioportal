package org.mskcc.cbio.portal.persistence;


import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.mskcc.cbio.portal.model.DBClinicalData;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author abeshoua
 */
public interface ClinicalDataMapper {
    List<DBClinicalData> byInternalCaseId(@Param("ids") List<Integer> ids);   
    List<DBClinicalData> byInternalStudyId(@Param("ids") List<Integer> ids);
}
