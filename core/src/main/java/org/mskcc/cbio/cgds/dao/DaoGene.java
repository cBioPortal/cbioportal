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

/**
 * Data Access Object to Gene Table.
 * For faster access, consider using DaoGeneOptimized.
 *
 * @author Ethan Cerami.
 */
final class DaoGene {

    /**
     * Private Constructor to enforce Singleton Pattern.
     */
    private DaoGene() {
    }
    
    private static int fakeEntrezId = -1;
    private static synchronized int getNextFakeEntrezId() throws DaoException {
        while (getGene(fakeEntrezId)!=null) {
            fakeEntrezId --;
        }
        return fakeEntrezId;
    }
    
    public static synchronized int addGeneWithoutEntrezGeneId(CanonicalGene gene) throws DaoException {
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
    public static int addGene(CanonicalGene gene) throws DaoException {
        if (MySQLbulkLoader.isBulkLoad()) {
            //  write to the temp file maintained by the MySQLbulkLoader
            MySQLbulkLoader.getMySQLbulkLoader("gene").insertRecord(Long.toString(gene.getEntrezGeneId()),
                    gene.getHugoGeneSymbolAllCaps(),gene.getType(),gene.getCytoband(),gene.getLength()==0?null:Integer.toString(gene.getLength()));
            addGeneAliases(gene);
            // return 1 because normal insert will return 1 if no error occurs
            return 1;
        } else {
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                int rows = 0;
                CanonicalGene existingGene = getGene(gene.getEntrezGeneId());
                if (existingGene == null) {
                    con = JdbcUtil.getDbConnection(DaoGene.class);
                    pstmt = con.prepareStatement
                            ("INSERT INTO gene (`ENTREZ_GENE_ID`,`HUGO_GENE_SYMBOL`,`TYPE`,`CYTOBAND`,`LENGTH`) "
                                    + "VALUES (?,?,?,?,?)");
                    pstmt.setLong(1, gene.getEntrezGeneId());
                    pstmt.setString(2, gene.getHugoGeneSymbolAllCaps());
                    pstmt.setString(3, gene.getType());
                    pstmt.setString(4, gene.getCytoband());
                    pstmt.setInt(5, gene.getLength());
                    rows += pstmt.executeUpdate();

                }

                rows += addGeneAliases(gene);

                return rows;
            } catch (SQLException e) {
                throw new DaoException(e);
            } finally {
                JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
            }
        }
    }
    
    /**
     * Add gene_alias records.
     * @param gene Canonical Gene Object.
     * @return number of records successfully added.
     * @throws DaoException Database Error.
     */
    public static int addGeneAliases(CanonicalGene gene)  throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (MySQLbulkLoader.isBulkLoad()) {
                //  write to the temp file maintained by the MySQLbulkLoader
                Set<String> aliases = gene.getAliases();
                for (String alias : aliases) {
                    MySQLbulkLoader.getMySQLbulkLoader("gene_alias").insertRecord(
                            Long.toString(gene.getEntrezGeneId()),
                            alias);

                }
                // return 1 because normal insert will return 1 if no error occurs
                return 1;
            } else {
                    con = JdbcUtil.getDbConnection(DaoGene.class);
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
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
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
    private static CanonicalGene getGene(long entrezGeneId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGene.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM gene WHERE ENTREZ_GENE_ID = ?");
            pstmt.setLong(1, entrezGeneId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractGene(rs);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }
    
    /**
     * Gets aliases for all genes.
     * @return map from entrez gene id to a set of aliases.
     * @throws DaoException Database Error.
     */
    private static Set<String> getAliases(long entrezGeneId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null, rs1 = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGene.class);
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
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }
    
    private static Map<Long,Set<String>> getAllAliases() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null, rs1 = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGene.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM gene_alias");
            rs = pstmt.executeQuery();
            Map<Long,Set<String>> map = new HashMap<Long,Set<String>>();
            while (rs.next()) {
                Long entrez = rs.getLong("ENTREZ_GENE_ID");
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
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }

    /**
     * Gets all Genes in the Database.
     *
     * @return ArrayList of Canonical Genes.
     * @throws DaoException Database Error.
     */
    public static ArrayList<CanonicalGene> getAllGenes() throws DaoException {
        Map<Long,Set<String>> mapAliases = getAllAliases();
        ArrayList<CanonicalGene> geneList = new ArrayList<CanonicalGene>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGene.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM gene");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                long entrezGeneId = rs.getInt("ENTREZ_GENE_ID");
                Set<String> aliases = mapAliases.get(entrezGeneId);
                CanonicalGene gene = new CanonicalGene(entrezGeneId,
                        rs.getString("HUGO_GENE_SYMBOL"), aliases);
                gene.setCytoband(rs.getString("CYTOBAND"));
                gene.setLength(rs.getInt("LENGTH"));
                gene.setType(rs.getString("TYPE"));
                geneList.add(gene);
            }
            return geneList;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
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
    private static CanonicalGene getGene(String hugoGeneSymbol) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGene.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM gene WHERE HUGO_GENE_SYMBOL = ?");
            pstmt.setString(1, hugoGeneSymbol);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractGene(rs);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }
    
    private static CanonicalGene extractGene(ResultSet rs) throws SQLException, DaoException {
        long entrezGeneId = rs.getInt("ENTREZ_GENE_ID");
            Set<String> aliases = getAliases(entrezGeneId);
            CanonicalGene gene = new CanonicalGene(entrezGeneId,
                    rs.getString("HUGO_GENE_SYMBOL"), aliases);
            gene.setCytoband(rs.getString("CYTOBAND"));
            gene.setLength(rs.getInt("LENGTH"));
            gene.setType(rs.getString("TYPE"));
            
            return gene;
    }

    /**
     * Gets the Number of Gene Records in the Database.
     *
     * @return number of gene records.
     * @throws DaoException Database Error.
     */
    public static int getCount() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGene.class);
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
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }
    
    /**
     * 
     * @param entrezGeneId 
     */
    public static void deleteGene(long entrezGeneId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGene.class);
            pstmt = con.prepareStatement("DELETE FROM gene WHERE ENTREZ_GENE_ID=?");
            pstmt.setLong(1, entrezGeneId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
        
        deleteGeneAlias(entrezGeneId);
    }
    
    /**
     * 
     * @param entrezGeneId 
     */
    public static void deleteGeneAlias(long entrezGeneId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGene.class);
            pstmt = con.prepareStatement("DELETE FROM gene_alias WHERE ENTREZ_GENE_ID=?");
            pstmt.setLong(1, entrezGeneId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }

    /**
     * Deletes all Gene Records in the Database.
     *
     * @throws DaoException Database Error.
     */
    public static void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGene.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE gene");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
        deleteAllAliasRecords();
    }
    
    private static void deleteAllAliasRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGene.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE gene_alias");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }
    
}