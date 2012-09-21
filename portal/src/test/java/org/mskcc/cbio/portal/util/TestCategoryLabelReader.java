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

package org.mskcc.cbio.portal.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import junit.framework.TestCase;

import org.mskcc.cbio.portal.util.CategoryLabelReader;

public class TestCategoryLabelReader extends TestCase
{
	 /**
     * Tests the utility method of the CategoryLabelReader class.
     *
     * @throws java.io.IOException IO Error.
     */
    public void testCategoryLabelReader() throws IOException
    {
		// TBD: change this to use getResourceAsStream()
        File file = new File("target/test-classes/test_readable_categories.txt");
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
