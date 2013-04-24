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
import org.mskcc.cbio.cgds.model.ClinicalAttribute;
import org.mskcc.cbio.importer.internal.ImportClinical;
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

    public static final String OK = "OK"; // string that indicates that the attribute should be imported
    public static final String UNANNOTATED = "Unannotated";
    public static final String IGNORE = "ignore";

    // column names for data matrix processing
    public static final String DAYS_TO_LAST_FOLLOWUP = "DAYS_TO_LAST_FOLLOWUP";
    public static final String DAYS_TO_DEATH = "DAYS_TO_DEATH";
    public static final String NA = "NA";


    public static void main(String[] args) throws Exception {
        if (args.length != 0) {
            System.out.println("command line usage:  no arguments!");
            System.exit(1);
        }
    }


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
    public String cleanAlias(String alias) {
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
     *
     * helper function for internal use
     *
     * Updates the keepers per the dictionary knownAliasToAttribute.
     * Basically does rowData \intersect knownAliasToAttribute and appends that to keepers
     *
     * @param knownAliasToAttribute
     * @param alias
     * @param keepers
     * @return
     */
    public void appendToFilteredRows(HashMap<String, ClinicalAttributesMetadata> knownAliasToAttribute,
                                     List<String> rowData,
                                     String alias,
                                     List<List<String>> keepers) {

        ClinicalAttributesMetadata attr = knownAliasToAttribute.get(alias);

        if (attr != null && attr.getAnnotationStatus().equals(OK)) {
            // it's been OKayed
            keepers.add(rowData);
        }
    }

    public void appendToNewAttributes(HashMap<String, ClinicalAttributesMetadata> knownAliasToAttribute,
                                      List<String> rowData,
                                      String alias,
                                      HashMap<String, ClinicalAttributesMetadata> newAttributes) {
//            boolean isKnown = knownAliasToAttribute.containsKey(rowName);

        if ( !( knownAliasToAttribute.containsKey(alias) || newAttributes.containsKey(alias)) ) {
            // nor have we see it so far in this session,
            // nor have we seen it in the past (i.e. not in google doc)

            String[] props = new String[9];
            props[0] = "";                                                              // COLUMN_HEADER
            props[1] = "";                                                              // DISPLAY_NAME
            props[2] = "";                                                              // DESCRIPTION
            props[3] = "";                                                              // DATATYPE
            props[4] = alias;                                                           // ALIASES
            props[5] = UNANNOTATED;                                                     // ANNOTATION_STATUS
//                props[6] =  StringUtil.toUpperCase(cancerStudyMetadata.getTumorType());     // DISEASE_SPECIFICITY
            props[6] = "";                                                              // DISEASE_SPECIFICITY
            props[7] = "";                                                              // NIKI_ANNOTATION

            newAttributes.put(alias, new ClinicalAttributesMetadata(props));
        }
    }

    /**
     *
     * helper function for internal use
     *
     * transposes "rows" to "columns"
     *
     * @param vectors
     * @return
     */
    public List<LinkedList<String>> transpose(List<List<String>> vectors) {
        // convert rows to columns, transpose
        List<LinkedList<String>> transposed = new LinkedList<LinkedList<String>>();
        int numberOfCols = vectors.get(0).size();

        for (int c = 0; c < numberOfCols; c++) {
            LinkedList<String> column = new LinkedList<String>();
            for (List<String> row : vectors) {
                String datum = row.get(c);
                column.add(datum);
            }

            transposed.add(column);
        }
        return transposed;
    }

    /**
     * oooo, making out with a matrix, oooo
     * helper function for internal use only
     *
     * do the necessary massage (ha ha ha very funny) to prepare the vectors for writing out
     *
     * @param vectors
     * @return
     */
    public DataMatrix makeOutMatrix(List<List<String>> vectors,
                                    HashMap<String, ClinicalAttributesMetadata> aliasToAttribute) throws Exception {
        ArrayList<String> colNames = new ArrayList<String>();
        LinkedList<String> displayNames = new LinkedList<String>();
        LinkedList<String> descriptions = new LinkedList<String>();
        LinkedList<String> datatypes = new LinkedList<String>();

        for (List<String> vec : vectors) {

            String rowName = vec.remove(0);
            ClinicalAttributesMetadata metaData = aliasToAttribute.get(rowName);

            // normalized name
            String normalName = metaData.getColumnHeader();
            colNames.add(normalName);

            // case id is always the first column
            String prefix = normalName.equals(ImportClinical.CASE_ID) ? ImportClinical.METADATA_PREIX : "";

            String displayName = metaData.getDisplayName();
            displayNames.add(prefix + displayName);

            String description = metaData.getDescription();
            descriptions.add(prefix + description);

            String datatype = metaData.getDatatype();
            datatypes.add(prefix + datatype);
        }

        List<LinkedList<String>> columns = transpose(vectors);

        // add in the metadata (in the correct order)
        columns.add(0, datatypes);
        columns.add(0, descriptions);
        columns.add(0, displayNames);

        DataMatrix outMatrix = new DataMatrix(columns, colNames);

        // case id should be the first column
        // N.B. don't put this block before creating the outMatrix
        int caseIdIndex = colNames.indexOf(ImportClinical.CASE_ID);
        if (caseIdIndex != -1) {
            String currFirstCol = colNames.get(0);
            colNames.set(0, ImportClinical.CASE_ID);       // flip
            colNames.set(caseIdIndex, currFirstCol);
        } else if (LOG.isInfoEnabled()) {
            LOG.info("clinical data has no column that maps to a CASE_ID column!");
        }

        outMatrix.setColumnOrder(colNames);
        outMatrix = processMatrix(outMatrix);

        return outMatrix;
    }

    /**
     * Calculates new columns for a matrix based on what is in the matrix.
     * The matrix being in the format to be written into a staging file
     *
     * If columns for a calculation are missing, then the calculation is not done
     * and the original matrix is returned unharmed.
     *
     * ! modifies the input DataMatrix m
     * @param m DataMatrix
     * @return new DataMatrix
     */
    public DataMatrix processMatrix(DataMatrix m) {
        int n_metadata = 3; // number of rows of metadata

        if (m.getColumnData(DAYS_TO_LAST_FOLLOWUP).size() == 0
                || m.getColumnData(DAYS_TO_DEATH).size() == 0) {
            LOG.info("clinical matrix missing one or both colums: "
                    + DAYS_TO_DEATH + "," + DAYS_TO_LAST_FOLLOWUP);
            return m;
        }

        List<String> followUps = m.getColumnData(DAYS_TO_LAST_FOLLOWUP).get(0);
        followUps = followUps.subList(n_metadata, followUps.size());

        List<String> deaths = m.getColumnData(DAYS_TO_DEATH).get(0);
        deaths = deaths.subList(n_metadata, deaths.size());

        List<String> overallSurvivals = new LinkedList<String>();
        overallSurvivals.add(0, "days alive after diagnosis");
        overallSurvivals.add(1, "max(days_to_death, days_to_last_followup)");
        overallSurvivals.add(2, "NUMBER");
        for (int i = 0; i < followUps.size(); i+=1) {
            String os;
            String fUp = followUps.get(i);
            String d = deaths.get(i);

            if (fUp.equals(NA) && d.equals(NA)) {
                os = NA;
            } else {
                os = fUp.equals(NA) ? d : fUp;
            }
            overallSurvivals.add(os);
        }

        // I'd prefer to make a copy and return that, but DataMatrix is initialized by rows, but does not save them.
        // So, copying is nontrivial so perhaps it's better to just leave this off.
        m.addColumn("OVERALL_SURVIVAL_DAYS", overallSurvivals);
        return m;
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

        // iterate through the rows of the data matrix
        // adding the row either to a list of keepers, rows (i.e. attribute) that will be added to the staging file,
        // or adding the row/attribute to a list of new attributes that are not in the google doc
        // (and therefore need to be added)
        List<List<String>> keepers = new LinkedList<List<String>>();
        HashMap<String, ClinicalAttributesMetadata> newAttributes = new HashMap<String, ClinicalAttributesMetadata>();
        List<ClinicalAttribute> keeperAttrs = new ArrayList<ClinicalAttribute>();
        for (int r = 0; r < dataMatrix.getNumberOfRows(); r++) {
            List<String> rowData = dataMatrix.getRowData(r);
            String cleanedRowName = cleanAlias(rowData.get(0));      // assuming that alias is the first item in the row

            appendToFilteredRows(knownAliasToAttribute, rowData, cleanedRowName, keepers);
            appendToNewAttributes(knownAliasToAttribute, rowData, cleanedRowName, newAttributes);
        }

        // make a list of attributes
        // todo: this is done on every staging file. wasteful
        for (List<String> row : keepers) {
            String rowName = row.get(0);
            ClinicalAttributesMetadata metadata = knownAliasToAttribute.get(cleanAlias(rowName));

            ClinicalAttribute attr =  new ClinicalAttribute(metadata.getColumnHeader(),
                    metadata.getDisplayName(), metadata.getDescription(), metadata.getDatatype());
            keeperAttrs.add(attr);
        }

        // insert the attributes into the db
//        DaoClinicalAttribute.deleteAllRecords();        // N.B. the db reflects what is in the spreadsheet
//        for (ClinicalAttribute attr : keeperAttrs) {
//            DaoClinicalAttribute.addDatum(attr);
//        }

        DataMatrix outMatrix = makeOutMatrix(keepers, knownAliasToAttribute);
//        outMatrix.write(System.out);


		if (LOG.isInfoEnabled()) { LOG.info("createStagingFile(), writing staging file."); }
        fileUtils.writeStagingFile(portalMetadata, cancerStudyMetadata, datatypeMetadata, outMatrix);
		if (LOG.isInfoEnabled()) { LOG.info("createStagingFile(), complete."); }

        // insert the new clinical attributes into google doc
        for (ClinicalAttributesMetadata attr : newAttributes.values()) {
            config.insertClinicalAttributesMetadata(attr);
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("updates to clinical_attributes google doc complete.");
        }
	}
}
