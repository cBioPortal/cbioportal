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

import java.io.*;
import java.util.*;
import joptsimple.*;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.Geneset;
import org.mskcc.cbio.portal.model.GenesetHierarchy;
import org.mskcc.cbio.portal.model.GenesetHierarchyLeaf;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.yaml.snakeyaml.Yaml;

public class ImportGenesetHierarchy extends ConsoleRunnable {

	@Override
	public void run() {
		try {
			String progName = "ImportGenesetHierarchy";
			String description = "Import gene set hierarchy files in YAML format.";
			// usage: --data <data_file.yaml>

			OptionParser parser = new OptionParser();
			OptionSpec<String> data = parser.accepts("data", "Gene set hierarchy data (.yaml)")
					.withRequiredArg().ofType(String.class);

			OptionSet options = null;
			try {
				options = parser.parse(args);
			}
			catch (Exception ex) {
				throw new UsageException(
						progName, description, parser, 
						ex.getMessage());
			}

			// if neither option was set then throw UsageException
			if (!options.has(data)) {
				throw new UsageException(
						progName, description, parser,
						"'data' argument required");
			}

			File genesetFile = new File(options.valueOf(data));
			ProgressMonitor.setCurrentMessage("Input file:\n" + genesetFile.getPath() + "\n");

			// First validate if gene sets in the hierarchy file, are in database. Therefore start an import process
			// with validation set to 'true'. This will mock the import process and checks the gene sets.
			boolean validate = true;
			importData(genesetFile, validate);

			// Check if gene set hierarchy tables are already filled
			boolean filledGeneSetHierarchyTables = DaoGenesetHierarchyNode.checkGenesetHierarchy();

			// If gene set hierarchy tables are already filled, ask if this data can be discarded
			if (filledGeneSetHierarchyTables) {

				// Asks if used wants to continue
				ProgressMonitor.setCurrentMessage("Previous gene set hierarchy found. Do you want to remove previous hierarchy and continue importing new hierarchy?");
				ProgressMonitor.setCurrentMessage("Type `yes` to continue or anything else to abort.");

				try (Scanner scanner = new Scanner(System.in)) {

					String confirmEmptyingGenesetHierarchy = scanner.next().toLowerCase();
					if (!confirmEmptyingGenesetHierarchy.equals("yes")) {
						ProgressMonitor.setCurrentMessage("Replacing the gene set hierarchy not confirmed; aborting.");
						return;
					}
				}

				ProgressMonitor.setCurrentMessage("Emptying `geneset_hierarchy_node` and `geneset_hierarchy_leaf` before filling with new data.\n");
				DaoGenesetHierarchyNode.deleteAllGenesetHierarchyRecords();
			}	

			// Start the gene set hierarchy import process
			validate = false;
			importData(genesetFile, validate);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Imports data from gene set hierarchy file.
	 */
	public static void importData(File genesetFile, boolean validate) throws Exception {

		// Load data and parse with snakeyaml
		InputStream input = new FileInputStream(genesetFile);
		Yaml yaml = new Yaml();
		Map<String, Object> genesetTree = (Map<String, Object>) yaml.load(input);
		input.close();

		// Initiate start nodeId to give to first iteration.
		int nodeIds = 0;

		// Parse the tree and import to geneset_hierarchy_node
		parseTree(genesetTree, nodeIds, validate);
	}

	/**
	 * Parses data from gene set hierarchy file and saves in database.
	 * @throws DaoException 
	 */
	private static void parseTree(Map<String, Object> genesetTree, int parentNodeId, boolean validate) throws DaoException {

		// Create set with child nodes
		Set<String> childNodes = genesetTree.keySet();

		// Iterate over the child nodes at this level
		for (String childNode: childNodes) {

			// Add leaf for gene sets
			if (childNode.equals("Gene sets")) {

				// Iterate over gene sets
				for (String genesetName: (List<String>) genesetTree.get("Gene sets")) {

					// Retrieve gene set from database
					Geneset geneset = DaoGeneset.getGenesetByExternalId(genesetName);

					// Check if gene set is in database
					if (geneset != null) {

						// Only write gene set to database when not validating
						if (!validate) {
							GenesetHierarchyLeaf genesetHierarchyLeaf = new GenesetHierarchyLeaf();
							genesetHierarchyLeaf.setNodeId(parentNodeId);
							genesetHierarchyLeaf.setGenesetId(geneset.getId());

							// Add leaf to geneset_hierarchy_leaf
							ProgressMonitor.setCurrentMessage("Parent ID: " + parentNodeId + ", Gene set ID: " + genesetName);
							DaoGenesetHierarchyLeaf.addGenesetHierarchyLeaf(genesetHierarchyLeaf);
						}
					} else {
						throw new RuntimeException("\nGene set `" + genesetName + "` not in `geneset` table in database. Please add it first before adding tree containing it.");
					}
				}

				// Add nodes for (sub)categories
			} else {

				try {
					int childNodeId; 
					if (!validate) {
						GenesetHierarchy genesetHierarchy = new GenesetHierarchy();
						genesetHierarchy.setNodeName(childNode);
						genesetHierarchy.setParentId(parentNodeId);

						// Add node to geneset_hierarchy_node
						DaoGenesetHierarchyNode.addGenesetHierarchy(genesetHierarchy);

						// Get node ID 
						childNodeId = genesetHierarchy.getNodeId();
						ProgressMonitor.setCurrentMessage("Node ID: " + childNodeId + ", Node name: " + childNode + ", Parent ID: " + parentNodeId);
					} else{
						// Initiate childNodeId, necessary to test if GenesetId is present during validation
						// , because true childNodeId will not be retrieved since no database connection is made. 
						childNodeId = 0;
					}

					// Go into the node
					parseTree((Map<String, Object>) genesetTree.get(childNode), childNodeId, validate);

				} catch (DaoException e) {
					throw new DaoException(e);
				}
			}
		}
	}

	public ImportGenesetHierarchy(String[] args) {
		super(args);
	}

	public static void main(String[] args) {
		ConsoleRunnable runner = new ImportGenesetHierarchy(args);
		runner.runInConsole();
	}  
}

