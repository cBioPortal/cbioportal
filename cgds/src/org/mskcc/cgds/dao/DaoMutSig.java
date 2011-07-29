package org.mskcc.cgds.dao;

/**
 * Created by IntelliJ IDEA.
 * User: lennartbastian
 * Date: 22/07/2011
 * Time: 10:55
 * To change this template use File | Settings | File Templates.
 */

import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.MutSig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DaoMutSig {
    // use a MySQLbulkLoader instead of SQL "INSERT" statements to load data into table
    private static MySQLbulkLoader myMySQLbulkLoader = null;
    private static DaoMutSig daoMutSig;

    private DaoMutSig() {
    }

    public static DaoMutSig getInstance() throws DaoException {
        if (daoMutSig == null) {
            daoMutSig = new DaoMutSig();
        }

        if (myMySQLbulkLoader == null) {
            myMySQLbulkLoader = new MySQLbulkLoader("gene");
        }
        return daoMutSig;
    }

    public static int addMutSig(MutSig mutSig) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        //System.err.print("Adding MutSig \n");
        CanonicalGene gene = mutSig.getCanonicalGene();
        try {
            if (MySQLbulkLoader.isBulkLoad()) {
                //  write to the temp file maintained by the MySQLbulkLoader
                myMySQLbulkLoader.insertRecord(Long.toString(gene.getEntrezGeneId()),
                        Integer.toString(mutSig.getRank()), Integer.toString(mutSig.getN()),
                        Integer.toString(mutSig.getn()), Integer.toString(mutSig.getnVal()),
                        Integer.toString(mutSig.getnVer()), Integer.toString(mutSig.getCpG()),
                        Integer.toString(mutSig.getCandG()), Integer.toString(mutSig.getAandT()),
                        Integer.toString(mutSig.getIndel()), mutSig.getpValue(), mutSig.getqValue());
                // return 1 because normal insert will return 1 if no error occurs
                return 1;
            } else {
                if (mutSig != null) {
                    con = JdbcUtil.getDbConnection();
                    pstmt = con.prepareStatement
                            ("INSERT INTO mut_sig (`CancerStudyID`,`Entrez_Gene_ID`, `rank`, `bigN`, `smallN`, `nVal`, `nVer`, `CpG`, `C+G`, `A+T`, " +
                                    "`Indel`, `p`, `q`) " + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
                    pstmt.setInt(1, mutSig.getCancerType());
                    pstmt.setLong(2, gene.getEntrezGeneId());
                    pstmt.setInt(3, mutSig.getRank());
                    pstmt.setInt(4, mutSig.getN());
                    pstmt.setInt(5, mutSig.getn());
                    pstmt.setInt(6, mutSig.getnVal());
                    pstmt.setInt(7, mutSig.getnVer());
                    pstmt.setInt(8, mutSig.getCpG());
                    pstmt.setInt(9, mutSig.getCandG());
                    pstmt.setInt(10, mutSig.getAandT());
                    pstmt.setInt(11, mutSig.getIndel());
                    pstmt.setString(12, mutSig.getpValue());
                    pstmt.setString(13, mutSig.getqValue());
                    int rows = pstmt.executeUpdate();
                    //System.err.println("Normal Load: " + rows);
                    return rows;
                } else {
                    return 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
            return 0;
        }

    }

    //getMutSig from a hugoGeneSymbol

    public static MutSig getMutSig(String hugoGeneSymbol) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        CanonicalGene gene = daoGene.getGene(hugoGeneSymbol);
        Long entrezGeneID = gene.getEntrezGeneId();
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM mut_sig WHERE ENTREZ_GENE_ID = ?");
            pstmt.setLong(1, entrezGeneID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                MutSig mutSig = new MutSig(rs.getInt("CancerStudyID"), gene, rs.getInt("rank"), rs.getInt("bigN"), rs.getInt("smallN"),
                        rs.getInt("nVal"), rs.getInt("nVer"), rs.getInt("CpG"), rs.getInt("C+G"), rs.getInt("A+T"),
                        rs.getInt("Indel"), rs.getString("p"), rs.getString("q"));
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

    //getMutSig from an entrezGeneID

    public static MutSig getMutSig(Long entrezGeneID) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        CanonicalGene gene = daoGene.getGene(entrezGeneID);
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM mut_sig WHERE ENTREZ_GENE_ID = ?");
            pstmt.setLong(1, entrezGeneID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                MutSig mutSig = new MutSig(rs.getInt("CancerStudyID"), gene, rs.getInt("rank"), rs.getInt("bigN"), rs.getInt("smallN"),
                        rs.getInt("nVal"), rs.getInt("nVer"), rs.getInt("CpG"), rs.getInt("C+G"), rs.getInt("A+T"),
                        rs.getInt("Indel"), rs.getString("p"), rs.getString("q"));
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

    //get all MutSigs in the Database

    public ArrayList<MutSig> getAllMutSig() throws DaoException {
        ArrayList<MutSig> mutSigList = new ArrayList<MutSig>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM mut_sig");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                CanonicalGene gene = daoGene.getGene(rs.getLong("ENTREZ_GENE_ID"));
                MutSig mutSig = new MutSig(rs.getInt("CancerStudyID"), gene, rs.getInt("rank"), rs.getInt("bigN"), rs.getInt("smallN"),
                        rs.getInt("nVal"), rs.getInt("nVer"), rs.getInt("CpG"), rs.getInt("C+G"), rs.getInt("A+T"),
                        rs.getInt("Indel"), rs.getString("p"), rs.getString("q"));
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


}
