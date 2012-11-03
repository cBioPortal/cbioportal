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

package org.mskcc.cbio.mutassessor;

import junit.framework.TestCase;
import org.mskcc.cbio.maf.MafRecord;
import org.mskcc.cbio.maf.MafUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 */
public class TestMutationAssessor extends TestCase
{
	/**
	 * Tests the sample input MAF file which already has mutation assessor columns.
	 */
	public void testWithMaColumns()
	{
		// TODO replace with getResourceStream()
		File input = new File("target/test-classes/with_ma_columns.txt");
		File output = new File("target/test-classes/with_cols_output.txt");

		// run MA tool
		this.addMaInfo(input, output, false, false);

		// check if everthing is ok with the output
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(output));

			String line = reader.readLine();
			MafUtil util =  new MafUtil(line);

			// assert unknown MA column is removed
			assertEquals(14, util.getHeaderCount());

			while ((line = reader.readLine()) != null)
			{
				if (line.length() > 0)
				{
					MafRecord record = util.parseRecord(line);
					this.validateMafRecord(record);
				}
			}

			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Tests the sample input MAF file which does not have oncotator columns.
	 */
	public void testWithoutMaColumns()
	{
		// TODO replace with getResourceStream()
		File input = new File("target/test-classes/without_ma_columns.txt");
		File output = new File("target/test-classes/without_cols_output.txt");

		// run MA tool
		this.addMaInfo(input, output, false, false);

		// check if everthing is ok with the output
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(output));

			String line = reader.readLine();
			MafUtil util =  new MafUtil(line);

			// assert 5 new columns are added
			assertEquals(14, util.getHeaderCount());

			while ((line = reader.readLine()) != null)
			{
				if (line.length() > 0)
				{
					MafRecord record = util.parseRecord(line);
					this.validateMafRecord(record);
				}
			}

			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Tests the shuffled input MAF file which already has oncotator columns.
	 */
	public void testShuffledWithMaCols()
	{
		// TODO replace with getResourceStream()
		File input = new File("target/test-classes/with_ma_cols_shuffled.txt");
		File output = new File("target/test-classes/with_cols_shuffled_out.txt");

		// run MA tool (with sort & add options enabled)
		this.addMaInfo(input, output, true, true);

		// check if everthing is ok with the output
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(output));

			String line = reader.readLine();
			MafUtil util =  new MafUtil(line);

			// assert number of columns (32 standard + 5 MA + 1 custom)
			assertEquals(38, util.getHeaderCount());

			// assert new indices
			assertEquals(3, util.getNcbiIndex());
			assertEquals(0, util.getHugoGeneSymbolIndex());
			// TODO test MA values
			//assertEquals(32, util.getOncoVariantClassificationIndex());
			//assertEquals(36, util.getOncoGeneSymbolIndex());

			while ((line = reader.readLine()) != null)
			{
				if (line.length() > 0)
				{
					MafRecord record = util.parseRecord(line);

					assertEquals("37", record.getNcbiBuild());
					this.validateMafRecord(record);
				}
			}

			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * Tests the shuffled input MAF file which does not have oncotator columns.
	 */
	public void testShuffledWithoutMaCols()
	{
		// TODO replace with getResourceStream()
		File input = new File("target/test-classes/without_ma_cols_shuffled.txt");
		File output = new File("target/test-classes/without_cols_shuffled_out.txt");

		// run MA tool (with sort & add options enabled)
		this.addMaInfo(input, output, true, true);

		// check if everthing is ok with the output
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(output));

			String line = reader.readLine();
			MafUtil util =  new MafUtil(line);

			// assert number of columns (32 standard + 5 MA + 1 Custom)
			assertEquals(38, util.getHeaderCount());

			// assert new indices
			assertEquals(3, util.getNcbiIndex());
			assertEquals(0, util.getHugoGeneSymbolIndex());
			// TODO test MA values
			//assertEquals(32, util.getOncoVariantClassificationIndex());
			//assertEquals(36, util.getOncoGeneSymbolIndex());

			while ((line = reader.readLine()) != null)
			{
				if (line.length() > 0)
				{
					MafRecord record = util.parseRecord(line);
					this.validateMafRecord(record);
				}
			}

			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	private void addMaInfo(File input,
			File output,
			boolean sort,
			boolean addMissing)
	{
		MutationAssessorService maService = new HashMaService();
		DataImporter importer = new DataImporter(maService);
		importer.setSortColumns(sort);
		importer.setAddMissingCols(addMissing);

		try
		{
			// oncotate the sample input files
			importer.addMutAssessorInfo(input, output);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void validateMafRecord(MafRecord record)
	{
		assertEquals("37", record.getNcbiBuild());

		// assert all MA columns have non empty values
		assertTrue(record.getMaFuncImpact().length() > 0);
		assertTrue(record.getMaProteinChange().length() > 0);
		assertTrue(record.getMaLinkMsa().length() > 0);
		assertTrue(record.getMaLinkPdb().length() > 0);
		assertTrue(record.getMaLinkVar().length() > 0);

		// assert all MA columns are overwritten with new values
		assertTrue(!record.getMaFuncImpact().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getMaProteinChange().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getMaLinkMsa().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getMaLinkPdb().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getMaLinkVar().equalsIgnoreCase("Unknown"));
	}
}
