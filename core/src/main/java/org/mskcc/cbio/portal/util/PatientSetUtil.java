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

package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.web_api.GetPatientLists;

import java.util.*;

/**
 * Utility class for the user-defined patient sets (patient ID list).
 * 
 * @author Selcuk Onur Sumer
 */
public class PatientSetUtil
{
	/**
	 * Checks whether the provided patient IDs are valid for a specific
	 * cancer study. This method returns a list of invalid patients if
	 * any, or returns an empty list if all the patients are valid.
	 * 
	 * @param studyId			stable cancer study id
	 * @param patientIds		patient IDs as a single string
	 * @return					list of invalid patients
	 * @throws DaoException		if a DB error occurs
	 */
	public static List<String> validatePatientSet(String studyId,
			String sampleIds) throws DaoException
	{
		ArrayList<String> invalidSample = new ArrayList<String>();
		
		// get cancer study for the given stable id
		CancerStudy study = DaoCancerStudy.getCancerStudyByStableId(studyId);
                int iStudyId = study.getInternalId();
				
		if (sampleIds != null)
		{
                    
			// validate each patient ID
			for(String sampleId: sampleIds.trim().split("\\s+"))
			{
				Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(iStudyId, sampleId);
				
				if (sample==null)
				{
					invalidSample.add(sampleId);
				}
                            }
				}
		
		return invalidSample;
	}
	
	/**
	 * Shortens the (possibly long) user-defined patient id list by hashing.
	 * Also adds the generated (key, text) pair to the database for
	 * future reference.
	 * 
	 * @param patientIds	patient ID string to be shortened
	 * @return			short (hashed) version of patient IDs
	 * @throws DaoException 
	 */
	public static String shortenPatientIds(String patientIds)
			throws DaoException
	{
		DaoTextCache dao = new DaoTextCache();
		
		// normalize patient IDs list to avoid redundant white spaces  
		String normalizedIds = normalizePatientIds(patientIds);
		
		// generate hash key for the patient IDs string
		String patientIdsKey = dao.generateKey(normalizedIds);
		
		// add new key and list pair to DB if record does not exist
		if (dao.getText(patientIdsKey) == null)
		{
			dao.cacheText(patientIdsKey, normalizedIds);
		}
		
		// return hash key
		return patientIdsKey;
	}
	
	/**
	 * Retrieves the patient ID list corresponding to the given key.
	 * 
	 * @param patientIdsKey	key for a specific patient id list
	 * @return				patient id list corresponding to the given key
	 * @throws DaoException 
	 */
	public static String getPatientIds(String patientIdsKey)
			throws DaoException
	{
		DaoTextCache dao = new DaoTextCache();
		
		String patientIds = dao.getText(patientIdsKey);
		
		return patientIds;
	}
	
	/**
	 * Normalize the given patient list by trimming and replacing any white space
	 * character with single space. This is to prevent same patient lists 
	 * to be interpreted as different lists just because of the different white
	 * space characters.
	 * 
	 * @param patientIds	list of patient ids to be normalized
	 * @return			normalized string with the same patient ids
	 */
	public static String normalizePatientIds(String patientIds)
	{
		return patientIds.trim().replaceAll("\\s", " ");
	}
}
