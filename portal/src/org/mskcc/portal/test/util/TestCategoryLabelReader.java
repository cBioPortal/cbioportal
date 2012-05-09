package org.mskcc.portal.test.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import junit.framework.TestCase;

import org.mskcc.portal.util.CategoryLabelReader;

public class TestCategoryLabelReader extends TestCase
{
	 /**
     * Tests the utility method of the CategoryLabelReader class.
     *
     * @throws java.io.IOException IO Error.
     */
    public void testCategoryLabelReader() throws IOException
    {
        File file = new File("test_data/test_readable_categories.txt");
        FileInputStream fin = new FileInputStream (file);

        Map<String, String> labelMap = CategoryLabelReader.readCategoryLabelMap(fin);
        
        // verify the size of the map
        assertEquals(8, labelMap.size());

        // verify some of the key,value pairs
        
        assertEquals (labelMap.get("TUMORSTAGE"),
        		"Tumor Stage");
        
        assertEquals (labelMap.get("ProgressionFreeStatus"),
        		"Progression Free Status");
    }
}
