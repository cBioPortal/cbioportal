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

package org.mskcc.cbio.cgds.web_api;

import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneticProfile;
import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.GeneticProfile;

import java.util.ArrayList;

/**
 * Web API for Getting Genetic Profiles.
 *
 * @author Ethan Cerami.
 */
public class GetGeneticProfiles {

    /**
     * Get the genetic profiles for a cancer study
     *
     * @param cancerStudyStableId   Stable Identifier for Cancer Study.
     * @return Genetic Profiles Table Output.
     * @throws DaoException Database Exception.
     */
    public static String getGeneticProfiles(String cancerStudyStableId) throws DaoException {
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
