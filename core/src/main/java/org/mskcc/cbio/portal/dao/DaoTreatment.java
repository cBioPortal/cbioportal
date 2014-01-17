/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.Treatment;

/**
 *
 * @author jgao
 */
public final class DaoTreatment {
    private DaoTreatment() {
        throw new AssertionError("DaoTreatment should not be instanciated");
    }
    
    public static int addDatum(Treatment treatment) {
        if (!MySQLbulkLoader.isBulkLoad()) {
            throw new IllegalStateException("Only build load mode is allowed for importing treatment data");
        }
        
        MySQLbulkLoader.getMySQLbulkLoader("treatment").insertRecord(
                Long.toString(treatment.getTreatmentId()),
                Integer.toString(treatment.getCancerStudyId()),
                treatment.getCaseId(),
                Integer.toString(treatment.getStartDate()),
                Integer.toString(treatment.getStopDate()),
                treatment.getType(),
                treatment.getAgent(),
                Double.toString(treatment.getDose()),
                treatment.getUnit(),
                treatment.getSchedule()
                );
        return 1;
    }
}
