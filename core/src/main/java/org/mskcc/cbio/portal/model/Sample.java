/** Copyright (c) 2013 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/
package org.mskcc.cbio.portal.model;

import java.util.Map;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 * Encapsulates Sample Data.
 *
 * @author Benjamin Gross
 */
public class Sample {

    public static enum Type
    {
        BLOOD_DERIVED_NORMAL("Blood Derived Normal"),
        NORMAL("Normal"),
        PRIMARY_TUMOR("Primary Tumor");

        private String propertyName;
        
        Type(String propertyName) { this.propertyName = propertyName; }
        public String toString() { return propertyName; }

        static public boolean has(String value) {
            if (value == null) return false;
            try { 
                return valueOf(value.toUpperCase()) != null; 
            }
            catch (IllegalArgumentException x) { 
                return false;
            }
        }
    }

    private int internalId;
    private String stableId;
    private Type sampleType;
    private int internalPatientId;
    private String cancerTypeId;

    public Sample(int internalId, String stableId, String sampleType, int internalPatientId, String cancerTypeId)
    {
        this(stableId, sampleType, internalPatientId, cancerTypeId);
        this.internalId = internalId;
    }

    public Sample(String stableId, String sampleType, int internalPatientId, String cancerTypeId)
    {
        this.stableId = stableId;
        this.sampleType = Sample.getType(sampleType);
        this.internalPatientId = internalPatientId;
		this.cancerTypeId = cancerTypeId;
    }

    private static Type getType(String sampleType)
    {
        sampleType = sampleType.replaceAll(" ", "_");
        if (Type.has(sampleType)) {
            return Type.valueOf(sampleType.toUpperCase());
        }
        else {
            return Type.PRIMARY_TUMOR;
        }
    }

    public int getInternalId()
    {
        return internalId;
    }

    public String getStableId()
    {
        return stableId;
    }
   
    public Type getType()
    {
        return sampleType;
    }

    public int getInternalPatientId()
    {
        return internalPatientId;
    }

    public String getCancerTypeId()
    {
        return cancerTypeId;
    }
}
