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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton utility class for creating a map from a configuration file
 * for more readable category names for clinical free form table parameters.
 * 
 * @author Selcuk Onur Sumer
 */
public class CategoryLabelReader
{
	/**
	 * Resource filename for the map content.
	 */
	public static final String CATEGORY_MAP_FILE = "/human_readable_categories.txt";
	
	/**
	 * Singleton instance.
	 */
	private static CategoryLabelReader instance = null;
	
	/**
	 * Map containing mapping for more human readable category text
	 */
	private Map<String, String> categoryLabelMap;
	
	public Map<String, String> getCategoryLabelMap()
	{
		return categoryLabelMap;
	}

	public static CategoryLabelReader getInstace() throws IOException
	{
		if (instance == null)
		{
			instance = new CategoryLabelReader();
		}
		
		return instance;
	}
	
	/**
	 * Constructor.
	 * 
	 * Creates a new map using the resource file.
	 * 
	 * @throws IOException	if an IO error occurs.
	 */
	private CategoryLabelReader() throws IOException
	{
		InputStream in = this.getClass().getResourceAsStream(CATEGORY_MAP_FILE);
		this.categoryLabelMap = CategoryLabelReader.readCategoryLabelMap(in);
		in.close();
	}
	
	/**
	 * Populates the category label map using the given input stream.
	 * 
	 * @param in			input stream for the file
	 * @return				mapping for category labels
	 * @throws IOException	if an IO error occurs
	 */
	public static Map<String, String> readCategoryLabelMap(InputStream in) throws IOException
	{
		Map<String, String> categoryLabelMap = new HashMap<String, String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		
		while ((line = reader.readLine()) != null)
		{
			String[] pair = line.split(":");
			
			// skip any line with incorrect formatting
			if (pair.length == 2)
			{
				categoryLabelMap.put(safeCategoryName(pair[0].trim()),
						pair[1].trim());
			}
		}
		
		return categoryLabelMap;
	}
	
	/**
     * Creates a safe string by replacing problematic characters (for
     * an HTML id) with an underscore for the given string.
     *  
     * @param name	parameter name
     * @return		modified string with replaced characters
     */
    public static String safeCategoryName(String name)
    {
    	return name.replaceAll("[ /#.,:;(){}\\]\\[\"\'\\\\]", "_");
    }
}
