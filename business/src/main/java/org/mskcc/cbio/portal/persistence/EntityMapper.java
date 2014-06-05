/** Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/
package org.mskcc.cbio.portal.persistence;

import org.mskcc.cbio.portal.model.*;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EntityMapper
{
	void insertEntity(Entity entity);
	void insertEntityLink(EntityLink entityLink);

	Entity getByInternalId(int internalId);
	List<Entity> getByStableId(String stableId);
	List<Entity> getByType(EntityType type);

	List<Entity> getParents(@Param("childId") int childId, @Param("type") EntityType type);
	List<Entity> getChildren(@Param("parentId") int parentId, @Param("type") EntityType type);
}
