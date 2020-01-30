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
    List<DBProfileDataCaseList> getProfileCaseLists(
        @Param("genetic_profile_ids") List<String> genetic_profile_ids
    );
    List<DBGeneticAltRow> getGeneticAlterationRow(
        @Param("genetic_profile_ids") List<String> genetic_profile_ids,
        @Param("genes") List<String> genes
    );
    List<DBMutationData> getMutationData(
        @Param("mutation_profile_ids") List<String> mutation_profile_ids,
        @Param("genes") List<String> genes
    );
    List<DBMutationData> getMutationDataBySample(
        @Param("mutation_profile_ids") List<String> mutation_profile_ids,
        @Param("genes") List<String> genes,
        @Param("sample_ids") List<String> sample_ids
    );
    List<DBMutationData> getMutationDataBySampleList(
        @Param("mutation_profile_ids") List<String> mutation_profile_ids,
        @Param("genes") List<String> genes,
        @Param("sample_list_id") String sample_list_id
    );
}
