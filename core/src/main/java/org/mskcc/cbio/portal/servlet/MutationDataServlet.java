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
			// generate list by processing possible valid patient list parameters
			ArrayList<String> targetPatientList = this.getPatientList(request);

			for (String profileId : geneticProfileList)
			{
				GeneticProfile profile = DaoGeneticProfile.getGeneticProfileByStableId(profileId);
                List<String> targetSampleList = StableIdUtil.getStableSampleIdsFromPatientIds(profile.getCancerStudyId(), targetPatientList);
				// add mutation data for each genetic profile
				data.addAll(mutationDataUtils.getMutationData(profileId,
					targetGeneList,
					targetSampleList));
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
	 * Generates a patient list by processing related request parameters,
	 * which are patientList, patientSetId and patientIdsKey. If none of these
	 * parameters are valid, then this method will return an empty list.
	 *
	 * @param request   servlet request containing parameters
	 * @return          a list of patients
	 * @throws DaoException
	 */
	protected ArrayList<String> getPatientList(HttpServletRequest request) throws DaoException
	{
		DaoPatientList daoPatientList = new DaoPatientList();

		String patientListStr = request.getParameter("caseList");
		String patientSetId = request.getParameter("caseSetId");
		String patientIdsKey = request.getParameter("caseIdsKey");

		ArrayList<String> patientList;

		// first check if patientSetId param provided
		if (patientSetId != null &&
		    patientSetId.length() != 0 &&
		    !patientSetId.equals("-1"))
		{
			patientList = new ArrayList<String>();

			// fetch a patient list for each patient set id
			// (this allows providing more than one patientSetId)
			for (String id : this.parseValues(patientSetId))
			{
				PatientList list = daoPatientList.getPatientListByStableId(id);

				if (list != null)
				{
					patientList.addAll(list.getPatientList());
				}
			}
		}
		// if there is no patientSetId, then check for patientIdsKey param
		else if(patientIdsKey != null &&
		        patientIdsKey.length() != 0)
		{
			patientList = new ArrayList<String>();

			// fetch a patient list for each patient ids key
			// (this allows providing more than one patientIdsKey)
			for (String key : this.parseValues(patientIdsKey))
			{
				patientList.addAll(this.parseValues(
					PatientSetUtil.getPatientIds(key)));
			}
		}
		else
		{
			// plain list of patients provided, just parse the values
			patientList = this.parseValues(patientListStr);
		}

		return patientList;
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
