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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.io.IOException;

/**
 * Data Access Object to Gene Table.
 * For faster access, consider using DaoGeneOptimized.
 *
 * @author Ethan Cerami.
 */
class DaoGene {
    // use a MySQLbulkLoader instead of SQL "INSERT" statements to load data into table
    private static MySQLbulkLoader geneMySQLbulkLoader = null;
    private static MySQLbulkLoader aliasMySQLbulkLoader = null;
    private static DaoGene daoGene;

    /**
     * Private Constructor to enforce Singleton Pattern.
     */
    private DaoGene() {
    }

    /**
     * Gets Global Singleton Instance.
     *
     * @return DaoGeneOptimized Singleton.
     * @throws DaoException Database Error.
     */
    public static DaoGene getInstance() throws DaoException {
        if (daoGene == null) {
            daoGene = new DaoGene();
        }

        if (geneMySQLbulkLoader == null) {
            geneMySQLbulkLoader = new MySQLbulkLoader("gene");
            aliasMySQLbulkLoader = new MySQLbulkLoader("gene_alias");
        }
        return daoGene;
    }
    
    private static int fakeEntrezId = -1;
    private synchronized int getNextFakeEntrezId() throws DaoException {
        while (getGene(fakeEntrezId)!=null) {
            fakeEntrezId --;
        }
        return fakeEntrezId;
    }
    
    public synchronized int addGeneWithoutEntrezGeneId(CanonicalGene gene) throws DaoException {
        CanonicalGene existingGene = getGene(gene.getHugoGeneSymbolAllCaps());
        gene.setEntrezGeneId(existingGene==null?getNextFakeEntrezId():existingGene.getEntrezGeneId());
        return addGene(gene);
    }

