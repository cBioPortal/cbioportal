/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.servlet;

import java.io.*;
import java.util.*;

import org.json.simple.JSONValue;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mskcc.cbio.portal.stats.FisherExact;

/**
 * Calculate the cumulative (one-tail) p-value out of fisher exact test
 * 
 * @param a a, b, c, d are the four cells in a 2x2 matrix
 * @param b
 * @param c
 * @param d
 * @return one-tailed P-value (right or left, whichever is smallest)
 * 
 */
public class CalcFisherExactTest extends HttpServlet  {

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

        String[] dataSets = httpServletRequest.getParameter("params").split(":");
        String result = "";
        for (String dataSet : dataSets) {
            if (!dataSet.isEmpty()) {
                int a = Integer.parseInt(dataSet.split(" ")[0]);
                int b = Integer.parseInt(dataSet.split(" ")[1]);
                int c = Integer.parseInt(dataSet.split(" ")[2]);
                int d = Integer.parseInt(dataSet.split(" ")[3]);    
                FisherExact fisher = new FisherExact(a + b + c + d);
                double pValue = fisher.getCumlativeP(a, b, c, d);
                result = result.concat(String.valueOf(pValue) + " ");                
            }
        }
        result = result.replaceAll("\\s+$", "");

        httpServletResponse.setContentType("text/html");
        PrintWriter out = httpServletResponse.getWriter();
        JSONValue.writeJSONString(result, out);
    }
}



