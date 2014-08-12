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

package org.mskcc.cbio.mapping;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Test class to test IsoformMapper.
 *
 * @author Selcuk Onur Sumer
 */
public class TestIsoformMapper extends TestCase
{
	public void testMapping()
	{
		// TODO replace with getResourceStream()
		String input =  "target/test-classes/gene_to_isoform.txt";
		IsoformMapper mapper = new IsoformMapper();

		try {
			mapper.buildSymbolToIsoformMap(input);
			String isoform = mapper.getCanonicalIsoformBySymbol("cdkn2A");
			// TODO revisit after sorting the isoforms
			assertEquals("NM_000077", isoform);

			assertEquals(3, mapper.symbolToIsoform.get("LOC404266").size());
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}
}
