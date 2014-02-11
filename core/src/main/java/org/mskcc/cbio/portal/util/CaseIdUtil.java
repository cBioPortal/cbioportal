/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.model.Sample;
import java.util.regex.*;

public class CaseIdUtil
{
	public static String getPatientId(String barCode)
	{
        return getId(barCode, false);
	}

	public static String getSampleId(String barCode)
	{
        return getId(barCode, true);
	}

    private static String getId(String barCode, boolean forSample)
    {
		// do not process non-TCGA bar codes...
		if (!barCode.startsWith("TCGA")) {
			return barCode;
		}

        String id = null;
		String barCodeParts[] = barCode.split("-");
		try {
            // an example bar code looks like this:  TCGA-13-1479-01A-01W
			id = barCodeParts[0] + "-" + barCodeParts[1] + "-" + barCodeParts[2];
            if (forSample) {
                id += "-" + barCodeParts[3];
                Matcher tcgaSampleBarcodeMatcher = Sample.TCGA_FULL_SAMPLE_BARCODE_REGEX.matcher(id);
                id = (tcgaSampleBarcodeMatcher.find()) ? tcgaSampleBarcodeMatcher.group(1) : id;
            }
		}
        catch (ArrayIndexOutOfBoundsException e) {
			id = barCode;
		}

		return id;
    }

    static public Sample.Type getTypeByTCGACode(String tcgaCode)
    {
        if (tcgaCode.equals("01")) {
            return Sample.Type.PRIMARY_SOLID_TUMOR;
        }
        else if (tcgaCode.equals("02")) {
            return Sample.Type.RECURRENT_SOLID_TUMOR;
        }
        else if (tcgaCode.equals("03")) {
            return Sample.Type.PRIMARY_BLOOD_TUMOR;
        }
        else if (tcgaCode.equals("04")) {
            return Sample.Type.RECURRENT_BLOOD_TUMOR;
        }
        else if (tcgaCode.equals("06")) {
            return Sample.Type.METASTATIC;
        }
        else if (tcgaCode.equals("10")) {
            return Sample.Type.BLOOD_NORMAL;
        }
        else if (tcgaCode.equals("11")) {
            return Sample.Type.SOLID_NORMAL;
        }
        else {
            return Sample.Type.PRIMARY_SOLID_TUMOR;
        }
    }
}