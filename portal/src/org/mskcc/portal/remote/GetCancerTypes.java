package org.mskcc.portal.remote;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.CancerStudy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

/**
 * Gets all Cancer Studies stored in CGDS Server.
 */
public class GetCancerTypes {

    /**
     * Gets all Cancer Studies stored in Remote CGDS Server.
     *
     * @return ArrayList of CancerStudy Objects.
     * @throws DaoException Database Access Exception.
     */
    public static ArrayList<CancerStudy> getCancerStudies() throws DaoException {
        ArrayList<CancerStudy> cancerStudyList = DaoCancerStudy.getAllCancerStudies();

        //  Before returning the list, sort it alphabetically
        Collections.sort(cancerStudyList, new CancerStudiesComparator());

        //  Then, insert "All" Cancer Types at beginning
        ArrayList<CancerStudy> finalCancerStudiesList = new ArrayList<CancerStudy>();
        CancerStudy cancerStudy = new CancerStudy("All Cancer Types", "All Cancer Types",
                "all", "all", true);
        finalCancerStudiesList.add(cancerStudy);
        finalCancerStudiesList.addAll(cancerStudyList);

        return finalCancerStudiesList;
    }
}

/**
 * Compares Cancer Studies, so that we can sort them alphabetically.
 */
class CancerStudiesComparator implements Comparator {

    /**
     * Compare two cancer studies.
     * @param o  First Cancer Study.
     * @param o1 Second Cancer Study.
     * @return int indicating name sort order.
     */
    public int compare(Object o, Object o1) {
        CancerStudy study0 = (CancerStudy) o;
        CancerStudy study1 = (CancerStudy) o1;
        return study0.getName().compareTo(study1.getName());
    }
}
