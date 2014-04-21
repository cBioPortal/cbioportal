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

package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Interaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
     * @throws org.mskcc.cbio.portal.dao.DaoException Database Error.
     */
    public static DaoInteraction getInstance() throws DaoException {
        if (daoInteraction == null) {
            daoInteraction = new DaoInteraction();
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
                MySQLbulkLoader.getMySQLbulkLoader("interaction").insertRecord(Long.toString(geneA.getEntrezGeneId()),
                        Long.toString(geneB.getEntrezGeneId()), interactionType,
                        dataSource, experimentTypes, pmids);

                // return 1 because normal insert will return 1 if no error occurs
                return 1;
            } else {
                con = JdbcUtil.getDbConnection(DaoInteraction.class);
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
            JdbcUtil.closeAll(DaoInteraction.class, con, pstmt, rs);
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
        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(DaoInteraction.class);
            return getInteractions(Collections.singleton(gene.getEntrezGeneId()),
                    false, true, dataSources, con);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeConnection(DaoInteraction.class, con);
        }
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
        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(DaoInteraction.class);
            return getInteractions(Collections.singleton(gene.getEntrezGeneId()),
                    seedGeneOnly, includeEdgesAmongLinkerGenes, dataSources, con);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeConnection(DaoInteraction.class, con);
        }
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
        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(DaoInteraction.class);
            return getInteractions(entrezGeneIds, false, true, dataSources, con); 
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeConnection(DaoInteraction.class, con);
        }
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
        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(DaoInteraction.class);
            return getInteractions(entrezGeneIds, true, false, dataSources, con); 
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeConnection(DaoInteraction.class, con);
        }
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
        if (con == null) {
            throw new NullPointerException("Null SQL connection");
        }
        ArrayList <Interaction> interactionList = new ArrayList <Interaction>();
        if (entrezGeneIds.isEmpty()) {
            return interactionList;
        }
        PreparedStatement pstmt;
        ResultSet rs = null;
        try {
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
            JdbcUtil.closeAll(rs);
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
            con = JdbcUtil.getDbConnection(DaoInteraction.class);
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
            JdbcUtil.closeAll(DaoInteraction.class, con, pstmt, rs);
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
            con = JdbcUtil.getDbConnection(DaoInteraction.class);
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
            JdbcUtil.closeAll(DaoInteraction.class, con, pstmt, rs);
        }
    }

    /**
     * Gets the Number of Interaction Records in the Database.
     *
     * @return number of gene records.
     * @throws org.mskcc.cbio.portal.dao.DaoException Database Error.
     */
    public int getCount() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoInteraction.class);
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
            JdbcUtil.closeAll(DaoInteraction.class, con, pstmt, rs);
        }
    }

    /**
     * Deletes all Gene Records in the Database.
     *
     * @throws org.mskcc.cbio.portal.dao.DaoException Database Error.
     */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoInteraction.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE interaction");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoInteraction.class, con, pstmt, rs);
        }
    }
}