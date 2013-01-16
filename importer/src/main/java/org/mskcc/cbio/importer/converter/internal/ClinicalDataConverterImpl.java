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
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

// package
package org.mskcc.cbio.importer.converter.internal;

// imports
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.CaseIDs;
import org.mskcc.cbio.importer.IDMapper;
import org.mskcc.cbio.importer.Converter;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.model.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Class which implements the Converter interface for use
 * with firehose clinical data (Merge_Clinical.Level_1:<TUMOR_TYPE>.clin.merged.txt).
 */
public class ClinicalDataConverterImpl implements Converter {

	// our logger
	private static Log LOG = LogFactory.getLog(ClinicalDataConverterImpl.class);

	// ref to configuration
	private Config config;

	// ref to file utils
	private FileUtils fileUtils;

	// ref to caseids
	private CaseIDs caseIDs;

	// ref to IDMapper
	private IDMapper idMapper;

    // string delimiter for aliases
    public static final String ALIAS_DELIMITER = ",";

    // string that indicates that the attribute should be imported
    public static final String OK = "OK";

    // name of the case id column
    public static final String CASE_ID = "CASE_ID";

	/**
	 * Constructor.
     *
     * @param config Config
	 * @param fileUtils FileUtils
	 * @param caseIDs CaseIDs;
	 * @param idMapper IDMapper
	 */
	public ClinicalDataConverterImpl(Config config, FileUtils fileUtils,
									 CaseIDs caseIDs, IDMapper idMapper) {

		// set members
		this.config = config;
        this.fileUtils = fileUtils;
		this.caseIDs = caseIDs;
		this.idMapper = idMapper;
	}

	/**
	 * Converts data for the given portal.
	 *
     * @param portal String
	 * @param runDate String
	 * @param applyOverrides Boolean
	 * @throws Exception
	 */
    @Override
	public void convertData(String portal, String runDate, Boolean applyOverrides) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 * Generates case lists for the given portal.
	 *
     * @param portal String
	 * @throws Exception
	 */
    @Override
	public void generateCaseLists(String portal) throws Exception {
		throw new UnsupportedOperationException();
	}

    /**
	 * Applies overrides to the given portal using the given data source.
	 *
	 * @param portal String
	 * @throws Exception
	 */
    @Override
	public void applyOverrides(String portal) throws Exception {
		throw new UnsupportedOperationException();
    }

	/**
	 * Creates a staging file from the given import data.
	 *
     * @param portalMetadata PortalMetadata
	 * @param cancerStudyMetadata CancerStudyMetadata
	 * @param datatypeMetadata DatatypeMetadata
	 * @param dataMatrices DataMatrix[]
	 * @throws Exception
	 */
	@Override
	public void createStagingFile(PortalMetadata portalMetadata, CancerStudyMetadata cancerStudyMetadata,
								  DatatypeMetadata datatypeMetadata, DataMatrix[] dataMatrices) throws Exception {

		// sanity check
		if (dataMatrices.length != 1) {
			throw new IllegalArgumentException("dataMatrices.length != 1, aborting...");
		}
		DataMatrix dataMatrix = dataMatrices[0];

        Collection<ClinicalAttributesMetadata> clinicalAttributes = config.getClinicalAttributesMetadata(Config.ALL);

        // make a map between the normalized attribute name and the clinicalAttribute,
        // for all attributes that have been okayed
        HashMap<String, ClinicalAttributesMetadata> normalizeName = new HashMap<String, ClinicalAttributesMetadata>();

        for (ClinicalAttributesMetadata clinicalAttribute : clinicalAttributes) {

            String[] aliases = clinicalAttribute.getAliases().split(ALIAS_DELIMITER);
            String status = clinicalAttribute.getAnnotationStatus().trim();

            if (status.equals(OK)) {
                for (String alias : aliases) {
                    // add to map
                    alias = alias.trim();
                    normalizeName.put(alias, clinicalAttribute);

                    if (clinicalAttribute.getColumnHeader().equals("")) {
                        if (LOG.isInfoEnabled()) { LOG.info("Okayed annotation doesn't have a column header: " + alias ); }
                    }
                }
            }
        }

        // filter through the rows of the data matrix (clinical data is row oriented),
        // returning only the rows that we are interested in.
        // i.e. the ones whose alias' have a corresponding normalized name
        List<LinkedList<String>> filteredRows  = new LinkedList<LinkedList<String>>();

        for (int r = 0; r < dataMatrix.getNumberOfRows(); r++) {
            LinkedList<String> rowData =  dataMatrix.getRowData(r);

            String rowName = rowData.get(0);

            if (normalizeName.containsKey(rowName)) {
                filteredRows.add(rowData);
            }
        }

        // make a column oriented data matrix out of filteredRows
        ArrayList<String> colNames = new ArrayList<String>();

        for (List<String> row : filteredRows) {
            // add the normalized name of each data vector
            // and remove the name since names metadata, are not data

            String rowName = row.remove(0);
            String normalName = normalizeName.get(rowName).getColumnHeader();
            colNames.add(normalName);
        }

        // convert rows to columns, transpose
        List<LinkedList<String>> columns  = new LinkedList<LinkedList<String>>();
        int numberOfCols = filteredRows.get(0).size();
        for (int c = 0; c < numberOfCols; c++) {
            LinkedList<String> column = new LinkedList<String>();
            for (List<String> row : filteredRows) {
                String datum = row.get(c);
                column.add(datum);
            }

            columns.add(column);
        }

        DataMatrix outMatrix = new DataMatrix(columns, colNames);

        // case id should be the first column
        int caseIdIndex = colNames.indexOf(CASE_ID);
        String currFirstCol = colNames.get(0);
        colNames.set(0, CASE_ID);
        colNames.set(caseIdIndex, currFirstCol);
        outMatrix.setColumnOrder(colNames);

        //insert meta data
//        for(String colName : colNames) {
//            ClinicalAttributesMetadata meetaData = normalizeName.get(colName);
//            LinkedList<String> metaDataRow = new LinkedList<String>();
//
//            metaDataRow.add();
//        }

        outMatrix.write(System.out);

		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), writing staging file.");
		}

		fileUtils.writeStagingFile(portalMetadata, cancerStudyMetadata, datatypeMetadata, dataMatrix);

		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), complete.");
		}
	}
}
