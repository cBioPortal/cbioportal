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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.util.Map;

/**
 * Base class designed to annotate all MAF files within a given directory.
 *
 * @author Selcuk Onur Sumer
 */
public abstract class MultiFileAnnotator
{
	protected String sourceDir;
	protected String targetDir;

	/**
	 * Driver method.
	 *
	 * @param sourceDir main source directory for input MAFs
	 * @param targetDir main output directory for output MAFs
	 */
	public void annotate(String sourceDir, String targetDir) throws IOException
	{
		this.sourceDir = sourceDir;
		this.targetDir = targetDir;

		List<File> inputMafList = this.getMafFiles(sourceDir);
		Map<File, File> map = this.makeOutputDirs(inputMafList, sourceDir, targetDir);
		this.annotateAll(map);
		this.generateCopyScript("cp", map, targetDir);
	}

	/**
	 * Makes output directories for the given MAF file list.
	 *
	 * @param mafList   list of input MAFs
	 * @param sourceDir main source directory for input MAFs
	 * @param targetDir main target directory for output MAFs
	 * @return          map of input MAFs to output directories
	 */
	protected Map<File, File> makeOutputDirs(List<File> mafList,
			String sourceDir,
			String targetDir)
	{
		Map<File, File> map = new HashMap<File, File>();

		File source = new File(sourceDir);
		File target = new File(targetDir);

		for (File file : mafList)
		{
			String parentPath = file.getParent();
			String relativePath = parentPath.substring(
					source.getAbsolutePath().length());
			File outputDir = new File(target.getAbsolutePath() + relativePath);

			// put output directory into the map
			map.put(file, outputDir);

			// make output directory
			outputDir.mkdirs();
		}

		return map;
	}

	/**
	 * Annotates all input MAF files within the given map. Writes output
	 * MAFs to the mapped directory.
	 *
	 * This function should be overridden by implementing classes to perform
	 * actual annotation.
	 *
	 * @param map   map of input MAF files to output directories
	 */
	abstract protected void annotateAll(Map<File, File> map);

	protected List<File> getMafFiles(String sourceDir)
	{
		File inFile = new File(sourceDir);

		List<File> mafList = null;

		if (inFile.isDirectory())
		{
			mafList = processDir(inFile);
		}

		return mafList;
	}

	/**
	 * Creates a copy script/batch file.
	 * (To copy back output files into the main source directory)
	 *
	 * @param scriptName    name of the script (cp, copy, etc.)
	 * @param map           map of input MAF files to output directories
	 * @param targetDir     main target directory for output MAFs
	 * @throws IOException
	 */
	protected void generateCopyScript(String scriptName,
			Map<File,File> map,
			String targetDir) throws IOException
	{
		File target = new File(targetDir);
		String scriptFilename = target.getAbsolutePath() + "/copy.sh";
		BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFilename));

		for (File file : map.keySet())
		{
			String targetFile = file.getAbsolutePath();
			String sourceFile = map.get(file).getAbsolutePath() + "/" + file.getName();

			writer.write(scriptName + " " +
			             sourceFile + " " +
			             targetFile);

			writer.newLine();
		}

		writer.close();
	}

	/**
	 * Recursively processes the given input directory and creates a
	 * list of MAF files under its all subdirectories.
	 *
	 * @param inFile    input directory
	 * @return          list of all MAFs under the given directory
	 */
	protected List<File> processDir(File inFile)
	{
		List<File> mafList = new ArrayList<File>();
		File[] contentList;

		if (inFile.isDirectory())
		{
			contentList = inFile.listFiles();

			if (contentList != null)
			{
				for (File file : contentList)
				{
					if (file.isDirectory())
					{
						mafList.addAll(processDir(file));
					}
					else if (isMaf(file))
					{
						mafList.add(file);
					}
				}
			}
		}

		return mafList;
	}

	/**
	 * Checks if the given file is a MAF file.
	 *
	 * @param file  file to check
	 * @return  true if the file is a MAF file, false otherwise
	 */
	protected boolean isMaf(File file)
	{
		// TODO we may want to make this more flexible
		return file.isFile() &&
		       (file.getName().toLowerCase().endsWith(".maf") ||
		        file.getName().toLowerCase().endsWith("data_mutations_extended.txt"));
	}
}
