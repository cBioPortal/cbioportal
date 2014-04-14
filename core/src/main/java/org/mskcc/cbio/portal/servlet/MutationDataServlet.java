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

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * A servlet designed to return a JSON array of mutation objects.
 *
 * @author Selcuk Onur Sumer
 */
public class MutationDataServlet extends HttpServlet
{
	private static final Logger logger = Logger.getLogger(MutationDataServlet.class);

    private MutationDataUtils mutationDataUtils = new MutationDataUtils();

    public MutationDataUtils getMutationDataUtils() {
        return mutationDataUtils;
    }

    public void setMutationDataUtils(MutationDataUtils mutationDataUtils) {
        this.mutationDataUtils = mutationDataUtils;
    }

    protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		this.doPost(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		// get request parameters
		String geneticProfiles = request.getParameter("geneticProfiles");
		String genes = request.getParameter("geneList");
		// we need slashes for miRNA input
		genes = genes.replaceAll("\\\\/", "/");

		// parse single strings to create list of strings
		ArrayList<String> geneticProfileList = this.parseValues(geneticProfiles);
		ArrayList<String> targetGeneList = this.parseValues(genes);

		// final array to be sent
		JSONArray data = new JSONArray();

		try
		{
			// generate list by processing possible valid case list parameters
			ArrayList<String> targetCaseList = this.getCaseList(request);

			for (String profileId : geneticProfileList)
			{
				// add mutation data for each genetic profile
				data.addAll(mutationDataUtils.getMutationData(profileId,
					targetGeneList,
					targetCaseList));
			}
		}
		catch (DaoException e)
		{
			e.printStackTrace();
		}

		response.setContentType("application/json");
		PrintWriter out = response.getWriter();

		try
		{
			JSONValue.writeJSONString(data, out);
		}
		finally
		{
			out.close();
		}
	}

	/**
	 * Generates a case list by processing related request parameters,
	 * which are caseList, caseSetId and caseIdsKey. If none of these
	 * parameters are valid, then this method will return an empty list.
	 *
	 * @param request   servlet request containing parameters
	 * @return          a list of cases
	 * @throws DaoException
	 */
	protected ArrayList<String> getCaseList(HttpServletRequest request) throws DaoException
	{
		DaoCaseList daoCaseList = new DaoCaseList();

		String caseListStr = request.getParameter("caseList");
		String caseSetId = request.getParameter("caseSetId");
		String caseIdsKey = request.getParameter("caseIdsKey");

		ArrayList<String> caseList;

		// first check if caseSetId param provided
		if (caseSetId != null &&
		    caseSetId.length() != 0 &&
		    !caseSetId.equals("-1"))
		{
			caseList = new ArrayList<String>();

			// fetch a case list for each case set id
			// (this allows providing more than one caseSetId)
			for (String id : this.parseValues(caseSetId))
			{
				CaseList list = daoCaseList.getCaseListByStableId(id);

				if (list != null)
				{
					caseList.addAll(list.getCaseList());
				}
			}
		}
		// if there is no caseSetId, then check for caseIdsKey param
		else if(caseIdsKey != null &&
		        caseIdsKey.length() != 0)
		{
			caseList = new ArrayList<String>();

			// fetch a case list for each case ids key
			// (this allows providing more than one caseIdsKey)
			for (String key : this.parseValues(caseIdsKey))
			{
				caseList.addAll(this.parseValues(
					CaseSetUtil.getCaseIds(key)));
			}
		}
		else
		{
			// plain list of cases provided, just parse the values
			caseList = this.parseValues(caseListStr);
		}

		return caseList;
	}

	/**
	 * Parses string values separated by white spaces or commas.
	 *
	 * @param values    string to be parsed
	 * @return          array list of parsed string values
	 */
	protected ArrayList<String> parseValues(String values)
	{
		if (values == null)
		{
			// return an empty list for null values
			return new ArrayList<String>(0);
		}

		// split by white space
		String[] parts = values.split("[\\s,]+");

		return new ArrayList<String>(Arrays.asList(parts));
	}

}
