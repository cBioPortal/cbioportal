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

package org.mskcc.cbio.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoClinicalAttributeMeta;
import org.mskcc.cbio.portal.dao.DaoClinicalData;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.ClinicalData;
import org.mskcc.cbio.portal.model.ClinicalParameterMap;
import org.mskcc.cbio.portal.util.AccessControl;
import org.mskcc.cbio.portal.util.CategoryLabelReader;
import org.mskcc.cbio.portal.util.SpringUtil;

public class ClinicalFreeFormJSON extends HttpServlet {
    public static final String STUDY_ID = "studyId";

    // class which process access control to cancer studies
    private AccessControl accessControl;

    /**
     * Initializes the servlet.
     */
    public void init() throws ServletException {
        super.init();
        accessControl = SpringUtil.getAccessControl();
    }

    /**
     * Handles HTTP GET Request.
     */
    protected void doGet(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    )
        throws ServletException, IOException {
        String studyId = httpServletRequest.getParameter(STUDY_ID);

        try {
            // final object to be send as JSON
            JSONObject jsonObject = new JSONObject();

            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(
                studyId
            );

            // check if cancerStudy exists
            if (
                cancerStudy == null ||
                accessControl
                    .isAccessibleCancerStudy(
                        cancerStudy.getCancerStudyStableId()
                    )
                    .size() !=
                1
            ) {
                // just create empty collections if cancers study cannot be found
                jsonObject.put("clinicalCaseSet", new JSONArray());
                jsonObject.put("categoryMap", new HashMap<String, Object>());
                jsonObject.put("freeFormData", new JSONArray());
            } else {
                HashSet<String> clinicalPatientSet = DaoClinicalData.getAllPatients(
                    cancerStudy.getInternalId()
                );

                HashSet<String> paramSet = DaoClinicalData.getDistinctParameters(
                    cancerStudy.getInternalId()
                );

                List<ClinicalData> freeFormData = DaoClinicalData.getDataByCancerStudy(
                    cancerStudy.getInternalId()
                );

                // map of <param, distinctCategorySet> pairs
                Map<String, Object> categoryMap = new HashMap<String, Object>();

                // array of clinical case IDs
                JSONArray patientIds = new JSONArray();

                // add the clinical patient set
                for (String patientId : clinicalPatientSet) {
                    patientIds.add(patientId);
                }

                jsonObject.put("clinicalCaseSet", patientIds);

                // get all distinct categories
                List<ClinicalParameterMap> paramMaps = DaoClinicalData.getDataSlice(
                    cancerStudy.getInternalId(),
                    paramSet
                );
                for (ClinicalParameterMap paramMap : paramMaps) {
                    Set<String> distinctCategorySet = paramMap.getDistinctCategories();
                    JSONArray distinctCategories = new JSONArray();

                    for (String category : distinctCategorySet) {
                        if (category.trim().length() > 0) {
                            distinctCategories.add(category);
                        }
                    }

                    categoryMap.put(
                        CategoryLabelReader.safeCategoryName(
                            paramMap.getName()
                        ),
                        distinctCategories
                    );
                }

                // add the category map
                jsonObject.put("categoryMap", categoryMap);

                // array of free form data
                JSONArray freeFormArray = new JSONArray();

                // get all clinical free form data for the specified cancer study
                for (ClinicalData data : freeFormData) {
                    JSONObject freeFormObject = new JSONObject();

                    //freeFormObject.put("cancerStudyId", data.getCancerStudyId());
                    freeFormObject.put("caseId", data.getStableId());
                    freeFormObject.put(
                        "paramName",
                        CategoryLabelReader.safeCategoryName(data.getAttrId())
                    );
                    freeFormObject.put("paramValue", data.getAttrVal());

                    freeFormArray.add(freeFormObject);
                }

                // add the free form data array
                jsonObject.put("freeFormData", freeFormArray);

                // add the map for human readable category names
                jsonObject.put(
                    "categoryLabelMap",
                    DaoClinicalAttributeMeta.getAllMap()
                );
            }

            httpServletResponse.setContentType("application/json");
            PrintWriter out = httpServletResponse.getWriter();

            try {
                JSONValue.writeJSONString(jsonObject, out);
            } finally {
                out.close();
            }
        } catch (DaoException e) {
            throw new ServletException(e);
        }
    }
}
