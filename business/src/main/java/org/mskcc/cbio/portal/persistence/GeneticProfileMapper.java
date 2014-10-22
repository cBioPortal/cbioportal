package org.mskcc.cbio.portal.persistence;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.mskcc.cbio.portal.model.DBGeneticProfile;
/**
 *
 * @author abeshoua
 */
public interface GeneticProfileMapper {
    List<DBGeneticProfile> byStableId(@Param("ids") List<String> ids);
    List<DBGeneticProfile> byInternalId(@Param("ids") List<Integer> ids);
    List<DBGeneticProfile> byInternalStudyId(@Param("ids") List<Integer> ids);
    List<DBGeneticProfile> getAll();
}
