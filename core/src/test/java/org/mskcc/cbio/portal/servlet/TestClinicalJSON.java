/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

import junit.framework.TestCase;
import org.mskcc.cbio.portal.model.ClinicalData;
import org.mskcc.cbio.portal.model.ClinicalAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Map;

public class TestClinicalJSON extends TestCase {

    public void testReflectToMap() {
        ClinicalJSON clinicalJSON = new ClinicalJSON();

        Map<String, String> map = clinicalJSON.reflectToMap( new ClinicalData(-1, "caseId", "attrId", "attrVal") );

        assertTrue( map.get("attr_id").equals("attrId") );
        assertTrue( map.get("attr_val").equals("attrVal") );
//        assertTrue( map.get("cancer_study_id").equals("-1") );
        assertTrue( map.get("case_id").equals("caseId") );

        map = clinicalJSON.reflectToMap(new ClinicalAttribute("attrId", "displayName", "description", "datatype", true));
        assertTrue( map.get("attr_id").equals("attrId") );
        assertTrue( map.get("display_name").equals("displayName") );
        assertTrue( map.get("description").equals("description") );
        assertTrue( map.get("datatype").equals("datatype") );

    }
}
