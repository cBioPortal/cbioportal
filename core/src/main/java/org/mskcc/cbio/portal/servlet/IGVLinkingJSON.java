/** Copyright (c) 2013 Memorial Sloan-Kettering Cancer Center.
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

import org.json.simple.JSONObject;
import org.mskcc.cbio.portal.util.IGVLinking;

import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author benjamin
 */
public class IGVLinkingJSON extends HttpServlet {

    public static final String CANCER_STUDY_ID = "cancer_study_id";
    public static final String CASE_ID = "case_id";
	public static final String LOCUS = "locus"; // IGV locus string, e.g. chr1:000-200
    
	private static final int CANCER_STUDY_ID_INDEX = 0;
	private static final int CASE_ID_INDEX = 1;
	private static final int LOCUS_INDEX = 2;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
	{
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
	{
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
	{
		JSONObject igvArgs = new JSONObject();

		String[] parameters = getParameters(request);
		if (parameters != null) {
			String[] args = IGVLinking.getIGVArgsForBAMViewing(parameters[CANCER_STUDY_ID_INDEX],
															   parameters[CASE_ID_INDEX],
															   parameters[LOCUS_INDEX]);
			igvArgs.put("bamFileUrl", args[0]);
			igvArgs.put("encodedLocus", args[1]);
			igvArgs.put("referenceGenome", args[2]);
            igvArgs.put("trackName", args[3]);
		}

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            JSONValue.writeJSONString(igvArgs, out);
        } finally {            
            out.close();
        }
    }

	private String[] getParameters(HttpServletRequest request)
	{
        String[] parameters = new String[3];
		parameters[CANCER_STUDY_ID_INDEX] = request.getParameter(CANCER_STUDY_ID);
		parameters[CASE_ID_INDEX] = request.getParameter(CASE_ID);
		parameters[LOCUS_INDEX] = request.getParameter(LOCUS);

		return (parameters[CANCER_STUDY_ID_INDEX] == null ||
				parameters[CASE_ID_INDEX] == null ||
				parameters[LOCUS_INDEX] == null) ? null : parameters;
		
	}
}
