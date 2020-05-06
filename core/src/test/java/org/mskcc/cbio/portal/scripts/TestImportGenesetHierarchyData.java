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

/*
 * @author Sander Tan
*/

package org.mskcc.cbio.portal.scripts;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneset;
import org.mskcc.cbio.portal.dao.DaoGenesetHierarchyNode;
import org.mskcc.cbio.portal.dao.DaoGenesetHierarchyLeaf;
import org.mskcc.cbio.portal.dao.JdbcUtil;
import org.mskcc.cbio.portal.model.Geneset;
import org.mskcc.cbio.portal.model.GenesetHierarchy;
import org.mskcc.cbio.portal.model.GenesetHierarchyLeaf;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/*
 * JUnit tests for ImportGenesetData class.
*/

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestImportGenesetHierarchyData {

	@Test
    public void testImportGenesetHierarchyData() throws Exception {
        ProgressMonitor.setConsoleMode(false);
        
        File file = new File("src/test/resources/genesets/unit-test-geneset-hierarchy_genesets.gmt");

        boolean updateInfo = false;
        boolean newVersion = true;
        int skippedGenes = ImportGenesetData.importData(file, updateInfo, newVersion);
        
        file = new File("src/test/resources/genesets/unit-test-geneset-hierarchy_tree.yaml");
        boolean validate = false;
        ImportGenesetHierarchy.importData(file, validate);

        // Test database entries
        
        // Get geneset id
        Geneset geneset = DaoGeneset.getGenesetByExternalId("UNITTEST_GENESET8");
        
        // Get parent node id from genesetHierarchyLeaf
        List<GenesetHierarchyLeaf> genesetHierarchyLeafs = DaoGenesetHierarchyLeaf.getGenesetHierarchyLeafsByGenesetId(geneset.getId());
        
        // Select the first and only gene set
        GenesetHierarchyLeaf genesetHierarchyLeaf = genesetHierarchyLeafs.get(0);
        
        // Get node name from genesetHierarchy
        GenesetHierarchy genesetHierarchy = getGenesetHierarchyFromNodeId(genesetHierarchyLeaf.getNodeId());
        
        // Check if node name is as expected
        assertEquals("Institutes Subcategory 2", genesetHierarchy.getNodeName());
    }
	
	/**
	 * Retrieve gene set hierarchy objects from geneset_hierarchy_node table in database.
	 * THis 
	 * @throws DaoException 
	 */
	public static GenesetHierarchy getGenesetHierarchyFromNodeId(int nodeId) throws DaoException {
		Connection connection = null;
	    PreparedStatement preparedStatement = null;
	    ResultSet resultSet = null;
	    
	    try {
	    	// Open connection to database
	        connection = JdbcUtil.getDbConnection(DaoGenesetHierarchyNode.class);
	        
	        // Prepare SQL statement
	        preparedStatement = connection.prepareStatement("SELECT * FROM geneset_hierarchy_node WHERE NODE_ID = ?");
	        preparedStatement.setInt(1, nodeId);

	        // Execute statement
	        resultSet = preparedStatement.executeQuery();
	        
	        // Extract genesetHierarchy values
	        if (resultSet.next()) {
	            GenesetHierarchy genesetHierarchy = new GenesetHierarchy();
	            genesetHierarchy.setNodeId(resultSet.getInt("NODE_ID"));
	            genesetHierarchy.setNodeName(resultSet.getString("NODE_NAME"));
	            genesetHierarchy.setParentId(resultSet.getInt("PARENT_ID"));
	            return genesetHierarchy;
	        }
	        
	    	return null;

	    } catch (SQLException e) {
	        throw new DaoException(e);
	    } finally {
	        JdbcUtil.closeAll(DaoGenesetHierarchyNode.class, connection, preparedStatement, resultSet);
	    }
	}
}
