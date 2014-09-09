/** Copyright (c) 2013 Memorial Sloan-Kettering Cancer Center.
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

import java.util.EnumSet;
import java.util.Set;
import org.mskcc.cbio.portal.util.StableIdUtil;

import java.util.regex.*;

/**
 * Encapsulates Sample Data.
 *
 * @author Benjamin Gross
 */
public class Sample {

    public static enum Type
    {
        PRIMARY_SOLID_TUMOR("Primary Solid Tumor"),
        RECURRENT_SOLID_TUMOR("Recurrent Solid Tumor"),
        PRIMARY_BLOOD_TUMOR("Primary Blood Tumor"),
        RECURRENT_BLOOD_TUMOR("Recurrent Blood Tumor"),
        METASTATIC("Metastatic"),
        BLOOD_NORMAL("Blood Derived Normal"),
        SOLID_NORMAL("Solid Tissues Normal");

        private String propertyName;
        
        Type(String propertyName) { this.propertyName = propertyName; }
        public String toString() { return propertyName; }
        public boolean isNormal() { return this==BLOOD_NORMAL || this==SOLID_NORMAL; }
        public static Set<Type> normalTypes() {return EnumSet.of(BLOOD_NORMAL, SOLID_NORMAL);}

        static public boolean has(String value)
        {
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

    public Sample(int internalId, String stableId, int internalPatientId, String cancerTypeId)
    {
        this(stableId, internalPatientId, cancerTypeId);
        this.internalId = internalId;
    }

    public Sample(String stableId, int internalPatientId, String cancerTypeId)
    {
        this.stableId = stableId;
        this.sampleType = getType(stableId);
        this.internalPatientId = internalPatientId;
		this.cancerTypeId = cancerTypeId;
    }

    private Type getType(String stableId)
    {
        Matcher tcgaSampleBarcodeMatcher = StableIdUtil.TCGA_SAMPLE_TYPE_BARCODE_REGEX.matcher(stableId);
        if (tcgaSampleBarcodeMatcher.find()) {
            return StableIdUtil.getTypeByTCGACode(tcgaSampleBarcodeMatcher.group(1));
        }
        else {
            return Type.PRIMARY_SOLID_TUMOR;
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Sample)) {
            return false;
        }
        
        Sample anotherSample = (Sample)obj;
        return (this.internalId == anotherSample.getInternalId());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + (this.stableId != null ? this.stableId.hashCode() : 0);
        return hash;
    }
}
