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

/**
 * Main class with the main method.
 */
public class MutationAssessorTool
{
	public static void main(String[] args)
	{
		String input;
		String output = null;

		boolean db = false;
		boolean sort = false;
		boolean addMissing = false;

		// process program arguments

		int i;

		for (i = 0; i < args.length; i++)
		{
			if (args[i].startsWith("-"))
			{
				if (args[0].equalsIgnoreCase("-db"))
				{
					db = true;
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

		if (db)
		{
			if (args.length - i < 1)
			{
				// TODO no input file or directory
				System.out.println("Error: Invalid number of arguments");
				System.exit(1);
			}
		}
		else if (args.length - i < 2)
		{
			// TODO output file not specified
			System.out.println("Error: Invalid number of arguments");
			System.exit(1);
		}

		input = args[i];

		if (args.length - i > 1)
		{
			output = args[i+1];
		}

		try
		{
			// if the switch -db is provided,
			// then process MA files and insert into DB
			if (db)
			{
				CacheBuilder builder;

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
			// else process a single MAF file and extend with MA information
			else
			{
				DataImporter importer = new DataImporter();
				importer.setSortColumns(sort);
				importer.setAddMissingCols(addMissing);

				// process given MAF file and add MA info if possible
				importer.addMutAssessorInfo(
						new File(input), new File(output));
			}
		}
		catch (Exception e)
		{
			System.out.println("Error occurred: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
