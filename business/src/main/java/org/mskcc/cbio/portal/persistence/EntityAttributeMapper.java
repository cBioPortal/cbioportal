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

public interface EntityAttributeMapper
{
	void insertEntityAttribute(EntityAttribute attribute);
	void insertAttributeMetadata(AttributeMetadata attributeMetadata);

	EntityAttribute getEntityAttributeById(@Param("entityId") int entityId,
	                                       @Param("attributeId") String attributeId);

	List<AttributeMetadata> getAllAttributeMetadata();
	AttributeMetadata getAttributeMetadataById(String attributeId);
}
