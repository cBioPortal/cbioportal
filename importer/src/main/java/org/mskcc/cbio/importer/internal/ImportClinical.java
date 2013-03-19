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
import org.mskcc.cbio.importer.converter.internal.ClinicalDataConverterImpl;
import org.mskcc.cbio.importer.model.ClinicalAttributesMetadata;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;
import java.util.*;

public class ImportClinical {

    public static final String IGNORE_LINE_PREFIX = "#";
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
            System.out.println("command line usage:  importClinical.pl <clinical.txt> <cancer_study_id>");
            System.exit(1);
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File clinical_f = new File(args[0]);
        BufferedReader reader = new BufferedReader(new FileReader(clinical_f));

        // give the user some info
        System.out.println("Reading data from:  " + clinical_f.getAbsolutePath());
        int numLines = FileUtil.getNumLines(clinical_f);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);

        String line = reader.readLine();
        String[] colnames = line.split(DELIMITER);
        ImportClinical importer = new ImportClinical();
        importer.updateAttrDb(colnames);            // update the database with new clinical attributes

        // map: String colnames -> ClinicalAttribute attrs
        List<ClinicalAttribute> columnAttrs = new ArrayList<ClinicalAttribute>();
        for (String colname : colnames) {
            ClinicalAttribute attr = DaoClinicalAttribute.getDatum(colname);
            columnAttrs.add(attr);
        }

        int cancerStudyId = DaoCancerStudy.getCancerStudyByStableId(args[1]).getInternalId();
        line = reader.readLine();
        List<Clinical> clinicals = new ArrayList<Clinical>();
        while (line != null) {

            if (line.substring(0,1).equals(IGNORE_LINE_PREFIX)) {
                line = reader.readLine();
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

            line = reader.readLine();
        }

        ConsoleUtil.showWarnings(pMonitor);

        DaoClinical.addAllData(clinicals);
        System.err.println("Done.");
    }

    /**
     * Takes a list of colnames and updates the database accordingly.
     * If there are colnames that are not in the database then, look for them in the spreadsheet
     * and import them into the database.
     *
     * If they are not OKayed in the spreadsheet throw an exception
     * @param colnames
     */
    public void updateAttrDb(String[] colnames) throws DaoException {
        Collection<ClinicalAttribute> allAttrs = DaoClinicalAttribute.getAll();

        // map: allAttrs -> names of all attrs (projection)
        Collection<String> allDisplayNames = new ArrayList<String>();
        for (ClinicalAttribute attr : allAttrs) {
            allDisplayNames.add(attr.getAttrId());
        }

        // compute colnames - setminus - allDisplayNames
        // find all the columns in the staging file that
        // are not in the db
        Collection<String> colnamesNotInDb =  new ArrayList<String>();
        for (String name : Arrays.asList(colnames)) {
            if (!allDisplayNames.contains(name)) {
                colnamesNotInDb.add(name);
            }
        }

        if (colnamesNotInDb.isEmpty()) {
            return;     // nothing to do, die
        }

        // make a map: String name -> ClinicalAttributeMetadata
        // out of all the OKayed attributes
        Collection<ClinicalAttributesMetadata> attrMetadatas = config.getClinicalAttributesMetadata(Config.ALL);
        Map<String, ClinicalAttributesMetadata> name2meta = new HashMap<String, ClinicalAttributesMetadata>();

        for (ClinicalAttributesMetadata attr : attrMetadatas) {
            if (attr.getAnnotationStatus().equals(ClinicalDataConverterImpl.OK)) {
                name2meta.put(attr.getColumnHeader(), attr);
            }
        }

        for (String colname : colnamesNotInDb) {
            ClinicalAttributesMetadata attr = name2meta.get(colname);

            if (attr == null) {
                throw new DaoException("column name [" + colname + "] is in the " +
                        "staging file but not OKayed in the spreadsheet");
            }

            DaoClinicalAttribute.addDatum(new ClinicalAttribute(attr.getColumnHeader(),
                    attr.getDisplayName(),
                    attr.getDescription(),
                    attr.getDatatype()));
        }
    }
}
