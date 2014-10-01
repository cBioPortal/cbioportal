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

package org.mskcc.cbio.mutassessor;

import org.mskcc.cbio.oncotator.MultiFileAnnotator;

import java.io.File;
import java.util.Map;

/**
 * Designed to add MA info into all MAF files within a given directory.
 *
 * @author Selcuk Onur Sumer
 */
public class MultiFileAssessor extends MultiFileAnnotator
{
	/**
	 * Adds MA info for all input MAF files within the given map. Writes output
	 * MAFs to the mapped directory.
	 *
	 * @param map   map of input MAF files to output directories
	 */
	protected void annotateAll(Map<File, File> map)
	{
		for (File file : map.keySet())
		{
			File outDir = map.get(file);
			String inputMaf = file.getAbsolutePath();
			String outputMaf = outDir.getAbsolutePath() + "/" + file.getName();

			// TODO allow user to change these?
			boolean db = false;
			boolean sort = true;
			boolean addMissing = true;

			MutationAssessorTool.driver(inputMaf,
				outputMaf,
				db,
				sort,
				addMissing);
		}
	}
}
