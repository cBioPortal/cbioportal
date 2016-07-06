/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.mskcc.cbio.portal.model.DBCosmicCount;

/**
 *
 * @author abeshoua
 */
public interface CosmicCountMapper {
	List<DBCosmicCount> getCosmicCountsByKeyword(@Param("keywords") List<String> keywords);
}
