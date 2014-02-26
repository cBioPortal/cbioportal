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
import org.mskcc.cbio.importer.model.*;

import org.apache.commons.logging.*;

import org.jsoup.*;
import org.jsoup.nodes.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class BCRHTMLDictImporter extends ImporterBaseImpl implements Importer
{
    private static final Log LOG = LogFactory.getLog(BCRHTMLDictImporter.class);

    private Config config;
    private FileUtils fileUtils;
    private DatabaseUtils databaseUtils;

    public BCRHTMLDictImporter(Config config, FileUtils fileUtils, DatabaseUtils databaseUtils)
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
        String bcrDictionaryURL = referenceMetadata.getImporterArgs().get(0);

        if (!bcrDictionaryURL.isEmpty()) {
            logMessage(LOG, "importReferenceData, processing Biospecimen Core Resource dictionary: " +
                       bcrDictionaryURL);
            config.importBCRClinicalAttributes(parseHTML(bcrDictionaryURL));
        }
        else {
            logMessage(LOG, "importReferenceData, missing Biospecimen Core Resource dictionary filename.");
        }
    }

    private List<BCRDictEntry> parseHTML(String bcrDictionaryURL) throws Exception
    {
        List<BCRDictEntry> bcrs = new ArrayList<BCRDictEntry>();

        Document doc = Jsoup.connect(bcrDictionaryURL).get();
        for (Element table : doc.select("table")) {
            for (Element row : table.select("tr")) {
                for (Element col : row.select("td")) {
                    System.out.println(col.text());
                }
            }
        }

        return bcrs;
    }
}
