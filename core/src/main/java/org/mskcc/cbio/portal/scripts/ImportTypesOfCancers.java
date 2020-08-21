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

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.model.TypeOfCancer;

import java.io.*;
import java.util.*;

/**
 * Load all the types of cancer and their names from a file.
 *
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class ImportTypesOfCancers extends ConsoleRunnable {
    public void run() {
        try {
	    	if (args.length < 1) {
	            // an extra --noprogress option can be given to avoid the messages regarding memory usage and % complete
	            throw new UsageException(
	                    "importTypesOfCancer.pl",
	                    null,
	                    "<types_of_cancer.txt> <clobber>");
	        }
	
	        ProgressMonitor.setCurrentMessage("Loading cancer types...");
	        File file = new File(args[0]);
	        // default to clobber = true (existing behavior)
	        boolean clobber = (args.length > 1 && (args[1].equalsIgnoreCase("f") || args[1].equalsIgnoreCase("false"))) ? false : true;	
	        load(file, clobber);
        } catch (RuntimeException e) {
            throw e;
        } catch (IOException|DaoException e) {
            throw new RuntimeException(e);
        }
    }

    public static void load(File file, boolean clobber) throws IOException, DaoException {
		SpringUtil.initDataSource();
        if (clobber) DaoTypeOfCancer.deleteAllRecords(); //TODO - this option should not exist...in a relational DB it basically means the whole DB is cleaned-up...there should be more efficient ways to do this...and here it is probably an unwanted side effect. REMOVE??
        TypeOfCancer aTypeOfCancer = new TypeOfCancer();
        Scanner scanner = new Scanner(file);
        int numNewCancerTypes = 0;
        
        while(scanner.hasNextLine()) {
            String[] tokens = scanner.nextLine().split("\t", -1);
            if (tokens.length != 5) {
                throw new IOException(
                    "Cancer type file '" + file.getPath() +
                    "' is not a five-column tab-delimited file");
            }
            
            String typeOfCancerId = tokens[0].trim();
            //if not clobber, then existing cancer types should be skipped:
            if (!clobber && DaoTypeOfCancer.getTypeOfCancerById(typeOfCancerId.toLowerCase()) != null ) {
            	ProgressMonitor.logWarning("Cancer type with id '" + typeOfCancerId + "' already exists. Skipping.");
            }
            else {
	            aTypeOfCancer.setTypeOfCancerId(typeOfCancerId.toLowerCase());
	            aTypeOfCancer.setName(tokens[1].trim());
	            aTypeOfCancer.setDedicatedColor(tokens[3].trim());
	            aTypeOfCancer.setShortName(typeOfCancerId);
	            aTypeOfCancer.setParentTypeOfCancerId(tokens[4].trim().toLowerCase());
	            DaoTypeOfCancer.addTypeOfCancer(aTypeOfCancer);
	            numNewCancerTypes++;
            }
        }
        ProgressMonitor.setCurrentMessage(" --> Loaded " + numNewCancerTypes + " new cancer types.");
        ProgressMonitor.setCurrentMessage("Done.");
        ConsoleUtil.showMessages();
    }

    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args  the command line arguments to be used
     */
    public ImportTypesOfCancers(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportTypesOfCancers(args);
        runner.runInConsole();
    }
}
