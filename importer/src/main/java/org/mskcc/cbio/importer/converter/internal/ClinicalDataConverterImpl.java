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
import com.google.gdata.util.common.base.StringUtil;
import static org.mskcc.cbio.cgds.scripts.ImportClinical.IGNORE_LINE_PREFIX;
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
	 * Any datatypes within the excludes datatypes set will not have be overridden.
	 *
	 * @param portal String
	 * @param excludeDatatypes Set<String>
	 * @throws Exception
	 */
    @Override
	public void applyOverrides(String portal, Set<String> excludeDatatypes) throws Exception {
		throw new UnsupportedOperationException();
    }

    /**
     * helper function for internal use
     *
     * converts the Collection of clinicalAttributes into this hash map:
     * { alias -> ClinicalAttributeMetadata }
     *
     * @param clinicalAttributes
     * @return
     */
    public HashMap<String, ClinicalAttributesMetadata>  createStringToClinicalAttributeMetaData(Collection<ClinicalAttributesMetadata> clinicalAttributes) {
        HashMap<String, ClinicalAttributesMetadata> knownAliasToAttribute = new HashMap<String, ClinicalAttributesMetadata>();
        for (ClinicalAttributesMetadata clinicalAttribute : clinicalAttributes) {

            String[] aliases = clinicalAttribute.getAliases().split(ALIAS_DELIMITER);

            // add to map
            for (String alias : aliases) {
                alias = alias.trim();
                knownAliasToAttribute.put(alias, clinicalAttribute);
            }
        }

        return knownAliasToAttribute;
    }

    /**
     * helper function for internal use
     *
     * cleans up the alias string, but more importantly, reduces "similar" aliases to a single alias.
     *
     * e.g. patient.followups.followupv2.0-2.radiationtherapy -> patient.followups.followup.radiationtherapy
     *
     * the idea being that each followup doesn't counter as a different alias.  Put another way, "version"
     * numbers don't count as different attributes
     *
     * @param alias
     * @return
     */
    public String cleanUpAlias(String alias) {
        if (alias.contains("radiations.radiation")
                || alias.contains("drugs.drug")
                || alias.contains("fishtestcomponentresults.fishtestcomponentresult")
                || alias.contains("immunophenotypecytochemistrytestingresults.immunophenotypecytochemistrytestingresultvalues")
                || alias.contains("molecularanalysisabnormalitytestingresults.molecularanalysisabnormalitytestingresultvalues")) {
            alias = alias.replaceAll("-\\d+.", ".");
        }

        if (alias.contains("followups.followup")) {
            alias = alias.replaceAll("v\\d+.\\d+(-\\d+)?.",".");
        }

        // - followed by numbers at the end of the line should never count as new
        alias = alias.replaceAll("-\\d+$", "");

        return alias.trim();
    }

    /**
     * Updates the filteredRows accordingly.
     * @param knownAliasToAttribute
     * @param alias
     * @param filteredRows
     * @return
     */
    public List<List<String>> appendToFilteredRows(HashMap<String, ClinicalAttributesMetadata> knownAliasToAttribute,
                                                   String alias,
                                                   List<List<String>> filteredRows) {

        return filteredRows;
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

        HashMap<String, ClinicalAttributesMetadata> knownAliasToAttribute
                = createStringToClinicalAttributeMetaData(clinicalAttributes);

        // filter through the rows of the data matrix (clinical data is row oriented),
        // returning only the rows that we are interested in.
        // i.e. the ones whose alias' have a corresponding normalized name
        List<List<String>> filteredRows = new LinkedList<List<String>>();

        HashMap<String, ClinicalAttributesMetadata> newAttributes = new HashMap<String, ClinicalAttributesMetadata>();

        for (int r = 0; r < dataMatrix.getNumberOfRows(); r++) {
            List<String> rowData = dataMatrix.getRowData(r);

            String rowName = cleanUpAlias(rowData.get(0));

            appendToFilteredRows(knownAliasToAttribute, rowName, filteredRows);

            boolean isKnown = knownAliasToAttribute.containsKey(rowName);

            if (isKnown) {
                if (knownAliasToAttribute.get(rowName).getAnnotationStatus().equals(OK)) {
                    // it's been OKayed
                    filteredRows.add(rowData);
                }
            } else {
                if (!newAttributes.containsKey(rowName)) {
                    // haven't seen before so make a new clinical attribute
                    String UNANNOTATED = "Unannotated";

                    String[] props = new String[9];
                    props[0] = "";                                                              // COLUMN_HEADER
                    props[1] = "";                                                              // DISPLAY_NAME
                    props[2] = "";                                                              // DESCRIPTION
                    props[3] = "";                                                              // DATATYPE
                    props[4] = rowName;                                                         // ALIASES
                    props[5] = UNANNOTATED;                                                     // ANNOTATION_STATUS
//                props[6] =  StringUtil.toUpperCase(cancerStudyMetadata.getTumorType());     // DISEASE_SPECIFICITY
                    props[6] = "";                                                              // DISEASE_SPECIFICITY
                    props[7] = "";                                                              // NIKI_ANNOTATION

                    newAttributes.put(rowName, new ClinicalAttributesMetadata(props));
                }
            }
        }

        // insert the new clinical attributes into google doc
        for (ClinicalAttributesMetadata attr : newAttributes.values()) {
            config.insertClinicalAttributesMetadata(attr);
        }

        // normalize the names,
        // and put together the metaData
        ArrayList<String> colNames = new ArrayList<String>();
        LinkedList<String> displayNames = new LinkedList<String>();
        LinkedList<String> descriptions = new LinkedList<String>();

        for (List<String> row : filteredRows) {

            String rowName = row.remove(0);
            ClinicalAttributesMetadata metaData = knownAliasToAttribute.get(rowName);

            // normalized name
            String normalName = metaData.getColumnHeader();
            colNames.add(normalName);

            // case id is always the first column
            String prefix = normalName.equals(CASE_ID) ? IGNORE_LINE_PREFIX : "";

            String displayName = metaData.getDisplayName();
            displayNames.add(prefix + displayName);

            String description = metaData.getDescription();
            descriptions.add(prefix + description);
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

        // add in the metadata
        columns.add(0, descriptions);
        columns.add(0, displayNames);

        DataMatrix outMatrix = new DataMatrix(columns, colNames);

        // case id should be the first column
        int caseIdIndex = colNames.indexOf(CASE_ID);
        if (caseIdIndex != -1) {
            String currFirstCol = colNames.get(0);
            colNames.set(0, CASE_ID);
            colNames.set(caseIdIndex, currFirstCol);
            if (LOG.isInfoEnabled()) {
                LOG.info("clinical data has no column that maps to a CASE_ID column!");
            }
        }

        outMatrix.setColumnOrder(colNames);

//        outMatrix.write(System.out);

		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), writing staging file.");
		}

//		fileUtils.writeStagingFile(portalMetadata, cancerStudyMetadata, datatypeMetadata, dataMatrix);
        fileUtils.writeStagingFile(portalMetadata, cancerStudyMetadata, datatypeMetadata, outMatrix);

		if (LOG.isInfoEnabled()) {
			LOG.info("createStagingFile(), complete.");
		}
	}
}
