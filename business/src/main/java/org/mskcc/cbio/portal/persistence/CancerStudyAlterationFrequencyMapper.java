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
	void insert(int cancerStudyId, long entrezGeneId, String freqType, float frequency, int numPatients);
	List<CancerStudyAlterationFrequency> get(@Param("entrezGeneIds") List<Long> entrezGeneId, @Param("internalStudyIds") List<Integer> internalStudyIds);
}
