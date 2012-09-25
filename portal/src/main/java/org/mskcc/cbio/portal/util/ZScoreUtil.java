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

import org.mskcc.cbio.portal.servlet.QueryBuilder;
import org.mskcc.cbio.cgds.model.GeneticProfile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;

// TODO: perhaps delete this class
public class ZScoreUtil {
    public static final double Z_SCORE_THRESHOLD_DEFAULT = 2;
    public static final double RPPA_SCORE_THRESHOLD_DEFAULT = 2;
    public static final double OUTLIER_THRESHOLD_DEFAULT = 1;

    public static double getZScore(HashSet<String> geneticProfileIdSet,
            ArrayList<GeneticProfile> profileList, HttpServletRequest request) {
        double zScoreThreshold = ZScoreUtil.Z_SCORE_THRESHOLD_DEFAULT;

        //  If user has selected an outlier mRNA expression profile,
        //  switch to OUTLIER_THRESHOLD_DEFAULT.
        if (GeneticProfileUtil.outlierExpressionSelected(geneticProfileIdSet, profileList)) {
            zScoreThreshold = OUTLIER_THRESHOLD_DEFAULT;
        } else {
            String zScoreThesholdStr = request.getParameter(QueryBuilder.Z_SCORE_THRESHOLD);
            if (zScoreThesholdStr != null) {
                try {
                    zScoreThreshold = Double.parseDouble(zScoreThesholdStr);

                    // take absolute value
                    if( zScoreThreshold < 0.0 ){
                       zScoreThreshold = -zScoreThreshold;
                    }

                } catch (NumberFormatException e) {
                }
            }
        }
        return zScoreThreshold;
    }
    
    public static double getRPPAScore(HttpServletRequest request) {
        String rppaScoreStr = request.getParameter(QueryBuilder.RPPA_SCORE_THRESHOLD);
        if (rppaScoreStr == null) {
            return RPPA_SCORE_THRESHOLD_DEFAULT;
        } else {
            try {
                return Double.parseDouble(rppaScoreStr);
            } catch (NumberFormatException e) {
                return RPPA_SCORE_THRESHOLD_DEFAULT;
            }
        }
    }
}
