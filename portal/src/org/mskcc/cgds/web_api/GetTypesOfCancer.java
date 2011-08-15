package org.mskcc.cgds.web_api;

import java.util.ArrayList;

import org.mskcc.cgds.dao.DaoTypeOfCancer;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.TypeOfCancer;

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
}