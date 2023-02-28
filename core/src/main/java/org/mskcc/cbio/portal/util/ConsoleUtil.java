/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.util;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.scripts.UsageException;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * Misc Utility Methods for Console Applications.
 *
 * @author Ethan Cerami
 */
public class ConsoleUtil {
    private static String msg = "";

    /**
     * Outputs Progress Messages to Console.
     * Uses ASNI Terminal Codes
     * For future reference, ANSI Codes are here:
     * http://www.dee.ufcg.edu.br/~rrbrandt/tools/ansi.html
     *
     */
    public static synchronized void showProgress() {
        if (ProgressMonitor.isShowProgress()) {
            int currentValue = ProgressMonitor.getCurValue();
            if (currentValue % 100 == 0) {
                System.err.print(".");
            }
            
            // this writes progress every 1000 records
            if (currentValue % 1000 == 0) {
                NumberFormat format = DecimalFormat.getPercentInstance();
                double percent = ProgressMonitor.getPercentComplete();
                msg = new String("Percentage Complete:  "
                        + format.format(percent));
                System.err.println("\n" + msg);
                Runtime rt = Runtime.getRuntime();
                long used = rt.totalMemory() - rt.freeMemory();
                System.err.println("Mem Allocated:  " + getMegabytes(rt.totalMemory())
                        + ", Mem used:  " + getMegabytes(used) + ", Mem free:  "
                        + getMegabytes(rt.freeMemory()));
            }
            if (currentValue == ProgressMonitor.getMaxValue()) {
                System.err.println();
            }
        }
    }

    public static void showWarnings() {
        List<String> warningList = ProgressMonitor.getWarnings();
        if (warningList.size() > 0) {
            System.err.println("\nWarnings / Errors:");
            System.err.println("-------------------");
            for (int i = 0; i < warningList.size(); i++) {
                System.err.println(i + ".  " + warningList.get(i));
            }
        }
    }
    
    /**
     * Prints messages and warnings.
     */
    public static void showMessages() {
        showWarnings();
        List<String> debugMessages = ProgressMonitor.getDebugMessages();
        if (debugMessages.size() > 0) {
            System.err.println("-------------------");
            for (int i = 0; i < debugMessages.size(); i++) {
                System.err.println(debugMessages.get(i));
            }
        }
    }

    private static String getMegabytes(long bytes) {
        double mBytes = (bytes / 1024.0) / 1024.0;
        DecimalFormat formatter = new DecimalFormat("#,###,###.###");
        return formatter.format(mBytes) + " MB";
    }

    /**
     * Default method to be used when Importer class main method expects only 'data' and 'meta' as mandatory options 
     * and an optional 'loadMode' parameter
     *  
     * @param args: the same args given to main() method of the tool
     * @param description: short description of the tool (to display in the usage line if necessary)
     * @param hasLoadMode: set to true to let this method validate whether the command line argument loadMode was given 
     * 
     * @return the parsed options
     */
	public static OptionSet parseStandardDataAndMetaOptions(String[] args, String description, boolean hasLoadMode) {
		// using a real options parser, helps avoid bugs
		OptionParser parser = new OptionParser();
		parser.accepts("noprogress", "this option can be given to avoid the messages regarding memory usage and % complete");
		OptionSpec<Void> help = parser.accepts( "help", "print this help info" );
		parser.accepts( "data", "profile data file" ).withRequiredArg().describedAs( "data_file.txt" ).ofType( String.class );
		parser.accepts( "meta", "meta (description) file" ).withRequiredArg().describedAs( "meta_file.txt" ).ofType( String.class );
		if (hasLoadMode) {
			parser.accepts( "loadMode", "direct (per record) or bulk load of data" )
			          .withRequiredArg().describedAs( "[directLoad|bulkLoad (default)]" ).ofType( String.class );
		}
		String progName = "importScript";
		
		OptionSet options = null;
		try {
			options = parser.parse( args );
		} catch (OptionException e) {
			throw new UsageException(progName, description, parser,
			        e.getMessage());
		}
		  
		if( options.has( help ) ){
			throw new UsageException(progName, description, parser);
		}
		
		//these extra checks are needed, since withRequiredArg above only indicated that the option 
		//has a mandatory argument but does not make the option itself mandatory.
		if(!options.has("data")) {
			throw new UsageException(progName, description, parser,
			        "Error: 'data' argument required.");
		}
		
		if(!options.has("meta")) {
			throw new UsageException(progName, description, parser,
			        "Error: 'meta' argument required.");
		}

		if (hasLoadMode) {
			if( options.has( "loadMode" ) ){
				String actionArg = (String) options.valueOf( "loadMode" );
				if (actionArg.equalsIgnoreCase("directLoad")) {
					MySQLbulkLoader.bulkLoadOff();
				} else if (actionArg.equalsIgnoreCase( "bulkLoad" )) {
					MySQLbulkLoader.bulkLoadOn();
				} else {
					throw new UsageException(progName, description, parser,
							"Error: unknown loadMode action:  " + actionArg);
				}
			}
			else {
				throw new UsageException(progName, description, parser,
						"Error: 'loadMode' argument required.");
			}
		}
		return options;
	}
	
	
    /**
     * Default method to be used when Importer class main method expects only 'data' and 'study' as mandatory options 
     *  
     * @param args: the same args given to main() method of the tool
     * @param description: short description of the tool (to display in the usage line if necessary)
     * 
     * @return the parsed options
     */
	public static OptionSet parseStandardDataAndStudyOptions(String[] args, String description) {
		// using a real options parser, helps avoid bugs
		OptionParser parser = new OptionParser();
		parser.accepts("noprogress", "this option can be given to avoid the messages regarding memory usage and % complete");
		OptionSpec<Void> help = parser.accepts( "help", "print this help info" );
		parser.accepts( "data", "profile data file" ).withRequiredArg().describedAs( "data_file.txt" ).ofType( String.class );
		parser.accepts( "study", "cancer study identifier" ).withRequiredArg().describedAs( "e.g. brca_tcga" ).ofType( String.class );

        String progName = "importScript";

		OptionSet options = null;
		try {
			options = parser.parse( args );
		} catch (OptionException e) {
            throw new UsageException(progName, description, parser,
                    e.getMessage());
		}

		if( options.has( help ) ){
		    throw new UsageException(progName, description, parser);
		}
		//these extra checks are needed, since withRequiredArg above only indicated that the option 
		//has a mandatory argument but does not make the option itself mandatory.
		if(!options.has("data")) {
            throw new UsageException(progName, description, parser,
                    "Error: 'data' argument required.");
		}

		if(!options.has("study")) {
            throw new UsageException(progName, description, parser,
                    "Error: 'study' argument required.");
		}

		return options;
    }
    
