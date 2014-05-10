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

import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.MutSig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Lennart Bastian, Gideon Dresdner
 * DaoMutSig defines methods that interact with the CGDS database
 * getMutSig methods return MutSig objects. addMutSig takes a MutSig object and adds it to CGDS
 * getAllMutSig returns an arraylist
 *
 */

public class DaoMutSig {

    private DaoMutSig() {
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

        CanonicalGene gene = mutSig.getCanonicalGene();
        MySQLbulkLoader.bulkLoadOff();
        try {
            if (MySQLbulkLoader.isBulkLoad()) {
                //  write to the temp file maintained by the MySQLbulkLoader
                MySQLbulkLoader.getMySQLbulkLoader("mut_sig").insertRecord(Integer.toString(mutSig.getCancerType()),
                        Long.toString(gene.getEntrezGeneId()),
                        Integer.toString(mutSig.getRank()),
                        Integer.toString(mutSig.getNumBasesCovered()),
                        Integer.toString(mutSig.getNumMutations()),
                        Float.toString(mutSig.getpValue()),
                        Float.toString(mutSig.getqValue()));

                // return 1 because normal insert will return 1 if no error occurs
                return 1;
            } else {
                if (mutSig != null) {
                    con = JdbcUtil.getDbConnection(DaoMutSig.class);

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
                    pstmt.setFloat(6, mutSig.getpValue());
                    pstmt.setFloat(7, mutSig.getqValue());


                    return pstmt.executeUpdate();
                }

                else {
                    return 0;
                }
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutSig.class, con, pstmt, rs);
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
            throw new java.lang.IllegalArgumentException("This HugoGeneSymbol does not exist in Database: " + hugoGeneSymbol);
        } else {
            long entrezGeneID = gene.getEntrezGeneId();
            try {
                con = JdbcUtil.getDbConnection(DaoMutSig.class);
                pstmt = con.prepareStatement
                        ("SELECT * FROM mut_sig WHERE ENTREZ_GENE_ID = ? AND CANCER_STUDY_ID = ?");
                pstmt.setLong(1, entrezGeneID);
                pstmt.setInt(2, cancerStudy);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    return DaoMutSig.assignMutSig(gene, rs);
                } else {
                    return null;
                }
            } catch (SQLException e) {
                throw new DaoException(e);
            } finally {
                JdbcUtil.closeAll(DaoMutSig.class, con, pstmt, rs);
            }
        }
    }

    public static MutSig getMutSig(Long entrezGeneID, int cancerStudy) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        try {
            con = JdbcUtil.getDbConnection(DaoMutSig.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM mut_sig WHERE ENTREZ_GENE_ID = ? AND CANCER_STUDY_ID = ?");
            pstmt.setLong(1, entrezGeneID);
            pstmt.setInt(2, cancerStudy);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                //first go into gene database, and make a Canonical Gene Object with
                CanonicalGene gene = daoGene.getGene(rs.getLong("ENTREZ_GENE_ID"));
                return DaoMutSig.assignMutSig(gene, rs);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutSig.class, con, pstmt, rs);
        }
    }

    public static ArrayList<MutSig> getAllMutSig(int cancerStudy) throws DaoException {
        ArrayList<MutSig> mutSigList = new ArrayList<MutSig>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

        try {
            con = JdbcUtil.getDbConnection(DaoMutSig.class);
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
            JdbcUtil.closeAll(DaoMutSig.class, con, pstmt, rs);
        }
    }

    public static int countMutSig(int cancerStudy) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoMutSig.class);
            pstmt = con.prepareStatement
                    ("SELECT count(*) FROM mut_sig WHERE CANCER_STUDY_ID = ?");
            pstmt.setInt(1, cancerStudy);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutSig.class, con, pstmt, rs);
        }
    }

    public static ArrayList<MutSig> getAllMutSig(int cancerStudy, double qValueThreshold) throws DaoException {
        ArrayList<MutSig> mutSigList = new ArrayList<MutSig>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        try {
            con = JdbcUtil.getDbConnection(DaoMutSig.class);
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
            JdbcUtil.closeAll(DaoMutSig.class, con, pstmt, rs);
        }
    }

    public static void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutSig.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE mut_sig");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutSig.class, con, pstmt, rs);
        }
    }

    private static MutSig assignMutSig(CanonicalGene gene, ResultSet rs)
            throws SQLException, DaoException {

        return new MutSig(rs.getInt("CANCER_STUDY_ID"),
                gene,
                rs.getInt("RANK"),
                rs.getInt("NumBasesCovered"),
                rs.getInt("numMutations"),
                rs.getFloat("P_Value"),
                rs.getFloat("Q_Value"));
    }

    /**
     * asks the database whether or not there are mutsigs for a given cancer study
     * @param cancerStudy
     * @return true or false
     *
     */
    public static boolean hasMutsig(CancerStudy cancerStudy) throws DaoException {
        return countMutSig(cancerStudy.getInternalId()) == 0;
    }
}
