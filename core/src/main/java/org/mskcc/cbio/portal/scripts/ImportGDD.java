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

import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.service.GDDService;

import java.io.*;
import java.util.*;
/**
 * Command Line Tool to Import GDD Data.
 */
public class ImportGDD {
    
    public static void importData(File dataFile, ProgressMonitor pMonitor) throws Exception {
        try (FileReader reader = new FileReader(dataFile)) {
            pMonitor.setCurrentMessage("Read data from: " + dataFile.getAbsolutePath());
            BufferedReader buf = new BufferedReader(reader);
            
            String column_names[] = buf.readLine().split("\t");
            if (column_names[0].trim().equals("STABLE_ID") && column_names[1].trim().equals("CLASSIFICATION")) {
                String line = buf.readLine();
                while (line != null) {
                    if (line.startsWith("#")) {
                        continue;
                    }

                    String columns[] = line.split("\t");
                    String stable_id = columns[0];
                    String classification = columns[1];

                    System.out.println(stable_id + " " + classification);
                    GDDService gddService = SpringUtil.getGddService();
                    gddService.insertGddData(stable_id, classification);

                    line = buf.readLine();                
                }
                System.out.println("Finished reading file.");
            }
            else {
                System.out.println("Column name error.");
            }
        }
    }
    
    public static void main(String args[]) throws Exception {
        String filename;        
        if (args.length < 1) {
            filename = "/Users/angelica/GDDProject/data_gdd.txt";
//            System.out.println("Missing GDD data file.");
//            return;
        }
        else {
            filename = args[0];
        }
        
        ProgressMonitor pMonitor = new ProgressMonitor();
        ProgressMonitor.setConsoleMode(true);
        File dataFile = new File(filename);
        
        if (dataFile.isDirectory()) {
            File files[] = dataFile.listFiles();
            
            for (File file : files) {
                if (file.getName().endsWith(".txt")) {
                    ImportGDD.importData(file, pMonitor);
                }
            }
            
            if (files.length == 0) {
                ProgressMonitor.setCurrentMessage("No GDD data found in directory, skipping import: " + dataFile.getCanonicalPath());
            }
        }
        else {
            ImportGDD.importData(dataFile, pMonitor);
        }
    }
    
}
