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
 * 
 * By James Xu
 */

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.MutationalSignatureMeta;
import org.mskcc.cbio.portal.util.*;

import org.apache.commons.cli.*;

import java.io.*;
import java.util.*;


public class ImportMutationalSignatureMetaData extends ConsoleRunnable {

    @Override
    public void run() {
        if (args.length == 0) {
            System.out.println(ImportMutationalSignatureMetaData.printImportMutationalSignatureMetadataHelp());
            return;
        }
        String progName = "ImportMutationalSignatureMetaData";
        Options options = ImportMutationalSignatureMetaData.getOptions(args);
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = null;
        
        try {
            commandLine = parser.parse(options, args);

            if(commandLine.hasOption( "h" ) ){
                System.out.println(ImportMutationalSignatureMetaData.printImportMutationalSignatureMetadataHelp());
            }
            
            // Check options
            boolean replace = commandLine.hasOption("r");

            ProgressMonitor.setConsoleMode(true);

            File mutationalSignatureMetaFile;
            int numLines;
            if(commandLine.hasOption("metafile")) {
                mutationalSignatureMetaFile = new File((String) commandLine.getOptionValue("metafile"));

                System.out.println("Reading gene data from:  " + mutationalSignatureMetaFile.getAbsolutePath());
                numLines = FileUtil.getNumLines(mutationalSignatureMetaFile);
                System.out.println(" --> total number of lines:  " + numLines);
                ProgressMonitor.setMaxValue(numLines);
                if(replace){
                    System.out.println("All mutational signature descriptions will be replaced with those in " + mutationalSignatureMetaFile.getAbsolutePath());
                }
                ImportMutationalSignatureMetaData.importData(mutationalSignatureMetaFile, replace);
            }
            
        } 
        catch(ParseException ex){
            throw new UsageException(progName, "Error parsing arguments.", ImportMutationalSignatureMetaData.printImportMutationalSignatureMetadataHelp());
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Adds the mutational signatures parsed from the file into the Database.
     * This is for importing mutational signature metadata
     * MetaData file has first column as mutational_signature_id and second column as description, each row is a new signature
     * @param mutationalSignatureMetaFile File with mutational signature information
     * @throws IOException
     * @throws DaoException
     */
    
    public static void importData (File mutationalSignatureMetaFile, boolean replace) throws IOException, DaoException{
        try{
            FileReader reader = new FileReader(mutationalSignatureMetaFile);
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
                    MutationalSignatureMeta mutationalSignatureMeta = new MutationalSignatureMeta();
                    mutationalSignatureMeta.setMutationalSignatureId(parts[0]);
                    mutationalSignatureMeta.setDescription(parts[1]);

                    //check if mutational signature already exists in database by the mutational signature id
                    MutationalSignatureMeta existingMutationalSignature = DaoMutationalSignature.getMutationalSignatureById(mutationalSignatureMeta.getMutationalSignatureId());
                    
                    if (existingMutationalSignature == null){ //add a new mutational signature if database does not contain it
                        ProgressMonitor.setCurrentMessage("Adding: " + mutationalSignatureMeta.getMutationalSignatureId());
                        DaoMutationalSignature.addMutationalSignature(mutationalSignatureMeta);
                    }
                    else{//if it already exists, either replace description or do nothing
                        if (replace){
                            ProgressMonitor.setCurrentMessage("Updating mutational signature: " + parts[0]);
                            DaoMutationalSignature.updateMutationalSignature(mutationalSignatureMeta); 
                        }
                        else{
                            ProgressMonitor.setCurrentMessage(parts[0] + " exists, but replace flag is false, so this signature's description will not be replaced.");
                        }
                    } 
                }
                else{
                    ProgressMonitor.logWarning("File does not contain data or file has blank lines at the end.");
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
        options.addOption("h", false, "print this help info.")
        .addOption("r",  false, "replace mutational signatures info");
        Option fileOption = new Option("metafile",true, "Required: import mutational signatures metadata file");
        fileOption.setRequired(true);
        options.addOption(fileOption);
        return options;
    }
    
    /**
     * returns the line to help user understand the different options available
    */
    private static String printImportMutationalSignatureMetadataHelp(){
        return "Command line arguments are: '-h' for help, " +
            "'-metafile' for the mutational signature metadata file (required and takes file path as parameter), " +
            "'-r' to replace data already in the database";
    }
    
    public ImportMutationalSignatureMetaData(String[] args){
        super(args);
    }
    
    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportMutationalSignatureMetaData(args);
        runner.runInConsole();
    }

}