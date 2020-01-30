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

import com.google.common.base.Joiner;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.util.IGVLinking;

/**
 *
 * @author benjamin
 */
public class IGVLinkingJSON extends HttpServlet {
    public static final String CMD = "cmd";
    public static final String CANCER_STUDY_ID = "cancer_study_id";
    public static final String CASE_ID = "case_id";
    public static final String LOCUS = "locus"; // IGV locus string, e.g. chr1:000-200

    private static final int CANCER_STUDY_ID_INDEX = 0;
    private static final int CASE_ID_INDEX = 1;
    private static final int LOCUS_INDEX = 2;
    private static final String GENE_LIST = "gene_list";

    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    )
        throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response
    )
        throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void processRequest(
        HttpServletRequest request,
        HttpServletResponse response
    )
        throws ServletException, IOException {
        String cmd = request.getParameter(CMD);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONObject igvArgs = new JSONObject();
        try {
            if ((cmd != null) && (cmd.equals("get_igv_args"))) {
                String cancerStudyStableId = request.getParameter(
                    CANCER_STUDY_ID
                );
                cancerStudyStableId =
                    cancerStudyStableId == null ? "" : cancerStudyStableId;
                String geneList = request.getParameter(GENE_LIST);
                geneList = geneList == null ? "" : geneList;
                List<String> onlyGenesList = new ArrayList<String>();

                String[] geneList_ = geneList.split(" ");
                for (String gene_ : geneList_) {
                    CanonicalGene gene = DaoGeneOptimized
                        .getInstance()
                        .getGene(gene_);
                    if (
                        gene != null &&
                        !gene.isMicroRNA() &&
                        !gene.isPhosphoProtein()
                    ) {
                        onlyGenesList.add(gene_);
                    }
                }
                String encodedGeneList = "";
                if (onlyGenesList.size() > 0) {
                    encodedGeneList =
                        URLEncoder.encode(
                            Joiner.on(' ').join(onlyGenesList),
                            "UTF-8"
                        );
                }
                String[] args = IGVLinking.getIGVArgsForSegViewing(
                    cancerStudyStableId,
                    encodedGeneList
                );
                igvArgs.put("segfileUrl", args[0]);
                igvArgs.put("geneList", args[1]);
                igvArgs.put("referenceId", args[2]);
                igvArgs.put("fileName", args[3]);
            } else {
                String[] parameters = getParameters(request);
                if (parameters != null) {
                    String[] args = IGVLinking.getIGVArgsForBAMViewing(
                        parameters[CANCER_STUDY_ID_INDEX],
                        parameters[CASE_ID_INDEX],
                        parameters[LOCUS_INDEX]
                    );
                    if (args != null && args.length == 4) {
                        igvArgs.put("bamFileUrl", args[0]);
                        igvArgs.put("encodedLocus", args[1]);
                        igvArgs.put("referenceGenome", args[2]);
                        igvArgs.put("trackName", args[3]);
                    }
                }
            }
            JSONValue.writeJSONString(igvArgs, out);
        } catch (Exception e) {
            JSONValue.writeJSONString(new JSONObject(), out);
        } finally {
            out.close();
        }
    }

    private String[] getParameters(HttpServletRequest request) {
        String[] parameters = new String[3];
        parameters[CANCER_STUDY_ID_INDEX] =
            request.getParameter(CANCER_STUDY_ID);
        parameters[CASE_ID_INDEX] = request.getParameter(CASE_ID);
        parameters[LOCUS_INDEX] = request.getParameter(LOCUS);

        return (
                parameters[CANCER_STUDY_ID_INDEX] == null ||
                parameters[CASE_ID_INDEX] == null ||
                parameters[LOCUS_INDEX] == null
            )
            ? null
            : parameters;
    }
}
