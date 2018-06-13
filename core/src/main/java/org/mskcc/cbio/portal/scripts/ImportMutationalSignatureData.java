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
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.MutationalSignature;
import org.mskcc.cbio.portal.util.*;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.apache.commons.cli.*;

import java.io.*;
import java.util.*;


public class ImportMutationalSignatureData extends ConsoleRunnable {

    @Override
    public void run() {
        try {
            String progName = "ImportMutationalSignatures";
            String description = "Import mutational signature data files.";

            Options options = ImportMutationalSignatureData.getOptions(args);
            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = null;
            commandLine = parser.parse(options, args);

            if(commandLine.hasOption( "h" ) ){
                System.out.println("Command line arguments are: 'h' for help, 'mut-sig-file' for the mutational signature file, 'replace' to replace data already in the database");
            }
            
            // Check options
            boolean replace = commandLine.hasOption("replace");

            ProgressMonitor.setConsoleMode(true);

            File mutationalSignatureFile;
            int numLines;
            if(commandLine.hasOption("mut-sig-file")) {
                mutationalSignatureFile = new File((String) commandLine.getOptionValue("mut-sig-file"));

                System.out.println("Reading gene data from:  " + mutationalSignatureFile.getAbsolutePath());
                numLines = FileUtil.getNumLines(mutationalSignatureFile);
                System.out.println(" --> total number of lines:  " + numLines);
                ProgressMonitor.setMaxValue(numLines);
                ImportMutationalSignatureData.importData(mutationalSignatureFile, replace);
            }
            
        } 
        catch(ParseException ex){
                System.out.println(ex.getMessage()); 
        }
        catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Adds the mutational signatures parsed from the file into the Database.
     * This is for importing mutational signature metadata
     * @param mutationalSignatureFile File with mutational signature information
     * @throws IOException
     * @throws DaoException
     */
    
    public static void importData (File mutationalSignatureFile, boolean replace) throws IOException, DaoException{
        try{
            FileReader reader = new FileReader(mutationalSignatureFile);
            BufferedReader buf = new BufferedReader(reader);
            String line;
            while ((line = buf.readLine()) != null){
                ProgressMonitor.incrementCurValue();
                ConsoleUtil.showProgress();
                
                
                String[] parts = line.split("\t");
                boolean arrayIsEmpty = false;
                for(int i = 0; i < parts.length; i++){
                    if (parts[i].equals("") || parts[i] == null){
                        arrayIsEmpty = true;
                    }
                    else{
                        arrayIsEmpty = false;
                    }
                }

                if (!arrayIsEmpty){
                    //assume mutational signature id and description are first two columns in file
                    MutationalSignature mutationalSignature = new MutationalSignature();
                    mutationalSignature.setMutationalSignatureId(parts[0]);
                    mutationalSignature.setDescription(parts[1]);

                    //check if mutational signature already exists by the mutational signature id
                    MutationalSignature existingMutationalSignature = DaoMutationalSignature.getMutationalSignatureById(mutationalSignature.getMutationalSignatureId());

                    // only replaces description
                    if (replace) {
                        if ( existingMutationalSignature == null) {
                            ProgressMonitor.logWarning("Could not find mutational signature: " + parts[0] + " in DB. Record will be skipped.");
                        } else {
                            ProgressMonitor.setCurrentMessage("Updating mutational signature: " + parts[0]);

                            // Get mutational signature id for the already existing gene
                            mutationalSignature.setMutationalSignatureId(existingMutationalSignature.getMutationalSignatureId());
                            DaoMutationalSignature.updateMutationalSignature(mutationalSignature, true);
                        }

                        // Add a new mutational signature
                    } else {
                        if (existingMutationalSignature == null){
                            ProgressMonitor.setCurrentMessage("Adding: " + mutationalSignature.getMutationalSignatureId());
                            DaoMutationalSignature.addMutationalSignature(mutationalSignature);
                        }
                        else{
                            ProgressMonitor.logWarning("Mutational Signature" + existingMutationalSignature.getMutationalSignatureId() + " already exists in the database");
                        }
                    }
                }
                else{
                    ProgressMonitor.logWarning("File does not contain data.");
                }
                
            }
        }
        catch(Exception e){
            throw e;
        }
    }
    
    /**
     * Sets command line arguments
     *
     * @param args  the arguments given on the command line
     */
    private static Options getOptions(String[] args) {
        Options options = new Options();
        options.addOption("h", "help", false, "print this help info.")
            .addOption("mut-sig-file", "mutational-signatures-file", true, "import mutational signatures metadata file")
            .addOption("replace", true, "replace mutational signatures info");
        return options;
        
    }
    
    public ImportMutationalSignatureData(String[] args){
        super(args);
    }
    
    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportMutationalSignatureData(args);
        runner.runInConsole();
    }

}