/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.mskcc.cbio.portal.model.DBPatientList;

/**
 *
 * @author abeshoua
 */
public interface PatientListMapper {
    List<DBPatientList> byStableId(@Param("ids") List<String> ids);
    List<DBPatientList> byInternalId(@Param("ids") List<Integer> ids);
    List<DBPatientList> byInternalStudyId(@Param("ids") List<Integer> ids);
    List<DBPatientList> getAll();
}
