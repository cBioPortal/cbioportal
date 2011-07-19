package org.mskcc.cgds.web_api;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneticProfile;
import org.mskcc.cgds.model.GeneticProfile;
import org.mskcc.cgds.servlet.WebService;

import java.util.ArrayList;

public class GetGeneticProfiles {
   
   /**
    * Get the genetic profiles for a cancer study
    * 
    * @param cancerStudyId
    * @return
    * @throws DaoException
    */
    public static String getGeneticProfiles(int cancerStudyId) throws DaoException {
        DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
        ArrayList<GeneticProfile> list =
                daoGeneticProfile.getAllGeneticProfiles(cancerStudyId);
        StringBuffer buf = new StringBuffer();
        if (list.size() > 0) {

            buf.append("genetic_profile_id\tgenetic_profile_name\tgenetic_profile_description\t" +
                    "cancer_study_id\t" +
                    "genetic_alteration_type\tshow_profile_in_analysis_tab\n");
            for (GeneticProfile geneticProfile : list) {
                buf.append(geneticProfile.getStableId() + "\t");
                buf.append(geneticProfile.getProfileName() + "\t");
                buf.append(geneticProfile.getProfileDescription() +"\t");
                buf.append(geneticProfile.getCancerStudyId() + "\t");
                buf.append(geneticProfile.getGeneticAlterationType() + "\t");
                buf.append(geneticProfile.showProfileInAnalysisTab() + "\n");
            }
            return buf.toString();
        } else {
            buf.append("Error:  No genetic profiles available for " + WebService.CANCER_STUDY_ID
                    +":  " + cancerStudyId + ".\n");
            return buf.toString();
        }
    }
}
