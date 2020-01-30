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
import java.util.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * A servlet designed to return a JSON array of mutation objects.
 *
 * @author Selcuk Onur Sumer
 */
public class MutationDataServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(
        MutationDataServlet.class
    );

    @Autowired
    private MutationDataUtils mutationDataUtils;

    // class which process access control to cancer studies
    private AccessControl accessControl;

    public MutationDataUtils getMutationDataUtils() {
        return mutationDataUtils;
    }

    public void setMutationDataUtils(MutationDataUtils mutationDataUtils) {
        this.mutationDataUtils = mutationDataUtils;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(
            this,
            config.getServletContext()
        );
        accessControl = SpringUtil.getAccessControl();
    }

    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    )
        throws ServletException, IOException {
        this.doPost(request, response);
    }

    protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response
    )
        throws ServletException, IOException {
        // get request parameters
        String geneticProfiles = request.getParameter("geneticProfiles");
        String genes = request.getParameter("geneList");
        // we need slashes for miRNA input
        genes = genes.replaceAll("\\\\/", "/");

        // parse single strings to create list of strings
        ArrayList<String> geneticProfileList =
            this.parseValues(geneticProfiles);
        ArrayList<String> targetGeneList = this.parseValues(genes);

        // final array to be sent
        JSONArray data = new JSONArray();

        try {
            // generate list by processing possible valid sample list parameters
            ArrayList<String> targetSampleList = this.getSampleList(request);

            for (String profileId : geneticProfileList) {
                // Get the Genetic Profile
                GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId(
                    profileId
                );
                CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(
                    geneticProfile.getCancerStudyId()
                );
                if (
                    accessControl
                        .isAccessibleCancerStudy(
                            cancerStudy.getCancerStudyStableId()
                        )
                        .size() ==
                    1
                ) {
                    // add mutation data for each genetic profile
                    data.addAll(
                        mutationDataUtils.getMutationData(
                            profileId,
                            targetGeneList,
                            targetSampleList
                        )
                    );
                }
            }
        } catch (DaoException e) {
            e.printStackTrace();
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            JSONValue.writeJSONString(data, out);
        } finally {
            out.close();
        }
    }

    /**
     * Generates a sample list by processing related request parameters,
     * which are sampleList, sampleSetId and sampleIdsKey. If none of these
     * parameters are valid, then this method will return an empty list.
     *
     * @param request   servlet request containing parameters
     * @return          a list of samples
     * @throws DaoException
     */
    protected ArrayList<String> getSampleList(HttpServletRequest request)
        throws DaoException {
        DaoSampleList daoSampleList = new DaoSampleList();

        String sampleListStr = request.getParameter("caseList");
        String sampleSetId = request.getParameter("caseSetId");
        String sampleIdsKey = request.getParameter("caseIdsKey");

        ArrayList<String> sampleList;

        // first check if sampleSetId param provided
        if (
            sampleSetId != null &&
            sampleSetId.length() != 0 &&
            !sampleSetId.equals("-1")
        ) {
            sampleList = new ArrayList<String>();

            // fetch a sample list for each sample set id
            // (this allows providing more than one sampleSetId)
            for (String id : this.parseValues(sampleSetId)) {
                SampleList list = daoSampleList.getSampleListByStableId(id);

                if (list != null) {
                    sampleList.addAll(list.getSampleList());
                }
            }
        }
        // if there is no sampleSetId, then check for sampleIdsKey param
        else if (sampleIdsKey != null && sampleIdsKey.length() != 0) {
            sampleList = new ArrayList<String>();

            // fetch a sample list for each sample ids key
            // (this allows providing more than one sampleIdsKey)
            for (String key : this.parseValues(sampleIdsKey)) {
                sampleList.addAll(
                    this.parseValues(SampleSetUtil.getSampleIds(key))
                );
            }
        } else {
            // plain list of samples provided, just parse the values
            sampleList = this.parseValues(sampleListStr);
        }

        return sampleList;
    }

    /**
     * Parses string values separated by white spaces or commas.
     *
     * @param values    string to be parsed
     * @return          array list of parsed string values
     */
    protected ArrayList<String> parseValues(String values) {
        if (values == null) {
            // return an empty list for null values
            return new ArrayList<String>(0);
        }

        // split by white space
        String[] parts = values.split("[\\s,]+");

        return new ArrayList<String>(Arrays.asList(parts));
    }
}
