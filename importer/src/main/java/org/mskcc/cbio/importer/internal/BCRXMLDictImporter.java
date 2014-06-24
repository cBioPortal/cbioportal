/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.importer.internal;

import org.mskcc.cbio.importer.*;
import org.mskcc.cbio.importer.model.ReferenceMetadata;
import org.mskcc.cbio.importer.model.BCRDictEntry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.xml.sax.SAXException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

public class BCRXMLDictImporter extends ImporterBaseImpl implements Importer
{
    private static final Log LOG = LogFactory.getLog(BCRXMLDictImporter.class);

    private Config config;
    private FileUtils fileUtils;
    private DatabaseUtils databaseUtils;

    public BCRXMLDictImporter(Config config, FileUtils fileUtils, DatabaseUtils databaseUtils)
    {
        this.config = config;
        this.fileUtils = fileUtils;
        this.databaseUtils = databaseUtils;
    }

    @Override
    public void importData(String portal, Boolean initPortalDatabase,
                           Boolean initTumorTypes, Boolean importReferenceData) throws Exception
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void importCancerStudy(String cancerStudyDirectoryName, boolean skip, boolean force) throws Exception
    {
		throw new UnsupportedOperationException();
    }
        
    @Override
        public void importTypesOfCancer() throws Exception
    {
		throw new UnsupportedOperationException();
    }

    @Override
    public void importCaseLists(String portal) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void importReferenceData(ReferenceMetadata referenceMetadata) throws Exception
    {
        String bcrXmlFilename = referenceMetadata.getImporterArgs().get(0);

        if (!bcrXmlFilename.isEmpty()) {
            logMessage(LOG, "importReferenceData, processing Biospecimen Core Resource dictionary: " +
                       bcrXmlFilename);
            config.importBCRClinicalAttributes(parseXML(bcrXmlFilename));
        }
        else {
            logMessage(LOG, "importReferenceData, missing Biospecimen Core Resource dictionary filename.");
        }
    }

    private List<BCRDictEntry> parseXML(String xmlFilename) throws Exception
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        List<BCRDictEntry> bcrs = new ArrayList<BCRDictEntry>();
        BCRXMLDictParser handler = new BCRXMLDictParser(bcrs);
        saxParser.parse(xmlFilename, handler);

        return bcrs;
    }
}
