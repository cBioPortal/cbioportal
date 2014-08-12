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

import java.util.Map;
import java.io.File;

/**
 * Designed to oncotate all MAF files within a given directory.
 *
 * @author Selcuk Onur Sumer
 */
public class MultiFileOncotator extends MultiFileAnnotator
{
	/**
	 * Oncotates all input MAF files within the given map. Writes output
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
			boolean useCache = true;
			boolean sort = true;
			boolean addMissing = true;

			int oncoResult = OncotateTool.driver(inputMaf,
				outputMaf,
				useCache,
				sort,
				addMissing);

			if (oncoResult != 0)
			{
				// TODO write this in another log file into the main output dir
				System.out.println("Process completed with " + oncoResult + " error(s).");
			}
		}
	}
}
