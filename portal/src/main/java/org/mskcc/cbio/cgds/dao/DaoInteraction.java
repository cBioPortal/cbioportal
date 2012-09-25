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
import org.mskcc.cbio.cgds.model.Interaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;

/**
 * Data Access Object to Interaction Table.
 *
 * @author Ethan Cerami.
 */
public class DaoInteraction {
    // use a MySQLbulkLoader instead of SQL "INSERT" statements to load data into table
    private static MySQLbulkLoader myMySQLbulkLoader = null;
    private static DaoInteraction daoInteraction;
    private static final String NA = "NA";

    /**
     * Private Constructor to enforce Singleton Pattern.
     */
    private DaoInteraction() {
    }

    /**
     * Gets Global Singleton Instance.
     *
     * @return DaoInteraction Singleton.
     * @throws org.mskcc.cgds.dao.DaoException Database Error.
     */
    public static DaoInteraction getInstance() throws DaoException {
        if (daoInteraction == null) {
            daoInteraction = new DaoInteraction();
        }

        if (myMySQLbulkLoader == null) {
            myMySQLbulkLoader = new MySQLbulkLoader("interaction");
        }
        return daoInteraction;
    }

    /**
     * Adds a new Interaction Record to the Database.
     *
     * @param geneA             Gene A.
     * @param geneB             Gene B.
     * @param interactionType   Interaction Type.
     * @param dataSource        Data Source.
     * @param experimentTypes   Experiment Types.
     * @param pmids             PubMed IDs.
     * @return number of records added.
     * @throws DaoException Database Error.
     */
    public int addInteraction(CanonicalGene geneA, CanonicalGene geneB,
            String interactionType, String dataSource, String experimentTypes, String pmids)
            throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        //  Basic Parameter Checking.  Some are required.  Some are not.
        if (interactionType == null) {
            throw new IllegalArgumentException ("Interaction type cannot be null");
        }
        if (dataSource == null) {
            throw new IllegalArgumentException ("Data Source cannot be null");
        }
        if (experimentTypes == null) {
            experimentTypes = NA;
        }
        if (pmids == null) {
            pmids = NA;
        }

