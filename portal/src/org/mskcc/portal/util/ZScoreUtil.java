package org.mskcc.portal.util;

import org.mskcc.portal.servlet.QueryBuilder;
import org.mskcc.portal.model.GeneticProfile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;

// TODO: perhaps delete this class
public class ZScoreUtil {
    public static final double Z_SCORE_THRESHOLD_DEFAULT = 2;
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
}
