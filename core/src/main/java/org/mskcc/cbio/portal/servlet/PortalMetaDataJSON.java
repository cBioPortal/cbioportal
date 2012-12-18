/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.servlet;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.CaseList;
import org.mskcc.cbio.cgds.model.GeneticProfile;
import org.mskcc.cbio.cgds.util.AccessControl;
import org.mskcc.cbio.cgds.web_api.ProtocolException;
import org.mskcc.cbio.portal.model.GeneSet;
import org.mskcc.cbio.portal.remote.GetCaseSets;
import org.mskcc.cbio.portal.remote.GetGeneticProfiles;
import org.mskcc.cbio.portal.util.GeneSetUtil;
import org.mskcc.cbio.portal.util.XDebug;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This Servlet Returns a JSON Representation of all Cancer Studies and all
 * Gene Sets within the Portal.
 *
 * @author Ethan Cerami.
 */
public class PortalMetaDataJSON extends HttpServlet {

	// class which process access control to cancer studies
	private AccessControl accessControl;

    /**
     * Initializes the servlet.
     *
     * @throws ServletException Serlvet Init Error.
     */
    public void init() throws ServletException {
        super.init();
		ApplicationContext context = 
			new ClassPathXmlApplicationContext("classpath:applicationContext-security.xml");
		accessControl = (AccessControl)context.getBean("accessControl");
	}

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  Http Servlet Request Object.
     * @param httpServletResponse Http Servelt Response Object.
     * @throws javax.servlet.ServletException Servlet Error.
     * @throws java.io.IOException            IO Error.
     */
    protected void doGet(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException,
            IOException {
        XDebug xdebug = new XDebug(httpServletRequest);

        //  Cancer All Cancer Studies
        try {
			List<CancerStudy> cancerStudiesList = accessControl.getCancerStudies();

            //  Get all Genomic Profiles and Case Sets for each Cancer Study
            Map rootMap = new LinkedHashMap();
            Map cancerStudyMap = new LinkedHashMap();
            rootMap.put("cancer_studies", cancerStudyMap);
            for (CancerStudy cancerStudy : cancerStudiesList) {
                ArrayList<CaseList> caseSets = GetCaseSets.getCaseSets
                        (cancerStudy.getCancerStudyStableId());

                ArrayList<GeneticProfile> geneticProfiles =
                        GetGeneticProfiles.getGeneticProfiles
                                (cancerStudy.getCancerStudyStableId());
                JSONArray jsonGenomicProfileList = new JSONArray();
                for (GeneticProfile geneticProfile : geneticProfiles) {
                    Map map = new LinkedHashMap();
                    map.put("id", geneticProfile.getStableId());
                    map.put("alteration_type", geneticProfile.getGeneticAlterationType().toString());
                    map.put("show_in_analysis_tab", geneticProfile.showProfileInAnalysisTab());
                    map.put("name", geneticProfile.getProfileName());
                    map.put("description", geneticProfile.getProfileDescription());
                    jsonGenomicProfileList.add(map);
                }

                JSONArray jsonCaseList = new JSONArray();
                for (CaseList caseSet : caseSets) {
                    Map map = new LinkedHashMap();
                    map.put("id", caseSet.getStableId());
                    map.put("name", caseSet.getName());
                    map.put("description", caseSet.getDescription());
                    jsonCaseList.add(map);
                }
                Map jsonCancerStudySubMap = new LinkedHashMap();
                jsonCancerStudySubMap.put("name", cancerStudy.getName());
                jsonCancerStudySubMap.put("description", cancerStudy.getDescription());
                jsonCancerStudySubMap.put("citation", cancerStudy.getCitation());
                jsonCancerStudySubMap.put("pmid", cancerStudy.getPmid());
                jsonCancerStudySubMap.put("genomic_profiles", jsonGenomicProfileList);
                jsonCancerStudySubMap.put("case_sets", jsonCaseList);
                jsonCancerStudySubMap.put("has_mutation_data", cancerStudy.hasMutationData(geneticProfiles));
                jsonCancerStudySubMap.put("has_mutsig_data", cancerStudy.hasMutSigData());
                jsonCancerStudySubMap.put("has_gistic_data", cancerStudy.hasGisticData());
                cancerStudyMap.put(cancerStudy.getCancerStudyStableId(), jsonCancerStudySubMap);
            }

            //  Get all Gene Sets
            GeneSetUtil geneSetUtil = GeneSetUtil.getInstance();
            Map jsonGeneSetMap = new LinkedHashMap();
            rootMap.put("gene_sets", jsonGeneSetMap);
            ArrayList<GeneSet> geneSetList = geneSetUtil.getGeneSetList();
            for (GeneSet geneSet : geneSetList) {
                Map geneSetMap = new LinkedHashMap();
                geneSetMap.put("name", geneSet.getName());
                geneSetMap.put("gene_list", geneSet.getGeneList());
                jsonGeneSetMap.put(geneSet.getId(), geneSetMap);
            }

            httpServletResponse.setContentType("application/json");
            String jsonText = JSONValue.toJSONString(rootMap);
            PrintWriter writer = httpServletResponse.getWriter();
            writer.write(jsonText);
            writer.flush();
            writer.close();
        } catch (DaoException e) {
            throw new ServletException(e);
        } catch (ProtocolException e) {
            throw new ServletException(e);
		}
    }

}