/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.oncotator;

import junit.framework.TestCase;
import org.mskcc.cbio.maf.MafHeaderUtil;
import org.mskcc.cbio.maf.MafRecord;
import org.mskcc.cbio.maf.MafUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Test class for the Oncotator tool.
 *
 * @author Selcuk Onur Sumer
 */
public class TestOncotator extends TestCase
{

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
			MafHeaderUtil headerUtil = new MafHeaderUtil();

			String line = headerUtil.extractHeader(reader);
			MafUtil util = new MafUtil(line);

			// assert number of columns remains same
			assertEquals(36, util.getHeaderCount());

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
			MafHeaderUtil headerUtil = new MafHeaderUtil();

			String line = headerUtil.extractHeader(reader);
			MafUtil util = new MafUtil(line);

			// assert 27 new columns are added
			assertEquals(36, util.getHeaderCount());

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
			MafHeaderUtil headerUtil = new MafHeaderUtil();

			String line = headerUtil.extractHeader(reader);
			MafUtil util = new MafUtil(line);

			// assert number of columns (32 standard + 27 Oncotator + 1 custom)
			assertEquals(60, util.getHeaderCount());

			// assert new indices
			this.validateColumnIndices(util);

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
			MafHeaderUtil headerUtil = new MafHeaderUtil();

			String line = headerUtil.extractHeader(reader);
			MafUtil util = new MafUtil(line);

			// assert number of columns (32 standard + 27 Oncotator + 1 Custom)
			assertEquals(60, util.getHeaderCount());

			// assert new indices
			this.validateColumnIndices(util);

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

	/**
	 * Validates column indices for a sorted MAF.
	 *
	 * @param util  MAF util containing index info
	 */
	private void validateColumnIndices(MafUtil util)
	{
		assertEquals(0, util.getHugoGeneSymbolIndex());
		assertEquals(1, util.getEntrezGeneIdIndex());
		assertEquals(2, util.getCenterIndex());
		assertEquals(3, util.getNcbiIndex());
		assertEquals(4, util.getChrIndex());
		assertEquals(5, util.getStartPositionIndex());
		assertEquals(6, util.getEndPositionIndex());
		assertEquals(7, util.getStrandIndex());
		assertEquals(8, util.getVariantClassificationIndex());
		assertEquals(9, util.getVariantTypeIndex());
		assertEquals(10, util.getReferenceAlleleIndex());
		assertEquals(11, util.getTumorSeqAllele1Index());
		assertEquals(12, util.getTumorSeqAllele2Index());
		assertEquals(13, util.getDbSNPIndex());
		assertEquals(14, util.getDbSnpValStatusIndex());
		assertEquals(15, util.getTumorSampleIndex());
		assertEquals(16, util.getMatchedNormSampleBarcodeIndex());
		assertEquals(17, util.getMatchNormSeqAllele1Index());
		assertEquals(18, util.getMatchNormSeqAllele2Index());
		assertEquals(19, util.getTumorValidationAllele1Index());
		assertEquals(20, util.getTumorValidationAllele2Index());
		assertEquals(21, util.getMatchNormValidationAllele1Index());
		assertEquals(22, util.getMatchNormValidationAllele2Index());
		assertEquals(23, util.getVerificationStatusIndex());
		assertEquals(24, util.getValidationStatusIndex());
		assertEquals(25, util.getMutationStatusIndex());
		assertEquals(26, util.getSequencingPhaseIndex());
		assertEquals(27, util.getSequenceSourceIndex());
		assertEquals(28, util.getValidationMethodIndex());
		assertEquals(29, util.getScoreIndex());
		assertEquals(30, util.getBamFileIndex());
		assertEquals(31, util.getSequencerIndex());

		assertEquals(32, util.getOncoCosmicOverlappingIndex());
		assertEquals(33, util.getOncoDbSnpRsIndex());
		assertEquals(34, util.getOncoDbSnpValStatusIndex());
		assertEquals(35, util.getOncoVariantClassificationIndex());
		assertEquals(36, util.getOncoProteinChangeIndex());
		assertEquals(37, util.getOncoGeneSymbolIndex());
		assertEquals(38, util.getOncoRefseqMrnaIdIndex());
		assertEquals(39, util.getOncoRefseqProtIdIndex());
		assertEquals(40, util.getOncoUniprotNameIndex());
		assertEquals(41, util.getOncoUniprotAccessionIndex());
		assertEquals(42, util.getOncoCodonChangeIndex());
		assertEquals(43, util.getOncoTranscriptChangeIndex());
		assertEquals(44, util.getOncoExonAffectedIndex());
		assertEquals(45, util.getOncoProteinPosStartIndex());
		assertEquals(46, util.getOncoProteinPosEndIndex());

		assertEquals(47, util.getOncoVariantClassificationBeIndex());
		assertEquals(48, util.getOncoProteinChangeBeIndex());
		assertEquals(49, util.getOncoGeneSymbolBeIndex());
		assertEquals(50, util.getOncoRefseqMrnaIdBeIndex());
		assertEquals(51, util.getOncoRefseqProtIdBeIndex());
		assertEquals(52, util.getOncoUniprotNameBeIndex());
		assertEquals(53, util.getOncoUniprotAccessionBeIndex());
		assertEquals(54, util.getOncoCodonChangeBeIndex());
		assertEquals(55, util.getOncoTranscriptChangeBeIndex());
		assertEquals(56, util.getOncoExonAffectedBeIndex());
		assertEquals(57, util.getOncoProteinPosStartBeIndex());
		assertEquals(58, util.getOncoProteinPosEndBeIndex());
	}

