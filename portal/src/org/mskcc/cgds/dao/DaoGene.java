package org.mskcc.cgds.dao;

import org.mskcc.cgds.model.CanonicalGene;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.io.IOException;

/**
 * Data Access Object to Gene Table.
 * For faster access, consider using DaoGeneOptimized.
 *
 * @author Ethan Cerami.
 */
class DaoGene {
    // use a MySQLbulkLoader instead of SQL "INSERT" statements to load data into table
    private static MySQLbulkLoader myMySQLbulkLoader = null;
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

        if (myMySQLbulkLoader == null) {
            myMySQLbulkLoader = new MySQLbulkLoader("gene");
        }
        return daoGene;
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
                myMySQLbulkLoader.insertRecord(Long.toString(gene.getEntrezGeneId()),
                        gene.getHugoGeneSymbol());
                // return 1 because normal insert will return 1 if no error occurs
                return 1;
            } else {
                CanonicalGene existingGene = getGene(gene.getEntrezGeneId());
                if (existingGene == null) {
                    con = JdbcUtil.getDbConnection();
                    pstmt = con.prepareStatement
                            ("INSERT INTO gene (`ENTREZ_GENE_ID`,`HUGO_GENE_SYMBOL`) "
                                    + "VALUES (?,?)");
                    pstmt.setLong(1, gene.getEntrezGeneId());
                    pstmt.setString(2, gene.getHugoGeneSymbol());
                    int rows = pstmt.executeUpdate();
                    return rows;
                } else {
                    return 0;
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
     * @throws DaoException Database Error.
     */
    public int flushGenesToDatabase() throws DaoException {
        try {
            return myMySQLbulkLoader.loadDataFromTempFileIntoDBMS();
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
                CanonicalGene gene = new CanonicalGene(entrezGeneId,
                        rs.getString("HUGO_GENE_SYMBOL"));
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
     * Gets all Genes in the Database.
     *
     * @return ArrayList of Canonical Genes.
     * @throws DaoException Database Error.
     */
    public ArrayList<CanonicalGene> getAllGenes() throws DaoException {
        ArrayList<CanonicalGene> geneList = new ArrayList<CanonicalGene>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM gene");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                CanonicalGene gene = new CanonicalGene(rs.getLong("ENTREZ_GENE_ID"),
                        rs.getString("HUGO_GENE_SYMBOL"));
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
                return new CanonicalGene(rs.getInt("ENTREZ_GENE_ID"), hugoGeneSymbol);
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
    }
}