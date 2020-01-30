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

package org.mskcc.cbio.portal.util;

import java.util.*;
import java.util.regex.*;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.Sample;

public class StableIdUtil {
    public static final String TCGA_BARCODE_PREFIX = "TCGA";

    public static final Pattern TCGA_SAMPLE_BARCODE_REGEX = Pattern.compile(
        "^(TCGA-\\w\\w-\\w\\w\\w\\w-\\d\\d).*$"
    );

    public static final Pattern TCGA_SAMPLE_TYPE_BARCODE_REGEX = Pattern.compile(
        "^TCGA-\\w\\w-\\w\\w\\w\\w-(\\d\\d).*$"
    );

    public static final Pattern TCGA_PATIENT_BARCODE_FROM_SAMPLE_REGEX = Pattern.compile(
        "^(TCGA-\\w\\w-\\w\\w\\w\\w)\\-\\d\\d.*$"
    );

    public static String getPatientId(String barcode) {
        return getId(barcode, false);
    }

    public static String getSampleId(String barcode) {
        return getId(barcode, true);
    }

    private static String getId(String barcode, boolean forSample) {
        // do not process non-TCGA bar codes...
        if (!barcode.startsWith(TCGA_BARCODE_PREFIX)) {
            return barcode;
        }

        String id = null;
        String barcodeParts[] = clean(barcode).split("-");
        try {
            // an example bar code looks like this:  TCGA-13-1479-01A-01W
            id =
                barcodeParts[0] + "-" + barcodeParts[1] + "-" + barcodeParts[2];
            if (forSample) {
                id += "-" + barcodeParts[3];
                Matcher tcgaSampleBarcodeMatcher = TCGA_SAMPLE_BARCODE_REGEX.matcher(
                    id
                );
                id =
                    (tcgaSampleBarcodeMatcher.find())
                        ? tcgaSampleBarcodeMatcher.group(1)
                        : id;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // many (all?) barcodes in overrides are just patient id's - tack on a primary solid tumor code
            // (we don't have any blood cancer overrides that need a sample code)
            id = barcode + "-01";
        }

        return id;
    }

    private static String clean(String barcode) {
        if (barcode.contains("Tumor")) {
            return barcode.replace("Tumor", "01");
        } else if (barcode.contains("Normal")) {
            return barcode.replace("Normal", "11");
        } else {
            return barcode;
        }
    }

    public static Sample.Type getTypeByTCGACode(String tcgaCode) {
        if (tcgaCode.equals("01")) {
            return Sample.Type.PRIMARY_SOLID_TUMOR;
        } else if (tcgaCode.equals("02")) {
            return Sample.Type.RECURRENT_SOLID_TUMOR;
        } else if (tcgaCode.equals("03")) {
            return Sample.Type.PRIMARY_BLOOD_TUMOR;
        } else if (tcgaCode.equals("04")) {
            return Sample.Type.RECURRENT_BLOOD_TUMOR;
        } else if (tcgaCode.equals("06")) {
            return Sample.Type.METASTATIC;
        } else if (tcgaCode.equals("10")) {
            return Sample.Type.BLOOD_DERIVED_NORMAL;
        } else if (tcgaCode.equals("11")) {
            return Sample.Type.SOLID_TISSUES_NORMAL;
        } else {
            return Sample.Type.PRIMARY_SOLID_TUMOR;
        }
    }

    public static boolean isNormal(String barcode) {
        Matcher tcgaSampleBarcodeMatcher = TCGA_SAMPLE_TYPE_BARCODE_REGEX.matcher(
            barcode
        );
        if (tcgaSampleBarcodeMatcher.find()) {
            Sample.Type type = getTypeByTCGACode(
                tcgaSampleBarcodeMatcher.group(1)
            );
            return (
                type.equals(Sample.Type.BLOOD_DERIVED_NORMAL) ||
                type.equals(Sample.Type.SOLID_TISSUES_NORMAL)
            );
        }
        return false;
    }
}
