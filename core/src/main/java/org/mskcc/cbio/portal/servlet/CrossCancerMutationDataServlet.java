/*
 * Copyright (c) 2015 - 2016 Memorial Sloan-Kettering Cancer Center.
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
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cbio.maf.TabDelimitedFileUtil;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.web_api.*;
import org.owasp.validator.html.PolicyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * A servlet designed to return a JSON array of mutation objects.
 *
 * @author Arman
 * @author Selcuk Onur Sumer
 */
public class CrossCancerMutationDataServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(
        CrossCancerMutationDataServlet.class
    );
    // class which process access control to cancer studies
    private AccessControl accessControl;

    @Autowired
    private MutationDataUtils mutationDataUtils;

    public MutationDataUtils getMutationDataUtils() {
        return mutationDataUtils;
    }

    public void setMutationDataUtils(MutationDataUtils mutationDataUtils) {
        this.mutationDataUtils = mutationDataUtils;
    }

    /**
     * Initializes the servlet.
     *
     * @throws ServletException Serlvet Init Error.
     */
    public void init() throws ServletException {
        super.init();
        accessControl = SpringUtil.getAccessControl();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(
            this,
            config.getServletContext()
        );
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
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();

        // final array to be sent
        JSONArray data = new JSONArray();

        // Get the gene list
        String geneList = request.getParameter("geneList");

        // Get the priority
        Integer dataTypePriority;
        try {
            dataTypePriority =
                Integer.parseInt(
                    request.getParameter(QueryBuilder.DATA_PRIORITY).trim()
                );
        } catch (NumberFormatException e) {
            dataTypePriority = 0;
        }

        String[] cancerStudyIdList = request
            .getParameter(QueryBuilder.CANCER_STUDY_LIST)
            .split(",");
        HashMap<String, Boolean> studyMap = new HashMap<>();
        for (String id : cancerStudyIdList) {
            studyMap.put(id, Boolean.TRUE);
        }

        try {
            //  Cancer All Cancer Studies
            List<CancerStudy> cancerStudiesList = accessControl.getCancerStudies();
            for (CancerStudy cancerStudy : cancerStudiesList) {
                if (
                    !studyMap.containsKey(cancerStudy.getCancerStudyStableId())
                ) {
                    continue;
                }
                String cancerStudyId = cancerStudy.getCancerStudyStableId();
                if (cancerStudyId.equalsIgnoreCase("all")) continue;

                //  Get all Genetic Profiles Associated with this Cancer Study ID.
                ArrayList<GeneticProfile> geneticProfileList = GetGeneticProfiles.getGeneticProfiles(
                    cancerStudyId
                );

                //  Get all Patient Lists Associated with this Cancer Study ID.
                ArrayList<SampleList> sampleSetList = GetSampleLists.getSampleLists(
                    cancerStudyId
                );

                //  Get the default patient set
                AnnotatedSampleSets annotatedSampleSets = new AnnotatedSampleSets(
                    sampleSetList,
                    dataTypePriority
                );
                SampleList defaultSampleSet = annotatedSampleSets.getDefaultSampleList();

                if (defaultSampleSet == null) continue;

                List<String> sampleList = defaultSampleSet.getSampleList();

                //  Get the default genomic profiles
                CategorizedGeneticProfileSet categorizedGeneticProfileSet = new CategorizedGeneticProfileSet(
                    geneticProfileList
                );
                HashMap<String, GeneticProfile> defaultGeneticProfileSet = null;
                switch (dataTypePriority) {
                    case 2:
                        defaultGeneticProfileSet =
                            categorizedGeneticProfileSet.getDefaultCopyNumberMap();
                        break;
                    case 1:
                        defaultGeneticProfileSet =
                            categorizedGeneticProfileSet.getDefaultMutationMap();
                        break;
                    case 0:
                    default:
                        defaultGeneticProfileSet =
                            categorizedGeneticProfileSet.getDefaultMutationAndCopyNumberMap();
                }

                for (GeneticProfile profile : defaultGeneticProfileSet.values()) {
                    ArrayList<String> targetGeneList =
                        this.parseValues(geneList);
                    if (
                        profile.getGeneticAlterationType() !=
                        GeneticAlterationType.MUTATION_EXTENDED
                    ) continue;
                    // add mutation data for each genetic profile
                    JSONArray mutationData = mutationDataUtils.getMutationData(
                        profile.getStableId(),
                        targetGeneList,
                        sampleList
                    );
                    data.addAll(mutationData);
                }
            }

            JSONValue.writeJSONString(data, writer);
        } catch (DaoException e) {
            throw new ServletException(e);
        } catch (ProtocolException e) {
            throw new ServletException(e);
        } finally {
            writer.close();
        }
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
