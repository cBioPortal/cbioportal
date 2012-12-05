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

package org.mskcc.cbio.oncotator;

import junit.framework.TestCase;
import org.mskcc.cbio.maf.MafRecord;
import org.mskcc.cbio.maf.MafUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Test class for the Oncotator tool.
 */
public class TestOncotator extends TestCase
{
	// TODO test new columns!

	/**
	 * Tests the sample input MAF file which already has oncotator columns.
	 */
	public void testWithOncoColumns()
	{
		// TODO replace with getResourceStream()
		File input = new File("target/test-classes/with_onco_columns.txt");
		File output = new File("target/test-classes/with_cols_output.txt");

		// run oncotator
		this.oncotate(input, output, true, false, false);

		// check if everthing is ok with the output
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(output));

			String line = reader.readLine();
			MafUtil util =  new MafUtil(line);

			// TODO assert number of columns remains same
			//assertEquals(14, util.getHeaderCount());

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
	public void testWithoutOncoColumns()
	{
		// TODO replace with getResourceStream()
		File input = new File("target/test-classes/without_onco_columns.txt");
		File output = new File("target/test-classes/without_cols_output.txt");

		// run oncotator
		this.oncotate(input, output, true, false, false);

		// check if everthing is ok with the output
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(output));

			String line = reader.readLine();
			MafUtil util =  new MafUtil(line);

			// assert 23 new columns are added
			assertEquals(32, util.getHeaderCount());

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
	public void testShuffledWithOncoCols()
	{
		// TODO replace with getResourceStream()
		File input = new File("target/test-classes/with_onco_cols_shuffled.txt");
		File output = new File("target/test-classes/with_cols_shuffled_out.txt");

		// run oncotator (with sort & add options enabled)
		this.oncotate(input, output, true, true, true);

		// check if everthing is ok with the output
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(output));

			String line = reader.readLine();
			MafUtil util =  new MafUtil(line);

			// assert number of columns (32 standard + 23 Oncotator + 1 custom)
			assertEquals(56, util.getHeaderCount());

			// assert new indices
			assertEquals(3, util.getNcbiIndex());
			assertEquals(0, util.getHugoGeneSymbolIndex());
			assertEquals(32, util.getOncoCosmicOverlappingIndex());
			assertEquals(35, util.getOncoVariantClassificationIndex());
			assertEquals(37, util.getOncoGeneSymbolIndex());

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
	public void testShuffledWithoutOncoCols()
	{
		// TODO replace with getResourceStream()
		File input = new File("target/test-classes/without_onco_cols_shuffled.txt");
		File output = new File("target/test-classes/without_cols_shuffled_out.txt");

		// run oncotator (with sort & add options enabled)
		this.oncotate(input, output, true, true, true);

		// check if everthing is ok with the output
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(output));

			String line = reader.readLine();
			MafUtil util =  new MafUtil(line);

			// assert number of columns (32 standard + 23 Oncotator + 1 Custom)
			assertEquals(56, util.getHeaderCount());

			// assert new indices
			assertEquals(3, util.getNcbiIndex());
			assertEquals(0, util.getHugoGeneSymbolIndex());
			assertEquals(32, util.getOncoCosmicOverlappingIndex());
			assertEquals(35, util.getOncoVariantClassificationIndex());
			assertEquals(37, util.getOncoGeneSymbolIndex());

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

	private void oncotate(File input,
			File output,
			boolean useCache,
			boolean sort,
			boolean addMissing)
	{
		OncotatorCacheService cacheService = new HashCacheService();
		OncotatorService oncotatorService = new CachedOncotatorService(cacheService);
		Oncotator oncotator = new Oncotator(oncotatorService);
		oncotator.setUseCache(useCache);
		oncotator.setSortColumns(sort);
		oncotator.setAddMissingCols(addMissing);

		// there should be no inital errors
		assertEquals(oncotatorService.getErrorCount(), 0);

		try
		{
			// oncotate the sample input files
			oncotator.oncotateMaf(input, output);

			// assert no errors after oncotating
			assertEquals(0, oncotatorService.getErrorCount());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void validateMafRecord(MafRecord record)
	{
		assertEquals("37", record.getNcbiBuild());

		if (record.getVariantClassification().equalsIgnoreCase("Silent"))
		{
			// assert mutation type is copied to the oncotator column
			assertEquals("Silent", record.getOncotatorVariantClassification());
		}
		else
		{
			// assert all oncotator columns have non empty values
			assertTrue(record.getOncotatorVariantClassification().length() > 0);
			assertTrue(record.getOncotatorCosmicOverlapping().length() > 0);
			assertTrue(record.getOncotatorDbSnpRs().length() > 0);
			assertTrue(record.getOncotatorProteinChange().length() > 0);
			assertTrue(record.getOncotatorGeneSymbol().length() > 0);

			// assert all oncotator columns are overwritten with new values
			assertTrue(!record.getOncotatorVariantClassification().equalsIgnoreCase("Unknown"));
			assertTrue(!record.getOncotatorCosmicOverlapping().equalsIgnoreCase("Unknown"));
			assertTrue(!record.getOncotatorDbSnpRs().equalsIgnoreCase("Unknown"));
			assertTrue(!record.getOncotatorProteinChange().equalsIgnoreCase("Unknown"));
			assertTrue(!record.getOncotatorGeneSymbol().equalsIgnoreCase("Unknown"));
		}
	}
}
