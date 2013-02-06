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

package org.mskcc.cbio.liftover;

import java.io.*;

/**
 * Script to convert MAF files with ncbi build hg18 to hg19.
 * This script requires an executable binary file named liftOver.
 * It is available at http://hgdownload.cse.ucsc.edu/admin/exe/
 * Detailed information about liftOver tool can be found at
 * http://genome.ucsc.edu/cgi-bin/hgLiftOver
 *
 * @author Selcuk Onur Sumer
 */
public class Hg18ToHg19
{
	public static final String IN_FILE = "oldfile.txt";
	public static final String MAPPED_FILE = "newfile.txt";
	public static final String UNMAPPED_FILE = "unmapped.txt";
	public static final String AUX_FILE = "auxfile.txt";
	public static final String DEFAULT_CHAIN_FILE = "hg18ToHg19.over.chain";
	public static final String DEFAULT_LIFT_OVER = "./liftOver";


	/**
	 * Arguments:
	 *  1) original input (MAF) file.
	 *  2) name of the new output file to be created.
	 */
	public static void main(String[] args)
	{
		if (args.length < 2)
		{
			System.out.println("command line usage: " +
			                   "hg18to19.sh <input_maf_file> <output_maf_file> " +
			                   "[liftover_binary_file] [chain_file]");
			System.exit(1);
		}

		String input = args[0];
		String output = args[1];
		String binary = DEFAULT_LIFT_OVER;
		String chain = DEFAULT_CHAIN_FILE;

		if (args.length > 2)
		{
			binary = args[2];
		}

		if (args.length > 3)
		{
			chain = args[3];
		}

		try
		{
			driver(input, output, binary, chain);
		}
		catch (IOException e)
		{
			System.out.println("[error] IO error while processing the input MAF");
			e.printStackTrace();
		}

	}

	/**
	 * Driver method for the lift over process.
	 *
	 * @param inputMaf          input MAF file (assumed to be build 36 / hg18)
	 * @param outputMaf         output MAF file with updated coordinates
	 * @param liftOverBinary    executable (external) liftover binary filename
	 * @param chainFile         chain file required by the liftover binary
	 * @return                  zero if no error, positive value on error
	 * @throws IOException
	 */
	public static int driver(String inputMaf,
			String outputMaf,
			String liftOverBinary,
			String chainFile) throws IOException
	{
		// extract required information from the MAF file
		System.out.println("[info] Creating input files for lift over tool...");
		PreLiftOver.prepareInput(inputMaf, IN_FILE, AUX_FILE);

		// run the liftOver tool for conversion
		System.out.println("[info] Running liftOver tool...");

		// system call with required arguments
		// ./liftOver oldfile.txt hg18ToHg19.over.chain newfile.txt unmapped.txt
		String[] liftOverArgs = {liftOverBinary, IN_FILE, chainFile, MAPPED_FILE, UNMAPPED_FILE};

		if (liftOver(liftOverArgs) != 0)
		{
			System.out.println("[warning] liftOver process is not terminated successfully");
		}

		// process files created by liftOver to update old MAF
		System.out.println("[info] Updating positions and creating the new MAF...");

		int updateResult = PostLiftOver.updateMaf(inputMaf,
		                       MAPPED_FILE,
		                       UNMAPPED_FILE,
		                       AUX_FILE,
		                       outputMaf);

		// clean intermediate files
		(new File(IN_FILE)).delete();
		(new File(AUX_FILE)).delete();
		(new File(MAPPED_FILE)).delete();
		(new File(UNMAPPED_FILE)).delete();

		return updateResult;
	}

	/**
	 * Executes the external liftOver tool via system call.
	 *
	 * @param args          process arguments (including the process itself)
	 * @return              exit value of the process
	 * @throws IOException  if an IO error occurs
	 */
	public static int liftOver(String[] args) throws IOException
	{
		Process liftOver = Runtime.getRuntime().exec(args);

		InputStream stdin = liftOver.getInputStream();
		InputStream stderr = liftOver.getErrorStream();
		InputStreamReader isr = new InputStreamReader(stdin);
		InputStreamReader esr = new InputStreamReader(stderr);
		BufferedReader inReader = new BufferedReader(isr);
		BufferedReader errReader = new BufferedReader(esr);

		// echo liftOver's output messages to stdout
		String line = null;

		while ((line = inReader.readLine()) != null)
		{
			System.out.println(line);
		}

		// also echo error messages
		while ((line = errReader.readLine()) != null)
		{
			System.out.println(line);
		}

		int exitValue = -1;

		// wait for process to complete
		try
		{
			exitValue = liftOver.waitFor();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		return exitValue;
	}
}
