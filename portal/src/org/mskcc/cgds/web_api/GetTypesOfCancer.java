package org.mskcc.cgds.web_api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoTypeOfCancer;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.TypeOfCancer;
import org.mskcc.cgds.model.CancerStudy;

/**
 * Get all the types of cancer in the dbms.
 * Used by the Web API.
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class GetTypesOfCancer {

    public static String getTypesOfCancer() throws DaoException, ProtocolException {

        ArrayList<TypeOfCancer> typeOfCancerList = DaoTypeOfCancer.getAllTypesOfCancer();
        StringBuffer buf = new StringBuffer();
        if (typeOfCancerList.size() > 0) {

            buf.append("type_of_cancer_id\tname\n");
            for (TypeOfCancer typeOfCancer : typeOfCancerList) {
                buf.append(typeOfCancer.getTypeOfCancerId() + "\t");
                buf.append(typeOfCancer.getName() + "\n");
            }
            return buf.toString();
        } else {
            throw new ProtocolException ("No Types of Cancer Available.\n");
        }
    }

    /**
     * Gets all Cancer Studies stored in Remote CGDS Server.
     *
     * @return ArrayList of CancerStudy Objects.
     * @throws DaoException Database Access Exception.
     */
    public static String getCancerStudies() throws DaoException {
        ArrayList<CancerStudy> cancerStudyList = DaoCancerStudy.getAllCancerStudies();

        //  Before returning the list, sort it alphabetically
        Collections.sort(cancerStudyList, new CancerStudiesComparator());

        //  Then, insert "All" Cancer Types at beginning
        ArrayList<CancerStudy> finalCancerStudiesList = new ArrayList<CancerStudy>();
        CancerStudy allCancerStudy = new CancerStudy("All Cancer Types", "All Cancer Types",
                "all", "all", true);
        finalCancerStudiesList.add(allCancerStudy);
        finalCancerStudiesList.addAll(cancerStudyList);

        StringBuffer buf = new StringBuffer();
        buf.append("cancer_study_id\tname\tdescription\n");
        for (CancerStudy cancerStudy : finalCancerStudiesList) {
            // changed to output stable identifier, instead of internal integer identifer.
            buf.append(cancerStudy.getCancerStudyStableId() + "\t");
            buf.append(cancerStudy.getName() + "\t");
            buf.append(cancerStudy.getDescription() + "\n");
        }
        return buf.toString();
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