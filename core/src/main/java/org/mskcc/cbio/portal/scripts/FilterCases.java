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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

/**
 * Filter cases in files
 * @author jgao
 */
public class FilterCases {
    public static void main(String[] args) throws Exception {
	if (args.length == 0) {
            System.out.println("command line usage:  filterCases.pl regex.txt input_file output_file id_row_or_col starting_row_or_col:\n"
		    + "\te.g. filterCases.pl case_id.txt input_file output_file c10 r1");
            return;
        }
	
	Set<String> regexes = fileToSet(args[0]);
	String concatRegex = "(("+StringUtils.join(regexes, ")|(")+"))";
	
	String strIdIx = args[3].toLowerCase(); // row or column of IDs
	int idIx = Integer.parseInt(strIdIx.substring(1));
	
	String strStart = args[4].toLowerCase(); // starting to filter
	boolean byRow = strStart.startsWith("r");
	int start = Integer.parseInt(strStart.substring(1));
	
	if (byRow) {
	    cutRows(args[1], args[2], concatRegex, idIx, start);
	} else {
	    cutColumns(args[1], args[2], concatRegex, idIx, start);
	}
	
    }
    
    private static Set<String> fileToSet(String caseIdFile) throws IOException {
	BufferedReader in = new BufferedReader(new FileReader(caseIdFile));
	Set<String> ret = new HashSet<String>();
	for (String line = in.readLine(); line != null; line = in.readLine()) {
	    ret.add(line);
	}
	return ret;
    }
    
    private static void cutColumns(String inFile, String outFile,
	    String concatRegex, int idRow, int startColum) throws IOException {
	
	BufferedReader in = new BufferedReader(new FileReader(inFile));
	
	for (int i=0; i<idRow; i++) {
	    in.readLine();
	}
	
	Pattern p = Pattern.compile(concatRegex);
	String line = in.readLine();
	
	String[] headers = line.split("\t");
	boolean[] keep = new boolean[headers.length];
	
	for (int i=0; i<startColum; i++) {
	    keep[i] = true;
	}
	
	for (int i=startColum; i<headers.length; i++) {
	    Matcher m = p.matcher(headers[i]);
	    keep[i] = m.find();
	}
	
	in.close();
	
	in = new BufferedReader(new FileReader(inFile));
	BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
	
	for (line = in.readLine(); line != null; line = in.readLine()) {
	    String[] parts = line.split("\t");
	    List<String> partsKept = new ArrayList<String>();
	    for (int i=0; i<parts.length; i++) {
		if (keep[i]) {
		    partsKept.add(parts[i]);
		}
	    }
	    out.write(StringUtils.join(partsKept,"\t"));
	    out.newLine();
	}
	in.close();
	out.close();
    }
    
    private static void cutRows(String inFile, String outFile,
	    String concatRegex, int idIx, int startColumn) throws IOException {
	
	BufferedReader in = new BufferedReader(new FileReader(inFile));
	BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
	for (int i=0; i<startColumn; i++) {
	    out.append(in.readLine());
	    out.newLine();
	}
	
	Pattern p = Pattern.compile(concatRegex);
	
	for (String line = in.readLine(); line != null; line = in.readLine()) {
	    String[] parts = line.split("\t");
	    Matcher m = p.matcher(parts[idIx]);
	    if (m.find()) {
		out.append(line);
		out.newLine();
	    }
	}
	in.close();
	out.close();
    }
}
