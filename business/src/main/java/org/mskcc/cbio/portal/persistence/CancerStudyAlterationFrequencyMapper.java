/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.persistence;
import org.mskcc.cbio.portal.model.CancerStudyAlterationFrequency;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 *
 * @author abeshoua
 */

public interface CancerStudyAlterationFrequencyMapper {
	List<CancerStudyAlterationFrequency> getMut(@Param("entrez_gene_ids") List<Long> entrez_gene_ids, @Param("internal_study_ids") List<Integer> internal_study_ids);
	List<CancerStudyAlterationFrequency> getCna(@Param("entrez_gene_ids") List<Long> entrez_gene_ids, @Param("internal_study_ids") List<Integer> internal_study_ids);
	List<CancerStudyAlterationFrequency> getMutCna(@Param("entrez_gene_ids") List<Long> entrez_gene_ids, @Param("internal_study_ids") List<Integer> internal_study_ids);
	List<CancerStudyAlterationFrequency> getMutByPosition(@Param("entrez_gene_id") Long entrez_gene_id, @Param("internal_study_id") Integer internal_study_id, @Param("position") Integer position);
	List<CancerStudyAlterationFrequency> getMutByType(@Param("entrez_gene_id") Long entrez_gene_id, @Param("internal_study_id") Integer internal_study_id, @Param("type") String type);
	List<CancerStudyAlterationFrequency> getMutByPositionAndType(@Param("entrez_gene_id") Long entrez_gene_id, @Param("internal_study_id") Integer internal_study_id, @Param("position") Integer position, @Param("type") String type);
	Integer getNumPatients(@Param("internal_study_id") Integer internal_study_id);
}
