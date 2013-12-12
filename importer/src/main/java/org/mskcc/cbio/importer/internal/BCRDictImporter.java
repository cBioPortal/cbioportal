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

public class BCRDictImporter extends ImporterBaseImpl implements Importer
{
    private static final Log LOG = LogFactory.getLog(BCRDictImporter.class);

    private Config config;
    private FileUtils fileUtils;
    private DatabaseUtils databaseUtils;

    public BCRDictImporter(Config config, FileUtils fileUtils, DatabaseUtils databaseUtils)
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
        BCRDictParser handler = new BCRDictParser(bcrs);
        saxParser.parse(xmlFilename, handler);

        return bcrs;
    }
}
