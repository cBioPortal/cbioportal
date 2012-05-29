package org.mskcc.portal.util;

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
	public static final String CATEGORY_MAP_FILE = "human_readable_categories.txt";
	
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
