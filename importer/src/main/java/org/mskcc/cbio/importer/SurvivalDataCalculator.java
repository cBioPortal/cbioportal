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

// package
package org.mskcc.cbio.importer;

import org.mskcc.cbio.importer.model.DataMatrix;
import org.mskcc.cbio.importer.model.SurvivalStatus;

import java.util.List;

/**
 * Interface used to import portal data.
 */
public interface SurvivalDataCalculator
{
    /**
     * The list is in ascending (time) order,
     * i.e., patient matrix would come before follow-up matrices.
     */
    SurvivalStatus computeSurvivalData(List<DataMatrix> dataMatrices);
}
