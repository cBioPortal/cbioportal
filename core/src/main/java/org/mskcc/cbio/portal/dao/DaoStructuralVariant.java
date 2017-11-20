/*
 * Copyright (c) 2017 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
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
import org.mskcc.cbio.portal.model.StructuralVariant;
import org.mskcc.cbio.portal.util.ProgressMonitor;

import java.sql.*;
import java.util.Set;

public class DaoStructuralVariant {

    private DaoStructuralVariant() {
    }
    /**
     * Adds a new Structural variant record to the database.
     * @param structuralVariant
     * @return number of records successfully added
     * @throws DaoException 
     */
    
    public static long getLargestInternalId() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement("SELECT MAX(`INTERNAL_ID`) FROM `structural_variant`");
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }
    
    public static void addStructuralVariantToBulkLoader(StructuralVariant structuralVariant) throws DaoException {
        
        // write to the temp file maintained by the MySQLbulkLoader
        MySQLbulkLoader.getMySQLbulkLoader("structural_variant").insertRecord(
           Integer.toString(structuralVariant.getGeneticProfileId()),
           Long.toString(structuralVariant.getInternalId()),
           Integer.toString(structuralVariant.getSampleIdInternal()),
           Long.toString(structuralVariant.getSite1EntrezGeneId()),
           structuralVariant.getSite1EnsemblTranscriptId(),
           Integer.toString(structuralVariant.getSite1Exon()),
           structuralVariant.getSite1Chromosome(),
           Integer.toString(structuralVariant.getSite1Position()),
           structuralVariant.getSite1Description(),
           Long.toString(structuralVariant.getSite2EntrezGeneId()),
           structuralVariant.getSite2EnsemblTranscriptId(),
           Integer.toString(structuralVariant.getSite2Exon()),
           structuralVariant.getSite2Chromosome(),
           Integer.toString(structuralVariant.getSite2Position()),
           structuralVariant.getSite2Description(),
           structuralVariant.getSite2EffectOnFrame(),
           structuralVariant.getDnaSupport(),
           structuralVariant.getRnaSupport(),
           Integer.toString(structuralVariant.getNormalReadCount()),
           Integer.toString(structuralVariant.getTumorReadCount()),
           Integer.toString(structuralVariant.getNormalVariantCount()),
           Integer.toString(structuralVariant.getTumorVariantCount()),
           Integer.toString(structuralVariant.getNormalPairedEndReadCount()),
           Integer.toString(structuralVariant.getTumorPairedEndReadCount()),
           Integer.toString(structuralVariant.getNormalSplitReadCount()),
           Integer.toString(structuralVariant.getTumorSplitReadCount()),
           structuralVariant.getAnnotation(),
           structuralVariant.getBreakpointType(),
           structuralVariant.getCenter(),
           structuralVariant.getConnectionType(),
           structuralVariant.getEventInfo(),
           structuralVariant.getVariantClass(),
           Integer.toString(structuralVariant.getLength()),
           structuralVariant.getComments(),
           structuralVariant.getExternalAnnotation());
    }
}

