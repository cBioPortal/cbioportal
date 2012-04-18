package org.mskcc.cgds.dao;

import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.MutSig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Lennart Bastian, Gideon Dresdner
 * DaoMutSig defines methods that interact with the CGDS database
 * getMutSig methods return MutSig objects. addMutSig takes a MutSig object and adds it to CGDS
 * getAllMutSig returns an arraylist
 *
 */
public class DaoMutSig {
    // use a MySQLbulkLoader instead of SQL "INSERT" statements to load data into table
    private static MySQLbulkLoader myMySQLbulkLoader = null;
    private static DaoMutSig daoMutSig;

    private DaoMutSig() {
    }

    //getInstance() is a static method which returns a new instance of daoMutSig. useful for calling non-static
    //methods such as getMutSig

    public static DaoMutSig getInstance() throws DaoException {
        if (daoMutSig == null) {
            daoMutSig = new DaoMutSig();
        }

        if (myMySQLbulkLoader == null) {
            myMySQLbulkLoader = new MySQLbulkLoader("mut_sig");
        }
        return daoMutSig;
    }

    /*
     * Adds a new MutSig Record to the Database.
     *
     * @param mutSig Mut Sig Object.
     * @return number of records successfully added.
     * @throws DaoException Database Error.
     */

    public static int addMutSig(MutSig mutSig) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        //System.err.print("Adding MutSig \n");
        CanonicalGene gene = mutSig.getCanonicalGene();
        MySQLbulkLoader.bulkLoadOff();
        try {
            if (MySQLbulkLoader.isBulkLoad()) {
                //  write to the temp file maintained by the MySQLbulkLoader
                myMySQLbulkLoader.insertRecord(Integer.toString(mutSig.getCancerType()),
                        Long.toString(gene.getEntrezGeneId()),
                        Integer.toString(mutSig.getRank()),
                        Integer.toString(mutSig.getNumBasesCovered()),
                        Integer.toString(mutSig.getNumMutations()),
                        mutSig.getpValue(),
                        mutSig.getqValue());

                // return 1 because normal insert will return 1 if no error occurs
                return 1;
            } else {
                if (mutSig != null) {
                    con = JdbcUtil.getDbConnection();

                    pstmt = con.prepareStatement
                            ("INSERT INTO mut_sig (`CANCER_STUDY_ID`," +
                                    "`ENTREZ_GENE_ID`, " +
                                    "`RANK`, " +
                                    "`NumBasesCovered`, " +
                                    "`NumMutations`, " +
                                    "`P_Value`, " +
                                    "`Q_Value`) "  +
                                    "VALUES (?,?,?,?,?,?,?)");

                    pstmt.setInt(1, mutSig.getCancerType());
                    pstmt.setLong(2,gene.getEntrezGeneId());
                    pstmt.setInt(3,mutSig.getRank());
                    pstmt.setInt(4,mutSig.getNumBasesCovered());
                    pstmt.setInt(5,mutSig.getNumMutations());
                    pstmt.setString(6, mutSig.getpValue());
                    pstmt.setString(7, mutSig.getqValue());
                    
                    int rows = pstmt.executeUpdate();
                    return rows;
                }

                else {
                    return 0;
                }
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
            return 0;
        }

    }

    public static MutSig getMutSig(String hugoGeneSymbol, int cancerStudy) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        //get a new DaoGene Object, and get the EntrezGeneID
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        CanonicalGene gene = daoGene.getGene(hugoGeneSymbol);

        if (gene == null) {
            System.err.print("This HugoGeneSymbol does not exist in Database: " + hugoGeneSymbol);
            return null;
        } else {
            Long entrezGeneID = gene.getEntrezGeneId();
            try {
                con = JdbcUtil.getDbConnection();
                pstmt = con.prepareStatement
                        ("SELECT * FROM mut_sig WHERE ENTREZ_GENE_ID = ? AND CANCER_STUDY_ID = ?");
                pstmt.setLong(1, entrezGeneID);
                pstmt.setInt(2, cancerStudy);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    MutSig mutSig = DaoMutSig.assignMutSig(gene, rs);
                    return mutSig;
                } else {
                    return null;
                }
            } catch (SQLException e) {
                throw new DaoException(e);
            } finally {
                JdbcUtil.closeAll(con, pstmt, rs);
            }
        }
    }

    public static MutSig getMutSig(Long entrezGeneID, int cancerStudy) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM mut_sig WHERE ENTREZ_GENE_ID = ? AND CANCER_STUDY_ID = ?");
            pstmt.setLong(1, entrezGeneID);
            pstmt.setInt(2, cancerStudy);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                //first go into gene database, and make a Canonical Gene Object with
                CanonicalGene gene = daoGene.getGene(rs.getLong("ENTREZ_GENE_ID"));
                MutSig mutSig = DaoMutSig.assignMutSig(gene, rs);
                return mutSig;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public ArrayList<MutSig> getAllMutSig(int cancerStudy) throws DaoException {
        ArrayList<MutSig> mutSigList = new ArrayList<MutSig>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM mut_sig WHERE CANCER_STUDY_ID = ?");
            pstmt.setInt(1, cancerStudy);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                CanonicalGene gene = daoGene.getGene(rs.getLong("ENTREZ_GENE_ID"));
                MutSig mutSig = DaoMutSig.assignMutSig(gene, rs);
                mutSigList.add(mutSig);
            }
            return mutSigList;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public ArrayList<MutSig> getAllMutSig(int cancerStudy, double qValueThreshold) throws DaoException {
        ArrayList<MutSig> mutSigList = new ArrayList<MutSig>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM mut_sig WHERE CANCER_STUDY_ID = ? AND Q_Value < ?");
            pstmt.setInt(1, cancerStudy);
            pstmt.setDouble(2,qValueThreshold);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                CanonicalGene gene = daoGene.getGene(rs.getLong("ENTREZ_GENE_ID"));
                MutSig mutSig = DaoMutSig.assignMutSig(gene, rs);
                mutSigList.add(mutSig);
            }
            return mutSigList;
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
            pstmt = con.prepareStatement("TRUNCATE TABLE mut_sig");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    private static MutSig assignMutSig(CanonicalGene gene, ResultSet rs)
            throws SQLException, DaoException {
//        MutSig mutSig = new MutSig(rs.getInt("CANCER_STUDY_ID"),gene, rs.getInt("RANK"),rs.getInt("BIG_N"),
//                rs.getInt("SMALL_N"), rs.getString("P_VALUE"), rs.getString("LESS_THAN_Q_VALUE"));
        
        MutSig mutSig = new MutSig(rs.getInt("CANCER_STUDY_ID"),
                gene,
                rs.getInt("RANK"),
                rs.getInt("NumBasesCovered"),
                rs.getInt("numMutations"),
                rs.getString("P_Value"),
                rs.getString("Q_Value"));

        return mutSig;
    }
}
