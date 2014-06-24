/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/


package org.mskcc.cbio.portal.scripts;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jgao
 */
public class MergeProfileData {
    public static void main(String[] args) {
        if (args.length<3) {
            System.err.println( "incorrect number of arguments. Arguments should be "
                 + "'<output_file> <profile data file 1> <profile data file 2> ...'." );
            return;
        }
      
        try {
            Map<String,Map<String,String>> data = new LinkedHashMap<String,Map<String,String>>();
            Set<String> cases = new LinkedHashSet<String>();
            for (int i=1; i<args.length; i++) {
                readData(args[i], data, cases);
            }
            writeData(args[0], data, cases);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 
     * @param file
     * @return Map<Gene, Map<Case,Value>>
     * @throws IOException 
     */
    private static void readData(String file, Map<String,Map<String,String>> data,
            Set<String> cases) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        String line = in.readLine();
        String[] headers = line.split("\t");
        int nCases = headers.length;
        for (int i=1; i<nCases; i++) {
            cases.add(headers[i]);
        }
        while ((line=in.readLine())!=null) {
            String[] parts = line.split("\t");
            Map<String,String> map = new LinkedHashMap<String,String>();
            data.put(parts[0], map);
            for (int i=1; i<nCases; i++) {
                map.put(headers[i], parts[i]);
            }
        }
        in.close();
    }
    
    private static void writeData(String file, Map<String,Map<String,String>> data,
            Set<String> cases) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write("geneSymbol");
        for (String header : cases) {
            out.write("\t"+header);
        }
        out.newLine();
        
        for (Map.Entry<String,Map<String,String>> entry : data.entrySet()) {
            out.write(entry.getKey());
            Map<String,String> map = entry.getValue();
            for (String header : cases) {
                out.write("\t");
                String value = map.get(header);
                if (value==null) {
                    out.write("NA");
                } else {
                    out.write(value);
                }
            }
            out.newLine();
        }
        out.close();
    }
}
