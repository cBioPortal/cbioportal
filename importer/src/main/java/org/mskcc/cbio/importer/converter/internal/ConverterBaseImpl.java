/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.importer.converter.internal;

import org.mskcc.cbio.importer.CaseIDs;
import org.mskcc.cbio.importer.model.*;

import org.apache.commons.logging.Log;

import java.util.List;

public abstract class ConverterBaseImpl
{
    protected void logMessage(Log log, String message)
    {
        if (log.isInfoEnabled()) {
            log.info(message);
        }
    }

    protected int filterColumnsBySampleType(CaseIDs caseIDs, DataMatrix dataMatrix, ConversionType conversionType)
	{
		int columnsIgnored = 0;
		List<String> columnHeaders = dataMatrix.getColumnHeaders();
		for (int lc = 2; lc < columnHeaders.size(); lc++) {
			if (caseIDs.isSampleId(columnHeaders.get(lc))) {
				switch (conversionType) {
					case TUMOR_ONLY:
						if (caseIDs.isNormalId(columnHeaders.get(lc))) {
							dataMatrix.ignoreColumn(lc, true);
							++columnsIgnored;
						}
						break;
					case NORMAL_ONLY:
						if (!caseIDs.isNormalId(columnHeaders.get(lc))) {
							dataMatrix.ignoreColumn(lc, true);
							++columnsIgnored;
						}
						break;
					default:
						break;
				}
			}
		}
		return columnsIgnored;
	}
}
