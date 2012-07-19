package org.mskcc.portal.util;

public class SequenceCenterUtil {

    public static String getSequencingCenterAbbrev(String center) {
        if (center != null) {
            if (center.equalsIgnoreCase("broad.mit.edu")) {
                return "Broad";
            } else if (center.equalsIgnoreCase("genome.wustl.edu")) {
                return "WashU";
            } else if (center.equalsIgnoreCase("hgsc.bcm.edu")) {
                return "Baylor";
            } else {
                return center;
            }
        }
        return center;
    }
}
