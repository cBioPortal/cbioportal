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

// package

package org.mskcc.cbio.importer.fetcher.internal;

import junit.framework.TestCase;

import java.util.Map;

/**
 * Test class for FoundationFetcherImpl.
 *
 * @author Selcuk Onur Sumer
 */
public class TestFoundationFetcherImpl extends TestCase
{
	public void testCdsParser()
	{
		FoundationFetcherImpl fetcher = new FoundationFetcherImpl(null, null, null, null);
		Map<String, String> result;

		String cdsEffect = "821CG>TC";
		String strand = "-";
		result = fetcher.parseCdsEffect(cdsEffect, strand);
		assertEquals("GC", result.get("refAllele"));
		assertEquals("AG", result.get("varAllele"));

		cdsEffect = "676G>T";
		strand = "+";
		result = fetcher.parseCdsEffect(cdsEffect, strand);
		assertEquals("G", result.get("refAllele"));
		assertEquals("T", result.get("varAllele"));

		cdsEffect = "457_457delT";
		strand = "-";
		result = fetcher.parseCdsEffect(cdsEffect, strand);
		assertEquals("A", result.get("refAllele"));
		assertEquals("-", result.get("varAllele"));

		cdsEffect = "5266_5267insC";
		strand = "-";
		result = fetcher.parseCdsEffect(cdsEffect, strand);
		assertEquals("-", result.get("refAllele"));
		assertEquals("G", result.get("varAllele"));
	}

	public void testComplement()
	{
		FoundationFetcherImpl fetcher = new FoundationFetcherImpl(null, null, null, null);

		String complement = fetcher.complementOf("-");
		assertEquals("-", complement);

		complement = fetcher.complementOf("TCGA");
		assertEquals("AGCT", complement);

		complement = fetcher.complementOf("tcga");
		assertEquals("AGCT", complement);
	}

	public void testPosition()
	{
		FoundationFetcherImpl fetcher = new FoundationFetcherImpl(null, null, null, null);

		String position = "chr17:17942983";

		Map<String, String> result = fetcher.parsePosition(position);

		assertEquals("chr17", result.get("chromosome"));
		assertEquals("17942983", result.get("startPos"));
		assertEquals("", result.get("endPos"));

		String endPos = fetcher.calcEndPos(result.get("startPos"), "-");
		assertEquals("17942984", endPos);

		endPos = fetcher.calcEndPos(result.get("startPos"), "AAA");
		assertEquals("17942985", endPos);
	}

	public void testCounts()
	{
		FoundationFetcherImpl fetcher = new FoundationFetcherImpl(null, null, null, null);

		String percentReads = "48.0";
		String depth = "548";

		String tAltCount = fetcher.calcTumorAltCount(percentReads, depth);
		String tRefCount = fetcher.calcTumorRefCount(percentReads, depth);

		assertEquals("263", tAltCount);
		assertEquals("285", tRefCount);
	}
}
