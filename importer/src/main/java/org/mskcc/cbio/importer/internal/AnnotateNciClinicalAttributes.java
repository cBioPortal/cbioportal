/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
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
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.mskcc.cbio.importer.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.DatabaseUtils;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.Importer;
import org.mskcc.cbio.importer.model.BcrClinicalAttributeEntry;
import org.mskcc.cbio.importer.model.ReferenceMetadata;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

//quick and dirty
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class AnnotateNciClinicalAttributes implements Importer {
//public class AnnotateNciClinicalAttributes {

    // our logger
    private static final Log LOG = LogFactory.getLog(ImporterImpl.class);

    // ref to configuration
    private Config config;

    // ref to file utils
    private FileUtils fileUtils;

    // ref to database utils
    private DatabaseUtils databaseUtils;

    public static final String DICT_ENTRY = "dictEntry";
    public static final String NAME = "name";

//    public static void main(String[] args) throws Exception {
//
//        System.err.println("NCI Clinical Attributes are not being annotated as of now.");
////        if(args.length != 1) {
////            System.err.println("Wrong number of arguments (expected 1, got " + args.length + ")" );
////            System.exit(-1);
////        }
////
////        ApplicationContext context =
////                new ClassPathXmlApplicationContext("/Users/dresdneg/dev/cbio-cancer-genomics-portal/importer/src/main/resources/applicationContext-importer.xml");
////        Config config = (Config) context.getBean("config");
////                        "$PORTAL_HOME/importer/src/main/resources/applicationContext-importer.xml");
////
////        String path = args[0].trim();
////        File xml = new File(path);
//    }

    /**
     * Constructor.
     *
     * @param config Config
     * @param fileUtils FileUtils
     * @param databaseUtils DatabaseUtils
     */
    public AnnotateNciClinicalAttributes(Config config, FileUtils fileUtils, DatabaseUtils databaseUtils) {

        // set members
        this.config = config;
        this.fileUtils = fileUtils;
        this.databaseUtils = databaseUtils;
    }

//    @Override
    public void importData(String portal, Boolean initPortalDatabase, Boolean initTumorTypes, Boolean importReferenceData) throws Exception {
        throw new UnsupportedOperationException();
    }

//    @Override
    public void importReferenceData(ReferenceMetadata referenceMetadata) throws Exception {
        String bcrXmlFilename = referenceMetadata.getImporterArgs().get(0);
        importReferenceData(bcrXmlFilename);
    }

    public void importReferenceData(String bcrXmlFilename) throws IOException, SAXException, ParserConfigurationException {
        List<BcrClinicalAttributeEntry> bcrs = parseXML(bcrXmlFilename);

        for (BcrClinicalAttributeEntry bcr : bcrs) {
//            System.out.println(bcr);
            config.updateClinicalAttributesMetadata(bcr);
        }
    }

    public List<BcrClinicalAttributeEntry> parseXML(String xmlFilename)
            throws ParserConfigurationException, SAXException, IOException {
        List<BcrClinicalAttributeEntry> bcrs = new ArrayList<BcrClinicalAttributeEntry>();

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        BcrDictHandler handler = new BcrDictHandler(bcrs);
        saxParser.parse(xmlFilename, handler);

        return bcrs;
    }
}
