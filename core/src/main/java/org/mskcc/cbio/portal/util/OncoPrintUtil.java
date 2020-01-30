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

import java.util.Formatter;
import java.util.List;
import org.mskcc.cbio.portal.model.SampleList;

public class OncoPrintUtil {

    /**
     * Constructs the OncoPrint patient set description.
     *
     * @param sampleSetId String
     * @param sampleSets List<SampleList>
     *
     * @return String
     */
    public static String getSampleSetDescription(
        String sampleSetId,
        List<SampleList> sampleSets
    ) {
        StringBuilder builder = new StringBuilder();
        for (SampleList sampleSet : sampleSets) {
            if (sampleSetId.equals(sampleSet.getStableId())) {
                builder.append(
                    sampleSet.getName() + ": " + sampleSet.getDescription()
                );
            }
        }
        return builder.toString();
    }

    /**
     * Format percentage.
     *
     * <p/>
     * if value == 0 return "--"
     * case value
     * 0: return "--"
     * 0<value<=0.01: return "<1%"
     * 1<value: return "<value>%"
     *
     * @param value double
     *
     * @return String
     */
    public static String alterationValueToString(double value) {
        // in oncoPrint show 0 percent as 0%, not --
        if (0.0 < value && value <= 0.01) {
            return "<1%";
        }

        // if( 1.0 < value ){
        Formatter f = new Formatter();
        f.format("%.0f", value * 100.0);
        return f.out().toString() + "%";
    }
}
