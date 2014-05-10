/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.util;

public class CaseIdUtil
{
	public static String getCaseId(String barCode)
	{
		// do not process non-TCGA bar codes...
		if (!barCode.startsWith("TCGA"))
		{
			return barCode;
		}

		// process bar code
		// an example bar code looks like this:  TCGA-13-1479-01A-01W

		String barCodeParts[] = barCode.split("-");

		String caseId = null;

		try
		{
			caseId = barCodeParts[0] + "-" + barCodeParts[1] + "-" + barCodeParts[2];

			// the following condition was prompted by case ids coming from
			// private cancer studies (like SKCM_BROAD) with case id's of
			// the form MEL-JWCI-WGS-XX or MEL-Ma-Mel-XX or MEL-UKRV-Mel-XX
			// TODO this causes problems for some cases, so disabling it
//			if (!barCode.startsWith("TCGA") &&
//			    barCodeParts.length == 4)
//			{
//				caseId += "-" + barCodeParts[3];
//			}
		} catch (ArrayIndexOutOfBoundsException e) {
			caseId = barCode;
		}

		return caseId;
	}
}