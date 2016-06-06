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

package org.mskcc.cbio.portal.html.special_gene;

import org.mskcc.cbio.portal.model.ExtendedMutation;

import java.util.ArrayList;

/**
 * Interface for a Special Gene, Deserving of Extra Annotation.
 *
 * @author Ethan Cerami.
*/
public abstract class SpecialGene {

    /**
     * Gets the extra annocation headers (if any), associated with the special gene.
     * @return ArrayList of annotation headers.
     */
    public abstract ArrayList<String> getDataFieldHeaders();

    /**
     * Gets Footer Annotation Describing Anything special about these mutations.
     * @return footer message.
     */
    public abstract String getFooter();

    /**
     * Gets the extra annotation data fields (if any), associated with the specified
     * mutation.
     *
     * @param mutation ExtentedMutation Object.
     * @return ArrayList of Data Field String objects.
     */
    public abstract ArrayList<String> getDataFields (ExtendedMutation mutation);

    protected void setNtPosition(long ntPosition, ArrayList<String> dataFields) {
        if (ntPosition > 0) {
            dataFields.add(Long.toString(ntPosition));
        } else {
            dataFields.add("--");
        }
    }
}
