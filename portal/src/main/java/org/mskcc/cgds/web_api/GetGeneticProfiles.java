package org.mskcc.cgds.web_api;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneticProfile;
import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.GeneticProfile;

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
            DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
            ArrayList<GeneticProfile> list =
                    daoGeneticProfile.getAllGeneticProfiles(cancerStudyInternalId);
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
