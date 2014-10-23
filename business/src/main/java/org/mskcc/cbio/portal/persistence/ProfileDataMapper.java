/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.mskcc.cbio.portal.model.DBGeneticAltRow;
import org.mskcc.cbio.portal.model.DBMutationData;
import org.mskcc.cbio.portal.model.DBProfileDataCaseList;

/**
 *
 * @author abeshoua
 */
public interface ProfileDataMapper {
    List<DBProfileDataCaseList> profileCaseList(@Param("ids") List<Integer> ids);
    List<DBGeneticAltRow> altRow(@Param("ids") List<Integer> ids,
                                               @Param("genes") List<Integer> genes);
    List<DBMutationData> mutByInternalId(@Param("ids") List<Integer> ids,
                                               @Param("genes") List<Integer> genes);
    List<DBMutationData> mutByInternalIdInternalCaseId(@Param("ids") List<Integer> ids,
                                               @Param("genes") List<Integer> genes,
                                               @Param("case_ids") List<Integer> case_ids);
}
