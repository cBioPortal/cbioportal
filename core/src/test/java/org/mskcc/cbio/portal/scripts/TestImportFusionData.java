/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/

package org.mskcc.cbio.portal.scripts;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoMutation;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.portal.util.ProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Test class to test functionality of ImportFusionData
 *
 * @author Selcuk Onur Sumer
 */
public class TestImportFusionData extends TestCase
{
	public void testImportFusionData()
	{
		MySQLbulkLoader.bulkLoadOn();

		ProgressMonitor pMonitor = new ProgressMonitor();
		pMonitor.setConsoleMode(false);

		// TODO change this to use getResourceAsStream()
		File file = new File("target/test-classes/data_fusions.txt");
		ImportFusionData parser = new ImportFusionData(file, 1, pMonitor);

		try
		{
			loadGenes();
			parser.importData();
			MySQLbulkLoader.flushAll();

			checkImportedData();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (DaoException e)
		{
			e.printStackTrace();
		}
	}

	private void checkImportedData() throws DaoException
	{
		ArrayList<ExtendedMutation> list = DaoMutation.getAllMutations();

		assertEquals(2, list.size()); // all except "FAKE"

		list = DaoMutation.getMutations(1, "TCGA-XX-A3XX");

		assertEquals(1, list.size());
		assertEquals("saturn", list.get(0).getSequencingCenter());
		assertEquals("FGFR3", list.get(0).getGeneSymbol());
		assertEquals("Fusion", list.get(0).getMutationType());
		assertEquals("Fusion1", list.get(0).getProteinChange());

		list = DaoMutation.getMutations(1, "TCGA-YY-A2YY");

		assertEquals(1, list.size());
		assertEquals("jupiter", list.get(0).getSequencingCenter());
		assertEquals("ERBB2", list.get(0).getGeneSymbol());
		assertEquals("Fusion", list.get(0).getMutationType());
		assertEquals("Fusion2", list.get(0).getProteinChange());
	}

	private void loadGenes() throws DaoException
	{
		ResetDatabase.resetDatabase();
		DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

		// genes for "data_fusions.txt"
		daoGene.addGene(new CanonicalGene(2261L, "FGFR3"));
		daoGene.addGene(new CanonicalGene(2064L, "ERBB2"));

		MySQLbulkLoader.flushAll();
	}
}
