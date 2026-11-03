/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.MutSig;

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

        CanonicalGene gene = mutSig.getCanonicalGene();
        
        if (ClickHouseBulkLoader.isBulkLoad()) {
                //  write to the temp file maintained by the ClickHouseBulkLoader
                ClickHouseBulkLoader.getClickHouseBulkLoader("mut_sig").insertRecord(Integer.toString(mutSig.getCancerType()),
                        Long.toString(gene.getEntrezGeneId()),
                        Integer.toString(mutSig.getRank()),
                        Integer.toString(mutSig.getNumBasesCovered()),
                        Integer.toString(mutSig.getNumMutations()),
                        Float.toString(mutSig.getpValue()),
                        Float.toString(mutSig.getqValue()));

                // return 1 because normal insert will return 1 if no error occurs
                return 1;
            }
        
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (mutSig != null) {
                con = JdbcUtil.getDbConnection(DaoMutSig.class);

                pstmt = con.prepareStatement
                        ("INSERT INTO mut_sig (`cancer_study_id`," +
                                "`entrez_gene_id`, " +
                                "`rank`, " +
                                "`NumBasesCovered`, " +
                                "`NumMutations`, " +
                                "`p_value`, " +
                                "`q_value`) "  +
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
                        ("SELECT * FROM mut_sig WHERE entrez_gene_id = ? AND cancer_study_id = ?");
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
                    ("SELECT * FROM mut_sig WHERE entrez_gene_id = ? AND cancer_study_id = ?");
            pstmt.setLong(1, entrezGeneID);
            pstmt.setInt(2, cancerStudy);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                //first go into gene database, and make a Canonical Gene Object with
                CanonicalGene gene = daoGene.getGene(rs.getLong("entrez_gene_id"));
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
                    ("SELECT * FROM mut_sig WHERE cancer_study_id = ?");
            pstmt.setInt(1, cancerStudy);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                CanonicalGene gene = daoGene.getGene(rs.getLong("entrez_gene_id"));
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
                    ("SELECT count(*) FROM mut_sig WHERE cancer_study_id = ?");
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
                    ("SELECT * FROM mut_sig WHERE cancer_study_id = ? AND q_value < ?");
            pstmt.setInt(1, cancerStudy);
            pstmt.setDouble(2,qValueThreshold);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                CanonicalGene gene = daoGene.getGene(rs.getLong("entrez_gene_id"));
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

        return new MutSig(rs.getInt("cancer_study_id"),
                gene,
                rs.getInt("rank"),
                rs.getInt("NumBasesCovered"),
                rs.getInt("NumMutations"),
                rs.getFloat("p_value"),
                rs.getFloat("q_value"));
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
