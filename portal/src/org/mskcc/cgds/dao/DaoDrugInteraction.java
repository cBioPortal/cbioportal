package org.mskcc.cgds.dao;

import org.apache.commons.lang.StringUtils;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.Drug;
import org.mskcc.cgds.model.DrugInteraction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DaoDrugInteraction {
    private static MySQLbulkLoader myMySQLbulkLoader = null;
    private static DaoDrugInteraction daoDrugInteraction;
    private static final String NA = "NA";

    private DaoDrugInteraction() {
    }

    public static DaoDrugInteraction getInstance() throws DaoException {
        if (daoDrugInteraction == null) {
            daoDrugInteraction = new DaoDrugInteraction();
        }

        if (myMySQLbulkLoader == null) {
            myMySQLbulkLoader = new MySQLbulkLoader("drug_interaction");
        }
        return daoDrugInteraction;
    }

    public int addDrugInteraction(Drug drug,
                                  CanonicalGene targetGene,
                                  String interactionType,
                                  String dataSource,
                                  String experimentTypes,
                                  String pmids) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        if (interactionType == null) {
            throw new IllegalArgumentException ("Drug interaction type cannot be null");
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
                myMySQLbulkLoader.insertRecord(
                        drug.getId(),
                        Long.toString(targetGene.getEntrezGeneId()),
                        interactionType,
                        dataSource,
                        experimentTypes,
                        pmids);

                return 1;
            } else {
                con = JdbcUtil.getDbConnection();
                pstmt = con.prepareStatement
                        ("INSERT INTO drug_interaction (`DRUG`,`TARGET`, `INTERACTION_TYPE`," +
                                "`DATA_SOURCE`, `EXPERIMENT_TYPES`, `PMIDS`)"
                                + "VALUES (?,?,?,?,?,?)");
                pstmt.setString(1, drug.getId());
                pstmt.setLong(2, targetGene.getEntrezGeneId());
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

    public ArrayList<DrugInteraction> getInteractions(long entrezGeneId) throws DaoException {
        return getInteractions(Collections.singleton(entrezGeneId));
    }

    public ArrayList<DrugInteraction> getInteractions(CanonicalGene gene) throws DaoException {
        return getInteractions(Collections.singleton(gene));
    }

    public ArrayList<DrugInteraction> getInteractions(Collection<?> genes) throws DaoException {
        ArrayList<DrugInteraction> interactionList = new ArrayList<DrugInteraction>();
        if (genes.isEmpty())
            return interactionList;

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection();
            Set<Long> entrezGeneIds = new HashSet<Long>();

            for (Object gene : genes) {
                if(gene instanceof CanonicalGene)
                    entrezGeneIds.add(((CanonicalGene) gene).getEntrezGeneId());
                else if(gene instanceof Long)
                    entrezGeneIds.add((Long) gene);
                else
                    entrezGeneIds.add(Long.parseLong(gene.toString()));
            }

            String idStr = "(" + StringUtils.join(entrezGeneIds, ",") + ")";

            pstmt = con.prepareStatement("SELECT * FROM drug_interaction WHERE TARGET IN " + idStr);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                DrugInteraction interaction = extractInteraction(rs);
                interactionList.add(interaction);
            }

            return interactionList;

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public ArrayList<DrugInteraction> getTargets(Drug drug) throws DaoException {
        return getTargets(Collections.singleton(drug));
    }

    public ArrayList<DrugInteraction> getTargets(Collection<Drug> drugs) throws DaoException {
        ArrayList<DrugInteraction> interactionList = new ArrayList<DrugInteraction>();
        if (drugs.isEmpty())
            return interactionList;

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection();
            Set<String> drugIDs = new HashSet<String>();
            for (Drug drug : drugs)
                drugIDs.add("'" + drug.getId() + "'");

            String idStr = "(" + StringUtils.join(drugIDs, ",") + ")";

            pstmt = con.prepareStatement("SELECT * FROM drug_interaction WHERE DRUG IN " + idStr);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                DrugInteraction interaction = extractInteraction(rs);
                interactionList.add(interaction);
            }

            return interactionList;

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }


    public ArrayList<DrugInteraction> getAllInteractions() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList<DrugInteraction> interactionList = new ArrayList <DrugInteraction>();

        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM drug_interaction");
            rs = pstmt.executeQuery();

            while (rs.next()) {
                DrugInteraction interaction = extractInteraction(rs);
                interactionList.add(interaction);
            }

            return interactionList;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    private DrugInteraction extractInteraction(ResultSet rs) throws SQLException {
        DrugInteraction interaction = new DrugInteraction();
        interaction.setDrug(rs.getString("DRUG"));
        interaction.setTargetGene(rs.getLong("TARGET"));
        interaction.setInteractionType(rs.getString("INTERACTION_TYPE"));
        interaction.setDataSource(rs.getString("DATA_SOURCE"));
        interaction.setExperimentTypes(rs.getString("EXPERIMENT_TYPES"));
        interaction.setPubMedIDs(rs.getString("PMIDS"));
        return interaction;
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
                    ("SELECT COUNT(*) FROM drug_interaction");
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

    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE drug_interaction");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public int flushToDatabase() throws DaoException {
        try {
            return myMySQLbulkLoader.loadDataFromTempFileIntoDBMS();
        } catch (IOException e) {
            System.err.println("Could not open temp file");
            e.printStackTrace();
            return -1;
        }
    }

}
