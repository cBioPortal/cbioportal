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

package org.mskcc.cbio.mutassessor;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Main class with main method.
 */
public class MutationAssessorTool
{
	public static void main(String[] args)
	{
		DataImporter mafProcessor = new DataImporter();
		CacheBuilder builder;

		String input;
		String output = null;

		boolean db = false;

		// validate arguments
		if (args.length == 0)
		{
			// TODO invalid number of arguments, print usage
			System.out.println("Error: Invalid number of arguments");
			System.exit(1);
		}

		if (args[0].equals("-db"))
		{
			if (args.length < 2)
			{
				// TODO no input file or directory
				System.out.println("Error: Invalid number of arguments");
				System.exit(1);
			}
		}
		else if (args.length < 2)
		{
			// TODO output file not specified
			System.out.println("Error: Invalid number of arguments");
			System.exit(1);
		}

		// if the switch -db is provided, then process MA files and insert into DB
		if (args[0].equals("-db"))
		{
			db = true;
			input = args[1];

			if (args.length >= 2)
			{
				output = args[2];
			}
		}
		// else process a single MAF file and extend with MA information
		else
		{
			input = args[0];
			output = args[1];
		}

		try
		{
			// db switch is provided
			if (db)
			{
				// no output specified, write to DB (slower)
				if (output != null)
				{
					builder = new CacheBuilder(output);
				}
				// output specified, create an SQL script file (faster)
				else
				{
					builder = new CacheBuilder();
				}

				// start cache building process
				builder.buildCache(input);
			}
			// process given MAF file and add MA info if possible
			else
			{
				mafProcessor.addMutAssessorInfo(
						new File(input), new File(output));
			}
		}
		catch (IOException e)
		{
			System.out.println("IO error: " + e.getMessage());
			e.printStackTrace();
		}
		catch (SQLException e)
		{
			System.out.println("Database error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
