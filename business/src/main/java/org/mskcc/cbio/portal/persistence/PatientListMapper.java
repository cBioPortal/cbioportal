/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.mskcc.cbio.portal.model.DBPatient;
import org.mskcc.cbio.portal.model.DBPatientList;

/**
 *
 * @author abeshoua
 */
public interface PatientListMapper {
	List<DBPatientList> getIncompletePatientLists(@Param("patient_list_ids") List<String> patient_list_ids);
	List<DBPatientList> getIncompletePatientListsByStudy(@Param("study_id") String study_id);
	List<DBPatientList> getAllIncompletePatientLists();
	List<DBPatient> getList(@Param("list_id") String list_id);
}
