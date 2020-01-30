/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mskcc.cbio.portal.web_api;

import java.util.ArrayList;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;

/**
 * Web API for Getting Patient Lists.
 *
 * @author Ethan Cerami.
 */
public class GetSampleLists {

    /**
     * Gets all Patient Sets Associated with a specific Cancer Study.
     *
     * @param cancerStudyId Cancer Study ID.
     * @return ArrayList of SampleSet Objects.
     * @throws DaoException Database Error.
     */
    public static ArrayList<SampleList> getSampleLists(String cancerStudyId)
        throws DaoException {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(
            cancerStudyId
        );
        if (cancerStudy != null) {
            DaoSampleList daoSampleList = new DaoSampleList();
            ArrayList<SampleList> sampleList = daoSampleList.getAllSampleLists(
                cancerStudy.getInternalId()
            );
            return sampleList;
        } else {
            ArrayList<SampleList> sampleList = new ArrayList<SampleList>();
            return sampleList;
        }
    }

    /**
     * Get Patient List for Specified Stable Cancer Study ID.
     *
     * @param cancerStudyStableId Stable Cancer Study ID.
     * @return Table output.
     * @throws DaoException Database Error.
     */
    public static String getSampleListsAsTable(String cancerStudyStableId)
        throws DaoException {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(
            cancerStudyStableId
        );
        StringBuilder buf = new StringBuilder();
        if (cancerStudy != null) {
            int cancerStudyInternalId = cancerStudy.getInternalId();
            DaoSampleList daoSampleList = new DaoSampleList();
            ArrayList<SampleList> list = daoSampleList.getAllSampleLists(
                cancerStudyInternalId
            );
            if (list.size() > 0) {
                buf.append(
                    "case_list_id\tcase_list_name\tcase_list_description\t" +
                    "cancer_study_id\t" +
                    "case_ids\n"
                );
                for (SampleList sampleList : list) {
                    buf.append(sampleList.getStableId()).append("\t");
                    buf.append(sampleList.getName()).append("\t");
                    buf.append(sampleList.getDescription()).append("\t");
                    buf.append(sampleList.getCancerStudyId()).append("\t");
                    for (String aSample : sampleList.getSampleList()) {
                        buf.append(aSample).append(" ");
                    }
                    buf.append("\n");
                }
            } else {
                buf
                    .append("Error:  No case lists available for:  ")
                    .append(cancerStudyStableId)
                    .append(".\n");
                return buf.toString();
            }
        }
        return buf.toString();
    }
}
