/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.DaoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.mskcc.cbio.portal.dao.JdbcUtil;

/**
 *
 * @author heinsz
 */
public  class TestGenePanelUtil {
    
    public static boolean getGenePanel(int geneticProfileId, String genePanel) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(TestGenePanelUtil.class);
            pstmt = con.prepareStatement(
                    "SELECT count(*) FROM gene_panel_profile_map " +
                    "WHERE gene_panel_profile_map.PROFILE_ID = ? AND gene_panel_profile_map.PANEL_ID  = ?");
            pstmt.setInt(1, geneticProfileId);
            pstmt.setString(2, genePanel);
            rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(TestGenePanelUtil.class, con, pstmt, rs);
        }
    }
}
