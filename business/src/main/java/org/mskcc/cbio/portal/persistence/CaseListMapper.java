/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.mskcc.cbio.portal.model.DBCaseList;

/**
 *
 * @author abeshoua
 */
public interface CaseListMapper {
    List<DBCaseList> byStableId(@Param("ids") List<String> ids);
    List<DBCaseList> byInternalId(@Param("ids") List<Integer> ids);
    List<DBCaseList> byInternalStudyId(@Param("ids") List<Integer> ids);
    List<DBCaseList> getAll();
}
