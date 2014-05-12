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

package org.mskcc.cbio.portal.web_api;

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.GeneticProfile;

import java.util.ArrayList;

/**
 * Web API for Getting Genetic Profiles.
 *
 * @author Ethan Cerami.
 */
public class GetGeneticProfiles {

    /**
     * Gets all Genetic Profiles associated with a specific cancer study.
     *
     * @param cancerStudyId Cancer Study ID.
     * @return ArrayList of GeneticProfile Objects.
     * @throws DaoException Remote / Network IO Error.
     */
    public static ArrayList<GeneticProfile> getGeneticProfiles(String cancerStudyId)
            throws DaoException {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
        if (cancerStudy != null) {
            return DaoGeneticProfile.getAllGeneticProfiles(cancerStudy.getInternalId());
        } else {
            return new ArrayList<GeneticProfile>();
        }
    }

    /**
     * Get the genetic profiles for a cancer study
     *
     * @param cancerStudyStableId   Stable Identifier for Cancer Study.
     * @return Genetic Profiles Table Output.
     * @throws DaoException Database Exception.
     */
    public static String getGeneticProfilesAsTable(String cancerStudyStableId) throws DaoException {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyStableId);
        StringBuilder buf = new StringBuilder();
        if (cancerStudy != null) {
            int cancerStudyInternalId = cancerStudy.getInternalId();
            ArrayList<GeneticProfile> list =
                    DaoGeneticProfile.getAllGeneticProfiles(cancerStudyInternalId);
            if (list.size() > 0) {

                buf.append("genetic_profile_id\tgenetic_profile_name\tgenetic_profile_description\t" +
                        "cancer_study_id\t" +
                        "genetic_alteration_type\tshow_profile_in_analysis_tab\n");
                for (GeneticProfile geneticProfile : list) {
                    buf.append(geneticProfile.getStableId()).append("\t");
                    buf.append(geneticProfile.getProfileName()).append("\t");
                    buf.append(geneticProfile.getProfileDescription()).append("\t");
                    buf.append(geneticProfile.getCancerStudyId()).append("\t");
                    buf.append(geneticProfile.getGeneticAlterationType()).append("\t");
                    buf.append(geneticProfile.showProfileInAnalysisTab()).append("\n");
                }
            } else {
                buf.append("Error:  No genetic profiles available for: ")
                        .append(cancerStudyStableId).append(".\n");
                return buf.toString();
            }
        }
        return buf.toString();
    }
}
