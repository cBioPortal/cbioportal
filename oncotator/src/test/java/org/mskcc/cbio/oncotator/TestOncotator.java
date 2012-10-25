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

/**
 * Test class to test Oncotator tool.
 */
public class TestOncotator extends TestCase
{
	/**
	 * Tests the sample input MAF file which already has oncotator columns.
	 */
	public void testWithOncoColumns()
	{
		OncotatorCacheService cacheService = new HashCacheService();
		OncotatorService oncotatorService = new OncotatorService(cacheService);
		OncotateTool oncotator = new OncotateTool(oncotatorService);

		// there should be no inital errors
		assertEquals(oncotatorService.getErrorCount(), 0);

		// TODO replace with getResourceStream()
		File input = new File("target/test-classes/with_onco_columns.txt");
		File output = new File("target/test-classes/with_cols_output.txt");

		try
		{
			// oncotate the sample input files
			oncotator.oncotateMaf(input, output, false);

			// now check if everthing is ok with the output

			assertEquals(0, oncotatorService.getErrorCount()); // no errors

			BufferedReader reader = new BufferedReader(new FileReader(output));

			String line = reader.readLine();
			MafUtil util =  new MafUtil(line);

			// assert number of columns remains same
			assertEquals(13, util.getHeaderCount());

			while ((line = reader.readLine()) != null)
			{
				if (line.length() > 0)
				{
					MafRecord record = util.parseRecord(line);

					if (record.getVariantClassification().equalsIgnoreCase("Silent"))
					{
						// assert mutation type is copied to the oncotator column
						assertEquals("Silent", record.getOncotatorVariantClassification());
					}
					else
					{
						// assert all oncotator columns are overwritten with new values
						assertTrue(!record.getOncotatorVariantClassification().equalsIgnoreCase("Unknown"));
						assertTrue(!record.getOncotatorCosmicOverlapping().equalsIgnoreCase("Unknown"));
						assertTrue(!record.getOncotatorDbSnpRs().equalsIgnoreCase("Unknown"));
						assertTrue(!record.getOncotatorProteinChange().equalsIgnoreCase("Unknown"));
					}
				}
			}

			reader.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Tests the sample input MAF file which does not have oncotator columns.
	 */
	public void testWithoutOncoColumns()
	{
		OncotatorCacheService cacheService = new HashCacheService();
		OncotatorService oncotatorService = new OncotatorService(cacheService);
		OncotateTool oncotator = new OncotateTool(oncotatorService);

		// there should be no inital errors
		assertEquals(oncotatorService.getErrorCount(), 0);

		// TODO replace with getResourceStream()
		File input = new File("target/test-classes/without_onco_columns.txt");
		File output = new File("target/test-classes/without_cols_output.txt");

		try
		{
			// oncotate the sample input files
			oncotator.oncotateMaf(input, output, false);

			// now check if everthing is ok with the output

			assertEquals(0, oncotatorService.getErrorCount()); // no errors

			BufferedReader reader = new BufferedReader(new FileReader(output));

			String line = reader.readLine();
			MafUtil util =  new MafUtil(line);

			// assert 5 new columns are added
			assertEquals(13, util.getHeaderCount());

			while ((line = reader.readLine()) != null)
			{
				if (line.length() > 0)
				{
					MafRecord record = util.parseRecord(line);

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
					}
				}
			}

			reader.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
