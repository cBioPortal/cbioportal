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
package org.mskcc.cbio.portal.util;

import org.apache.commons.lang.StringUtils;
import org.mskcc.cbio.portal.model.ClinicalData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ClinicalReader {

    private File clinicalFile;
    private int cancerStudyId;

    String DELIMITER = "\t";
    String COMMENT = "#";
    String CASE_ID = "CASE_ID";

    public ClinicalReader() { }

    public ArrayList<ClinicalData> read(File file, int cancerStudyId) throws IOException {

        FileReader reader = new FileReader(file);
        BufferedReader buf = new BufferedReader(reader);

        // get case id field, all other fields are clinical attributes
        String line = buf.readLine();
        int caseIdField = -1;

        String[] fields = line.split(DELIMITER);
        int fields_length = fields.length;

        for (int i = 0; i < fields_length; i++) {
            String field = fields[i];

            if (StringUtils.equals(field, CASE_ID)) {
                caseIdField = i;
            }
        }

        if (caseIdField == -1) {
            throw new IOException(String.format("%s does not have a column called 'CASE_ID'", file.getName()));
        }

        line = buf.readLine();

        while (line != null) {
            System.out.println(line);
        }

        ArrayList<ClinicalData> clinicals = new ArrayList<ClinicalData>();

        return clinicals;
    }
}