	private void validateMafRecord(MafRecord record)
	{
		assertEquals("37", record.getNcbiBuild());


		// assert all oncotator columns have non empty values

		assertTrue(record.getOncotatorCosmicOverlapping().length() > 0);
		assertTrue(record.getOncotatorDbSnpRs().length() > 0);
		assertTrue(record.getOncotatorDbSnpValStatus().length() > 0);

		assertTrue(record.getOncotatorVariantClassification().length() > 0);
		assertTrue(record.getOncotatorProteinChange().length() > 0);
		assertTrue(record.getOncotatorGeneSymbol().length() > 0);
		assertTrue(record.getOncotatorRefseqMrnaId().length() > 0);
		assertTrue(record.getOncotatorRefseqProtId().length() > 0);
		assertTrue(record.getOncotatorUniprotName().length() > 0);
		assertTrue(record.getOncotatorUniprotAccession().length() > 0);
		assertTrue(record.getOncotatorCodonChange().length() > 0);
		assertTrue(record.getOncotatorTranscriptChange().length() > 0);

		assertTrue(record.getOncotatorVariantClassificationBestEffect().length() > 0);
		assertTrue(record.getOncotatorProteinChangeBestEffect().length() > 0);
		assertTrue(record.getOncotatorGeneSymbolBestEffect().length() > 0);
		assertTrue(record.getOncotatorRefseqMrnaIdBestEffect().length() > 0);
		assertTrue(record.getOncotatorRefseqProtIdBestEffect().length() > 0);
		assertTrue(record.getOncotatorUniprotNameBestEffect().length() > 0);
		assertTrue(record.getOncotatorUniprotAccessionBestEffect().length() > 0);
		assertTrue(record.getOncotatorCodonChangeBestEffect().length() > 0);
		assertTrue(record.getOncotatorTranscriptChangeBestEffect().length() > 0);

		// assert all oncotator columns are overwritten with new values

		assertTrue(!record.getOncotatorCosmicOverlapping().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorDbSnpRs().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorDbSnpValStatus().equalsIgnoreCase("Unknown"));

		assertTrue(!record.getOncotatorVariantClassification().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorProteinChange().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorGeneSymbol().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorRefseqMrnaId().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorRefseqProtId().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorUniprotName().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorUniprotAccession().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorCodonChange().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorTranscriptChange().equalsIgnoreCase("Unknown"));

		assertTrue(!record.getOncotatorVariantClassificationBestEffect().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorProteinChangeBestEffect().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorGeneSymbolBestEffect().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorRefseqMrnaIdBestEffect().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorRefseqProtIdBestEffect().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorUniprotNameBestEffect().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorUniprotAccessionBestEffect().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorCodonChangeBestEffect().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorTranscriptChangeBestEffect().equalsIgnoreCase("Unknown"));

		assertTrue(!record.getOncotatorVariantClassification().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorCosmicOverlapping().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorDbSnpRs().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorProteinChange().equalsIgnoreCase("Unknown"));
		assertTrue(!record.getOncotatorGeneSymbol().equalsIgnoreCase("Unknown"));

		// assert all best effect values are equal to best canonical values
		assertTrue(record.getOncotatorVariantClassification().equals(
				record.getOncotatorVariantClassificationBestEffect()));
		assertTrue(record.getOncotatorProteinChange().equals(
				record.getOncotatorProteinChangeBestEffect()));
		assertTrue(record.getOncotatorGeneSymbol().equals(
				record.getOncotatorGeneSymbolBestEffect()));
		assertTrue(record.getOncotatorRefseqMrnaId().equals(
				record.getOncotatorRefseqMrnaIdBestEffect()));
		assertTrue(record.getOncotatorRefseqProtId().equals(
				record.getOncotatorRefseqProtIdBestEffect()));
		assertTrue(record.getOncotatorUniprotName().equals(
				record.getOncotatorUniprotNameBestEffect()));
		assertTrue(record.getOncotatorUniprotAccession().equals(
				record.getOncotatorUniprotAccessionBestEffect()));
		assertTrue(record.getOncotatorCodonChange().equals(
				record.getOncotatorCodonChangeBestEffect()));
		assertTrue(record.getOncotatorTranscriptChange().equals(
				record.getOncotatorTranscriptChangeBestEffect()));
		assertTrue(record.getOncotatorExonAffected() ==
				record.getOncotatorExonAffectedBestEffect());
		assertTrue(record.getOncotatorProteinPosStart() ==
				record.getOncotatorProteinPosStartBestEffect());
		assertTrue(record.getOncotatorProteinPosEnd() ==
				record.getOncotatorProteinPosEndBestEffect());
	}
}
