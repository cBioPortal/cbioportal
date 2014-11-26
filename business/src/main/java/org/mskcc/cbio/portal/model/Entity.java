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
package org.mskcc.cbio.portal.model;

import java.io.Serializable;

/**
 * Represents an Entity (Study, Patient, Sample).
 *
 * @author Benjamin Gross
 */
public class Entity implements Serializable
{
    public EntityType type;
    public int internalId;
    public String stableId;

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Entity)) {
            return false;
        }
        
        Entity anotherEntity = (Entity)obj;
        return (this.internalId == anotherEntity.internalId);
    } 
}
