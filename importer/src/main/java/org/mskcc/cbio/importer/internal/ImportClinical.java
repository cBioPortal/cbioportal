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
package org.mskcc.cbio.importer.internal;

import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.dao.DaoClinical;
import org.mskcc.cbio.cgds.dao.DaoClinicalAttribute;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.Clinical;
import org.mskcc.cbio.cgds.model.ClinicalAttribute;
import org.mskcc.cbio.cgds.util.ConsoleUtil;
import org.mskcc.cbio.cgds.util.FileUtil;
import org.mskcc.cbio.cgds.util.ProgressMonitor;
import org.mskcc.cbio.importer.Config;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;
import java.util.*;

public class ImportClinical {

    public static final String METADATA_PREIX = "#";
    public static final String DELIMITER = "\t";
    public static final String CASE_ID = "CASE_ID";

    public static final String contextFile = "classpath:applicationContext-importer.xml";
    private static final ApplicationContext context = new ClassPathXmlApplicationContext(contextFile);
    public Config config = (Config) context.getBean("config");

    public ImportClinical() { }

    public String readCancerStudyId(String filename) throws IOException {

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
     * Imports clinical data and clinical attributes (from the worksheet)
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("command line usage:  importClinical <clinical.txt> <cancer_study_id>");
            System.exit(1);
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File clinical_f = new File(args[0]);
        FileReader reader =  new FileReader(clinical_f);
        BufferedReader buff = new BufferedReader(reader);

        // give the user some info
        System.out.println("Reading data from:  " + clinical_f.getAbsolutePath());
        int numLines = FileUtil.getNumLines(clinical_f);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);

        List<ClinicalAttribute> columnAttrs = ImportClinical.grabAttrs(buff);
        int cancerStudyId = DaoCancerStudy.getCancerStudyByStableId(args[1]).getInternalId();

        String line = buff.readLine();
        List<Clinical> clinicals = new ArrayList<Clinical>();
        while (line != null) {

            if (line.substring(0,1).equals(METADATA_PREIX)) {
                // ignore lines with the METADATA_PREFIX
                line = buff.readLine();
                continue;
            }

            String[] fields = line.split(DELIMITER);
            String caseId = null;
            for (int i = 0; i < fields.length; i++) {
                Clinical clinical = new Clinical();
                clinical.setCancerStudyId(cancerStudyId);

                if (columnAttrs.get(i).getAttrId().equals(CASE_ID)) {
                    caseId = fields[i];
                    continue;
                } else {
                    clinical.setCaseId(caseId);
                    clinical.setAttrId(columnAttrs.get(i).getAttrId());
                    clinical.setAttrVal(fields[i]);
                    clinicals.add(clinical);
                }
            }

            line = buff.readLine();
        }

        ConsoleUtil.showWarnings(pMonitor);

        DaoClinical.addAllData(clinicals);
        System.err.println("Done.");
    }

    /**
     * helper function for spliting the *next* line in the reader
     * so, N.B. --  ! it alters the state of the reader
     * @param bufferedReader
     * @return
     */
    private static String[] splitFields(BufferedReader buff) throws IOException {
        String line = buff.readLine();
        line = line.replaceAll(METADATA_PREIX, "");
        String[] fields = line.split(DELIMITER);

        return fields;
    }

    /**
     * Grabs the metadatas (clinical attributes) from the file, inserts them into the database,
     * and returns them as a list.
     *
     * @param reader
     * @return clinicalAttributes
     */
    public static List<ClinicalAttribute> grabAttrs(BufferedReader buff) throws DaoException, IOException {
        List<ClinicalAttribute> attrs = new ArrayList<ClinicalAttribute>();

        String[] colnames = splitFields(buff);
        String[] displayNames = splitFields(buff);
        String[] descriptions = splitFields(buff);
        String[] datatypes = splitFields(buff);

        if (displayNames.length != colnames.length
                ||  descriptions.length != colnames.length
                ||  datatypes.length != colnames.length) {
            throw new DaoException("attribute and metadata mismatch in clinical staging file");
        }

        for (int i = 0; i < colnames.length; i+=1) {
            ClinicalAttribute attr =
                    new ClinicalAttribute(colnames[i], displayNames[i], descriptions[i], datatypes[i]);
            try {
                DaoClinicalAttribute.getDatum(attr.getAttrId());
            } catch (DaoException e) {
                DaoClinicalAttribute.addDatum(attr);
            }
            attrs.add(attr);
        }

        return attrs;
    }
}
