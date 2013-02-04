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

import java.io.*;
import java.util.Date;

/**
 * Command Line Tool to Oncotate a Single MAF File.
 *
 * @author Selcuk Onur Sumer
 */
public class OncotateTool
{
    public static void main(String[] args)
    {
        String inputMaf = null;
	    String outputMaf = null;

	    // default config params
	    boolean useCache = true;    // use cache or not
	    boolean sort = false;       // sort output MAF cols or not
	    boolean addMissing = false; // add missing standard cols or not


	    // process program arguments

	    int i;

	    for (i = 0; i < args.length; i++)
	    {
		    if (args[i].startsWith("-"))
		    {
			    if (args[0].equalsIgnoreCase("-nocache"))
			    {
				    useCache = false;
			    }
			    else if (args[0].equalsIgnoreCase("-sort"))
			    {
				    sort = true;
			    }
			    else if (args[0].equalsIgnoreCase("-std"))
			    {
				    addMissing = true;
			    }
		    }
		    else
		    {
			    break;
		    }
	    }

	    if (args.length - i < 2)
	    {
		    System.out.println("command line usage: oncotateMaf.sh [-nocache] [-sort] [-std] " +
		                       "<input_maf_file> <output_maf_file>");
		    System.exit(1);
	    }

	    inputMaf = args[i];
	    outputMaf = args[i+1];

	    int oncoResult = 0;

        try
        {
	        oncoResult = driver(inputMaf,
				outputMaf,
				useCache,
				sort,
				addMissing);
        }
        catch (RuntimeException e)
        {
            System.out.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
	        System.exit(1);
        }
        finally
        {
	        // check errors at the end
	        if (oncoResult != 0)
	        {
		        // TODO produce different error codes, for different types of errors?
		        System.out.println("Process completed with " + oncoResult + " error(s).");
		        System.exit(2);
	        }
        }
    }

	public static int driver(String inputMaf,
			String outputMaf,
			boolean useCache,
			boolean sort,
			boolean addMissing)
	{
		Date start = new Date();
		int oncoResult = 0;

		Oncotator tool = new Oncotator(useCache);
		tool.setSortColumns(sort);
		tool.setAddMissingCols(addMissing);

		try {
			oncoResult = tool.oncotateMaf(new File(inputMaf),
			                 new File(outputMaf));
		} catch (IOException e) {
			System.out.println("IO error occurred: " + e.getMessage());
			e.printStackTrace();
		} catch (OncotatorServiceException e) {
			System.out.println("Service error occurred: " + e.getMessage());
			e.printStackTrace();
		}
		finally {
			Date end = new Date();
			double timeElapsed = (end.getTime() - start.getTime()) / 1000.0;

			System.out.println("Total number of records processed: " +
			                   tool.getNumRecordsProcessed());

			if (tool.getBuildNumErrors() > 0)
			{
				System.out.println("Number of records skipped due to incompatible build no: " +
				                   tool.getBuildNumErrors());
			}

			System.out.println("Total time: " + timeElapsed + " seconds.");
		}

		return oncoResult;
	}
}