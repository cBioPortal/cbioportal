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

import org.apache.commons.collections.map.MultiKeyMap;

import java.util.List;

@Service
public class EntityService
{
	private EntityMapper entityMapper;
  private static final MultiKeyMap patientMap = new MultiKeyMap();
  private static final MultiKeyMap sampleMap = new MultiKeyMap();

  @Autowired
  public void setEntityMapper(EntityMapper entityMapper)
  {
    this.entityMapper = entityMapper;
  }

  @Transactional
  public Entity insertCancerStudyEntity(String cancerStudyStableId)
  {
    Entity entity = new Entity();
    entity.stableId = cancerStudyStableId;
    entity.type = EntityType.STUDY;
    entityMapper.insertEntity(entity);
    return entity;
  }

  @Transactional
  public Entity insertPatientEntity(String cancerStudyStableId, String patientStableId)
  {
    Entity entity = new Entity();
    entity.stableId = patientStableId;
    entity.type = EntityType.PATIENT;
    entityMapper.insertEntity(entity);
    if (!patientMap.containsKey(cancerStudyStableId, patientStableId)) {
      patientMap.put(cancerStudyStableId, patientStableId, entity);
    }
    return entity;
  }

	@Transactional
	public Entity insertSampleEntity(String cancerStudyStableId,
                                   String patientStableId,
                                   String sampleStableId)
	{
    Entity entity = new Entity();
    entity.stableId = sampleStableId;
    entity.type = EntityType.SAMPLE;
		entityMapper.insertEntity(entity);
    if (!sampleMap.containsKey(cancerStudyStableId, patientStableId, sampleStableId)) {
      sampleMap.put(cancerStudyStableId, patientStableId, sampleStableId, entity);
    }
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
      if (patientMap.containsKey(cancerStudyStableId, patientStableId)) {
        return (Entity)patientMap.get(cancerStudyStableId, patientStableId);
      }
  		Entity cancerStudyEntity = getCancerStudy(cancerStudyStableId);
  		Entity patientEntity = filterByStableId(entityMapper.getChildren(cancerStudyEntity.internalId, EntityType.PATIENT),
                                              patientStableId);
      if (patientEntity != null) {
        patientMap.put(cancerStudyStableId, patientStableId, patientEntity);
      }
      return patientEntity;
  	}

  	public Entity getSample(String cancerStudyStableId, String patientStableId, String sampleStableId)
  	{
      if (sampleMap.containsKey(cancerStudyStableId, patientStableId, sampleStableId)) {
        return (Entity)sampleMap.get(cancerStudyStableId, patientStableId, sampleStableId);
      }
  		Entity patientEntity = getPatient(cancerStudyStableId, patientStableId);
  		Entity sampleEntity = filterByStableId(entityMapper.getChildren(patientEntity.internalId, EntityType.SAMPLE),
                                             sampleStableId);
      if (sampleEntity != null) {
        sampleMap.put(cancerStudyStableId, patientStableId, sampleStableId, sampleEntity);
      }
      return sampleEntity;
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
