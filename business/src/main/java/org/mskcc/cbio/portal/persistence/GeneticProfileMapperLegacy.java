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
public interface GeneticProfileMapperLegacy {
    List<DBGeneticProfile> getGeneticProfiles(
        @Param("genetic_profile_ids") List<String> genetic_profile_ids
    );
    List<DBGeneticProfile> getGeneticProfilesByStudy(
        @Param("study_id") String study_id
    );
    List<DBGeneticProfile> getAllGeneticProfiles();
}
