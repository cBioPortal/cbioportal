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
import org.mskcc.cbio.importer.model.*;

import org.apache.commons.logging.*;
import org.apache.commons.lang.StringUtils;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

public class BCRHTMLDictImporter extends ImporterBaseImpl implements Importer
{
    private static final String DISPLAY_AND_COLUMN_NAME_HEADER = "CDE Name";
    private static final String DEFINITION_HEADER = "Definition";
    private static final String DISEASE_TYPE_HEADER = "Disease Type";
    private static final String TUMOR_TYPES_DELIMITER = " \\| ";
    private static final String COLUMN_NAME_REGEX = "xmlTag: ";

    private static final Log LOG = LogFactory.getLog(BCRHTMLDictImporter.class);

    private Config config;
    private FileUtils fileUtils;
    private DatabaseUtils databaseUtils;
    
    private int displayAndColumnNameIndex;
    private int definitionIndex;
    private int tumorTypeIndex;

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
    public void importTypesOfCancer() throws Exception
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void importCaseLists(String portal) throws Exception
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void importReferenceData(ReferenceMetadata referenceMetadata) throws Exception
    {
        String bcrDictionaryFilename = referenceMetadata.getImporterArgs().get(0);

        if (!bcrDictionaryFilename.isEmpty()) {
            logMessage(LOG, "importReferenceData, processing Biospecimen Core Resource dictionary: " + bcrDictionaryFilename);
            config.importBCRClinicalAttributes(getBCRDictEntries(bcrDictionaryFilename));
        }
        else {
            logMessage(LOG, "importReferenceData, missing Biospecimen Core Resource dictionary filename.");
        }
    }

    private List<BCRDictEntry> getBCRDictEntries(String bcrDictionaryFilename) throws Exception
    {
        Document doc = Jsoup.parse(new File(bcrDictionaryFilename), null);
        return getBCRDictEntriesFromTable(doc.select("table").first());
    }

    private List<BCRDictEntry> getBCRDictEntriesFromTable(Element table)
    {
        List<BCRDictEntry> bcrs = new ArrayList<BCRDictEntry>();

        setColumnIndices(table.select("thead").first());
        for (Element row : table.select("tbody").first().select("tr")) {
            bcrs.addAll(getBCRDictEntryFromRow(row));
        }

        if (bcrs.isEmpty()) {
            fatal();
        }

        return bcrs;
    }

    private void setColumnIndices(Element htmlTableHeader)
    {
        displayAndColumnNameIndex = getColumnIndex(htmlTableHeader, DISPLAY_AND_COLUMN_NAME_HEADER);
        definitionIndex = getColumnIndex(htmlTableHeader, DEFINITION_HEADER);
        tumorTypeIndex = getColumnIndex(htmlTableHeader, DISEASE_TYPE_HEADER);
        if (displayAndColumnNameIndex < 0 || definitionIndex < 0 || tumorTypeIndex < 0) {
            fatal();
        }
    }

    private int getColumnIndex(Element htmlTableHeader, String attributeName)
    {
        Elements columns = htmlTableHeader.select("th");
        for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
            Element column = columns.get(colIndex);
            if (column.text().contains(attributeName)) {
                return colIndex;
            }
        }
        return -1;
    }

    private List<BCRDictEntry> getBCRDictEntryFromRow(Element row)
    {
        List<BCRDictEntry> bcrs = new ArrayList<BCRDictEntry>();

        // there can be multiple column attribute headers per row
        for (String columnName : getColumnNamesFromRow(row)) {
            BCRDictEntry bcr = new BCRDictEntry();
            bcr.id = columnName.trim();
            bcr.displayName = getDisplayNameFromRow(row);
            bcr.description = row.select("td").get(definitionIndex).text();
            bcr.cancerStudy = "";
            bcr.tumorType = getTumorTypesFromRow(row);
            bcrs.add(bcr);
        }

        return bcrs;
    }

    private List<String> getColumnNamesFromRow(Element row)
    {
        List<String> values = Arrays.asList(row.select("td").get(displayAndColumnNameIndex).text().split(COLUMN_NAME_REGEX));
        return values.subList(1, values.size());
    }

    private String getDisplayNameFromRow(Element row)
    {
        return row.select("td").get(displayAndColumnNameIndex).text().split(COLUMN_NAME_REGEX)[0].trim();
    }

    private String getTumorTypesFromRow(Element row)
    {
        return StringUtils.join(row.select("td").get(tumorTypeIndex).text().split(TUMOR_TYPES_DELIMITER), ",").toLowerCase();
    }

    private void fatal()
    {
        throw new IllegalArgumentException("BCR HTML Dictionary format has changed, aborting...");
    }
}
