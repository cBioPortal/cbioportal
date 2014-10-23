package org.mskcc.cbio.portal.persistence;


import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.mskcc.cbio.portal.model.DBClinicalField;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author abeshoua
 */
public interface ClinicalFieldMapper {
    List<DBClinicalField> byInternalCaseId(@Param("ids") List<Integer> ids);   
    List<DBClinicalField> byInternalStudyId(@Param("ids") List<Integer> ids);
    List<DBClinicalField> getAll();
}
