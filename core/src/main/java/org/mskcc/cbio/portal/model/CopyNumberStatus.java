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

/**
 * Class for copy number status
 */
public class CopyNumberStatus {
    public static final int COPY_NUMBER_AMPLIFICATION = 2;
    public static final int COPY_NUMBER_GAIN = 1;
    public static final int NO_CHANGE = 0;
    public static final int HEMIZYGOUS_DELETION = -1;
    public static final int HOMOZYGOUS_DELETION = -2;
    public static final int NO_DATA = -9999;

    public static int getCNAStatus(int entrezGeneId, String caseId, String valueStr) {

		try {
			Double value = Double.valueOf(valueStr);
			if (value < -2 || value > 2) {
				throw new IllegalArgumentException("Can't handle CNA value of:  " + value
                                        + " Case ID:  " + caseId + ", Entrez Gene ID:  " + entrezGeneId);
			} else {
				if (value > 2) {
					return COPY_NUMBER_AMPLIFICATION;
				} else {
					return value.intValue();
				}
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Can't handle CNA value of:  " + valueStr
                                + " Case ID:  " + caseId + ", Entrez Gene ID:  " + entrezGeneId);
		}
		
	}
}