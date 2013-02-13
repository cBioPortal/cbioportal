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
package org.mskcc.cbio.cgds.scripts;

import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.dao.DaoClinical;
import org.mskcc.cbio.cgds.dao.DaoClinicalAttribute;
import org.mskcc.cbio.cgds.model.Clinical;
import org.mskcc.cbio.cgds.model.ClinicalAttribute;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ImportClinical {

    // our logger
    private static Log LOG = LogFactory.getLog(ImportClinical.class);

    // "commented out" string, i.e.metadata
    public static final String IGNORE_LINE_PREFIX = "#";

    public static final String DELIMITER = "\t";
    public static final String CASE_ID = "CASE_ID";

    public static String readCancerStudyId(String filename) throws IOException {

        FileReader metadata_f = new FileReader(filename);
        BufferedReader metadata = new BufferedReader(metadata_f);

        String line = metadata.readLine();
        while (line != null) {

            String[] fields = line.split(":");

            if (fields[0].trim().equals("cancer_study_identifier")) {
                return fields[1].trim();
            }

            line = metadata.readLine();
        }

        throw new IOException("cannot find cancer_study_identifier");
    }

    /**
     *
     * Import clinical data.
     *
     * Go over every attribute and check whether it exists in the db (clinical_attribute table).  If it exist, then
     * assume that it has been OKayed in the google doc.  If not, check the google doc.
     *
     * If it exists in the google doc and has been OKayed,
     * import it into the database.
     * If it exists in the google doc but has not been OKayed,
     * ignore.
     * If it does not exist in the google doc,
     * add it to the google doc with status "Unannotated" (but do not add to database)
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("command line usage:  importClinical.pl <clinical.txt> <metadata.txt>");
            System.exit(1);
        }

        FileReader clinical_f = new FileReader(args[0]);
        BufferedReader reader = new BufferedReader(clinical_f);
        String line = reader.readLine();
        String[] colnames = line.split(DELIMITER);

        // List of ClinicalAttributes corresponds to the names of the columns
        List<ClinicalAttribute> columnAttrs = new ArrayList<ClinicalAttribute>();
        for (String colname : colnames) {
            ClinicalAttribute attr = DaoClinicalAttribute.getDatum(colname);
            columnAttrs.add(attr);
        }

        line = reader.readLine();
        List<Clinical> clinicals = new ArrayList<Clinical>();
        while (line != null) {

            if (line.substring(0,1).equals(IGNORE_LINE_PREFIX)) {
                line = reader.readLine();
                continue;
            }

            String cancer_study_identifier = readCancerStudyId(args[1]);
            int cancerStudyId = DaoCancerStudy.getCancerStudyByStableId(cancer_study_identifier).getInternalId();

            String[] fields = line.split(DELIMITER);
            String caseId = null;
            for (int i = 0; i < fields.length; i++) {
                Clinical clinical = new Clinical();
                clinical.setCancerStudyId(cancerStudyId);

                if (columnAttrs.get(i).getAttributeId().equals(CASE_ID)) {
                    caseId = fields[i];
                    continue;
                } else {
                    clinical.setCaseId(caseId);
                    clinical.setAttrId(columnAttrs.get(i).getAttributeId());
                    clinical.setAttrVal(fields[i]);
                    clinicals.add(clinical);
//                    System.out.println(clinical);
                }
            }

            line = reader.readLine();
        }

        DaoClinical.addAllData(clinicals);
    }
}
