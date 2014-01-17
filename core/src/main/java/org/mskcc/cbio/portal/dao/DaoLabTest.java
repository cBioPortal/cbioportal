/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.LabTest;

/**
 *
 * @author jgao
 */
public final class DaoLabTest {
    private DaoLabTest() {
        throw new AssertionError("DaoLabTest should not be instanciated");
    }
    
    public static int addDatum(LabTest labTest) {
        if (!MySQLbulkLoader.isBulkLoad()) {
            throw new IllegalStateException("Only buld load mode is allowed for importing lab test data");
        }
        
        MySQLbulkLoader.getMySQLbulkLoader("lab_test").insertRecord(
                Long.toString(labTest.getLabTestId()),
                Integer.toString(labTest.getCancerStudyId()),
                labTest.getCaseId(),
                Integer.toString(labTest.getDate()),
                labTest.getTest(),
                Double.toString(labTest.getResult()),
                labTest.getUnit(),
                labTest.getNormalRange(),
                labTest.getNotes()
                );
        return 1;
    }
}
