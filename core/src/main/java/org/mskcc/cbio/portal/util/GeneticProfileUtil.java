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

import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.GeneticAlterationType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Genetic Profile Util Class.
 *
 */
public class GeneticProfileUtil {

    /**
     * Gets the GeneticProfile with the Specified GeneticProfile ID.
     * @param profileId GeneticProfile ID.
     * @param profileList List of Genetic Profiles.
     * @return GeneticProfile or null.
     */
    public static GeneticProfile getProfile(String profileId,
            ArrayList<GeneticProfile> profileList) {
        for (GeneticProfile profile : profileList) {
            if (profile.getStableId().equals(profileId)) {
                return profile;
            }
        }
        return null;
    }

    /**
     * Returns true if Any of the Profiles Selected by the User Refer to mRNA Expression
     * outlier profiles.
     *
     * @param geneticProfileIdSet   Set of Chosen Profiles IDs.
     * @param profileList           List of Genetic Profiles.
     * @return true or false.
     */
    public static boolean outlierExpressionSelected(HashSet<String> geneticProfileIdSet,
            ArrayList<GeneticProfile> profileList) {
        Iterator<String> geneticProfileIdIterator = geneticProfileIdSet.iterator();
        while (geneticProfileIdIterator.hasNext()) {
            String geneticProfileId = geneticProfileIdIterator.next();
            GeneticProfile geneticProfile = getProfile (geneticProfileId, profileList);
            if (geneticProfile != null && geneticProfile.getGeneticAlterationType().
                    equals(GeneticAlterationType.MRNA_EXPRESSION)) {
                String profileName = geneticProfile.getProfileName();
                if (profileName != null) {
                    if (profileName.toLowerCase().contains("outlier")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
