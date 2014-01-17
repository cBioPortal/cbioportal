/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.Diagnostic;

/**
 *
 * @author jgao
 */
public final class DaoDiagnostic {
    private DaoDiagnostic() {
        throw new AssertionError("DaoDiagnostic should not be instanciated");
    }
    
    public static int addDatum(Diagnostic diagnostic) {
        if (!MySQLbulkLoader.isBulkLoad()) {
            throw new IllegalStateException("Only buld load mode is allowed for importing diagnostic data");
        }
        
        MySQLbulkLoader.getMySQLbulkLoader("diagnostic").insertRecord(
                Long.toString(diagnostic.getDiagosticId()),
                Integer.toString(diagnostic.getCancerStudyId()),
                diagnostic.getCaseId(),
                Integer.toString(diagnostic.getDate()),
                diagnostic.getType(),
                diagnostic.getSide(),
                diagnostic.getTarget(),
                diagnostic.getResult(),
                diagnostic.getStatus()
                );
        return 1;
    }
}