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
package org.mskcc.cbio.portal.service;

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.persistence.EntityMapper;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EntityService
{
	private EntityMapper entityMapper;

  @Autowired
  public void setEntityMapper(EntityMapper entityMapper)
  {
    this.entityMapper = entityMapper;
  }

	@Transactional
	public Entity insertEntity(String stableId, EntityType type)
	{
    Entity entity = new Entity();
    entity.stableId = stableId;
    entity.type = type;
		entityMapper.insertEntity(entity);
		return entity;
	}

	@Transactional
  	public EntityLink insertEntityLink(int parentId, int childId)
  	{
      EntityLink entityLink = new EntityLink();
      entityLink.parentId = parentId;
      entityLink.childId = childId;
  		entityMapper.insertEntityLink(entityLink);
  		return entityLink;
  	}

  	public Entity getCancerStudy(String stableId)
  	{
      List<Entity> cancerStudyEntities =
  	  entityMapper.getByStableId(stableId);
  		assert cancerStudyEntities.size() == 1;
  		return cancerStudyEntities.get(0);
  	}

  	public Entity getPatient(String cancerStudyStableId, String patientStableId)
  	{
  		Entity cancerStudyEntity = getCancerStudy(cancerStudyStableId);
  		return filterByStableId(entityMapper.getChildren(cancerStudyEntity.internalId, EntityType.PATIENT),
                              patientStableId);
  	}

  	public Entity getSample(String cancerStudyStableId, String patientStableId, String sampleStableId)
  	{
  		Entity patientEntity = getPatient(cancerStudyStableId, patientStableId);
  		return filterByStableId(entityMapper.getChildren(patientEntity.internalId, EntityType.SAMPLE),
  		                        sampleStableId);
  	}

  	private Entity filterByStableId(List<Entity> entities, String stableIdFilter)
  	{
  		Entity entityToReturn = null;
  		for (Entity entity : entities) {
  			if (entity.stableId.equals(stableIdFilter)) {
  				entityToReturn = entity;
  				break;
  			}
  		}
  		assert entityToReturn != null;
  		return entityToReturn;
  	}

}