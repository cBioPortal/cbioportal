/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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
package org.mskcc.cbio.importer.converter.internal;

import com.google.common.collect.*;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.portal.scripts.ImportClinicalData;
import org.mskcc.cbio.importer.*;
import org.mskcc.cbio.importer.model.*;

import org.apache.commons.logging.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

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
    public static final int N_METADATA = 3; // number of rows of metadata

    public static void main(String[] args) throws Exception {

        // for testing purposes only

        Table rawTable = HashBasedTable.create();

        rawTable.put("1", 1, "1,1");
        rawTable.put("1", 2, "1,2");

        System.out.println(rawTable);

        if (args.length != 0) {
            System.out.println("command line usage:  no arguments!");
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
	 * @param applyCaseLists boolean
	 * @throws Exception
	 */
    @Override
	public void applyOverrides(String portal, Set<String> excludeDatatypes, boolean applyCaseLists) throws Exception {
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
            String prefix = normalName.equals(ImportClinicalData.CASE_ID_COLUMN_NAME) ? ImportClinicalData.METADATA_PREIX : "";

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
        int caseIdIndex = colNames.indexOf(ImportClinicalData.CASE_ID_COLUMN_NAME);
        if (caseIdIndex != -1) {
            String currFirstCol = colNames.get(0);
            colNames.set(0, ImportClinicalData.CASE_ID_COLUMN_NAME);       // flip
            colNames.set(caseIdIndex, currFirstCol);
        } else if (LOG.isInfoEnabled()) {
            LOG.info("clinical data has no column that maps to a CASE_ID column!");
        }

        outMatrix.setColumnOrder(colNames);

        return outMatrix;
    }

    /**
     * N.B. does not preserve order
     * @param m DataMatrix
     * @return new DataMatrix
     */
    public DataMatrix removeDuplicateRows(DataMatrix m) {
        Set<List<String>> rowSet = new HashSet<List<String>>();
        for (int i=N_METADATA ; i < m.getNumberOfRows(); i +=1) {        // i = 3, skip headers
            rowSet.add(m.getRowData(i));
        }

        List<LinkedList<String>> rows = new ArrayList<LinkedList<String>>();
        Iterator<List<String>> iterator = rowSet.iterator();
        rows.add((LinkedList<String>) m.getRowData(0));     // put headers back in
        rows.add((LinkedList<String>) m.getRowData(1));
        rows.add((LinkedList<String>) m.getRowData(2));
        while (iterator.hasNext()) {
            rows.add((LinkedList<String>) iterator.next());
        }

        return new DataMatrix(rows, m.getColumnHeaders());
    }

    /**
     * Calculates days to last followup and ! appends ! it to the matrix
     *
     * goes to the spreadsheet and grabs the appropriate metadata,
     * and then calculates over all survival.
     *
     * Note: the input matrix must have columns DAYS_TO_LAST_FOLLOWUP
     * and DAYS_TO_DEATH in order to make this calculation.
     * Also, the spreadsheet must have OVERALL_SURVIVAL_DAYS as a clinical attribute
     *
     * @param   matrix
     * @return  matrix
     */
    public DataMatrix addOverAllSurvival(DataMatrix matrix) throws IOException {

        // check for whether the right data exists
        if (matrix.getColumnData(ClinicalAttributesMetadata.DAYS_TO_LAST_FOLLOWUP).size() == 0
                || matrix.getColumnData(ClinicalAttributesMetadata.DAYS_TO_DEATH).size() == 0) {
            LOG.info("clinical matrix missing one or both colums: "
                    + ClinicalAttributesMetadata.DAYS_TO_DEATH + "," + ClinicalAttributesMetadata.DAYS_TO_LAST_FOLLOWUP);
            return matrix;
        }

        Collection<ClinicalAttributesMetadata> overallSurvivalMetaDatas
                = config.getClinicalAttributesMetadata("OVERALL_SURVIVAL_DAYS");

        if (overallSurvivalMetaDatas.size() != 1) {
            LOG.info("the number of clinical attribute metadatas with column header 'OVERALL_SURVIVAL_DAYS' != 1");
        }

        // grab the data
        List<String> daysToLastFollowUp = matrix.getColumnData(ClinicalAttributesMetadata.DAYS_TO_LAST_FOLLOWUP).get(0);
        daysToLastFollowUp = daysToLastFollowUp.subList(N_METADATA, daysToLastFollowUp.size());

        List<String> daysToDeath = matrix.getColumnData(ClinicalAttributesMetadata.DAYS_TO_DEATH).get(0);
        daysToDeath = daysToDeath.subList(N_METADATA, daysToDeath.size());

        ClinicalAttributesMetadata overAllSurvivalMetadata = overallSurvivalMetaDatas.iterator().next();

        // prepare new column
        List<String> overallSurvivals = new LinkedList<String>();
        overallSurvivals.add(0, overAllSurvivalMetadata.getDisplayName());
        overallSurvivals.add(1, overAllSurvivalMetadata.getDescription());
        overallSurvivals.add(2, overAllSurvivalMetadata.getDatatype());

        // populate new column
        for (int i = 0; i < daysToLastFollowUp.size(); i+=1) {
            String os;
            String fUp = daysToLastFollowUp.get(i);
            String d = daysToDeath.get(i);

            if (fUp.equals(ClinicalAttributesMetadata.NA) && d.equals(ClinicalAttributesMetadata.NA)) {
                os = ClinicalAttributesMetadata.NA;
            } else {
                os = fUp.equals(ClinicalAttributesMetadata.NA) ? d : fUp;
            }
            overallSurvivals.add(os);
        }

        matrix.addColumn(overAllSurvivalMetadata.getColumnHeader(), overallSurvivals);

        return matrix;
    }

    /**
     * Aggregates and chooses the latest attribute in a DataMatrix.
     * Find every row in the clinical data file that matches the provided attribute
     * somewhere in the first column, sort by the "version" (denoted as "v" in the file)
     * and return a list whose first element is the provided attribute string,
     * and whose the remaining values are the latest non-NA values for each column.
     *
     * So in short what this does is merge rows.
     *
     * @param dataMatrix
     * @return latests
     */
    public List<String> calcLatestAttribute(DataMatrix dataMatrix, String attribute) {

        List<String> latest = new ArrayList<String>();

        Pattern attributePattern = Pattern.compile(attribute);


        return latest;
    }

    /**
     * Calculates new columns for a matrix based on what is in the matrix.
     * The matrix being in the format to be written into a staging file
     *
     * If columns for a calculation are missing, then the calculation is not done
     * and the original matrix is returned unharmed.
     *
     * ! modifies the input DataMatrix m
     * @param matrix
     * @return matrix
     */
    public DataMatrix processMatrix(DataMatrix matrix) throws IOException {

        matrix = removeDuplicateRows(matrix);
        matrix = addOverAllSurvival(matrix);

        return matrix;
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
        outMatrix = processMatrix(outMatrix);

		if (LOG.isInfoEnabled()) { LOG.info("createStagingFile(), writing staging file."); }
        fileUtils.writeStagingFile(portalMetadata.getStagingDirectory(), cancerStudyMetadata, datatypeMetadata, outMatrix);
		if (LOG.isInfoEnabled()) { LOG.info("createStagingFile(), complete."); }

        if (datatypeMetadata.requiresMetafile()){
            if (LOG.isInfoEnabled()) {
                LOG.info("createStagingFile(), writing metadata file.");
            }
            fileUtils.writeMetadataFile(portalMetadata.getStagingDirectory(), cancerStudyMetadata, datatypeMetadata, dataMatrix);
        } 

        // insert the new clinical attributes into google doc
        for (ClinicalAttributesMetadata attr : newAttributes.values()) {
            config.insertClinicalAttributesMetadata(attr);
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("updates to clinical_attributes google doc complete.");
        }
	}
}
