package org.mskcc.cbio.portal.remote;

import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.GeneticProfile;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneticProfile;
import org.mskcc.cgds.dao.DaoCancerStudy;

import java.util.ArrayList;

/**
 * Gets all Genetic Profiles associated with a specific cancer study.
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
            DaoGeneticProfile daoProfile = new DaoGeneticProfile();
            return daoProfile.getAllGeneticProfiles(cancerStudy.getInternalId());
        } else {
            return new ArrayList<GeneticProfile>();
        }
    }
}
