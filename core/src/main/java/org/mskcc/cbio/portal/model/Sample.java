/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mskcc.cbio.portal.model;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.*;
import org.mskcc.cbio.portal.util.StableIdUtil;

/**
 * Encapsulates Sample Data.
 *
 * @author Benjamin Gross
 */
public class Sample {

    public static enum Type {
        PRIMARY_SOLID_TUMOR("Primary Solid Tumor"),
        RECURRENT_SOLID_TUMOR("Recurrent Solid Tumor"),
        PRIMARY_BLOOD_TUMOR("Primary Blood Tumor"),
        RECURRENT_BLOOD_TUMOR("Recurrent Blood Tumor"),
        METASTATIC("Metastatic"),
        BLOOD_DERIVED_NORMAL("Blood Derived Normal"),
        SOLID_TISSUES_NORMAL("Solid Tissues Normal");

        private String propertyName;

        Type(String propertyName) {
            this.propertyName = propertyName;
        }

        public String toString() {
            return propertyName;
        }

        public boolean isNormal() {
            return this == BLOOD_DERIVED_NORMAL || this == SOLID_TISSUES_NORMAL;
        }

        public static Set<Type> normalTypes() {
            return EnumSet.of(BLOOD_DERIVED_NORMAL, SOLID_TISSUES_NORMAL);
        }

        public static boolean has(String value) {
            if (value == null) return false;
            try {
                return valueOf(value.toUpperCase()) != null;
            } catch (IllegalArgumentException x) {
                return false;
            }
        }
    }

    private int internalId;
    private String stableId;
    private Type sampleType;
    private int internalPatientId;
    private String cancerTypeId;

    public Sample(
        String stableId,
        int internalPatientId,
        String cancerTypeId,
        String sampleType
    ) {
        this(stableId, internalPatientId, cancerTypeId);
        this.sampleType = getType(stableId, sampleType);
    }

    public Sample(
        int internalId,
        String stableId,
        int internalPatientId,
        String cancerTypeId
    ) {
        this(stableId, internalPatientId, cancerTypeId);
        this.internalId = internalId;
    }

    public Sample(String stableId, int internalPatientId, String cancerTypeId) {
        this.stableId = stableId;
        this.sampleType = getType(stableId, null);
        this.internalPatientId = internalPatientId;
        this.cancerTypeId = cancerTypeId;
    }

    private Type getType(String stableId, String sampleType) {
        Matcher tcgaSampleBarcodeMatcher = StableIdUtil.TCGA_SAMPLE_TYPE_BARCODE_REGEX.matcher(
            stableId
        );
        if (tcgaSampleBarcodeMatcher.find()) {
            return StableIdUtil.getTypeByTCGACode(
                tcgaSampleBarcodeMatcher.group(1)
            );
        } else if (sampleType != null && Type.has(sampleType)) {
            return Type.valueOf(sampleType.toUpperCase());
        } else {
            return Type.PRIMARY_SOLID_TUMOR;
        }
    }

    public int getInternalId() {
        return internalId;
    }

    public String getStableId() {
        return stableId;
    }

    public Type getType() {
        return sampleType;
    }

    public int getInternalPatientId() {
        return internalPatientId;
    }

    public String getCancerTypeId() {
        return cancerTypeId;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Sample)) {
            return false;
        }

        Sample anotherSample = (Sample) obj;
        return (this.internalId == anotherSample.getInternalId());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash =
            41 * hash + (this.stableId != null ? this.stableId.hashCode() : 0);
        return hash;
    }
}