    /**
     * Adds a new Gene Record to the Database.
     *
     * @param gene Canonical Gene Object.
     * @return number of records successfully added.
     * @throws DaoException Database Error.
     */
    public int addGene(CanonicalGene gene) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (MySQLbulkLoader.isBulkLoad()) {
                //  write to the temp file maintained by the MySQLbulkLoader
                geneMySQLbulkLoader.insertRecord(Long.toString(gene.getEntrezGeneId()),
                        gene.getHugoGeneSymbolAllCaps());
                addGeneAliases(gene);
                // return 1 because normal insert will return 1 if no error occurs
                return 1;
            } else {
                int rows = 0;
                CanonicalGene existingGene = getGene(gene.getEntrezGeneId());
                if (existingGene == null) {
                    con = JdbcUtil.getDbConnection();
                    pstmt = con.prepareStatement
                            ("INSERT INTO gene (`ENTREZ_GENE_ID`,`HUGO_GENE_SYMBOL`) "
                                    + "VALUES (?,?)");
                    pstmt.setLong(1, gene.getEntrezGeneId());
                    pstmt.setString(2, gene.getHugoGeneSymbolAllCaps());
                    rows += pstmt.executeUpdate();
                    
                }
                    
                rows += addGeneAliases(gene);
                    
                return rows;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
    
    /**
     * Add gene_alias records.
     * @param gene Canonical Gene Object.
     * @return number of records successfully added.
     * @throws DaoException Database Error.
     */
    public int addGeneAliases(CanonicalGene gene)  throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (MySQLbulkLoader.isBulkLoad()) {
                //  write to the temp file maintained by the MySQLbulkLoader
                Set<String> aliases = gene.getAliases();
                for (String alias : aliases) {
                    aliasMySQLbulkLoader.insertRecord(
                            Long.toString(gene.getEntrezGeneId()),
                            alias);

                }
                // return 1 because normal insert will return 1 if no error occurs
                return 1;
            } else {
                    con = JdbcUtil.getDbConnection();
                    Set<String> aliases = gene.getAliases();
                    Set<String> existingAliases = getAliases(gene.getEntrezGeneId());
                    int rows = 0;
                    for (String alias : aliases) {
                        if (!existingAliases.contains(alias)) {
                            pstmt = con.prepareStatement("INSERT INTO gene_alias "
                                    + "(`ENTREZ_GENE_ID`,`GENE_ALIAS`) VALUES (?,?)");
                            pstmt.setLong(1, gene.getEntrezGeneId());
                            pstmt.setString(2, alias);
                            rows += pstmt.executeUpdate();
                        }
                    }
                    
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
    public int flushGenesToDatabase() throws DaoException {
        try {
            return geneMySQLbulkLoader.loadDataFromTempFileIntoDBMS()
                    + aliasMySQLbulkLoader.loadDataFromTempFileIntoDBMS();
        } catch (IOException e) {
            System.err.println("Could not open temp file");
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Gets the Gene with the Specified Entrez Gene ID.
     * For faster access, consider using DaoGeneOptimized.
     *
     * @param entrezGeneId Entrez Gene ID.
     * @return Canonical Gene Object.
     * @throws DaoException Database Error.
     */
    public CanonicalGene getGene(long entrezGeneId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM gene WHERE ENTREZ_GENE_ID = ?");
            pstmt.setLong(1, entrezGeneId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Set<String> aliases = getAliases(entrezGeneId);
                CanonicalGene gene = new CanonicalGene(entrezGeneId,
                        rs.getString("HUGO_GENE_SYMBOL"), aliases);
                return gene;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
    
    /**
     * Gets aliases for a gene.
     * @param entrezGeneId Entrez Gene ID.
     * @return a set of aliases.
     * @throws DaoException Database Error.
     */
    private Set<String> getAliases(long entrezGeneId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null, rs1 = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM gene_alias WHERE ENTREZ_GENE_ID = ?");
            pstmt.setLong(1, entrezGeneId);
            rs = pstmt.executeQuery();
            Set<String> aliases = new HashSet<String>();
            while (rs.next()) {
                aliases.add(rs.getString("GENE_ALIAS"));
            }
            return aliases;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
    
    /**
     * 
     * @return
     * @throws DaoException 
     */
    private Map<Long,Set<String>> getAliases()  throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null, rs1 = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM gene_alias");
            rs = pstmt.executeQuery();
            Map<Long,Set<String>> map = new HashMap<Long,Set<String>>();
            while (rs.next()) {
                long entrez = rs.getLong("ENTREZ_GENE_ID");
                Set<String> aliases = map.get(entrez);
                if (aliases==null) {
                    aliases = new HashSet<String>();
                    map.put(entrez, aliases);
                }
                aliases.add(rs.getString("GENE_ALIAS"));
            }
            return map;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets all Genes in the Database.
     *
     * @return ArrayList of Canonical Genes.
     * @throws DaoException Database Error.
     */
    public ArrayList<CanonicalGene> getAllGenes() throws DaoException {
        ArrayList<CanonicalGene> geneList = new ArrayList<CanonicalGene>();
        Map<Long,Set<String>> mapEntrezAliases = getAliases();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM gene");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                long entrezGeneId = rs.getLong("ENTREZ_GENE_ID");
                Set<String> aliases = mapEntrezAliases.get(entrezGeneId);
                CanonicalGene gene = new CanonicalGene(entrezGeneId,
                        rs.getString("HUGO_GENE_SYMBOL"), aliases);
                geneList.add(gene);
            }
            return geneList;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets the Gene with the Specified HUGO Gene Symbol.
     * For faster access, consider using DaoGeneOptimized.
     *
     * @param hugoGeneSymbol HUGO Gene Symbol.
     * @return Canonical Gene Object.
     * @throws DaoException Database Error.
     */
    public CanonicalGene getGene(String hugoGeneSymbol) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM gene WHERE HUGO_GENE_SYMBOL = ?");
            pstmt.setString(1, hugoGeneSymbol);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                long entrezGeneId = rs.getInt("ENTREZ_GENE_ID");
                Set<String> aliases = getAliases(entrezGeneId);
                CanonicalGene gene = new CanonicalGene(entrezGeneId,
                        rs.getString("HUGO_GENE_SYMBOL"), aliases);
                return gene;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets the Number of Gene Records in the Database.
     *
     * @return number of gene records.
     * @throws DaoException Database Error.
     */
    public int getCount() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT COUNT(*) FROM gene");
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
     * 
     * @param entrezGeneId 
     */
    public void deleteGene(long entrezGeneId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("DELETE FROM gene WHERE ENTREZ_GENE_ID=?");
            pstmt.setLong(1, entrezGeneId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
        
        deleteGeneAlias(entrezGeneId);
    }
    
    /**
     * 
     * @param entrezGeneId 
     */
    public void deleteGeneAlias(long entrezGeneId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("DELETE FROM gene_alias WHERE ENTREZ_GENE_ID=?");
            pstmt.setLong(1, entrezGeneId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Deletes all Gene Records in the Database.
     *
     * @throws DaoException Database Error.
     */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE gene");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
        deleteAllAliasRecords();
    }
    
    private void deleteAllAliasRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE gene_alias");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
    
}