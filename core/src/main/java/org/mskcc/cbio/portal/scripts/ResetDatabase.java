/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.*;

/**
 * Empty the database.
 *
 * @author Ethan Cerami
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class ResetDatabase {

    public static final int MAX_RESET_SIZE = 6;

    /**
     * Remove all records from any size database.
     * Whenever a new Dao* class is defined, must add its deleteAllRecords() method here.
     *
     * @throws DaoException
     */
    public static void resetAnySizeDatabase() throws DaoException {
        System.out.print("resetting all database.");
        DaoUser.deleteAllRecords();
        DaoUserAuthorities.deleteAllRecords();
        DaoTypeOfCancer.deleteAllRecords();
        DaoCancerStudy.deleteAllRecords();
        DaoMicroRna daoMicroRna = new DaoMicroRna();
        daoMicroRna.deleteAllRecords();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        daoGene.deleteAllRecords();
        DaoCaseProfile.deleteAllRecords();
        DaoGeneticAlteration daoGenetic = DaoGeneticAlteration.getInstance();
        daoGenetic.deleteAllRecords();
        DaoMicroRnaAlteration daoMicroRnaAlteration = DaoMicroRnaAlteration.getInstance();
        daoMicroRnaAlteration.deleteAllRecords();
        DaoMutSig.deleteAllRecords();
        DaoGeneticProfile.deleteAllRecords();
        DaoCaseList daoCaseList = new DaoCaseList();
        daoCaseList.deleteAllRecords();
        DaoClinicalData.deleteAllRecords();
        DaoCopyNumberSegmentFile.deleteAllRecords();
        DaoMutation.deleteAllRecords();
        DaoMutationFrequency daoMutationFrequency = new DaoMutationFrequency();
        daoMutationFrequency.deleteAllRecords();
        DaoGeneticProfileCases.deleteAllRecords();
        DaoInteraction daoInteraction = DaoInteraction.getInstance();
        daoInteraction.deleteAllRecords();
        DaoProteinArrayData.getInstance().deleteAllRecords();
        DaoProteinArrayInfo.getInstance().deleteAllRecords();
        DaoProteinArrayTarget.getInstance().deleteAllRecords();
        DaoDrug.getInstance().deleteAllRecords();
        DaoUniProtIdMapping.deleteAllRecords();
        DaoDrugInteraction.getInstance().deleteAllRecords();
        DaoSangerCensus daoSangerCensus = DaoSangerCensus.getInstance();
        daoSangerCensus.deleteAllRecords();
        DaoTextCache daoTextCache = new DaoTextCache();
        daoTextCache.deleteAllKeys();
        DaoClinicalTrial daoClinicalTrial = DaoClinicalTrial.getInstance();
        daoClinicalTrial.deleteAllRecords();
        System.out.println("all database reset.");
    }

    public static void resetDatabase() throws DaoException {

        // safety measure: don't reset a big database
        if (MAX_RESET_SIZE < DaoCancerStudy.getCount()) {
            throw new DaoException("Database has " + DaoCancerStudy.getCount() +
                    " studies, and we don't reset a database with more than " + MAX_RESET_SIZE + " records.");
        } else {
            resetAnySizeDatabase();
        }

    }

    public static void main(String[] args) throws DaoException {
        StatDatabase.statDb();
        ResetDatabase.resetDatabase();
        System.err.println("Database Cleared and Reset.");
    }
}
