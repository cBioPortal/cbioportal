package org.mskcc.cgds.dao;

// CREATED BY P. MANKOO: 27 JULY, 2009.

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.mskcc.cgds.model.Drug;
import org.mskcc.cgds.model.CanonicalGene;

public class DaoDrug {

    public int addDrug(CanonicalGene gene,
		       String drugType, long drugId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
		("INSERT INTO drug (" +
                 "`ENTREZ_GENE_ID`, " +
		 "`DRUG_TYPE`, " +
		 "`DRUG_ID`)" +
		 "VALUES (?,?,?)");
            pstmt.setLong(1, gene.getEntrezGeneId());
            pstmt.setString(2, drugType);
            pstmt.setLong(3, drugId);
            int rows = pstmt.executeUpdate();
            return rows;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public ArrayList<Drug> getDrugs(CanonicalGene gene) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList <Drug> drugLists = new ArrayList <Drug>();
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
		("SELECT * FROM drug WHERE ENTREZ_GENE_ID = ?");
            pstmt.setLong(1, gene.getEntrezGeneId());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Drug drug = extractDrug(rs);
                drugLists.add(drug);
            }
            return drugLists;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public ArrayList<Drug> getAllDrugs() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList <Drug> drugLists = new ArrayList <Drug>();
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM drug");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Drug drug = extractDrug(rs);
                drugLists.add(drug);
            }
            return drugLists;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public int getNumDrugs() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList <Drug> drugLists = new ArrayList <Drug>();
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT COUNT(*) FROM drug");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public int getCount () throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList <Drug> drugLists = new ArrayList <Drug>();
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT COUNT(*) FROM drug");
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

    public Drug getDrug(long drugId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Drug drug = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM drug WHERE DRUG_ID = ?");
            pstmt.setLong(1, drugId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                drug = extractDrug(rs);
            }
            return drug;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    private Drug extractDrug(ResultSet rs) throws DaoException, SQLException {
        Drug drug = new Drug();
        long entrezGene = rs.getLong("ENTREZ_GENE_ID");

        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        drug.setGene(daoGene.getGene(entrezGene));

        drug.setDrugId(rs.getLong("DRUG_ID"));
        drug.setDrugType(rs.getString("DRUG_TYPE"));
        return drug;
    }

    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE drug");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}