        try {
            if (MySQLbulkLoader.isBulkLoad()) {
                //  write to the temp file maintained by the MySQLbulkLoader
                myMySQLbulkLoader.insertRecord(Long.toString(geneA.getEntrezGeneId()),
                        Long.toString(geneB.getEntrezGeneId()), interactionType,
                        dataSource, experimentTypes, pmids);

                // return 1 because normal insert will return 1 if no error occurs
                return 1;
            } else {
                con = JdbcUtil.getDbConnection();
                pstmt = con.prepareStatement
                        ("INSERT INTO interaction (`GENE_A`,`GENE_B`, `INTERACTION_TYPE`," +
                                "`DATA_SOURCE`, `EXPERIMENT_TYPES`, `PMIDS`)"
                                + "VALUES (?,?,?,?,?,?)");
                pstmt.setLong(1, geneA.getEntrezGeneId());
                pstmt.setLong(2, geneB.getEntrezGeneId());
                pstmt.setString(3, interactionType);
                pstmt.setString(4, dataSource);
                pstmt.setString(5, experimentTypes);
                pstmt.setString(6, pmids);
                return pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets all Interactions involving the Specified Gene and Interactions among
     * linker genes.
     * @param gene Gene
     * @return ArrayList of Interaction Objects.
     * @throws DaoException Database Error.
     */
    public ArrayList<Interaction> getInteractions (CanonicalGene gene)
        throws DaoException {
        return getInteractions(gene, null);
    }

    /**
     * Gets all Interactions involving the Specified Gene and Interactions among
     * linker genes from specific data sources.
     * @param gene Gene
     * @param dataSources data sources, if null, retrieve all
     * @return ArrayList of Interaction Objects.
     * @throws DaoException Database Error.
     */
    public ArrayList<Interaction> getInteractions (CanonicalGene gene,
            Collection<String> dataSources)
        throws DaoException {
        return getInteractions(Collections.singleton(gene.getEntrezGeneId()),
                false, true, dataSources, null);
    }

    /**
     * Gets all Interactions involving the Specified Gene.
     * @param gene Gene
     * @param seedGeneOnly if true, retrieve interactions among seed genes only
     * @param includeEdgesAmongLinkerGenes if true, retrieve edges between linker genes.
     * @return ArrayList of Interaction Objects.
     * @throws DaoException Database Error.
     */
    public ArrayList<Interaction> getInteractions (CanonicalGene gene, 
            boolean seedGeneOnly, boolean includeEdgesAmongLinkerGenes,
            Collection<String> dataSources)
        throws DaoException {
        return getInteractions(Collections.singleton(gene.getEntrezGeneId()),
                seedGeneOnly, includeEdgesAmongLinkerGenes, dataSources, null);
    }
    
    /**
     * Gets all Interactions involving the Specified Genes and Interactions among
     * linker genes.
     * @param entrezGeneIds Entrez Gene IDs.
     * @return ArrayList of Interaction Objects.
     * @throws DaoException Database Error.
     */
    public ArrayList<Interaction> getInteractions (Collection<Long> entrezGeneIds,
            Collection<String> dataSources) throws DaoException {
        return getInteractions(entrezGeneIds, false, true, dataSources, null);
    }
    
    /**
     * Gets all Interactions involving the Specified Genes.
     * @param entrezGeneIds Entrez Gene IDs.
     * @return ArrayList of Interaction Objects.
     * @throws DaoException Database Error.
     */
    public ArrayList<Interaction> getInteractionsAmongSeeds (Collection<Long> entrezGeneIds,
            Collection<String> dataSources)
        throws DaoException {
        return getInteractions(entrezGeneIds, true, false, dataSources, null);
    }

    /**
     * Gets all Interactions involving the Specified Genes.
     * @param entrezGeneIds Entrez Gene IDs.
     * @param seedGeneOnly if true, retrieve interactions among seed genes only
     * @param includeEdgesAmongLinkerGenes if true, retrieve edges between linker genes.
     * @return ArrayList of Interaction Objects.
     * @throws DaoException Database Error.
     */
    public ArrayList<Interaction> getInteractions (Collection<Long> entrezGeneIds,
            boolean seedGeneOnly, boolean includeEdgesAmongLinkerGenes, 
            Collection<String> dataSources, Connection con)
        throws DaoException {
        ArrayList <Interaction> interactionList = new ArrayList <Interaction>();
        if (entrezGeneIds.isEmpty()) {
            return interactionList;
        }
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
			if (con == null) {
				con = JdbcUtil.getDbConnection();
			}
            String idStr = "("+StringUtils.join(entrezGeneIds, ",")+")";
            String dsStr = dataSources==null?null:("('"+StringUtils.join(dataSources,"','")+"')");
            if (seedGeneOnly) {
                String sql = "SELECT * FROM interaction where GENE_A IN "
                        + idStr + " AND GENE_B IN "+idStr
                        + (dataSources==null?"":(" AND DATA_SOURCE IN "+dsStr));
                pstmt = con.prepareStatement(sql);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    Interaction interaction = extractInteraction(rs);
                    interactionList.add(interaction);
                }
                return interactionList;
            } else {
                String sql = "SELECT * FROM interaction where GENE_A IN "
                        + idStr + " OR GENE_B IN "+idStr
                        + (dataSources==null?"":(" AND DATA_SOURCE IN "+dsStr));
                pstmt = con.prepareStatement(sql);
                rs = pstmt.executeQuery();
                
                if (includeEdgesAmongLinkerGenes) {
                    HashSet<Long> allGenes = new HashSet<Long>();
                    while (rs.next()) {
                        allGenes.add(rs.getLong("GENE_A"));
                        allGenes.add(rs.getLong("GENE_B"));
                    }
                    return getInteractions(allGenes, true, true, dataSources, con);
                } else {
                    while (rs.next()) {
                        Interaction interaction = extractInteraction(rs);
                        interactionList.add(interaction);
                    }
                    return interactionList;
                }
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
     * @throws org.mskcc.cgds.dao.DaoException Database Error.
     */
    public int flushToDatabase() throws DaoException {
        try {
            return myMySQLbulkLoader.loadDataFromTempFileIntoDBMS();
        } catch (IOException e) {
            System.err.println("Could not open temp file");
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Gets all Interactions in the Database.
     *
     * @return ArrayList of Interaction Objects.
     * @throws DaoException Database Error.
     */
    public ArrayList<Interaction> getAllInteractions() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList <Interaction> interactionList = new ArrayList <Interaction>();
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM interaction");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Interaction interaction = extractInteraction(rs);
                interactionList.add(interaction);
            }
            return interactionList;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    private Interaction extractInteraction(ResultSet rs) throws SQLException {
        Interaction interaction = new Interaction();
        interaction.setGeneA(rs.getLong("GENE_A"));
        interaction.setGeneB(rs.getLong("GENE_B"));
        interaction.setInteractionType(rs.getString("INTERACTION_TYPE"));
        interaction.setSource(rs.getString("DATA_SOURCE"));
        interaction.setExperimentTypes(rs.getString("EXPERIMENT_TYPES"));
        interaction.setPmids(rs.getString("PMIDS"));
        return interaction;
    }
    
    public ArrayList<String> getDataSources() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList <String> interactionList = new ArrayList <String>();
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT DISTINCT DATA_SOURCE FROM interaction");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                interactionList.add(rs.getString(1));
            }
            return interactionList;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets the Number of Interaction Records in the Database.
     *
     * @return number of gene records.
     * @throws org.mskcc.cgds.dao.DaoException Database Error.
     */
    public int getCount() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT COUNT(*) FROM interaction");
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
     * Deletes all Gene Records in the Database.
     *
     * @throws org.mskcc.cgds.dao.DaoException Database Error.
     */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE interaction");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}
