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

package org.mskcc.cbio.cgds.dao;

import org.mskcc.cbio.cgds.model.CanonicalGene;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Data Access Object for the Genetic Alteration Table.
 *
 * @author Ethan Cerami.
 */
public class DaoGeneticAlteration {
    private static final String DELIM = ",";
    public static final String NAN = "NaN";
    private static DaoGeneticAlteration daoGeneticAlteration = null;

    // use a MySQLbulkLoader instead of SQL "INSERT" statements to load data into table
    private static MySQLbulkLoader myMySQLbulkLoader = null;

    /**
     * Private Constructor (Singleton pattern).
     */
    private DaoGeneticAlteration() {
    }

    /**
     * Gets Instance of Dao Object. (Singleton pattern).
     *
     * @return DaoGeneticAlteration Object.
     * @throws DaoException Dao Initialization Error.
     */
    public static DaoGeneticAlteration getInstance() throws DaoException {
        if (daoGeneticAlteration == null) {
            daoGeneticAlteration = new DaoGeneticAlteration();
            
        }

        // unfortunately, since this is a singleton and MySQLbulkLoader.isBulkLoad()
        // can be turned on and off at any time, we must either create the MySQLbulkLoader
        // at the beginning, like this, or test whether we have it and
        // create it if needed in every method. For simplicity, I choose the former.
        // TODO: would be best to make DaoGeneticAlteration dynamic, make whether it is bulkloaded
        // a property of the DAO, not change the property during
        // the DAO's lifetime; beyond scope of current project
        if( myMySQLbulkLoader == null ){
            myMySQLbulkLoader = new MySQLbulkLoader( "genetic_alteration" );
        }
        return daoGeneticAlteration;
    }

    /**
     * Adds a Row of Genetic Alterations associated with a Genetic Profile ID and Entrez Gene ID.
     * @param geneticProfileId Genetic Profile ID.
     * @param entrezGeneId Entrez Gene ID.
     * @param values DELIM separated values.
     * @return number of rows successfully added.
     * @throws DaoException Database Error.
     */
    public int addGeneticAlterations(int geneticProfileId, long entrezGeneId, String[] values)
            throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        StringBuffer valueBuffer = new StringBuffer();
        for (String value:  values) {
            if (value.contains(DELIM)) {
                throw new IllegalArgumentException ("Value cannot contain delim:  " + DELIM
                    + " --> " + value);
            }
            valueBuffer.append (value + DELIM);
        }

        try {
           if (MySQLbulkLoader.isBulkLoad() ) {
              //  write to the temp file maintained by the MySQLbulkLoader
              myMySQLbulkLoader.insertRecord(Integer.toString( geneticProfileId ),
                      Long.toString( entrezGeneId ), valueBuffer.toString());
              // return 1 because normal insert will return 1 if no error occurs
              return 1;
           } else {
                con = JdbcUtil.getDbConnection();
                pstmt = con.prepareStatement
                        ("INSERT INTO genetic_alteration (`GENETIC_PROFILE_ID`, " +
                                " `ENTREZ_GENE_ID`," +
                                " `VALUES`) "
                                + "VALUES (?,?,?)");
                pstmt.setInt(1, geneticProfileId);
                pstmt.setLong(2, entrezGeneId);
                pstmt.setString(3, valueBuffer.toString());
                int rows = pstmt.executeUpdate();
                return rows;
           }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
    
    /**
     * Loads the temp file maintained by the MySQLbulkLoader into the DMBS.
     * 
     * @return number of records inserted
     * @throws DaoException Database Error.
     */
    public int flushGeneticAlteration() throws DaoException {
        try {
            return myMySQLbulkLoader.loadDataFromTempFileIntoDBMS();
        } catch (IOException e) {
            System.err.println("Could not open temp file");
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Gets the Specified Genetic Alteration.
     *
     * @param geneticProfileId  Genetic Profile ID.
     * @param caseId            Case ID.
     * @param entrezGeneId      Entrez Gene ID.
     * @return value or NAN.
     * @throws DaoException Database Error.
     */
    public String getGeneticAlteration(int geneticProfileId, String caseId,
            long entrezGeneId) throws DaoException {
        HashMap <String, String> caseMap = getGeneticAlterationMap (geneticProfileId, entrezGeneId);
        if (caseMap.containsKey(caseId)) {
            return caseMap.get(caseId);
        } else {
            return NAN;
        }
    }

    /**
     * Gets a HashMap of Values, keyed by Case ID.
     * @param geneticProfileId  Genetic Profile ID.
     * @param entrezGeneId      Entrez Gene ID.
     * @return HashMap of values, keyed by Case ID.
     * @throws DaoException Database Error.
     */
    public HashMap<String, String> getGeneticAlterationMap(int geneticProfileId,
            long entrezGeneId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        HashMap<String, String> map = new HashMap<String, String>();

        DaoGeneticProfileCases daoGeneticProfileCases = new DaoGeneticProfileCases();
        ArrayList<String> orderedCaseList = daoGeneticProfileCases.getOrderedCaseList
                (geneticProfileId);
        if (orderedCaseList == null || orderedCaseList.size() ==0) {
            throw new IllegalArgumentException ("Could not find any cases for genetic" +
                    " profile ID:  " + geneticProfileId);
        }
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM genetic_alteration WHERE" +
                            " ENTREZ_GENE_ID = ? AND GENETIC_PROFILE_ID = ?");
            pstmt.setLong(1, entrezGeneId);
            pstmt.setInt(2, geneticProfileId);


            rs = pstmt.executeQuery();
            if  (rs.next()) {
                String values = rs.getString("VALUES");
                String valueParts[] = values.split(DELIM);
                for (int i=0; i<valueParts.length; i++) {
                    String value = valueParts[i];
                    String caseId = orderedCaseList.get(i);
                    map.put(caseId, value);
                }
            }
            return map;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets all Genes in a Specific Genetic Profile.
     * @param geneticProfileId  Genetic Profile ID.
     * @return Set of Canonical Genes.
     * @throws DaoException Database Error.
     */
    public Set<CanonicalGene> getGenesInProfile(int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Set <CanonicalGene> geneList = new HashSet <CanonicalGene>();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM genetic_alteration WHERE GENETIC_PROFILE_ID = ?");
            pstmt.setInt(1, geneticProfileId);

            rs = pstmt.executeQuery();
            while  (rs.next()) {
                Long entrezGeneId = rs.getLong("ENTREZ_GENE_ID");
                geneList.add(daoGene.getGene(entrezGeneId));
            }
            return geneList;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets total number of records in table.
     * @return number of records.
     * @throws DaoException Database Error.
     */
    public int getCount() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT COUNT(*) FROM genetic_alteration");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Deletes all Genetic Alteration Records associated with the specified Genetic Profile ID.
     *
     * @param geneticProfileId Genetic Profile ID.
     * @throws DaoException Database Error.
     */
    public void deleteAllRecordsInGeneticProfile(long geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("DELETE from " +
                    "genetic_alteration WHERE GENETIC_PROFILE_ID=?");
            pstmt.setLong(1, geneticProfileId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Deletes all Records in Table.
     *
     * @throws DaoException Database Error.
     */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE genetic_alteration");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}
