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
 ** Memorial Sloan-Kettering Cancer Center_
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center_
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
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