    /**
     * Default method to be used when Importer class main method expects only 'data' and 'meta' as mandatory options 
     * and an optional 'loadMode' parameter and an optional 'update-info' parameter
     *  
     * @param args: the same args given to main() method of the tool
     * @param description: short description of the tool (to display in the usage line if necessary)
     * @param hasLoadMode: set to true to let this method validate whether the command line argument loadMode was given 
     * 
     * @return the parsed options
     */
	public static OptionSet parseStandardDataAndMetaUpdateOptions(String[] args, String description, boolean hasLoadMode) {
		// using a real options parser, helps avoid bugs
		OptionParser parser = new OptionParser();
		parser.accepts("noprogress", "this option can be given to avoid the messages regarding memory usage and % complete");
		OptionSpec<Void> help = parser.accepts( "help", "print this help info" );
        parser.accepts( "data", "profile data file" ).withRequiredArg().describedAs( "data_file.txt" ).ofType( String.class );
        parser.accepts( "update-info", "Update information for existing entities in the database").withOptionalArg().ofType(String.class);
		parser.accepts( "meta", "meta (description) file" ).withRequiredArg().describedAs( "meta_file.txt" ).ofType( String.class );
		if (hasLoadMode) {
			parser.accepts( "loadMode", "direct (per record) or bulk load of data" )
			          .withRequiredArg().describedAs( "[directLoad|bulkLoad (default)]" ).ofType( String.class );
		}
		String progName = "importScript";
		
		OptionSet options = null;
		try {
			options = parser.parse( args );
		} catch (OptionException e) {
			throw new UsageException(progName, description, parser,
			        e.getMessage());
		}
		  
		if( options.has( help ) ){
			throw new UsageException(progName, description, parser);
		}
		
		//these extra checks are needed, since withRequiredArg above only indicated that the option 
		//has a mandatory argument but does not make the option itself mandatory.
		if(!options.has("data")) {
			throw new UsageException(progName, description, parser,
			        "Error: 'data' argument required.");
		}
		
		if(!options.has("meta")) {
			throw new UsageException(progName, description, parser,
			        "Error: 'meta' argument required.");
		}

		if (hasLoadMode) {
			if( options.has( "loadMode" ) ){
				String actionArg = (String) options.valueOf( "loadMode" );
				if (actionArg.equalsIgnoreCase("directLoad")) {
					MySQLbulkLoader.bulkLoadOff();
				} else if (actionArg.equalsIgnoreCase( "bulkLoad" )) {
					MySQLbulkLoader.bulkLoadOn();
				} else {
					throw new UsageException(progName, description, parser,
							"Error: unknown loadMode action:  " + actionArg);
				}
			}
			else {
				throw new UsageException(progName, description, parser,
						"Error: 'loadMode' argument required.");
			}
		}
		return options;
	}
}
