/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.GenePanelData;
import org.mskcc.cbio.portal.model.GenePanelListData;

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
 *
 * @author dongli
 */
public class DaoGenePanel {
    
      /**
     * Private Constructor to enforce Singleton Pattern.
     */
    private DaoGenePanel() {
    }
    
    private static int fakeEntrezId = -1;
    private static synchronized int getNextFakeEntrezId() throws DaoException {
//        while (getGene(fakeEntrezId)!=null) {
//            fakeEntrezId --;
//        }
        return fakeEntrezId;
    }
    
//    public static synchronized int addGeneWithoutEntrezGeneId(CanonicalGene gene) throws DaoException {
//        CanonicalGene existingGene = getGene(gene.getHugoGeneSymbolAllCaps());
//        gene.setEntrezGeneId(existingGene==null?getNextFakeEntrezId():existingGene.getEntrezGeneId());
//        return addGene(gene);
//    }

    /**
     * Adds a new Gene Record to the Database.
     *
     * @param gene Canonical Gene Object.
     * @return number of records successfully added.
     * @throws DaoException Database Error.
     */
    public static int addGenePanel(GenePanelData genePanel) throws DaoException {
        if (MySQLbulkLoader.isBulkLoad()) {
            String test = Long.toString(genePanel.getGenePanelListId());
            
            //  write to the temp file maintained by the MySQLbulkLoader
            MySQLbulkLoader.getMySQLbulkLoader("gene_panel").insertRecord(Long.toString(genePanel.getGenePanelListId()),
                    genePanel.getStableId(),genePanel.getCancerStudyId(),genePanel.getDiscription());
            // return 1 because normal insert will return 1 if no error occurs
            return 1;
        }
        else 
        {
//            Connection con = null;
//            PreparedStatement pstmt = null;
//            ResultSet rs = null;
//            try {
//                int rows = 0;
//                CanonicalGene existingGene = getGene(gene.getEntrezGeneId());
//                if (existingGene == null) {
//                    con = JdbcUtil.getDbConnection(DaoGenePanel.class);
//                    pstmt = con.prepareStatement
//                            ("INSERT INTO gene_panel (`LIST_ID`,`STABLE_ID`,`CANCER_STUDY_IDENTIFIER`,`DESCRIPTION`) "
//                                    + "VALUES (?,?,?,?)");
//                    pstmt.setLong(1, gene.getEntrezGeneId());
//                    pstmt.setString(2, gene.getHugoGeneSymbolAllCaps());
//                    pstmt.setString(3, gene.getType());
//                    pstmt.setString(4, gene.getCytoband());
//                    pstmt.setInt(5, gene.getLength());
//                    rows += pstmt.executeUpdate();
//
//                }
//
//                rows += addGeneAliases(gene);
//
//                return rows;
//            } catch (SQLException e) {
//                throw new DaoException(e);
//            } finally {
//                JdbcUtil.closeAll(DaoGenePanel.class, con, pstmt, rs);
//            }
            return 1;
        }
    }
    
        public static int addGenePanelList(GenePanelListData genePanellist) throws DaoException {
        if (MySQLbulkLoader.isBulkLoad()) {
            //  write to the temp file maintained by the MySQLbulkLoader
            MySQLbulkLoader.getMySQLbulkLoader("gene_panel_list").insertRecord(Long.toString(genePanellist.getGenePanelListId()),
                    Long.toString(genePanellist.getGeneId()));
            // return 1 because normal insert will return 1 if no error occurs
            return 1;
        }
        else 
        {
            return 1;
        }
    }
    
    public static int getGeneId(String GeneId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGene.class);
            pstmt = con.prepareStatement
                    ("SELECT ENTREZ_GENE_ID FROM gene WHERE HUGO_GENE_SYMBOL = ?");
            pstmt.setString(1, GeneId);
            rs = pstmt.executeQuery();
            Integer result=null; 
            if(rs.next())
            {
                result = rs.getInt(1);
            }

            return result;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    
}
    
        public static int getMaxListId() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGene.class);
            pstmt = con.prepareStatement
                    ("SELECT Max(LIST_ID) FROM gene_panel");
            rs = pstmt.executeQuery();
            Integer result=null; 
            if(rs.next())
            {
                result = rs.getInt(1);
            }
            if(result==null)
            {
                result = 0;
            }
            return result;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    
}
    public static void deleteAllGenePanelRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmtPanel = null;
        PreparedStatement pstmtPanelList = null;
        ResultSet rs = null;
        ResultSet rs1 = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGene.class);
            pstmtPanel = con.prepareStatement("TRUNCATE TABLE gene_panel");
            pstmtPanel.executeUpdate();
            pstmtPanelList = con.prepareStatement("TRUNCATE TABLE gene_panel_list");
            pstmtPanelList.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmtPanel, rs);
            JdbcUtil.closeAll(DaoGene.class, con, pstmtPanelList, rs1);
        }
    }
}