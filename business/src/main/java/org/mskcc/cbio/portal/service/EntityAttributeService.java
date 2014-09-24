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
import org.mskcc.cbio.portal.persistence.EntityAttributeMapper;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.collections.map.MultiKeyMap;

import java.util.*;

@Service
public class EntityAttributeService
{
	private EntityAttributeMapper entityAttributeMapper;
  private static final MultiKeyMap entityAttributeMap = new MultiKeyMap();
  private static final Map<String,AttributeMetadata> attributeMetadataMap = new HashMap<String, AttributeMetadata>();

  @Autowired
  public void setEntityAttributeMapper(EntityAttributeMapper entityAttributeMapper)
  {
    this.entityAttributeMapper = entityAttributeMapper;
  }

	@Transactional
	public EntityAttribute insertEntityAttribute(int entityId, String attributeId, String attributeValue)
	{
    EntityAttribute entityAttribute = new EntityAttribute();
    entityAttribute.entityId = entityId;
    entityAttribute.attributeId = attributeId;
    entityAttribute.attributeValue = attributeValue;
    entityAttributeMapper.insertEntityAttribute(entityAttribute);
    if (!entityAttributeMap.containsKey(entityId, attributeId)) {
      entityAttributeMap.put(entityId, attributeId, entityAttribute);
    }
    return entityAttribute;
	}

  @Transactional
  public void updateEntityAttribute(EntityAttribute entityAttribute)
  {
    entityAttributeMapper.updateEntityAttribute(entityAttribute);
    if (entityAttributeMap.containsKey(entityAttribute.entityId, entityAttribute.attributeId)) {
      entityAttributeMap.remove(entityAttribute.entityId, entityAttribute.attributeId);
    }
    entityAttributeMap.put(entityAttribute.entityId,
                           entityAttribute.attributeId,
                           entityAttribute.attributeValue);
  } 

	@Transactional
  public AttributeMetadata insertAttributeMetadata(String attributeId, String displayName,
                                                   String description, AttributeDatatype datatype,
                                                   String type)
  {
    AttributeMetadata attributeMetadata = new AttributeMetadata();
    attributeMetadata.datatype = datatype;
    attributeMetadata.attributeId = attributeId;
    attributeMetadata.displayName = displayName;
    attributeMetadata.description = description;
    attributeMetadata.type = type;
    entityAttributeMapper.insertAttributeMetadata(attributeMetadata);
    if (!attributeMetadataMap.containsKey(attributeId)) {
      attributeMetadataMap.put(attributeId, attributeMetadata);
    }
    return attributeMetadata;
  }

  public EntityAttribute getAttribute(int entityId, String attributeId)
  {
    if (entityAttributeMap.containsKey(entityId, attributeId)) {
      return (EntityAttribute)entityAttributeMap.get(entityId, attributeId);
    }
    EntityAttribute entityAttribute =
      entityAttributeMapper.getEntityAttributeById(entityId, attributeId);
    if (entityAttribute != null) {
      entityAttributeMap.put(entityId, attributeId, entityAttribute);
    }
    return entityAttribute;
  }

  public List<AttributeMetadata> getAllAttributeMetadata()
  {
    return entityAttributeMapper.getAllAttributeMetadata();
  }

  public AttributeMetadata getAttributeMetadata(String attributeId)
  {
    if (attributeMetadataMap.containsKey(attributeId)) {
      return (AttributeMetadata)attributeMetadataMap.get(attributeId);
    }
    AttributeMetadata attributeMetadata =
      entityAttributeMapper.getAttributeMetadataById(attributeId);
    if (attributeMetadata != null) {
      attributeMetadataMap.put(attributeId, attributeMetadata);
    }
    return attributeMetadata;
  }
}