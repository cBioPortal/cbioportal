/*
 * Copyright (c) 2017 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.GenePanel;

/**
 *
 * @author heinsz
 */
public class DaoGenePanel {
    private static Map<String, GenePanel> genePanelMap = initMap();

    public static GenePanel getGenePanelByStableId(String stableId) {
        return genePanelMap.get(stableId);
    }

    private static Map<String, GenePanel> initMap() {
        Map<String, GenePanel> genePanelMap = null;
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
            pstmt = con.prepareStatement("SELECT * FROM gene_panel");
            rs = pstmt.executeQuery();
            genePanelMap = extractGenePanelMap(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
        }
        return genePanelMap;
    }

    private static Map<String, GenePanel> extractGenePanelMap(ResultSet rs)
        throws SQLException {
        Map<String, GenePanel> genePanelMap = new HashMap<>();
        while (rs.next()) {
            GenePanel gp = new GenePanel();
            gp.setInternalId(rs.getInt("INTERNAL_ID"));
            gp.setStableId(rs.getString("STABLE_ID"));
            gp.setDescription(rs.getString("DESCRIPTION"));
            genePanelMap.put(gp.getStableId(), gp);
        }
        return genePanelMap;
    }
}
