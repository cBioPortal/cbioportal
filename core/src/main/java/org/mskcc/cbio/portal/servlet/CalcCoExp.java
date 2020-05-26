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

import java.io.*;
import java.util.*;
import java.lang.Math;

import org.json.simple.JSONObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.mskcc.cbio.portal.util.CoExpUtil;
import org.mskcc.cbio.portal.util.XssRequestWrapper;

/**
 * Calculate co-expression scores for two incoming arrays
 *
 * @param : geneXarr -- String of profile data for the x axis gene
 * @param : geneYarr -- String of profile data for the y axis gene
 * @return : Pearson and spearman co-expression scores
 * 
 */
public class CalcCoExp extends HttpServlet  {

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @throws ServletException
     */
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws ServletException, IOException {
        doPost(httpServletRequest, httpServletResponse);
    }

    /**
     * Handles the HTTP POST Request.
     *
     * @param httpServletRequest  HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @throws ServletException
     */
    protected void doPost(HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse) throws ServletException, IOException {

        String geneXArr = httpServletRequest.getParameter("gene_x");
        String geneYArr = httpServletRequest.getParameter("gene_y");

        String[] _geneXstrArr = geneXArr.split("\\s+");
        String[] _geneYstrArr = geneYArr.split("\\s+");

        //Convert strings to doubles
        double[] _geneXvalArr = new double[_geneXstrArr.length];
        double[] _geneYvalArr = new double[_geneYstrArr.length];
        for (int i = 0; i < _geneXstrArr.length; i++) {
            double _valX = Double.parseDouble(_geneXstrArr[i]);
            double _valY = Double.parseDouble(_geneYstrArr[i]);
            _geneXvalArr[i] = _valX;
            _geneYvalArr[i] = _valY;
        }
        
        //Calculate Scores
        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
        SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();
        double pearson = pearsonsCorrelation.correlation(_geneXvalArr, _geneYvalArr);
        double spearman = spearmansCorrelation.correlation(_geneXvalArr, _geneYvalArr);

        httpServletResponse.setContentType("text/html");
        PrintWriter out = httpServletResponse.getWriter();
        JSONValue.writeJSONString(pearson + " " + spearman, out);
    }
}



