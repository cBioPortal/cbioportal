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
 */
public class Hg18ToHg19
{
	public static final String IN_FILE = "oldfile.txt";
	public static final String MAPPED_FILE = "newfile.txt";
	public static final String UNMAPPED_FILE = "unmapped.txt";
	public static final String CHAIN_FILE = "hg18ToHg19.over.chain";
	public static final String AUX_FILE = "auxfile.txt";
	public static final String LIFT_OVER = "./liftOver";


	/**
	 * Arguments:
	 *  1) original input (MAF) file.
	 *  2) name of the new output file to be created.
	 */
	public static void main(String[] args) throws IOException
	{
		if (args.length < 2)
		{
			System.out.println("command line usage: hg18to19.sh <input_maf_file> <output_maf_file>");
			System.exit(1);
		}

		// extract required information from the MAF file
		System.out.println("[info] Creating input files for lift over tool...");
		PreLiftOver.prepareInput(args[0], IN_FILE, AUX_FILE);

		// run the liftOver tool for conversion
		System.out.println("[info] Running liftOver tool...");

		// system call with required arguments
		// ./liftOver oldfile.txt hg18ToHg19.over.chain newfile.txt unmapped.txt
		String[] liftOverArgs = {LIFT_OVER, IN_FILE, CHAIN_FILE, MAPPED_FILE, UNMAPPED_FILE};

		if (liftOver(liftOverArgs) != 0)
		{
			System.out.println("[warning] liftOver process is not terminated successfully");
		}

		// process files created by liftOver to update old MAF
		System.out.println("[info] Updating positions and creating the new MAF...");

		PostLiftOver.updateMaf(args[0],
			MAPPED_FILE,
			UNMAPPED_FILE,
			AUX_FILE,
			args[1]);

		// clean intermediate files
		(new File(IN_FILE)).delete();
		(new File(AUX_FILE)).delete();
		(new File(MAPPED_FILE)).delete();
		(new File(UNMAPPED_FILE)).delete();
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
