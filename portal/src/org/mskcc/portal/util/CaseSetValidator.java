package org.mskcc.portal.util;

import java.util.ArrayList;
import java.util.List;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.CaseList;
import org.mskcc.portal.remote.GetCaseSets;

/**
 * Utility class for validation of the user-defined case sets.
 * 
 * @author Selcuk Onur Sumer
 */
public class CaseSetValidator
{
	/**
	 * Checks whether the provided case IDs are valid for a specific
	 * cancer study. This method returns a list of invalid cases if
	 * any, or returns an empty list if all the cases are valid.
	 * 
	 * @param studyId			stable cancer study id
	 * @param caseIds			case IDs as a single string
	 * @return					list of invalid cases
	 * @throws DaoException		if a DB error occurs
	 */
	public static List<String> validateCaseSet(String studyId,
			String caseIds) throws DaoException
	{
		ArrayList<String> invalidCases = new ArrayList<String>();
		
		// get list of all case sets for the given cancer study
		ArrayList<CaseList> caseLists = GetCaseSets.getCaseSets(studyId);
		
		if (!caseLists.isEmpty() &&
			caseIds != null)
		{
			// validate each case ID
			for(String caseId: caseIds.trim().split(" "))
			{
				boolean valid = false;
				
				// search all lists for the current case
				for (CaseList caseList: caseLists)
				{
					// if the case is found in any of the lists,
					// then it is valid, no need to search further
					if(caseList.getCaseList().contains(caseId))
					{
						valid = true;
						break;
					}
				}
				
				// if the case cannot be found in any of the lists,
				// then it is an invalid case for this cancer study
				if (!valid)
				{
					invalidCases.add(caseId);
				}
			}
		}
		
		return invalidCases;
	}
}
