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

package org.mskcc.cbio.portal.oncoPrintSpecLanguage;

import java.util.ArrayList;

public class OncoPrintSpec {
    // set of these, for each data type, subject to multiple constraints
    // no constraints for a dataType means show all values
    // at most 2 constraints for any dataType, which must define 1 or 2 non-empty intervals
    ArrayList<ContinuousDataTypeSpec> theContinuousDataTypeSpec;
    ArrayList<DiscreteDataTypeSpec> theDiscreteDataTypeSpec;

    public OncoPrintSpec() {
        theContinuousDataTypeSpec = new ArrayList<ContinuousDataTypeSpec>();
        theDiscreteDataTypeSpec = new ArrayList<DiscreteDataTypeSpec>();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        // TODO: order by DataType
        for (ContinuousDataTypeSpec aContinuousDataTypeSpec : theContinuousDataTypeSpec) {
            sb.append(aContinuousDataTypeSpec.toString());
        }
        for (DiscreteDataTypeSpec aDiscreteDataTypeSpec : theDiscreteDataTypeSpec) {
            sb.append(aDiscreteDataTypeSpec.toString());
        }
        return sb.toString();
    }
}
