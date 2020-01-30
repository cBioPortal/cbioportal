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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;
import org.mskcc.cbio.portal.model.ClinicalData;

public class ClinicalReader {
    private File clinicalFile;
    private int cancerStudyId;

    String DELIMITER = "\t";
    String COMMENT = "#";
    String CASE_ID = "CASE_ID";

    public ClinicalReader() {}

    public ArrayList<ClinicalData> read(File file, int cancerStudyId)
        throws IOException {
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
            throw new IOException(
                String.format(
                    "%s does not have a column called 'CASE_ID'",
                    file.getName()
                )
            );
        }

        line = buf.readLine();

        while (line != null) {
            System.out.println(line);
        }

        ArrayList<ClinicalData> clinicals = new ArrayList<ClinicalData>();

        return clinicals;
    }
}
